package com.redhorse.accountbank.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomSpinnerAdapter(
    private val context: Context,
    private val dataList: List<String>
) : BaseAdapter() {

    override fun getCount(): Int = dataList.size

    override fun getItem(position: Int): Any = dataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = dataList[position]
        textView.setTextColor(Color.parseColor("#3C3C3C"))
        view.setBackgroundColor(Color.parseColor("#FFFFFF")) // 배경색을 흰색으로 설정
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = dataList[position]
        textView.setTextColor(Color.parseColor("#3C3C3C"))
        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
        return view
    }
}
