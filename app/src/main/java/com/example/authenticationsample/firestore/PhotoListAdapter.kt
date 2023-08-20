package com.example.authenticationsample.firestore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticationsample.databinding.ItemPhotoBinding
import com.example.authenticationsample.firestore.model.PhotoItem
import com.squareup.picasso.Picasso

class PhotoListAdapter(val itemClicked:(Int, String)->Unit) : ListAdapter<PhotoItem, PhotoListAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photoItem: PhotoItem) {
            binding.apply {
                tvUsername.text = photoItem.username
                tvComment.text = photoItem.comment
                Picasso.get().load(photoItem.photoUrl).into(ivPhoto)

                groupEdit.isVisible = false
                groupComment.isVisible = true

                btnClose.setOnClickListener {
                    groupEdit.isVisible = false
                    groupComment.isVisible = true
                }

                btnDone.setOnClickListener {
                    itemClicked(position, etComment.text.toString())
                    btnClose.performClick()
                }

                btnEdit.setOnClickListener {
                    groupEdit.isVisible = true
                    groupComment.isVisible = false
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val currentPhoto = getItem(position)
        holder.bind(currentPhoto)
    }

    private class PhotoDiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem == newItem
        }
    }
}


