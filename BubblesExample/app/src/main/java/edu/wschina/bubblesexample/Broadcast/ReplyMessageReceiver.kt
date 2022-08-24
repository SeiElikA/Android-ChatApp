package edu.wschina.bubblesexample.Broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import edu.wschina.bubblesexample.Model.MessageModel

class ReplyMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = remoteInput.getString("key_text_reply")
        replyText?.let {
            MessageModel(context)
                .sendMessage(it)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_DEFAULT))
        notificationManager.cancel(1)
    }
}