package com.redhorse.accountbank.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.utils.PaymentProcessor
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PaymentAdapter(
    private val payments: MutableList<Payment>,
    private val onItemClick: (Payment) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>(), CoroutineScope {

    // CoroutineScope를 위한 Job 생성
    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job // Main 스레드에서 UI 업데이트

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val amountText: TextView = itemView.findViewById(R.id.amountText)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(payment: Payment) {
            titleText.text = payment.title
            typeText.text = payment.type
            amountText.text = "${formatCurrency(payment.amount)}" // 포맷된 금액 표시

            itemView.setOnClickListener {
                onItemClick(payment)
            }

            // 삭제 버튼 클릭 리스너
            deleteButton.setOnClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                builder.setMessage("삭제하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("예") { dialog, id ->
                        launch {
                            onDeleteClick(payment)
                        }
                    }
                    .setNegativeButton("아니오") { dialog, id ->
                        dialog.dismiss()
                    }
                builder.create().show()
            }
        }

        private suspend fun onDeleteClick(payment: Payment) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                payments.removeAt(position)
                notifyItemRemoved(position)
                PaymentProcessor.deletePaymentFromDB(itemView.context, payment.id)
            }
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

    fun clear() {
        job.cancel()
    }
}
