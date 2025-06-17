package com.yrlee.tpcafelog.ui.review

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.DialogSelectVisitedCafeBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.VisitCafeResponseItem
import com.yrlee.tpcafelog.util.PrefUtils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VisitedCafeSelectDialogFragment : DialogFragment() {
    private val TAG = "visit cafe select dialog fragment"

    private var cafeList: List<VisitCafeResponseItem>? = null
    lateinit var binding: DialogSelectVisitedCafeBinding
    private val userId = PrefUtils.getInt("user_id")

    interface OnCafeSelectedListener {
        fun onCafeSelected(visitCafeInfo: VisitCafeResponseItem)
    }

    private var listener: OnCafeSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnCafeSelectedListener ?: activity as? OnCafeSelectedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSelectVisitedCafeBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = AlertDialog.Builder(requireContext()).setView(binding.root)

        requestVisitedCafeList()
        return builder.create()
    }

    private fun requestVisitedCafeList() {
        if (userId == -1) {
            Log.d(TAG, "userId is -1")
            return
        }
        val call = RetrofitHelper.getMyService().getVisitedCafeList(user_id = userId)
        call.enqueue(object : Callback<MyResponse<List<VisitCafeResponseItem>>> {
            override fun onResponse(
                call: Call<MyResponse<List<VisitCafeResponseItem>>>,
                response: Response<MyResponse<List<VisitCafeResponseItem>>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    when(body?.status){
                        200 -> { // 방문 인증한 카페 있음
                            body.data?.let {
                                cafeList = it
                                binding.recyclerview.adapter = VisitedCafeAdapter(requireContext(), it) {
                                    listener?.onCafeSelected(it)
                                    dismiss()
                                }
                            }
                        }
                        400 -> { // 방문 인증한 카페 없음
                            dismiss()
                            AlertDialog.Builder(requireContext())
                                .setTitle(getString(R.string.message_no_visited_cafe))
                                .setMessage(getString(R.string.message_certify_visit_cafe_for_create_review))
                                .create().show()
                        }
                    }
                } else {
                    Log.d(TAG, response.errorBody()?.string() ?: "errorBody is null")
                }
            }

            override fun onFailure(call: Call<MyResponse<List<VisitCafeResponseItem>>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }
        })
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }
}