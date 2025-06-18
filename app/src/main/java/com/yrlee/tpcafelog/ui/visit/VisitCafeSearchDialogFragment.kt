package com.yrlee.tpcafelog.ui.visit

import android.app.Dialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.DialogSearchCafeBinding
import com.yrlee.tpcafelog.model.CafeItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VisitCafeSearchDialogFragment(val myLocation: Location) : DialogFragment() {
    private val TAG = "visit cafe search dialog fragment"

    private var cafeSearchJob: Job? = null
    private var page = 1
    private var totalCnt = 0
    private lateinit var adapter: CafeNameAdapter
    private var query = ""
    private var isLoading = false

    lateinit var binding:DialogSearchCafeBinding

    interface OnCafeSelectedListener {
        fun onCafeSelected(cafeInfo: CafeItem)
    }

    private var listener: OnCafeSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnCafeSelectedListener ?: activity as? OnCafeSelectedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSearchCafeBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = AlertDialog.Builder(requireContext()).setView(binding.root)

        adapter = CafeNameAdapter(requireContext()) {
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
                    lifecycleScope.launch {
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
                val address = if(it.address_name.isEmpty()) it.road_address_name else it.address_name
                CafeItem(it.id, it.place_name, address, it.category_name)
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