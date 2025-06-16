package com.yrlee.tpcafelog.ui.review

import android.app.Dialog
import android.content.Context
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
import com.yrlee.tpcafelog.databinding.DialogSelectVisitedCafeBinding
import com.yrlee.tpcafelog.model.CafeItem
import com.yrlee.tpcafelog.model.VisitCafeInfoItem
import com.yrlee.tpcafelog.ui.visit.CafeNameAdapter
import com.yrlee.tpcafelog.ui.visit.VisitCafeSearchDialogFragment.OnCafeSelectedListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VisitedCafeSelectDialogFragment(
    private val itemList: List<VisitCafeInfoItem>
) : DialogFragment() {

    interface OnCafeSelectedListener {
        fun onCafeSelected(visitInfo: VisitCafeInfoItem)
    }
    private var listener: VisitedCafeSelectDialogFragment.OnCafeSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnCafeSelectedListener ?: activity as? OnCafeSelectedListener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSelectVisitedCafeBinding.inflate(layoutInflater) // ViewBinding 사용 시

        val adapter = VisitedCafeAdapter(requireContext(), itemList) {
            listener?.onCafeSelected(it)
            dismiss()
        }
        binding.recyclerview.adapter = adapter

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }
}