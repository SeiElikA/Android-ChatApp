package edu.wschina.bubblesexample.Model

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import edu.wschina.bubblesexample.Broadcast.ReplyMessageReceiver
import edu.wschina.bubblesexample.DataModel.MessageData
import edu.wschina.bubblesexample.R
import edu.wschina.bubblesexample.Service.MyFirebaseMessagingService
import edu.wschina.bubblesexample.View.MainActivity

class NotificationModel(private val context: Context) {
    private var build = Builder()
    private var notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        notificationManager.createNotificationChannel(NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_DEFAULT))
    }

    @SuppressLint("NewApi")
    fun addNotification(msgData: MessageData, type: NotificationType): Builder {
        if(type == NotificationType.BUBBLE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val id = ProfileModel(context).id
            build.notification = getBubbleNotification(id, msgData.identity, msgData.message)
        } else {
            build.notification = getNormalNotification(msgData.identity, msgData.message)
        }
        return build
    }

    fun cancelNotification() {
        notificationManager.cancel(1)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getBubbleNotification(myIdentity: String, personName: String, message: String): Notification {
        // Create bubble intent
        val target = Intent(context, MainActivity::class.java)
        val bubbleIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_UPDATE_CURRENT)

        val icon = Icon.createWithResource(context, R.drawable.ic_baseline_person_24)


        val remoteInput = RemoteInput.Builder("key_text_reply")
            .setLabel("Reply message...")
            .build()

        val replyIntent = Intent(context, ReplyMessageReceiver::class.java)
        val replyPendingIntent = PendingIntent.getBroadcast(context, 0,replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val replyAction = Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_baseline_send_24),
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        MyFirebaseMessagingService.notReadMsg.add(MessageData(personName, message))
        val chatPersonMe: Person = Person.Builder()
            .setName(myIdentity) // notification my reply person name
            .setIcon(Icon.createWithResource(context, R.drawable.ic_launcher_background)) // person avatar
            .build()

        val style = Notification.MessagingStyle(chatPersonMe)
            .setGroupConversation(true)

        MyFirebaseMessagingService.notReadMsg.forEach {
            val chatPerson: Person = Person.Builder()
                .setName(it.identity) // notification person title
                .setIcon(icon) // person avatar
                .build()

            style.addMessage(
                it.message, // notification content
                System.currentTimeMillis(),
                chatPerson
            )
        }

        val id = "shortcut_info_id"
        val shortcutInfo = ShortcutInfo.Builder(context, id)
            .setLongLived(true)
            .setShortLabel("Group Message") // notification title
            .setIntent(Intent(Settings.ACTION_SETTINGS))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_baseline_group_24)) // notification icon
            .build()

        (context.getSystemService(FirebaseMessagingService.SHORTCUT_SERVICE) as ShortcutManager).pushDynamicShortcut(shortcutInfo)

        val bubbleData = Notification.BubbleMetadata
            .Builder(bubbleIntent, Icon.createWithResource(context, R.drawable.ic_baseline_send_24))
            .setDesiredHeight(600)
            .setAutoExpandBubble(false)
            .build()

        return Notification.Builder(context, "1")
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setSmallIcon(R.drawable.ic_baseline_send_24)
            .setShowWhen(true)
            .setStyle(style)
            .addAction(replyAction)
            .setBubbleMetadata(bubbleData)
            .setShortcutId(id)
            .build()
    }

    private fun getNormalNotification(personName: String, msgContent:String): Notification {
        return Notification.Builder(context, "1")
            .setContentTitle(personName)
            .setContentText(msgContent)
            .build()
    }

    inner class Builder() {
        var notification: Notification? = null

        fun sendNotification() {
            if(notification == null)
                throw Exception("Need add notification first")

            notificationManager.notify(1, notification)
        }
    }

    enum class NotificationType {
        @RequiresApi(Build.VERSION_CODES.R)
        BUBBLE,
        NORMAL
    }
}