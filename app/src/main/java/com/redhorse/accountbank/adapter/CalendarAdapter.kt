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
        val saveText: TextView = itemView.findViewById(R.id.saveText)

        fun bind(dayData: DayData) {
            if (dayData.date == LocalDate.MIN) {
                dayText.text = ""
                incomeText.visibility = View.GONE
                expenseText.visibility = View.GONE
                saveText.visibility = View.GONE
            } else {
                dayText.text = dayData.date.dayOfMonth.toString()

                incomeText.text = if (dayData.getTotalIncome() > 0) {
                    "${formatCurrency(dayData.getTotalIncome())}"
                } else {
                    ""
                }
                expenseText.text = if (dayData.getTotalExpense() > 0) {
                    "${formatCurrency(dayData.getTotalExpense())}"
                } else {
                    ""
                }
                saveText.text = if (dayData.getTotalSave() > 0) {
                    "${formatCurrency(dayData.getTotalSave())}"
                } else {
                    ""
                }

                incomeText.visibility = View.VISIBLE
                expenseText.visibility = View.VISIBLE
                saveText.visibility = View.VISIBLE

                // 글자 크기 조정
                adjustTextSize(incomeText)
                adjustTextSize(expenseText)
                adjustTextSize(saveText)

                // 클릭 리스너 설정
                itemView.setOnClickListener{
                    onItemClick(dayData)
                }
            }
        }

        private fun adjustTextSize(textView: TextView) {
            if (textView.visibility == View.VISIBLE && textView.text.length > 5) {
                textView.textSize = 8f
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
        return days.size
    }

    fun updateDays(newDays: List<DayData>) {
        days = newDays
        notifyDataSetChanged()
    }
}
