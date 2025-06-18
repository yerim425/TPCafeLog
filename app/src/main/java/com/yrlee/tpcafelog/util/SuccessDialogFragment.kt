package com.yrlee.tpcafelog.util

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.DialogSuccessBinding
import com.yrlee.tpcafelog.ui.visit.VisitCertifyActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SuccessDialogFragment(
    val msg: String,
    val cafeName: String,
    val currentPoint: Int,
    val gainedPoint: Int,
    private val onConfirm: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var binding: DialogSuccessBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSuccessBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(false)

        binding.tvCafeName.text = cafeName
        binding.tvMsg.text = msg
        binding.tvCurrentPoint.text = "${currentPoint}P"
        binding.tvGainedPoint.text = "(+0)"

        animatePointGain()

        binding.btnOk.setOnClickListener {
            onConfirm?.invoke() // 확인 시 콜백 실행
            dismiss()
        }

        return builder.create()
    }

    private fun animatePointGain() {
        lifecycleScope.launch {
            var currentPoint = 0
            while (currentPoint < gainedPoint) {
                currentPoint++
                binding.tvGainedPoint.text = "(+$currentPoint)"
                delay(200L) // 점수 올라가는 간격
            }
        }
    }
}