package im.status.ethereum.pushnotifications

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Spy

class PushNotificationHelperTest {

    private lateinit var helper: PushNotificationHelper

    @Test
    fun testAddStatusMessage() {
        val context = mock(Application::class.java)
        val manager = mock(NotificationManager::class.java)
        val filter = mock(IntentFilter::class.java)

        `when`(context.getSystemService(NotificationManager::class.java)).thenReturn(manager)

        // Create a spy for PushNotificationHelper
        val realHelper = PushNotificationHelper(context, filter)
        helper = spy(realHelper)

        val bundle = mock(Bundle::class.java)
        val personBundle = mock(Bundle::class.java)

        val conversationId = "conversation-id"
        val deepLink = "deep-link"

        `when`(bundle.getString("conversationId")).thenReturn(conversationId)
        `when`(bundle.getString("id")).thenReturn("id")
        `when`(bundle.getString("message")).thenReturn("message")
        `when`(bundle.getDouble("timestamp")).thenReturn(100000.0)
        `when`(bundle.getString("deepLink")).thenReturn(deepLink)

        `when`(personBundle.getString("name")).thenReturn("name")
        `when`(bundle.getBundle("notificationAuthor")).thenReturn(personBundle)

        doNothing().`when`(helper).showMessages(bundle)

        helper.addStatusMessage(bundle)
        assertNotNull(helper.getMessageGroup(conversationId))
    }
}
