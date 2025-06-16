package com.yrlee.tpcafelog.ui.visit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.databinding.ItemCafeNameBinding
import com.yrlee.tpcafelog.model.CafeItem

class CafeNameAdapter(
    val context: Context,
    private val onItemClick: (CafeItem) -> Unit
): Adapter<CafeNameAdapter.VH>() {

    val cafeNameList = mutableListOf<CafeItem>()

    inner class VH(val binding: ItemCafeNameBinding): ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                onItemClick(cafeNameList[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCafeNameBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = cafeNameList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.tvCafeName.text = cafeNameList[position].name
        holder.binding.tvCafeAddress.text = cafeNameList[position].address
    }

    fun addItems(items: List<CafeItem>){
        val pos = cafeNameList.size
        cafeNameList.addAll(items)
        notifyItemRangeInserted(pos, items.size)

    }

    fun clearItems(){
        cafeNameList.clear()
        notifyDataSetChanged()
    }
}