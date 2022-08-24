package edu.wschina.bubblesexample

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import edu.wschina.bubblesexample.Model.ProfileModel

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        // register token
        FirebaseMessaging.getInstance().subscribeToTopic("news")
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful)
                return@addOnCompleteListener

            ProfileModel(this).registerDeviceToken(it.result)
        }
    }
}