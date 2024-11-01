package com.redhorse.accountbank

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainboardFragment : Fragment(R.layout.fragment_mainboard) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mainboard, container, false)

        // CustomCardView 초기화
        val cardDay = view.findViewById<CustomCardView>(R.id.card_day)
        val cardEarnings = view.findViewById<CustomCardView>(R.id.card_earnings)
        val cardRemain = view.findViewById<CustomCardView>(R.id.card_remain)

        // 카드에 내용 추가
        cardDay.addTitle("오늘은 2025년 10월 28일 입니다.")
        cardDay.addSubtitle("월급날 D-27")

        cardEarnings.addTitle("수입 : 3,000,000 원")
        cardEarnings.addTitle("지출 : 950,000 원")
        cardEarnings.addTitle("적금 : 2,000,000 원")

        cardRemain.addTitle("소비 가능 금액 : 950,000 원")
        cardRemain.addSubtitle("아직 여유가 있군요 :)")

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}