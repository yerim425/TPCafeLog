package com.yrlee.tpcafelog.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ItemCafeBinding
import com.yrlee.tpcafelog.model.HomeCafeResponse
import com.yrlee.tpcafelog.model.Place
import com.yrlee.tpcafelog.util.PrefUtils

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
                //intent.putExtra("id", itemList[layoutPosition].id) // 서버에서 리뷰 리스트 받아 올때 사용
                //intent.putExtra("place_url", itemList[layoutPosition].place_url)
                val place = itemList[layoutPosition]
                intent.putExtra("place", Gson().toJson(place))
                context.startActivity(intent) // 카카오 장소 상세 페이지
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCafeBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itemList[position]
        with(holder){
            // 카페 이름
            binding.tvCafeName.text = item.place_name
            // 카페 사용자 평점
            if(item.avg_rating == null){
                binding.layoutRating.visibility = View.GONE
            }else{
                binding.layoutRating.visibility = View.VISIBLE
                binding.tvRating.text = item.avg_rating.toString()
            }
            // 카페 거리
            if(item.distance.isEmpty()) {
                binding.tvDistance.visibility = View.GONE
            }
            else{
                binding.tvDistance.text = item.distance + "m"
                binding.tvDistance.visibility = View.VISIBLE
            }
            // 카페 전화번호
            if(item.phone.isEmpty()){
                binding.layoutPhone.visibility = View.GONE
            }else{
                binding.layoutPhone.visibility = View.VISIBLE
                binding.tvPhone.text = item.phone
            }

            // 카페 주소
            val address = if(item.address_name.isEmpty()) item.road_address_name else item.address_name
            binding.tvAddress.text = address

            // 카페 카테고리
            binding.tvCategory.text = item.category_name

           // 이미지
            item.visitDatas.let{
                if(it==null){
                    if(item.img_url == null) Glide.with(context).load(R.drawable.ic_app_logo).placeholder(R.drawable.ic_app_logo).into(binding.ivCafe)
                    else Glide.with(context).load(item.img_url).placeholder(R.drawable.ic_app_logo).into(binding.ivCafe)

                    binding.layoutVisit.visibility = View.GONE
                }else{
                    Glide.with(context).load(item.img_url).placeholder(R.drawable.ic_app_logo).into(binding.ivCafe)
                    binding.layoutVisit.visibility = View.VISIBLE

                    // 방문인증 리사이클러뷰 세팅
                    binding.recyclerviewVisit.adapter = HomeCafeVisitAdapter(context, it)
                }
            }
            if(item.distance.isEmpty() || item.avg_rating == null){
                binding.ivDot.visibility = View.GONE
            }else binding.ivDot.visibility = View.VISIBLE

            // 해시태그
            if(item.hashtag_names == null){
                binding.tvHashtagNames.visibility = View.GONE
            }else{
                val names = item.hashtag_names?.split(",")?.joinToString(" ") { "#$it" } ?: ""
                binding.tvHashtagNames.text = names
                binding.tvHashtagNames.visibility = View.VISIBLE
            }

            // 좋아요
            if(PrefUtils.getInt("user_id") == -1 || PrefUtils.getInt("user_id") == 0){
                binding.cbFavorite.visibility = View.INVISIBLE
            }else binding.cbFavorite.visibility = View.VISIBLE

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

    fun updateDBdata(datas: List<HomeCafeResponse>){
        datas.forEach{ data ->
            val pos = itemList.indexOfFirst { it.id == data.place_id }
            if(pos != -1){
                itemList[pos].avg_rating = data.avg_rating
                itemList[pos].visit_cnt = data.visit_id
                itemList[pos].review_cnt = data.review_id
                itemList[pos].hashtag_names = data.hashtag_names
                data.visit_datas?.let {
                    itemList[pos].visitDatas = it
                    itemList[pos].img_url = it[it.lastIndex].visit_img_url
                    itemList[pos].isImgRequested = true
                }
                notifyItemChanged(pos)
            }
        }

    }

}