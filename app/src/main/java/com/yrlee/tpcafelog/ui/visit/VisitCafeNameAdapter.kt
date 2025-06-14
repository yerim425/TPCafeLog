package com.yrlee.tpcafelog.ui.visit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.databinding.ItemCafeNameBinding
import com.yrlee.tpcafelog.model.CafeName

class VisitCafeNameAdapter(
    val context: Context,
    private val onItemClick: (CafeName) -> Unit
): Adapter<VisitCafeNameAdapter.VH>() {

    val cafeNameList = mutableListOf<CafeName>()

    inner class VH(val binding: ItemCafeNameBinding): ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                onItemClick(cafeNameList[bindingAdapterPosition])
//                val pos = bindingAdapterPosition
//                if (pos != RecyclerView.NO_POSITION && pos < cafeNameList.size) {
//                    onItemClick(cafeNameList[pos])
//                }
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

    fun addItems(items: List<CafeName>){
        val pos = cafeNameList.size
        cafeNameList.addAll(items)
        notifyItemRangeInserted(pos, items.size)

    }

    fun clearItems(){
        cafeNameList.clear()
        notifyDataSetChanged()
    }
}