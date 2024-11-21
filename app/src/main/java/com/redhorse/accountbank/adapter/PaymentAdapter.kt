package com.redhorse.accountbank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.utils.formatCurrency

class PaymentAdapter(private val payments: List<Payment>) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val amountText: TextView = itemView.findViewById(R.id.amountText)

        fun bind(payment: Payment) {
            titleText.text = payment.title
            typeText.text = payment.type
            amountText.text = "${formatCurrency(payment.amount)}" // 포맷된 금액 표시
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_item, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        holder.bind(payment)
    }

    override fun getItemCount(): Int = payments.size
}
