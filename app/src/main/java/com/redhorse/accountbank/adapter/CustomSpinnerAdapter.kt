package com.redhorse.accountbank.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.redhorse.accountbank.R  // 패키지 경로에 맞게 수정

class CustomSpinnerAdapter(
    private val context: Context,
    private val dataList: List<String>
) : BaseAdapter() {

    override fun getCount(): Int = dataList.size

    override fun getItem(position: Int): Any = dataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    // 항목 뷰에서 텍스트 색상 설정
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = dataList[position]
        textView.setTextColor(context.resources.getColor(android.R.color.black)) // 원하는 색으로 설정
        view.setBackgroundColor(context.resources.getColor(android.R.color.white)) // 배경색을 흰색으로 설정
        return view
    }

    // 드롭다운 뷰에서 텍스트 색상 설정
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = dataList[position]
        textView.setTextColor(context.resources.getColor(android.R.color.black)) // 원하는 색으로 설정
        view.setBackgroundColor(context.resources.getColor(android.R.color.white)) // 배경색을 흰색으로 설정
        return view
    }
}
