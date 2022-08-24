package edu.wschina.bubblesexample.Adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.wschina.bubblesexample.DataModel.MessageData
import edu.wschina.bubblesexample.Model.ProfileModel
import edu.wschina.bubblesexample.R
import edu.wschina.bubblesexample.databinding.ItemInMsgBinding
import edu.wschina.bubblesexample.databinding.ItemOutMsgBinding

class MessageAdapter(private var context: Context, var msgList: MutableList<MessageData>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private val id = ProfileModel(context).id

    abstract class ViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        open var name: TextView? = null
        open var content: TextView? = null
        open var root: View? = null

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            val copy = menu?.add("Copy")
            copy?.setOnMenuItemClickListener {
                val txtView = v?.findViewById<TextView>(R.id.txtMsg)
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("label", txtView?.text.toString()))
                Toast.makeText(context, "Copy Successful", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
        }
    }

    class MessageInViewHolder(item: ItemInMsgBinding, context: Context) : ViewHolder(item.root, context) {
        override var name: TextView? = item.txtName
        override var content: TextView? = item.txtMsg
        override var root: View? = item.root

        init {
            root?.setOnCreateContextMenuListener(this)
        }
    }

    class MessageOutViewHolder(item: ItemOutMsgBinding, context: Context) : ViewHolder(item.root, context) {
        override var name: TextView? = item.txtName
        override var content: TextView? = item.txtMsg
        override var root: View? = item.root

        init {
            root?.setOnCreateContextMenuListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1) {
            MessageOutViewHolder(ItemOutMsgBinding.inflate(LayoutInflater.from(context), parent, false), context)
        } else {
            MessageInViewHolder(ItemInMsgBinding.inflate(LayoutInflater.from(context), parent, false), context)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = msgList[position]
        holder.name?.text = data.identity
        holder.content?.text = data.message
    }

    override fun getItemCount(): Int {
        return msgList.count()
    }

    override fun getItemViewType(position: Int): Int {
        val data = msgList[position]
        return if (data.identity == id) 1 else 0
    }
}