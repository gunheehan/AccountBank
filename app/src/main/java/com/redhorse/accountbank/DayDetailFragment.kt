import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.PaymentEditFragment
import com.redhorse.accountbank.R
import com.redhorse.accountbank.adapter.PaymentAdapter
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.PaymentDTO
import java.time.LocalDate

class DayDetailFragment : DialogFragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment의 레이아웃을 인플레이트합니다.
        val rootView = inflater.inflate(R.layout.fragment_day_detail, container, false)

        // arguments에서 데이터를 가져와서 dayData에 설정
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
                        amount = paymentDTO.amount,
                        date = paymentDTO.date
                    )
                }?.toMutableList() ?: mutableListOf()
            )
        }

        // UI에 데이터를 설정하는 로직
        setupUI(rootView)

        // Close 버튼 설정
        rootView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dismiss()  // Fragment를 닫음
        }

        return rootView
    }

    private fun setupUI(rootView: View) {
        dayData?.let {
            // 날짜 표시
            rootView.findViewById<TextView>(R.id.dayText).text = it.date.toString()

            // 결제 목록 표시
            val recyclerView = rootView.findViewById<RecyclerView>(R.id.paymentsRecyclerViews)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = PaymentAdapter(it.payments, onItemClick = {
                payment -> showEditPayment(payment)
            }) // PaymentDTO를 어댑터로 전달
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()  // 화면 너비의 85% 크기
            val height = (resources.displayMetrics.heightPixels * 0.65).toInt()  // 화면 높이의 65% 크기
            dialog.window?.setLayout(width, height)  // 모달 크기 설정
            dialog.window?.setGravity(Gravity.CENTER)  // 중앙 배치
        }
    }

    private fun showEditPayment(payment: Payment) {
        dismiss()
        Log.e("PaymentAdapter", payment.title)

        // 수정 모달 열기
        val editPaymentDialog = PaymentEditFragment.newInstance(payment)

        // 수정 모달 표시
        editPaymentDialog.show(parentFragmentManager, "PaymentEditFragment")
    }

}
