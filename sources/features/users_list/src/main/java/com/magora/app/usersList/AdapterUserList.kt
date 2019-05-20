package com.magora.app.usersList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.magora.app.model.User
import com.magora.realm.ui.BaseRealmListenableAdapter
import com.magora.realmpaginator.RealmPagedList
import kotlinx.android.synthetic.main.item_user.view.*

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */

class AdapterUserList(
    data: RealmPagedList<*, User>,
    private val onClick: (Int, Int) -> Unit
) : BaseRealmListenableAdapter<User, RecyclerView.ViewHolder>(data) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view).apply {
            itemView.setOnClickListener { v ->
                val adapterPosition = this.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    getItem(adapterPosition)?.let { user ->
                        onClick(user.id, adapterPosition)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserViewHolder).bind(getItem(position))
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.avatarImageView
        private val nameTextView = itemView.nameTextView

        fun bind(user: User?) {
            if (user != null) {
                Glide.with(itemView)
                    .load(user.avatarUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(avatarImageView)

                nameTextView.text = user.login
            }

            avatarImageView.transitionName = "transition:$adapterPosition"
            itemView.transitionName = "transition:$adapterPosition-123"
        }
    }
}