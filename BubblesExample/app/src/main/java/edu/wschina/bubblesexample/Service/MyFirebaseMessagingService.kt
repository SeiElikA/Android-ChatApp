package edu.wschina.bubblesexample.Service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.wschina.bubblesexample.DataModel.MessageData
import edu.wschina.bubblesexample.Model.ProfileModel
import edu.wschina.bubblesexample.R
import edu.wschina.bubblesexample.Broadcast.ReplyMessageReceiver
import edu.wschina.bubblesexample.Model.NotificationModel
import edu.wschina.bubblesexample.View.MainActivity

class MyFirebaseMessagingService: FirebaseMessagingService() {
    companion object {
        var notReadMsg = mutableListOf<MessageData>()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val userId = ProfileModel(this).id
        val msgContent = message.data["body"].toString()
        val personName = message.data["title"].toString()

        if(personName == userId)
            return

        val notificationModel = NotificationModel(this)
        notificationModel
            .addNotification(MessageData(personName, msgContent), if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) NotificationModel.NotificationType.BUBBLE else NotificationModel.NotificationType.NORMAL)
            .sendNotification()
    }
}