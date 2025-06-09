package com.yrlee.tpcafelog.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.databinding.ItemHashtagBinding

class HomeHashtagAdapter(val context: Context, private val items: List<String>): Adapter<HomeHashtagAdapter.VH>() {

    val checkedList: MutableList<String> = mutableListOf()

    inner class VH(val binding: ItemHashtagBinding): ViewHolder(binding.root){
        init{
            binding.root.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) checkedList.add(buttonView.text.toString())
                else checkedList.remove(buttonView.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemHashtagBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.root.text = items[position]
    }

    //fun getCheckedList(): List<String> = checkedList.toList()
}