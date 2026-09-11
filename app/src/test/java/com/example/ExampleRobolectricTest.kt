package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aura Gaming Booster", appName)
  }

  @Test
  fun `device specs and installed games extraction`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val specs = com.example.hardware.DeviceInfoHelper.getDeviceSpecs(context)
    assert(specs.deviceName.isNotEmpty())
    assert(specs.cpuCores > 0)
    assert(specs.socName.isNotEmpty())

    val games = com.example.hardware.DeviceInfoHelper.getInstalledGames(context)
    assert(games.isNotEmpty())
  }
}
