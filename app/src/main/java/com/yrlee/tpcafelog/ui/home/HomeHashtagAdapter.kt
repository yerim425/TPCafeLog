package com.yrlee.tpcafelog.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.data.local.OnHomeItemSelectListener
import com.yrlee.tpcafelog.databinding.ItemHashtagBinding
import com.yrlee.tpcafelog.model.HashTagItem
import com.yrlee.tpcafelog.util.Constants

class HomeHashtagAdapter(
    val context: Context,
    private val items: List<HashTagItem>,
    private val listener: OnHomeItemSelectListener
): Adapter<HomeHashtagAdapter.VH>() {

    val checkedList: MutableList<HashTagItem> = mutableListOf()

    inner class VH(val binding: ItemHashtagBinding): ViewHolder(binding.root){
        init{
            binding.root.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) checkedList.add(items[bindingAdapterPosition])
                else checkedList.remove(items[bindingAdapterPosition])
                listener.onItemSelected(Constants.HASHTAG_TYPE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemHashtagBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.root.text = "#"+items[position].name
    }

    fun getHashtagList(): List<HashTagItem>{
        return checkedList.toList()
    }
}