package com.redhorse.accountbank

import PaymentRepository
import RegularlyInfoItem
import SavePaymentRepository
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import com.redhorse.accountbank.item.CustomCardView
import com.redhorse.accountbank.modal.PaymentEditFragment
import com.redhorse.accountbank.modal.RegularlyModal
import com.redhorse.accountbank.modal.SimpleDialogFragment
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FixedInformationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixedInformationFragment : Fragment() {
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var savepaymentRepository: SavePaymentRepository
    private lateinit var expensesCardView: CustomCardView
    private lateinit var incomeCardView: CustomCardView
    private lateinit var saveCardView: CustomCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FixedUI","Start UI")
        // Inflate the layout for this fragment
        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)
        savepaymentRepository = SavePaymentRepository(dbHelper)

        val rootView = inflater.inflate(R.layout.fragment_fixed_information, container, false)
        val info_title = rootView.findViewById<CustomCardView>(R.id.fixed_info_title)
        info_title.addTitle("매달 입/출금 되는 정보를 입력해두면 간편하게 사용할 수 있어요!")
        info_title.addDescription("입력한 데이터는 매달 1일 자동으로 입력이 됩니다.", Color.DKGRAY)
        Log.d("FixedUI","Start Loaded")

        expensesCardView = rootView.findViewById<CustomCardView>(R.id.fixed_info_expenses)
        SetExpensesCard(expensesCardView, "정기 지출 금액","expense")

        incomeCardView = rootView.findViewById<CustomCardView>(R.id.fixed_info_earning)
        SetExpensesCard(incomeCardView, "정기 수입 금액","income")

        saveCardView = rootView.findViewById<CustomCardView>(R.id.fixed_info_save)
        SetExpensesCard(saveCardView, "정기 적금 금액","save")

        return rootView
    }

    private fun SetExpensesCard(dataView: CustomCardView, title: String, type: String) {
        dataView.addTitle(title)
        dataView.addImageAndButton(imageResId = R.drawable.icon_plus,
            onClickAction = {
                OnClickOpenModal()
            })

        CoroutineScope(Dispatchers.IO).launch {
            // 비동기 작업을 IO 스레드에서 수행
            val paymentsList = savepaymentRepository.getPaymentsByType(type)
            Log.d("FixedUI","${type} Loaded")
            // UI 업데이트는 Main 스레드에서 수행해야 함
            withContext(Dispatchers.Main) {
                // paymentsList가 비어있지 않으면 UI 업데이트
                if (paymentsList.isNotEmpty()) {
                    for (payment in paymentsList) {
                        Log.d("FixedUI","${payment.title} Loaded")
                        val regularlyInfoItem = RegularlyInfoItem(requireContext())
                        regularlyInfoItem.setData(payment, onClickEdit = ::editRegularlyData,
                            onClickDelete = ::deleteRegularlyData)

                        // UI에서 안전하게 Container에 추가
                        dataView.Container.addView(regularlyInfoItem)
                    }
                }
            }
        }
    }


    private fun editRegularlyData(payment: Payment)
    {
        val regularlyModal = RegularlyModal.newInstance(payment) { payment ->
            UpdatePayment(payment)
        }
        // 수정 모달 표시
        regularlyModal.show(parentFragmentManager, "RegularlyModal")
    }

    private fun deleteRegularlyData(payment: Payment)
    {
        val dialog = SimpleDialogFragment.newInstance(
            "정말 삭제하시겠습니까?",
            onYesClick = { CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    savepaymentRepository.deletePaymentById(payment.date, payment.id)
                    when (payment.type) {
                        "expense" -> updateContainer(expensesCardView, payment.type)
                        "income" -> updateContainer(incomeCardView, payment.type)
                        "save" -> updateContainer(saveCardView, payment.type)
                    }
                }
            }  },
            onNoClick = { null }
        )
        dialog.show(parentFragmentManager, "SimpleDialogFragment")

    }

    private fun OnClickOpenModal() {
        val regularlyModal = RegularlyModal.newInstance(null) { payment ->
            // 이곳에서 Payment 객체를 처리
            InsertNewPayment(payment)
        }
        // 수정 모달 표시
        regularlyModal.show(childFragmentManager, "RegularlyModal")
    }

    private fun InsertNewPayment(payment: Payment) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                savepaymentRepository.insertOrCreateTableAndInsert(payment)
                payment.date = formatToFullDate(payment.date.toInt())
                paymentRepository.insertOrCreateTableAndInsert(payment)
                when (payment.type) {
                    "expense" -> updateContainer(expensesCardView, payment.type)
                    "income" -> updateContainer(incomeCardView, payment.type)
                    "save" -> updateContainer(saveCardView, payment.type)
                }
            }
        }
    }

    private fun UpdatePayment(payment: Payment){
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                // 정기 데이터 업데이트
                savepaymentRepository.updatePaymentById(payment.id, payment)

                // 신규 데이터 추가
                payment.date = formatToFullDate(payment.date.toInt())
                paymentRepository.insertOrCreateTableAndInsert(payment)
                when (payment.type) {
                    "expense" -> updateContainer(expensesCardView, payment.type)
                    "income" -> updateContainer(incomeCardView, payment.type)
                    "save" -> updateContainer(saveCardView, payment.type)
                }
            }
        }
    }

    private fun updateContainer(view: CustomCardView, type: String){
        view.Container.removeAllViews()
        val title = when (type) {
            "expense" -> "정기 지출 금액"
            "income" -> "정기 수입 금액"
            "save" -> "정기 적금 금액"
            else -> "정기 지출 금액"
        }

        SetExpensesCard(view, title,type)
    }

    fun formatToFullDate(day: Int): String {
        // 현재 날짜 가져오기
        val currentDate = LocalDate.now()

        // 현재 년, 월 가져오기
        val currentYearMonth = YearMonth.of(currentDate.year, currentDate.monthValue)

        // 입력된 날짜가 월에 유효한 날짜인지 확인
        if (day < 1 || day > currentYearMonth.lengthOfMonth()) {
            Toast.makeText(context, "유효하지 않은 날짜입니다. $day", Toast.LENGTH_SHORT).show()
            day == 1
        }

        // 완전한 날짜로 변환
        return "${currentDate.year}-${"%02d".format(currentDate.monthValue)}-${"%02d".format(day)}"
    }
}