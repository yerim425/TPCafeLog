package com.yrlee.tpcafelog.ui.home

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ItemCafeBinding
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import com.yrlee.tpcafelog.model.Place

/*
data class Place(
    var id: String,
    var place_name: String,
    var category_name: String,
    var phone: String,
    var address_name: String,
    var road_address_name: String,
    @SerializedName("x") var longitude: String,
    @SerializedName("y") var latitude: String,
    var place_url: String,
    var distance: String
)
 */

class HomeCafeAdapter(val context: Context): RecyclerView.Adapter<HomeCafeAdapter.VH>() {

    var itemList = mutableListOf<Place>()

    inner class VH(val binding: ItemCafeBinding): ViewHolder(binding.root){
        init {
            binding.main.setOnClickListener {
                val intent = Intent(context, CafeDetailActivity::class.java)
                intent.putExtra("id", itemList[layoutPosition].id) // 서버에서 리뷰 리스트 받아 올때 사용
                intent.putExtra("place_url", itemList[layoutPosition].place_url)
                context.startActivity(intent) // 카카오 장소 상세 페이지
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCafeBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        itemList[position].apply {
            with(holder.binding){
                // 카페 이름
                tvCafeName.text = place_name
                // 카페 거리
                if(distance.isEmpty()) {
                    ivDot.visibility = View.INVISIBLE
                    tvDistance.visibility = View.INVISIBLE
                }
                else{
                    tvDistance.text = distance + "m"
                    ivDot.visibility = View.VISIBLE
                    tvDistance.visibility = View.VISIBLE
                }
                // 카페 카테고리
                // "가정,생활 > 여가시설 > 만화방 > 만화카페 > 벌툰"
//                val baseCategory = "음식점 > 카페"
//                if(category_name == baseCategory){
//                    tvCategory.visibility = View.INVISIBLE
//                }else{
//                    var category = ""
//                    if(category_name.length > baseCategory.length){
//                        val keyword = "카페 >"
//                        category = category_name.indexOf(keyword).takeIf { it >= 0 }?.let{
//                            category_name.substring(it + keyword.length).trim()} ?: category_name
//                    }
//                    else category = category_name
//                    tvCategory.text = category
//                    tvCategory.visibility = View.VISIBLE
//                }
                tvCategory.text = category_name
                // 카페 전화번호
                if(phone.isEmpty()){
                    tvPhone.visibility = View.INVISIBLE
                }else{
                    tvPhone.visibility = View.VISIBLE
                    tvPhone.text = phone
                }
                if(img_url==null){
                    Glide.with(context).load(R.drawable.ic_app_logo).into(ivCafe)
                }else{
                    Glide.with(context).load(img_url).into(ivCafe)
                }
                // 서버에 해당 카페의 정보가 있으면 ui 변경
//                layoutRating.visibility = View.INVISIBLE
//                line.visibility = View.GONE
//                recyclerviewVisit.visibility = View.GONE
                // 대표 이미지 등록
            }
        }
    }

    fun addItems(items: List<Place>){
        val startPos = itemList.size
        itemList.addAll(items)
        notifyItemRangeInserted(startPos, items.size)
    }

    fun updateImage(pos: Int, url: String?){
        itemList[pos].img_url = url
        notifyItemChanged(pos)
    }

    fun clearItemList(){
        itemList.clear()
        notifyDataSetChanged()
    }

}