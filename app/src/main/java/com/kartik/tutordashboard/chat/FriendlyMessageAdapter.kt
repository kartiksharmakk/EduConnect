package com.kartik.tutordashboard.chat

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kartik.tutordashboard.Adapter.QuestionAdapter
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.chat.ChatActivity.Companion.ANONYMOUS
import com.kartik.tutordashboard.databinding.ImageMessageBinding
import com.kartik.tutordashboard.databinding.MessageBinding

// The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
// See: https://github.com/firebase/FirebaseUI-Android
class FriendlyMessageAdapter(
    private val options: FirebaseRecyclerOptions<FriendlyMessage>,
    private val currentUserName: String?,
    private val currentEmail: String?,
    onclickListner: FriendlyMessageAdapter.onclickListner
) :
    FirebaseRecyclerAdapter<FriendlyMessage, ViewHolder>(options) {

    val clickListner: FriendlyMessageAdapter.onclickListner =onclickListner

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TEXT) {
            val view = inflater.inflate(R.layout.message, parent, false)
            val binding = MessageBinding.bind(view)
            MessageViewHolder(binding)
        } else {
            val view = inflater.inflate(R.layout.image_message, parent, false)
            val binding = ImageMessageBinding.bind(view)
            ImageMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: FriendlyMessage) {
        if (options.snapshots[position].text != null) {
            (holder as MessageViewHolder).bind(model)
        } else {
            (holder as ImageMessageViewHolder).bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (options.snapshots[position].text != null) VIEW_TYPE_TEXT else VIEW_TYPE_IMAGE
    }

    inner class MessageViewHolder(private val binding: MessageBinding) : ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {
            // TODO: implement

            if (item.email != currentEmail) {

                binding.messageTextViewRight.visibility = View.GONE
                binding.messengerImageViewRight.visibility = View.GONE
                binding.messengerTextViewRight.visibility = View.GONE

                binding.messageTextView.text = item.text
                setTextColor(item.name, binding.messageTextView, item.email)

                binding.messengerTextView.text = if (item.name == null) ANONYMOUS else item.name
                if (item.photoUrl != null) {
                    loadImageIntoView(binding.messengerImageView, item.photoUrl!!)
                } else {
                    binding.messengerImageView.setImageResource(R.drawable.ic_action_name)
                }

            } else {

                binding.messageTextView.visibility = View.GONE
                binding.messengerImageView.visibility = View.GONE
                binding.messengerTextView.visibility = View.GONE


                binding.messageTextViewRight.text = item.text
                setTextColor(item.name, binding.messageTextViewRight, item.email)

                binding.messengerTextViewRight.text =
                    if (item.name == null) ANONYMOUS else item.name
                if (item.photoUrl != null) {
                    loadImageIntoView(binding.messengerImageViewRight, item.photoUrl!!)
                } else {
                    binding.messengerImageViewRight.setImageResource(R.drawable.ic_action_name)
                }

            }
        }

        private fun setTextColor(userName: String?, textView: TextView, email: String?) {
            if (userName != ANONYMOUS && currentEmail == email && email != null) {
                textView.setBackgroundResource(R.drawable.rounded_message_blue)
                textView.setTextColor(Color.WHITE)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray)
                textView.setTextColor(Color.BLACK)
            }
        }
    }

    inner class ImageMessageViewHolder(private val binding: ImageMessageBinding) :
        ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {
            // TODO: implement


            if (item.email != currentEmail) {


                binding.messageImageViewRight.visibility = View.GONE
                binding.messengerImageViewRight.visibility = View.GONE
                binding.messengerTextViewRight.visibility = View.GONE

                loadImageIntoView(binding.messageImageView, item.imageUrl!!)

                binding.messengerTextView.text = if (item.name == null) ANONYMOUS else item.name
                if (item.photoUrl != null) {
                    loadImageIntoView(binding.messengerImageView, item.photoUrl!!)
                } else {
                    binding.messengerImageView.setImageResource(R.drawable.ic_action_name)
                }



                binding.messageImageView.setOnClickListener {
                    clickListner.onClickImage(item.imageUrl)
                }



            } else {

                binding.messageImageView.visibility = View.GONE
                binding.messengerImageView.visibility = View.GONE
                binding.messengerTextView.visibility = View.GONE


                loadImageIntoView(binding.messageImageViewRight, item.imageUrl!!)

                binding.messengerTextViewRight.text =
                    if (item.name == null) ANONYMOUS else item.name
                if (item.photoUrl != null) {
                    loadImageIntoView(binding.messengerImageViewRight, item.photoUrl!!)
                } else {
                    binding.messengerImageViewRight.setImageResource(R.drawable.ic_action_name)
                }

                binding.messageImageViewRight.setOnClickListener {
                    clickListner.onClickImage(item.imageUrl)
                }

            }
        }
    }

    private fun loadImageIntoView(view: ImageView, url: String) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Glide.with(view.context)
                        .load(downloadUrl)
                        .into(view)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Getting download url was not successful.",
                        e
                    )
                }
        } else {
            Glide.with(view.context).load(url).into(view)
        }
    }

    companion object {
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
    }

    interface onclickListner{
        fun onClickImage(imageUri: String?)
    }
}
