package com.redhorse.accountbank

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class DayDetailFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn1 = view.findViewById<Button>(R.id.btn_button1)
        val btn2 = view.findViewById<Button>(R.id.btn_button2)
        val btnEditText = view.findViewById<Button>(R.id.btn_buttonEditText)
        val editText = view.findViewById<EditText>(R.id.et_editText)

        btn1.setOnClickListener{
            Toast.makeText(context, "This is button 1", Toast.LENGTH_LONG).show()
            dismiss()
        }

        btn2.setOnClickListener{
            Toast.makeText(context, "This is button 2", Toast.LENGTH_LONG).show()
            dismiss()
        }

        btnEditText.setOnClickListener{
            val value = editText.text.toString()

            if(value.isEmpty()){
                Toast.makeText(context, "비었다", Toast.LENGTH_LONG).show()
            }
            Toast.makeText(context, value, Toast.LENGTH_LONG).show()
            dismiss()
        }
    }
}