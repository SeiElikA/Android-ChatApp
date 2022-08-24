package edu.wschina.bubblesexample.View

import android.app.*
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import edu.wschina.bubblesexample.Adapter.MessageAdapter
import edu.wschina.bubblesexample.DataModel.MessageData
import edu.wschina.bubblesexample.Model.MessageModel
import edu.wschina.bubblesexample.Model.NotificationModel
import edu.wschina.bubblesexample.Service.MyFirebaseMessagingService
import edu.wschina.bubblesexample.databinding.ActivityMainBinding
import okhttp3.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var control: ActivityMainBinding
    private var msgList = mutableListOf<MessageData>()
    private lateinit var adapter: MessageAdapter
    private lateinit var msgModel: MessageModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        control = ActivityMainBinding.inflate(layoutInflater)
        setContentView(control.root)

        msgModel = MessageModel(this)
        MyFirebaseMessagingService.notReadMsg.clear()

        // get chat history
        msgModel.getChatHistory({
            runOnUiThread {
                msgList = it.map { x -> MessageData(x.identity, x.message) }.toMutableList()
                setList()
            }
        }, failure = {
            runOnUiThread {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        })

        // send message event
        control.imgSend.setOnClickListener {
            val msg = control.txtInputMessage.text.toString()

            if (msg.isBlank()) {
                Toast.makeText(this, "Input message can't empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            msgModel.sendMessage(msg)

            control.txtInputMessage.setText("")
        }

        // on message receive
        msgModel.onMessageReceiveEvent = {
            runOnUiThread {
                msgList.add(it)
                refreshList()
            }
        }
    }

    private fun setList() {
        val layoutManager = LinearLayoutManager(this)
        control.listView.layoutManager = layoutManager
        adapter = MessageAdapter(this, msgList)
        control.listView.adapter = adapter
        control.listView.scrollToPosition(msgList.count() - 1)
    }

    private fun refreshList() {
        adapter.msgList = msgList
        adapter.notifyDataSetChanged()

        control.listView.scrollToPosition(msgList.size - 1)
    }
}