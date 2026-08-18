package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class NetworkState {
  data class Online(val connectionType: String = "Connected") : NetworkState()
  data class Offline(val reason: String = "No active internet connection") : NetworkState()
  object Reconnecting : NetworkState()

  val isConnected: Boolean
    get() = this is Online
}

class NetworkConnectivityMonitor(private val context: Context) {

  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

  // Allows user to simulate offline state to test offline caching & notifications
  private val _simulatedOffline = MutableStateFlow(false)
  val simulatedOffline: StateFlow<Boolean> = _simulatedOffline.asStateFlow()

  fun setSimulatedOffline(offline: Boolean) {
    _simulatedOffline.value = offline
  }

  private val realNetworkFlow: Flow<NetworkState> = callbackFlow {
    if (connectivityManager == null) {
      trySend(NetworkState.Offline("Connectivity Manager unavailable"))
      awaitClose { }
      return@callbackFlow
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        val type = getNetworkType(connectivityManager, network)
        trySend(NetworkState.Online(type))
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
        trySend(NetworkState.Reconnecting)
      }

      override fun onLost(network: Network) {
        if (!isCurrentlyConnected()) {
          trySend(NetworkState.Offline("Network connection was lost"))
        }
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        if (hasInternet && isValidated) {
          val type = getNetworkType(connectivityManager, network)
          trySend(NetworkState.Online(type))
        } else if (!hasInternet) {
          trySend(NetworkState.Offline("Limited or no internet access"))
        }
      }
    }

    // Emit initial status
    trySend(getCurrentNetworkState())

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    try {
      connectivityManager.registerNetworkCallback(request, callback)
    } catch (e: Exception) {
      trySend(getCurrentNetworkState())
    }

    awaitClose {
      try {
        connectivityManager.unregisterNetworkCallback(callback)
      } catch (e: Exception) {
        // Ignored on cleanup
      }
    }
  }.distinctUntilChanged()

  val networkState: Flow<NetworkState> = combine(
    realNetworkFlow,
    _simulatedOffline
  ) { realState, simulated ->
    if (simulated) {
      NetworkState.Offline("Simulated Offline Mode (Room Database cache active)")
    } else {
      realState
    }
  }.flowOn(Dispatchers.IO)

  fun isCurrentlyConnected(): Boolean {
    if (_simulatedOffline.value) return false
    val activeNetwork = connectivityManager?.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  fun getCurrentNetworkState(): NetworkState {
    if (_simulatedOffline.value) {
      return NetworkState.Offline("Simulated Offline Mode")
    }
    val activeNetwork = connectivityManager?.activeNetwork
      ?: return NetworkState.Offline("No active network connection")
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
      ?: return NetworkState.Offline("No network capabilities")

    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    return if (hasInternet) {
      val type = getNetworkType(connectivityManager, activeNetwork)
      NetworkState.Online(type)
    } else {
      NetworkState.Offline("Internet connection not validated")
    }
  }

  private fun getNetworkType(cm: ConnectivityManager, network: Network): String {
    val capabilities = cm.getNetworkCapabilities(network) ?: return "Connected"
    return when {
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 4G/5G"
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
      else -> "Online"
    }
  }
}
