package com.redhorse.accountbank.item

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.redhorse.accountbank.R

class CustomCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val container: LinearLayout
    val Container: LinearLayout
        get() = container

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card_view, this, true)
        container = findViewById(R.id.card_container)
// 배경을 투명하게 설정
        setCardBackgroundColor(context.getColor(android.R.color.transparent))
        // CardView의 모서리를 둥글게 설정
        radius = 20f

        // CardView의 그림자 효과 제거 (필요한 경우 조정)
        elevation = 0f

        // backgroundTintMode 설정 (필요한 경우)
        backgroundTintMode = null
    }

    fun addTitle(content: String) {
        val titleView = TextView(context).apply {
            text = content
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
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
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        container.addView(subtitleView)
    }

    fun addSubtitle(leftContent: String, rightContent: String) {
        val subtitleLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        val leftTextView = TextView(context).apply {
            text = leftContent
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { weight = 1f }
        }

        val rightTextView = TextView(context).apply {
            text = rightContent
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.END }
        }

        // 서브타이틀 레이아웃에 텍스트뷰 추가
        subtitleLayout.addView(leftTextView)
        subtitleLayout.addView(rightTextView)

        // 컨테이너에 서브타이틀 레이아웃 추가
        container.addView(subtitleLayout)
    }

    fun addDescription(content: String, color: Int = ContextCompat.getColor(context, R.color.card_text)) {
        val subtitleView = TextView(context).apply {
            text = content
            textSize = 12f
            setTextColor(color)
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
                    80, 80
                ).apply {
                    marginEnd = 16 // 이미지와 버튼 사이 간격
                }

                setOnClickListener { onClickAction() }
            }
            rowLayout.addView(imageView)

            // 새로 만든 행 레이아웃을 컨테이너에 추가
            container.addView(rowLayout, 0) // 기존 순서를 유지하려면 적절히 추가
        } else {
            throw IllegalStateException("addTitle을 먼저 호출해야 합니다.")
        }
    }

    fun addTextWithToggle(
        text: String,
        toggleInitialState: Boolean,
        onToggleValueChange: (Boolean) -> Unit
    ) {
        addRowWithTextAndAction(text) { rowLayout ->
            val toggleSwitch = SwitchCompat(context).apply {
                isChecked = toggleInitialState
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 16 }

                // 배경 색상 설정
                trackTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked), // 꺼진 상태
                        intArrayOf(android.R.attr.state_checked)   // 켜진 상태
                    ),
                    intArrayOf(
                        Color.parseColor("#B5A89A"), // 꺼진 상태 색상
                        Color.parseColor("#D4A24D")  // 켜진 상태 색상
                    )
                )

                // 토글 버튼 색상 설정
                thumbTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked), // 꺼진 상태
                        intArrayOf(android.R.attr.state_checked)   // 켜진 상태
                    ),
                    intArrayOf(
                        Color.parseColor("#8A7563"), // 꺼진 상태 색상
                        Color.parseColor("#C8923D")  // 켜진 상태 색상
                    )
                )

                setOnCheckedChangeListener { _, isChecked ->
                    onToggleValueChange(isChecked)
                }
            }
            rowLayout.addView(toggleSwitch)
        }
    }

    // Text와 Button 추가
    fun addTextWithButton(
        text: String,
        imageResId: Int,
        onClickAction: () -> Unit
    ) {
        addRowWithTextAndAction(text) { rowLayout ->
            val imageView = ImageView(context).apply {
                setImageResource(imageResId)
                layoutParams = LinearLayout.LayoutParams(
                    80, 80
                ).apply { marginEnd = 16 }
                setOnClickListener {
                    onClickAction()
                }
            }
            rowLayout.addView(imageView)
        }
    }

    // 텍스트와 동작 뷰를 포함하는 행을 추가
    private fun addRowWithTextAndAction(
        text: String,
        actionViewAdder: (LinearLayout) -> Unit
    ) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        // 텍스트 추가
        val textView = TextView(context).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // 남은 공간 채우기
            )
        }
        rowLayout.addView(textView)

        // 동작 뷰 추가
        actionViewAdder(rowLayout)

        // 새로 만든 행 레이아웃을 컨테이너에 추가 (마지막에 추가)
        container.addView(rowLayout)
    }

    fun clear() {
        container.removeAllViews()  // 컨테이너에 있는 모든 뷰 제거
    }
}
