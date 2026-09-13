package com.example.data.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Event emitted when a Firestore operation encounters a transient failure
 * and initiates an exponential backoff retry.
 */
data class FirestoreRetryEvent(
  val operationName: String,
  val attempt: Int,
  val maxAttempts: Int,
  val delayMs: Long,
  val errorSummary: String,
  val timestamp: Long = System.currentTimeMillis()
)

/**
 * Exponential backoff retry policy for Firestore operations.
 * Improves reliability when network conditions are unstable, high latency,
 * or fluctuating between cellular and Wi-Fi.
 */
object FirestoreRetryPolicy {
  private const val TAG = "FirestoreRetryPolicy"

  const val DEFAULT_MAX_ATTEMPTS = 3
  const val DEFAULT_INITIAL_DELAY_MS = 600L
  const val DEFAULT_MAX_DELAY_MS = 5000L
  const val DEFAULT_MULTIPLIER = 2.0
  const val DEFAULT_JITTER_RATIO = 0.25 // +/- 25% randomized jitter to prevent thundering herd

  /**
   * Determines whether an exception is transient and eligible for retry.
   * Non-transient exceptions (like PERMISSION_DENIED or INVALID_ARGUMENT) fail fast.
   */
  fun isRetryable(e: Throwable): Boolean {
    val message = e.message.orEmpty()

    if (e is FirebaseFirestoreException) {
      return when (e.code) {
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
        FirebaseFirestoreException.Code.ABORTED,
        FirebaseFirestoreException.Code.INTERNAL,
        FirebaseFirestoreException.Code.UNKNOWN -> true

        // Strict non-retryable codes to avoid battery/bandwidth drain
        FirebaseFirestoreException.Code.PERMISSION_DENIED,
        FirebaseFirestoreException.Code.NOT_FOUND,
        FirebaseFirestoreException.Code.ALREADY_EXISTS,
        FirebaseFirestoreException.Code.INVALID_ARGUMENT,
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> false

        else -> false
      }
    }

    if (e is SocketTimeoutException ||
      e is UnknownHostException ||
      e is ConnectException ||
      e is IOException
    ) {
      return true
    }

    // Heuristic keyword check for underlying gRPC/network transport errors
    return message.contains("UNAVAILABLE", ignoreCase = true) ||
      message.contains("DEADLINE_EXCEEDED", ignoreCase = true) ||
      message.contains("timeout", ignoreCase = true) ||
      message.contains("network", ignoreCase = true) ||
      message.contains("transport", ignoreCase = true) ||
      message.contains("connection closed", ignoreCase = true) ||
      message.contains("channel is shutdown", ignoreCase = true)
  }

  /**
   * Executes [block] with exponential backoff and jitter upon retryable failures.
   */
  suspend fun <T> executeWithRetry(
    operationName: String = "FirestoreOperation",
    maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
    maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
    multiplier: Double = DEFAULT_MULTIPLIER,
    onRetry: ((FirestoreRetryEvent) -> Unit)? = null,
    block: suspend (attempt: Int) -> T
  ): T {
    var currentDelay = initialDelayMs
    var lastException: Throwable? = null

    for (attempt in 1..maxAttempts) {
      try {
        return block(attempt)
      } catch (e: Throwable) {
        lastException = e
        val retryable = isRetryable(e)

        if (attempt >= maxAttempts || !retryable) {
          Log.w(
            TAG,
            "Operation '$operationName' halted at attempt $attempt/$maxAttempts (retryable=$retryable): ${e.message}"
          )
          throw e
        }

        // Calculate jittered delay: delay * (1 + random(-0.25, 0.25))
        val jitterMultiplier = 1.0 + ((Math.random() * 2.0 - 1.0) * DEFAULT_JITTER_RATIO)
        val jitteredDelay = (currentDelay * jitterMultiplier).toLong().coerceIn(100L, maxDelayMs)

        val event = FirestoreRetryEvent(
          operationName = operationName,
          attempt = attempt,
          maxAttempts = maxAttempts,
          delayMs = jitteredDelay,
          errorSummary = e.message ?: e.javaClass.simpleName
        )
        Log.i(
          TAG,
          "Transient failure in '$operationName' (attempt $attempt/$maxAttempts). Retrying in ${jitteredDelay}ms: ${e.message}"
        )
        onRetry?.invoke(event)

        delay(jitteredDelay)
        currentDelay = (currentDelay * multiplier).toLong().coerceAtMost(maxDelayMs)
      }
    }

    throw lastException ?: IllegalStateException("Operation '$operationName' failed after $maxAttempts attempts")
  }
}
