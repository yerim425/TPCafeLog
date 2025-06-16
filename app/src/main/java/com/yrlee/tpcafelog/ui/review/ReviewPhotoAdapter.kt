package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.yrlee.tpcafelog.databinding.ItemPhotoBinding
import com.yrlee.tpcafelog.model.PhotoItem

class ReviewPhotoAdapter(val context: Context, val photoList: MutableList<PhotoItem>) :
    Adapter<ReviewPhotoAdapter.VH>() {

    inner class VH(val binding: ItemPhotoBinding) : ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener {
                val item = photoList[bindingAdapterPosition]

                // 고정된 URL 이미지는 삭제 불가
                if (item is PhotoItem.Remote) {
                    Toast.makeText(context, "이 이미지는 삭제할 수 없어요.", Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
                AlertDialog.Builder(context)
                    .setTitle("사진 삭제하시겠어요?")
                    .setPositiveButton("삭제") { _, _ ->
                        photoList.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                    }
                    .create().show()
                true
            }
        }

        fun bind(item: PhotoItem) {
            when (item) {
                is PhotoItem.Remote -> {
                    Glide.with(context).load(item.url).into(binding.ivCafe)
                    binding.ivMain.visibility = View.VISIBLE
                }
                is PhotoItem.Local -> {
                    Glide.with(context).load(item.uri).into(binding.ivCafe)
                    binding.ivMain.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = photoList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(photoList[position])
    }
}