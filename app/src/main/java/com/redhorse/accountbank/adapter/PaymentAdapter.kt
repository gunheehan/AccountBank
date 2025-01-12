package com.redhorse.accountbank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.modal.SimpleDialogFragment
import com.redhorse.accountbank.utils.PaymentProcessor
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.*

class PaymentAdapter(
    private val payments: MutableList<Payment>,
    private val onItemClick: (Payment) -> Unit,
    private val OnDeleteClick: (String) -> Unit,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>(), CoroutineScope {

    // CoroutineScope를 위한 Job 생성
    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job // Main 스레드에서 UI 업데이트

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val subtypeText: TextView = itemView.findViewById(R.id.subtypeText)
        val amountText: TextView = itemView.findViewById(R.id.amountText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(payment: Payment) {
            titleText.text = payment.title
            typeText.text = getPaymentType(payment.type)
            subtypeText.text = getPaymentSubType(payment.subtype)
            amountText.text = "${formatCurrency(payment.amount)}" // 포맷된 금액 표시

            itemView.setOnClickListener {
                onItemClick(payment)
            }

            // 삭제 버튼 클릭 리스너
            deleteButton.setOnClickListener {
                val dialog = SimpleDialogFragment.newInstance(
                    "삭제하시겠습니까?",
                    onYesClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                val position = adapterPosition
                                if (position != RecyclerView.NO_POSITION) {
                                    payments.removeAt(position)
                                    notifyItemRemoved(position)
                                    PaymentProcessor.deletePaymentFromDB(itemView.context, payment.date, payment.id)
                                }
                                OnDeleteClick(payment.date)
                            }
                        }
                    },
                    onNoClick = { null }
                )
                dialog.show(fragmentManager, "SimpleDialogFragment") // FragmentManager 사용
            }
        }

        private fun getPaymentType(type: String): String {
            val typename = when(type) {
                "income" -> "수입"
                "expense" -> "지출"
                "save" -> "적금"
                else -> "기타"
            }

            return typename
        }

        private fun getPaymentSubType(type: Int): String{
            val typename = when(type){
                0 -> "식비"
                1 -> "간식"
                2 -> "여가"
                3 -> "교통"
                4 -> "기타"
                else -> "기타"
            }

            return typename
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
