package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.agent.AppLauncherManager
import com.example.agent.DeviceToolManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("C9-SHANICE", appName)
  }

  @Test
  fun `verify device tool manager telemetry`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = DeviceToolManager(context)
    val telemetry = manager.getDeviceTelemetry()
    assertNotNull(telemetry)
    assertNotNull(telemetry.deviceModel)
    assertNotNull(telemetry.androidVersion)
  }

  @Test
  fun `verify script evaluator`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = DeviceToolManager(context)
    val output = manager.evaluateScript("python", "print('hello from c9-shanice')")
    assertTrue(output.contains("hello from c9-shanice"))
  }

  @Test
  fun `verify app launcher manager`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val launcher = AppLauncherManager(context)
    val apps = launcher.getInstalledApps()
    assertNotNull(apps)
  }

  @Test
  fun `verify volume and vibration tools`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = DeviceToolManager(context)
    val vol = manager.getVolumePercent()
    assertTrue(vol in 0..100)
    manager.vibratePhone(50)
  }
}
