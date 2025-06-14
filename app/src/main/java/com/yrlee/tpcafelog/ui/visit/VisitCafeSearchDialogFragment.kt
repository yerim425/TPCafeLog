package com.yrlee.tpcafelog.ui.visit

import android.app.Dialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.DialogSearchCafeBinding
import com.yrlee.tpcafelog.model.CafeName
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VisitCafeSearchDialogFragment(val myLocation: Location) : DialogFragment() {
    private val TAG = "visit cafe search dialog fragment"

    private var cafeSearchJob: Job? = null
    private var page = 1
    private var totalCnt = 0
    private lateinit var adapter: VisitCafeNameAdapter
    private var query = ""
    private var isLoading = false

    val binding by lazy { DialogSearchCafeBinding.inflate(LayoutInflater.from(context)) }

    interface OnCafeSelectedListener {
        fun onCafeSelected(cafeName: CafeName)
    }

    private var listener: OnCafeSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnCafeSelectedListener ?: activity as? OnCafeSelectedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext()).setView(binding.root)

        adapter = VisitCafeNameAdapter(requireContext()) {
            listener?.onCafeSelected(it)
            dismiss()
        }

        binding.recyclerviewCafeName.adapter = adapter

        binding.edtSearchCafe.addTextChangedListener { text ->
            query = text.toString().trim()
            if (query.length >= 2) {
                cafeSearchJob?.cancel()
                page = 1
                cafeSearchJob = lifecycleScope.launch {
                    delay(300L)
                    requestSearchCafes(query)
                }
            }
        }

        binding.recyclerviewCafeName.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && !isLoading && adapter.itemCount < totalCnt) {
                    isLoading = true
                    page++
                    viewLifecycleOwner.lifecycleScope.launch {
                        requestSearchCafes(query)
                    }
                }
            }
        })

        return builder.create()
    }

    private suspend fun requestSearchCafes(query: String) {
        try {
            Log.d(TAG, "${myLocation.latitude}")
            val response = RetrofitHelper.getKakaoService().getSearchCafeNames(
                query = query,
                page = page,
                longitude = myLocation.longitude.toString(),
                latitude = myLocation.latitude.toString(),
            )
            if (page == 1) {
                adapter.clearItems()
                totalCnt = response.meta.total_count
                if(totalCnt==0) binding.tvNoCafe.visibility = View.VISIBLE
                else binding.tvNoCafe.visibility = View.GONE
            }

            val cafeNames = response.documents.map {
                CafeName(it.place_name, it.road_address_name)
            }

            adapter.addItems(cafeNames)
        } catch (e: Exception) {
            Log.e(TAG, "카페 검색 실패: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }
}