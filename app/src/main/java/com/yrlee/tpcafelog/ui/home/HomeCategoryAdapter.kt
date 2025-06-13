package com.yrlee.tpcafelog.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ItemCategoryBinding

class HomeCategoryAdapter(
    val context: Context,
    private val items: List<String>,
    private val listener: OnCategoryItemClickListener): Adapter<HomeCategoryAdapter.VH>() {

    var selectedPosition = items.indexOf(context.getString(R.string.coffee_shop))

    inner class VH(val binding: ItemCategoryBinding): ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.cb.text = items[position]
        holder.binding.cb.isChecked = position == selectedPosition
        holder.binding.cb.setOnClickListener {
            val prePos = selectedPosition
            selectedPosition = if (position == selectedPosition) {
                RecyclerView.NO_POSITION // 같은 걸 클릭하면 해제
            } else {
                position
            }

            notifyItemChanged(prePos)
            notifyItemChanged(position)

            listener.onCategoryItemSelected()
        }
    }

    //fun getCheckedList(): List<String> = checkedList.toList()

    // 선택된 카테고리 이름 가져오기
    fun getSelectedCategory(): String{
        return if(selectedPosition == -1) "" else items[selectedPosition]
    }

    // 카테고리 선택 전부 해제
    fun setUnselect(){
        val prePos = selectedPosition
        selectedPosition = -1
        notifyItemChanged(prePos)
    }

}