package com.redhorse.accountbank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.utils.formatCurrency
import java.time.LocalDate

class CalendarAdapter(private var days: List<DayData>, private val onItemClick:(DayData)->Unit) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val incomeText: TextView = itemView.findViewById(R.id.incomeText)
        val expenseText: TextView = itemView.findViewById(R.id.expenseText)

        fun bind(dayData: DayData) {
            if (dayData.date == LocalDate.MIN) {
                dayText.text = ""
                incomeText.visibility = View.GONE
                expenseText.visibility = View.GONE
            } else {
                // 날짜와 수입, 지출 표시
                dayText.text = dayData.date.dayOfMonth.toString()

                // 수입과 지출 텍스트를 0으로 초기화하고 필요한 경우에만 업데이트
                incomeText.text = if (dayData.getTotalIncome() > 0) {
                    "${formatCurrency(dayData.getTotalIncome())}" // 포맷팅된 수입 표시
                } else {
                    "0"
                }
                expenseText.text = if (dayData.getTotalExpense() > 0) {
                    "${formatCurrency(dayData.getTotalExpense())}" // 포맷팅된 지출 표시
                } else {
                    "0"
                }

                incomeText.visibility = View.VISIBLE
                expenseText.visibility = View.VISIBLE

                // 글자 크기 조정
                adjustTextSize(incomeText)
                adjustTextSize(expenseText)

                // 클릭 리스너 설정
                itemView.setOnClickListener{
                    onItemClick(dayData) // 클릭된 DayData 객체를 전달
                }
            }
        }

        private fun adjustTextSize(textView: TextView) {
            // 글자 크기를 줄여야 할 경우
            if (textView.visibility == View.VISIBLE && textView.text.length > 6) { // 예시: 6자 이상일 경우
                textView.textSize = 10f // 글자 크기 조정
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.day_item, parent, false) // dat_item.xml을 통해 아이템 레이아웃 설정
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val dayData = days[position]
        holder.bind(dayData)
    }

    override fun getItemCount(): Int {
        return days.size // 데이터 항목 수 반환
    }

    // 데이터 업데이트 메서드
    fun updateDays(newDays: List<DayData>) {
        days = newDays
        notifyDataSetChanged() // 데이터 변경 통지
    }
}
