package com.redhorse.accountbank

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
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
            setTextColor(Color.BLACK)
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

    fun addDescription(content: String, color: Int = Color.BLACK) {
        val subtitleView = TextView(context).apply {
            text = content
            textSize = 15f
            setTextColor(color)
            gravity = Gravity.END // 오른쪽 정렬
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

    fun addImageAndButton(
        imageResId: Int,
        buttonText: String,
        onClickAction: () -> Unit
    ) {
        // 부모 컨테이너에서 마지막으로 추가된 View를 확인
        val lastChild = container.getChildAt(container.childCount - 1)

        if (lastChild is TextView) {
            // 타이틀이 TextView일 경우, 해당 View를 포함하는 LinearLayout 생성
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL // 세로 중앙 정렬
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            }

            // 기존 타이틀 제거
            container.removeView(lastChild)

            // 타이틀을 새로운 레이아웃에 추가
            rowLayout.addView(lastChild.apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // 남은 공간 채우기
                )
            })

            // 이미지 추가
            val imageView = ImageView(context).apply {
                setImageResource(imageResId)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16 // 이미지와 버튼 사이 간격
                }
            }
            rowLayout.addView(imageView)

            // 버튼 추가
            val button = TextView(context).apply {
                if(buttonText.isEmpty()){
                    width = 60
                    height = 60
                }
                else{
                    text = buttonText
                }
                textSize = 14f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.BLUE) // 버튼 배경색
                gravity = Gravity.CENTER
                setPadding(16, 8, 16, 8) // 버튼 패딩
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { onClickAction() }
            }
            rowLayout.addView(button)

            // 새로 만든 행 레이아웃을 컨테이너에 추가
            container.addView(rowLayout, 0) // 기존 순서를 유지하려면 적절히 추가
        } else {
            throw IllegalStateException("addTitle을 먼저 호출해야 합니다.")
        }
    }
}
