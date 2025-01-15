package com.redhorse.accountbank.modal

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.AppInfo
import com.redhorse.accountbank.data.helper.AppinfoHelper
import com.redhorse.accountbank.utils.formatCurrency

class MainInfoModal : DialogFragment(){
    private lateinit var appinfoHelper: AppinfoHelper
    private lateinit var saveCallback: (() -> Unit)
    private lateinit var D_Day_title: EditText
    private lateinit var D_Day_day: EditText
    private lateinit var targetAmout: EditText

    companion object {
        fun newInstance(infohelper: AppinfoHelper, callback: () -> Unit): MainInfoModal {
            val fragment = MainInfoModal()
            fragment.appinfoHelper = infohelper
            fragment.saveCallback = callback
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.modal_maininfo, container, false)

        D_Day_title = rootView.findViewById(R.id.modal_maininfo_DDay_title)
        D_Day_day = rootView.findViewById(R.id.modal_maininfo_DDay_day)
        targetAmout = rootView.findViewById(R.id.modal_maininfo_targetAmount)

        val saveButton = rootView.findViewById<AppCompatButton>(R.id.modal_maininfo_saveButton)
        val cloaseButton = rootView.findViewById<AppCompatButton>(R.id.modal_maininfo_closeButton)

        saveButton.setOnClickListener(){
            onClickSave()
        }
        cloaseButton.setOnClickListener(){
            onClickClose()
        }

        SetData()
        return rootView
    }

    private fun SetData()
    {
        val title = appinfoHelper.getString(AppInfo.STRING_D_DAY_TITLE, "")
        if(!title.isEmpty()) {
            D_Day_title.setText(title)
        }
        val day = appinfoHelper.getInt(AppInfo.INT_D_DAY_DAY,0)
        if(day != 0){
            D_Day_day.setText(day.toString())
        }
        val amount = appinfoHelper.getInt(AppInfo.INT_EXPENSE_TARGET,0)
        if(amount != 0){
            targetAmout.setText(formatCurrency(amount))
        }
    }

    private fun onClickSave() {

        if (!D_Day_title.text.isEmpty() && !D_Day_day.text.isEmpty()) {
            appinfoHelper.save(AppInfo.STRING_D_DAY_TITLE, D_Day_title.text.toString())
            val daystring = D_Day_day.text.toString()

            try {
                val dayint = daystring.toInt()
                appinfoHelper.save(AppInfo.INT_D_DAY_DAY, dayint)
            } catch (e: NumberFormatException) {
                Log.e("onClickSave", "Invalid number format for D-Day day: $daystring", e) // 숫자 포맷 예외 처리
            }
        }

        if (!targetAmout.text.isEmpty()) {

            val amountstring = targetAmout.text.toString()
            try {
                val amountint = amountstring.toInt()
                appinfoHelper.save(AppInfo.INT_EXPENSE_TARGET, amountint)
            } catch (e: NumberFormatException) {
                Log.e("onClickSave", "Invalid number format for target amount: $amountstring", e) // 숫자 포맷 예외 처리
            }
        }

        saveCallback?.invoke()

        dismiss()
    }


    private fun onClickClose()
    {
        dismiss()
    }
}