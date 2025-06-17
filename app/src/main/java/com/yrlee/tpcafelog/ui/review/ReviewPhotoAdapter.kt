package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.net.Uri
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

class ReviewPhotoAdapter(val context: Context, var mainPhoto: String) : Adapter<ReviewPhotoAdapter.VH>() {

    val photoList: MutableList<PhotoItem> = mutableListOf()

    init {
        photoList.add(PhotoItem.Remote(mainPhoto))
    }

    inner class VH(val binding: ItemPhotoBinding) : ViewHolder(binding.root) {

        init{
            binding.root.setOnLongClickListener {
                val item = photoList[bindingAdapterPosition]

                // 고정된 URL 이미지는 삭제 불가 // 나중에 메인 이미지 변경하면 방문인증 테이블 데이터에 사진 변경하는 거로 바꾸기...
                if (item is PhotoItem.Remote) {
                    Toast.makeText(binding.root.context.applicationContext, "메인 사진은 삭제할 수 없어요.", Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
                AlertDialog.Builder(context)
                    .setTitle("사진 삭제하시겠어요?")
                    .setPositiveButton("삭제") { _, _ ->
                        photoList.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                        (context as ReviewAddActivity).setPhotoNum(itemCount)
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

    fun addPhotos(photos: List<PhotoItem.Local>){
        val pos = photoList.size
        photoList.addAll(photos)
        notifyItemRangeInserted(pos, photos.size)
        (context as ReviewAddActivity).setPhotoNum(itemCount)
    }

    fun getPhotoUris(): List<Uri>{
        return photoList.filterIsInstance<PhotoItem.Local>()
            .map { it.uri }
    }
}