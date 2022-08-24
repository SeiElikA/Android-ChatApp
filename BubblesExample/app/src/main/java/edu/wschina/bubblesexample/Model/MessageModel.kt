package edu.wschina.bubblesexample.Model

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import edu.wschina.bubblesexample.DataModel.InitMessageData
import edu.wschina.bubblesexample.DataModel.MessageData
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MessageModel(private val context: Context) {
    private lateinit var webSocket: WebSocket
    var onMessageReceiveEvent:((MessageData) -> Unit)? = null

    init {
        initWebSocket()
    }

    fun sendMessage(msg: String) {
        val id = ProfileModel(context).id
        val jsonObj = JSONObject()
        jsonObj.put("identity", id)
        jsonObj.put("message", msg)
        val msgData = jsonObj.toString()

        webSocket.send(msgData)
    }

    fun getChatHistory(completion: (List<InitMessageData>) -> Unit, failure: ((Exception) -> Unit)? = null) {
        val request = Request.Builder()
            .url("http://192.168.100.168:8000/history")
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failure?.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val dataList = Gson().fromJson(response.body?.string(), Array<InitMessageData>::class.java).toList()
                completion(dataList)
            }
        })
    }

    private fun initWebSocket() {
        val request = Request.Builder()
            .url("ws://192.168.100.168:3000")
            .build()

        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.e("TAG", "onOpen")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val jsonObj = JSONObject(text)
                val identity = jsonObj.get("identity").toString()
                val message = jsonObj.get("message").toString()
                val msgData = MessageData(identity, message)
                onMessageReceiveEvent?.invoke(msgData)
            }
        })

        client.dispatcher.executorService.shutdown()
    }
}