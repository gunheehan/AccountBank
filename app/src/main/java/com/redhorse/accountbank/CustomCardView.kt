package com.redhorse.accountbank

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
        setCardBackgroundColor(context.getColor(android.R.color.transparent)) // 투명 배경
        radius = 20f // 라운드 코너 설정
        elevation = 8f // 그림자 효과
    }

    fun addTitle(content: String) {
        val titleView = TextView(context).apply {
            text = content
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 } // 아래쪽 마진
        }
        container.addView(titleView)
    }

    fun addSubtitle(content: String) {
        val subtitleView = TextView(context).apply {
            text = content
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        container.addView(subtitleView)
    }

    fun addInput(hint: String) {
        val inputView = EditText(context).apply {
            this.hint = hint
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        container.addView(inputView)
    }

    fun addImage(imageResId: Int) {
        val imageView = ImageView(context).apply {
            setImageResource(imageResId)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        container.addView(imageView)
    }
}
