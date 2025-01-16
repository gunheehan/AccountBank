package com.redhorse.accountbank.modal

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.adapter.PaymentAdapter
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.PaymentDTO
import java.time.LocalDate

class DayDetailFragment : DialogFragment() {

    private lateinit var onEditDataCallback: () -> Unit
    private lateinit var onDetailUpdatedCallback: ((String) -> Unit)

    private var dayData: DayData? = null

    companion object {
        fun newInstance(date: String, paymentDTOs: List<PaymentDTO>): DayDetailFragment {
            val fragment = DayDetailFragment()
            val bundle = Bundle()
            bundle.putString("date", date)
            bundle.putParcelableArrayList("payments", ArrayList(paymentDTOs)) // PaymentDTO 리스트 전달
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun SetOnEditDataCallback(callback: () -> Unit) {
        this.onEditDataCallback = callback
    }

    fun setOnDetailUpdatedCallback(callback: (String) -> Unit) {
        this.onDetailUpdatedCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_day_detail, container, false)

        arguments?.let {
            val date = it.getString("date")
            val payments: List<PaymentDTO>? = it.getParcelableArrayList("payments")
            dayData = DayData(
                date = LocalDate.parse(date),
                payments = payments?.map { paymentDTO ->
                    Payment(
                        id = paymentDTO.id,
                        title = paymentDTO.title,
                        type = paymentDTO.type,
                        subtype = paymentDTO.subtype,
                        amount = paymentDTO.amount,
                        date = paymentDTO.date
                    )
                }?.toMutableList() ?: mutableListOf()
            )
        }

        setupUI(rootView)

        rootView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dismiss()
        }

        return rootView
    }

    private fun setupUI(rootView: View) {
        dayData?.let {
            rootView.findViewById<TextView>(R.id.dayText).text = it.date.toString()

            val recyclerView = rootView.findViewById<RecyclerView>(R.id.paymentsRecyclerViews)
            recyclerView.layoutManager = LinearLayoutManager(context)

            recyclerView.adapter = PaymentAdapter(
                payments = it.payments.toMutableList(),
                onItemClick = { payment -> showEditPayment(payment) },
                OnDeleteClick = { dayString -> UpdatePayment(dayString) },
                fragmentManager = parentFragmentManager
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.65).toInt()
            dialog.window?.setLayout(width, height)
            dialog.window?.setGravity(Gravity.CENTER)
        }
    }

    private fun showEditPayment(payment: Payment) {
        dismiss()

        val editPaymentDialog = PaymentEditFragment.newInstance(payment)
        editPaymentDialog.setOnSaveCallback(onEditDataCallback)
        editPaymentDialog.setOnDetailUpdatedCallback{ updatedDate ->
            UpdatePayment(updatedDate)
        }

        editPaymentDialog.show(parentFragmentManager, "PaymentEditFragment")
    }

    private fun UpdatePayment(dayString: String)
    {
        dismiss()
        onDetailUpdatedCallback!!.invoke(dayString)
    }
}
