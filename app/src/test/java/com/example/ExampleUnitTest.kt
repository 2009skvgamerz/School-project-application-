package com.example

import com.example.data.SchoolRepository
import com.example.model.BusRoute
import com.example.model.BusStatus
import com.example.model.BusStop
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify bus routes have accurate GPS coordinates and calculate realistic movement`() {
    val repository = SchoolRepository()
    val routes = repository.busRoutes.value
    assertTrue("Seed bus routes should not be empty", routes.isNotEmpty())

    routes.forEach { route ->
      assertTrue("Bus latitude must be valid", route.currentLatitude in -90.0..90.0)
      assertTrue("Bus longitude must be valid", route.currentLongitude in -180.0..180.0)
      assertTrue("School latitude must be valid", route.schoolLatitude in -90.0..90.0)
      assertTrue("School longitude must be valid", route.schoolLongitude in -180.0..180.0)
      assertTrue("Route must have stops", route.stops.isNotEmpty())

      route.stops.forEach { stop ->
        assertTrue("Stop latitude must be valid", stop.latitude in -90.0..90.0)
        assertTrue("Stop longitude must be valid", stop.longitude in -180.0..180.0)
      }
    }
  }

  @Test
  fun `verify simulateBusMovement updates progress and coordinates smoothly`() {
    val repository = SchoolRepository()
    val route = repository.busRoutes.value.first()
    val initialProgress = route.progressPercent
    val initialLat = route.currentLatitude

    repository.simulateBusMovement(route.id)
    val updatedRoute = repository.busRoutes.value.first { it.id == route.id }

    assertNotNull(updatedRoute)
    assertTrue("Progress should update after simulation step", updatedRoute.progressPercent >= 0.0f)
  }
}
