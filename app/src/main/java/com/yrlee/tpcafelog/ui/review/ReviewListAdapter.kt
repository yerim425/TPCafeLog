package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.local.OnReviewClickListener
import com.yrlee.tpcafelog.databinding.ItemReviewBinding
import com.yrlee.tpcafelog.model.ReviewListItemResponse

class ReviewListAdapter(
    private val context: Context,
    private val items: List<ReviewListItemResponse>,
    private val listener: OnReviewClickListener
): Adapter<ReviewListAdapter.VH>() {

    inner class VH(val binding: ItemReviewBinding): ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                listener.onItemClick(items[bindingAdapterPosition])
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemReviewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder){
            // 유저 정보 set
            Glide.with(binding.root).load(item.userInfo.img_url)
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.ivUserProfile)
            binding.tvUserName.text = item.userInfo.name
            binding.tvUserLevel.text = "Lv.${item.userInfo.level} ${item.userInfo.title}"

            // 카페 정보 set
            binding.tvCafeName.text = item.cafeInfo.name
            binding.tvCategory.text = item.cafeInfo.category
            binding.tvAddress.text = item.cafeInfo.address

            // 리뷰 정보 set
            Glide.with(binding.root).load(item.img_url).into(binding.ivCafe)
            binding.tvReviewContent.text = "\"${item.content}\""
            binding.tvRating.text = item.rating.toString()
            val tags = item.hashtag_names.joinToString(" ") { "#${it}" }
            binding.tvHashtagNames.text = tags
        }
    }

}