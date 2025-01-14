package com.redhorse.accountbank.item

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
        setCardBackgroundColor(context.getColor(android.R.color.transparent))
        radius = 20f
        elevation = 0f
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
            ).apply { bottomMargin = 16 }
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

        subtitleLayout.addView(leftTextView)
        subtitleLayout.addView(rightTextView)

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

    fun addImageAndButton(
        imageResId: Int,
        onClickAction: () -> Unit) {
        val lastChild = container.getChildAt(container.childCount - 1)

        if (lastChild is TextView) {
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

            container.removeView(lastChild)

            rowLayout.addView(lastChild.apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })

            val imageView = ImageView(context).apply {
                setImageResource(imageResId)
                layoutParams = LinearLayout.LayoutParams(
                    80, 80
                ).apply {
                    marginEnd = 16
                }

                setOnClickListener { onClickAction() }
            }
            rowLayout.addView(imageView)

            container.addView(rowLayout, 0)
        } else {
            throw IllegalStateException("")
        }
    }

    fun addTextWithToggle(
        text: String,
        toggleInitialState: Boolean,
        onToggleValueChange: (Boolean) -> Unit) {
        addRowWithTextAndAction(text) { rowLayout ->
            val toggleSwitch = SwitchCompat(context).apply {
                isChecked = toggleInitialState
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 16 }

                trackTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf(android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        Color.parseColor("#B5A89A"),
                        Color.parseColor("#D4A24D")
                    )
                )

                // 토글 버튼 색상 설정
                thumbTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf(android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        Color.parseColor("#8A7563"),
                        Color.parseColor("#C8923D")
                    )
                )

                setOnCheckedChangeListener { _, isChecked ->
                    onToggleValueChange(isChecked)
                }
            }
            rowLayout.addView(toggleSwitch)
        }
    }

    fun addTextWithButton(
        text: String,
        imageResId: Int,
        onClickAction: () -> Unit) {
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

    private fun addRowWithTextAndAction(
        text: String,
        actionViewAdder: (LinearLayout) -> Unit) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        val textView = TextView(context).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        rowLayout.addView(textView)
        actionViewAdder(rowLayout)

        container.addView(rowLayout)
    }

    fun clear() {
        container.removeAllViews()
    }
}
