package com.redhorse.accountbank

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView

class CustomCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val container: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card_view, this, true)
        container = findViewById(R.id.card_container)
    }

    fun addContent(viewType: ViewType, content: String? = null, imageResId: Int? = null) {
        val view = when (viewType) {
            ViewType.TITLE -> TextView(context).apply {
                text = content
                textSize = 20f
                setTypeface(typeface, Typeface.BOLD)
            }
            ViewType.SUBTITLE -> TextView(context).apply {
                text = content
                textSize = 15f
            }
            ViewType.INPUT -> EditText(context).apply {
                hint = content
            }
            ViewType.IMAGE -> ImageView(context).apply {
                imageResId?.let { setImageResource(it) }
            }
        }

        // 공통으로 아래쪽 마진을 추가
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 16 // 아래쪽 마진을 16dp로 설정
        }
        view.layoutParams = layoutParams

        container.addView(view)
    }


    enum class ViewType {
        TITLE, SUBTITLE, INPUT, IMAGE
    }
}

