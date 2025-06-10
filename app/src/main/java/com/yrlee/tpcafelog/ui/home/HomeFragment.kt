package com.yrlee.tpcafelog.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var categoryItems: Array<String>
    var choiceCategoryId = R.id.tv_category
    lateinit var categoryQuery: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryQuery = getString(R.string.coffee_shop)
        setCategoryList()
        setHashtagList()

    }

//    fun setCategoryButtonListener(){
//        binding.layoutCategory.tvCoffeeShop.setOnCheckedChangeListener(clickCategory())
//        binding.layoutCategory.tvDessert.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvCartoon.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvStudy.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvBook.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvKids.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvBoard.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvFruits.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvGallery.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvLive.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvMeeting.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvDog.setOnCheckedChangeListener { clickCategory(it) }
//        binding.layoutCategory.tvCat.setOnCheckedChangeListener { clickCategory(it) }
//    }

    fun setCategoryList(){
        categoryItems = arrayOf(
            getString(R.string.coffee_shop), getString(R.string.dessert_cafe), getString(R.string.cartoon_cafe), getString(R.string.board_cafe),
            getString(R.string.study_cafe), getString(R.string.book_cafe), getString(R.string.kids_cafe), getString(R.string.live_cafe),
            getString(R.string.galley_cafe), getString(R.string.dogs_cafe), getString(R.string.cats_cafe), getString(R.string.fruits_shop),
            getString(R.string.meeting_space)
        )
        binding.recyclerviewHomeCategory.adapter = HomeCategoryAdapter(requireContext(), categoryItems.toList())
    }

    fun clickCategory(v: View){

        view?.findViewById<CheckBox>(choiceCategoryId)?.isChecked = false
        choiceCategoryId = v.id
        when(v.id){
            R.id.tv_coffee_shop -> categoryQuery = getString(R.string.coffee_shop)
            R.id.tv_dessert -> categoryQuery = getString(R.string.dessert)
            R.id.tv_cartoon -> categoryQuery = getString(R.string.cartoon_cafe)
            R.id.tv_board -> categoryQuery = getString(R.string.board_cafe)
            R.id.tv_study -> categoryQuery = getString(R.string.study_cafe)
            R.id.tv_kids -> categoryQuery = getString(R.string.kids_cafe)
            R.id.tv_book -> categoryQuery = getString(R.string.book_cafe)
            R.id.tv_live -> categoryQuery = getString(R.string.live_cafe)
            R.id.tv_gallery -> categoryQuery = getString(R.string.galley_cafe)
            R.id.tv_fruits -> categoryQuery = getString(R.string.fruits_shop)
            R.id.tv_meeting -> categoryQuery = getString(R.string.meeting_space)
            R.id.tv_dog -> categoryQuery = getString(R.string.dogs_cafe)
            R.id.tv_cat -> categoryQuery = getString(R.string.cats_cafe)
        }

        // "검색 단어 + categoryQuery"로 키워드 검색 요청

        binding.edtSearchHome.clearFocus()


        choiceCategoryId = v.id
    }

    fun setHashtagList(){
        // DB에서 가져와서 RecyclerView 설정

        val items = resources.getStringArray(R.array.cafe_tags)
        binding.recyclerviewHomeHashtag.adapter = HomeHashtagAdapter(requireContext(), items.toList())
    }
}