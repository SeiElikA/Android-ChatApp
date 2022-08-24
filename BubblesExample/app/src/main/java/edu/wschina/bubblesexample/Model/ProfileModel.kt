package edu.wschina.bubblesexample.Model

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.util.*

class ProfileModel(context: Context) {
    private var sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)

    var id: String
        get() {
            val saveID = sharedPreferences.getString("id", "") ?: ""
            if(saveID.isEmpty()) {
                val newId = UUID.randomUUID().toString().substring(0, 8)
                this.id = newId
                return newId
            }
            return saveID
        }
        set(value) {
            sharedPreferences.edit {
                putString("id", value)
            }
        }

    fun registerDeviceToken(token: String) {
        val jsonObject = JSONObject()
        jsonObject.put("deviceToken", token)

        val request = Request.Builder()
            .url("http://192.168.100.168:8000/sendDeviceTokenAndroid")
            .method("POST", RequestBody.create("application/json".toMediaType(), jsonObject.toString()))
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", "onFailure: ${e.message.toString()}")
            }

            override fun onResponse(call: Call, response: Response) {
            }
        })
    }
}