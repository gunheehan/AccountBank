package com.redhorse.accountbank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.utils.formatCurrency

class CalendarAdapter(private var days: List<DayData>) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val incomeText: TextView = itemView.findViewById(R.id.incomeText)
        val expenseText: TextView = itemView.findViewById(R.id.expenseText)

        fun bind(dayData: DayData) {
            if (dayData.isEmpty) {
                dayText.text = ""
                incomeText.visibility = View.GONE
                expenseText.visibility = View.GONE
            } else {
                dayText.text = dayData.date.dayOfMonth.toString() // 날짜 표시

                // 수입과 지출 표시
                if (dayData.income > 0) {
                    incomeText.text = "${formatCurrency(dayData.income)}" // 포맷팅된 수입 표시
                    incomeText.visibility = View.VISIBLE
                } else {
                    incomeText.visibility = View.GONE
                }

                if (dayData.expense > 0) {
                    expenseText.text = "${formatCurrency(dayData.expense)}" // 포맷팅된 지출 표시
                    expenseText.visibility = View.VISIBLE
                } else {
                    expenseText.visibility = View.GONE
                }

                // 글자 크기 조정
                adjustTextSize(incomeText)
                adjustTextSize(expenseText)
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
        holder.bind(days[position]) // 해당 위치의 데이터를 바인딩
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
