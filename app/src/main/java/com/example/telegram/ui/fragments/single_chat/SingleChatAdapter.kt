package com.example.telegram.ui.fragments.single_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.telegram.R
import com.example.telegram.models.CommonModel
import com.example.telegram.database.CURRENT_UID
import com.example.telegram.utilits.DiffUtilCallback
import com.example.telegram.utilits.asTime
import kotlinx.android.synthetic.main.message_item.view.*

class SingleChatAdapter : RecyclerView.Adapter<SingleChatAdapter.SingleChatHolder>() {

    private var mListMessagesCache = mutableListOf<CommonModel>()
    private lateinit var mDiffResult: DiffUtil.DiffResult

    class SingleChatHolder(view: View) : RecyclerView.ViewHolder(view) {

        val blocUserMessage: ConstraintLayout = view.block_user_message
        var chatUserMessage: TextView = view.chat_user_message
        val chatUserMessageTime: TextView = view.chat_user_message_time

        val blocReceivedMessage: ConstraintLayout = view.block_received_message
        val chatReceivedMessage: TextView = view.chat_received_message
        val chatReceivedMessageTime: TextView = view.chat_received_message_time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleChatHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return SingleChatHolder(view)
    }

    override fun getItemCount(): Int = mListMessagesCache.size

    override fun onBindViewHolder(holder: SingleChatHolder, position: Int) {
        if (mListMessagesCache[position].from == CURRENT_UID) {
            holder.blocUserMessage.visibility = View.VISIBLE
            holder.blocReceivedMessage.visibility = View.GONE
            holder.chatUserMessage.text = mListMessagesCache[position].text
            holder.chatUserMessageTime.text = mListMessagesCache[position]
                .timeStamp.toString().asTime()
        } else {
            holder.blocReceivedMessage.visibility = View.VISIBLE
            holder.blocUserMessage.visibility = View.GONE
            holder.chatReceivedMessage.text = mListMessagesCache[position].text
            holder.chatReceivedMessageTime.text = mListMessagesCache[position]
                .timeStamp.toString().asTime()
        }
    }


    fun addItem(
        item: CommonModel,
        toBottom: Boolean,
        onSuccess: () -> Unit
    ) {
        if (toBottom) {
            if (!mListMessagesCache.contains(item)) {
                mListMessagesCache.add(item)
                notifyItemInserted(mListMessagesCache.size)
            }
        } else {
            if (!mListMessagesCache.contains(item)) {
                mListMessagesCache.add(item)
                mListMessagesCache.sortBy { it.timeStamp.toString() }
                notifyItemInserted(0)
            }
        }
        onSuccess()
    }
}

