package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.NovaBrain
import com.example.model.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    assertEquals("Nova Voice Agent", appName)
  }

  @Test
  fun `test nova brain query processing`() {
    val timeResult = NovaBrain.processQuery("What time is it?")
    assertTrue(timeResult.reply.contains("It is currently"))

    val appResult = NovaBrain.processQuery("Open Camera")
    assertEquals("open_app", appResult.intent)
    assertEquals("camera", appResult.appQuery)

    val jokeResult = NovaBrain.processQuery("Tell me a joke")
    assertNotNull(jokeResult.reply)
    assertTrue(jokeResult.reply.isNotBlank())

    val batteryResult = NovaBrain.processQuery("What's my battery percentage?")
    assertEquals("battery", batteryResult.intent)

    val noteResult = NovaBrain.processQuery("take a note call doctor")
    assertEquals("note", noteResult.intent)
    assertTrue(noteResult.reply.contains("call doctor"))
  }

  @Test
  fun `test nova brain voice sign out intent`() {
    val signOutResult = NovaBrain.processQuery("Sign out")
    assertEquals("sign_out", signOutResult.intent)
    assertTrue(signOutResult.reply.contains("Signing you out securely"))

    val logOutResult = NovaBrain.processQuery("log out")
    assertEquals("sign_out", logOutResult.intent)
  }

  @Test
  fun `test user session state`() {
    val defaultSession = UserSession(name = "Madhevan", email = "operator@nova.local", isSignedIn = true)
    assertTrue(defaultSession.isSignedIn)
    assertEquals("Madhevan", defaultSession.name)

    val signedOut = defaultSession.copy(isSignedIn = false)
    assertFalse(signedOut.isSignedIn)
  }
}
