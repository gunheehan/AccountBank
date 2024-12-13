package com.redhorse.accountbank

import PaymentRepository
import RegularlyInfoItem
import SavePaymentRepository
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import com.redhorse.accountbank.item.CustomCardView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)
        savepaymentRepository = SavePaymentRepository(dbHelper)

        val view = inflater.inflate(R.layout.fragment_fixed_information, container, false)
        val info_title = view.findViewById<CustomCardView>(R.id.fixed_info_title)
        info_title.addTitle("매달 입/출금 되는 정보를 입력해두면 간편하게 사용할 수 있어요!")
        info_title.addDescription("입력한 데이터는 매달 1일 자동으로 입력이 됩니다.", Color.DKGRAY)

        SetExpensesCard(view)
        return view
    }

    private fun SetExpensesCard(view: View){
        val info_expenses = view.findViewById<CustomCardView>(R.id.fixed_info_expenses)
        info_expenses.addTitle("정기 지출 금액")
        info_expenses.addImageAndButton(imageResId = R.drawable.rounded_button,
            buttonText = "",
            onClickAction = {
                Toast.makeText(context, "버튼이 클릭되었습니다!", Toast.LENGTH_SHORT).show()
            })

        val regularlyInfoItem = RegularlyInfoItem(requireContext())
        regularlyInfoItem.setData("넷플릭스 구독", "₩9,500", "매월 25일 결제")

// 예를 들어 CustomCardView에 추가
        info_expenses.Container.addView(regularlyInfoItem)
    }

    private fun OnClickOpenModal()
    {

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