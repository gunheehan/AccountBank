package com.redhorse.accountbank.modal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.redhorse.accountbank.R

class SimpleDialogFragment : DialogFragment() {

    private var title: String? = null
    private var onYesClickListener: (() -> Unit)? = null
    private var onNoClickListener: (() -> Unit)? = null

    // 새로운 인스턴스를 만들 때 제목과 버튼 클릭 리스너를 받아옴
    companion object {
        fun newInstance(title: String, onYesClick: () -> Unit, onNoClick: () -> Unit): SimpleDialogFragment {
            val fragment = SimpleDialogFragment()
            fragment.title = title
            fragment.onYesClickListener = onYesClick
            fragment.onNoClickListener = onNoClick
            return fragment
        }
    }

    // Dialog의 레이아웃과 버튼 클릭 리스너를 설정
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.modal_simple, container, false)
        val titleTextView: TextView = binding.findViewById(R.id.modal_simple_title)
        val yesButton: Button = binding.findViewById(R.id.modal_simple_yes_button)
        val noButton: Button = binding.findViewById(R.id.modal_simple_no_button)

        titleTextView.text = title

        yesButton.setOnClickListener {
            onYesClickListener?.invoke()
            dismiss()
        }

        noButton.setOnClickListener {
            onNoClickListener?.invoke()
            dismiss()
        }

        return binding
    }
}
