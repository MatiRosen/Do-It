package team.doit.do_it.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import team.doit.do_it.listeners.NotificationReceivedListener


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        var notificationReceivedListener = NotificationReceivedListener()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.data.isNotEmpty() && message.data["fromFragment"] == "UserChatFragment"){
            notificationReceivedListener.notificationReceived()
        }
    }
}