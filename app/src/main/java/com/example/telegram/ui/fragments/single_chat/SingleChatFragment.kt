package com.example.telegram.ui.fragments.single_chat

import android.view.View
import android.widget.AbsListView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.example.telegram.R
import com.example.telegram.database.*
import com.example.telegram.models.CommonModel
import com.example.telegram.models.UserModel
import com.example.telegram.ui.fragments.BaseFragment
import com.example.telegram.utilits.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_single_chat.*
import kotlinx.android.synthetic.main.toolbar_info.view.*

class SingleChatFragment(private val contact: CommonModel) :
    BaseFragment(R.layout.fragment_single_chat) {

    private lateinit var mListenerInfoToolbar: AppValueEventListener
    private lateinit var mReceivingUser: UserModel
    private lateinit var mToolbarInfo: View
    private lateinit var mRefUser: DatabaseReference
    private lateinit var mRefMessages: DatabaseReference
    private lateinit var mAdapter: SingleChatAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mMessagesListener: AppChildEventListener
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private var mCountMessages = 3
    private var mIsScrolling = false
    private var mSmoothScrollToPosition = true

    override fun onResume() {
        super.onResume()
        mSwipeRefreshLayout = chat_swipe_refresh
        initToolbar()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mRecyclerView = chat_recycler_view
        mAdapter = SingleChatAdapter()
        mRefMessages = REF_DATABASE_ROOT.child(
            NODE_MESSAGES
        ).child(CURRENT_UID).child(contact.id)
        mRecyclerView.adapter = mAdapter
        mMessagesListener = AppChildEventListener {
            mAdapter.addItem(it.getCommonModel(), mSmoothScrollToPosition) {
                if (mSmoothScrollToPosition) {
                    mRecyclerView.smoothScrollToPosition(mAdapter.itemCount)
                }
                mSwipeRefreshLayout.isRefreshing = false
            }
        }

        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessagesListener)

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mIsScrolling && dy < 0) {
                    updateData()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mIsScrolling = true
                }
            }
        })

        mSwipeRefreshLayout.setOnRefreshListener { updateData() }
    }

    private fun updateData() {
        mIsScrolling = false
        mSmoothScrollToPosition = false
        mCountMessages += 10
        mRefMessages.removeEventListener(mMessagesListener)
        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessagesListener)
    }

    private fun initToolbar() {
        mToolbarInfo = APP_ACTIVITY.mToolbar.toolbar_info
        mToolbarInfo.visibility = View.VISIBLE
        mListenerInfoToolbar = AppValueEventListener {
            mReceivingUser = it.getUserModel()
            initInfoToolbar()
        }
        mRefUser = REF_DATABASE_ROOT.child(
            NODE_USERS
        ).child(contact.id)
        mRefUser.addValueEventListener(mListenerInfoToolbar)
        chat_btn_send_message.setOnClickListener {
            mSmoothScrollToPosition = true
            val message = chat_input_message.text.toString()
            if (message.isEmpty()) {
                showToast("Введите сообщение")
            } else sendMessage(
                message,
                contact.id,
                TYPE_TEXT
            ) {
                chat_input_message.setText("")
            }
        }
    }

    private fun initInfoToolbar() {
        if (mReceivingUser.fullname.isEmpty()) {
            mToolbarInfo.toolbar_chat_fullname.text = contact.fullname
        } else mToolbarInfo.toolbar_chat_fullname.text = mReceivingUser.fullname
        mToolbarInfo.toolbar_chat_status.text = mReceivingUser.state
        if (mReceivingUser.photoUrl.isNotBlank())
            mToolbarInfo.toolbar_chat_image.downloadAndSetImage(mReceivingUser.photoUrl)
    }

    override fun onPause() {
        super.onPause()
        mToolbarInfo.visibility = View.GONE
        mRefUser.removeEventListener(mListenerInfoToolbar)
        mRefMessages.removeEventListener(mMessagesListener)
    }
}
