package com.yrlee.tpcafelog.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ItemVisitBinding
import com.yrlee.tpcafelog.model.CafeInfoVisit

class HomeCafeVisitAdapter(val context: Context, val itemList: List<CafeInfoVisit>): Adapter<HomeCafeVisitAdapter.VH>() {

    inner class VH(val binding: ItemVisitBinding): ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                if(itemList[bindingAdapterPosition].is_reviewed){
                    // 리뷰 디테일 화면으로..
                }else{
                    // 방문 사진 원본 화면으로..
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemVisitBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itemList[position]
        with(holder){
            Glide.with(binding.root).load(item.visit_img_url).placeholder(R.drawable.ic_app_logo).into(binding.ivCafe)
            if(item.is_reviewed){
                binding.layoutIsReviewed.visibility = View.VISIBLE
            }else{
                binding.layoutIsReviewed.visibility = View.GONE
            }
            binding.tvName.visibility = View.GONE
        }
    }


}