package edu.wschina.bubblesexample.View

import android.app.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import edu.wschina.bubblesexample.Model.NotificationModel
import edu.wschina.bubblesexample.Model.ProfileModel
import edu.wschina.bubblesexample.databinding.ActivityEmptyBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.util.*

class EmptyActivity : AppCompatActivity() {
    private lateinit var control: ActivityEmptyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        control = ActivityEmptyBinding.inflate(layoutInflater)
        setContentView(control.root)



        control.btnFunction.setOnClickListener {
            NotificationModel(this).cancelNotification() // set notification read
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}