package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.databinding.ItemHashtagBinding
import com.yrlee.tpcafelog.model.HashTagItem

class ReviewHashtagAdapter(val context: Context, val items: List<HashTagItem>): Adapter<ReviewHashtagAdapter.VH>() {

    private val checkedList = mutableListOf<HashTagItem>()

    inner class VH(val binding: ItemHashtagBinding): ViewHolder(binding.root){
        init {
            binding.cb.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    checkedList.add(items[layoutPosition])
                }else{
                    checkedList.remove(items[layoutPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemHashtagBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.cb.text = "#" + items[position].name
    }

    fun getCheckedList(): List<HashTagItem>{
        return checkedList
    }
}