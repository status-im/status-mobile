package im.status.ethereum.pushnotifications;

import org.junit.Test;
import static org.junit.Assert.*;
import android.app.Application;
import android.app.NotificationManager;
import android.os.Bundle;

import android.content.Intent;
import android.app.PendingIntent;
import android.content.IntentFilter;
import static org.mockito.Mockito.*;
import org.mockito.Spy;

public class PushNotificationHelperTest {

    PushNotificationHelper helper;


   @Test
    public void testAddStatusMessage() {
        final Application context = mock(Application.class);
        final NotificationManager manager = mock(NotificationManager.class);
        final IntentFilter filter = mock(IntentFilter.class);

        when(context.getSystemService(NotificationManager.class)).thenReturn(manager);

        // Create a spy for PushNotificationHelper
        PushNotificationHelper realHelper = new PushNotificationHelper(context, filter);
        helper = spy(realHelper);

        Bundle bundle = mock(Bundle.class);
        Bundle personBundle = mock(Bundle.class);

        String conversationId = "conversation-id";
        String deepLink = "deep-link";

        when(bundle.getString("conversationId")).thenReturn(conversationId);
        when(bundle.getString("id")).thenReturn("id");
        when(bundle.getString("message")).thenReturn("message");
        when(bundle.getDouble("timestamp")).thenReturn(100000.0);
        when(bundle.getString("deepLink")).thenReturn(deepLink);

        when(personBundle.getString("name")).thenReturn("name");
        when(bundle.getBundle("notificationAuthor")).thenReturn(personBundle);


        doNothing().when(helper).showMessages(bundle);

        helper.addStatusMessage(bundle);
        assertNotNull(helper.getMessageGroup(conversationId));
    }
}

