package com.redhorse.accountbank

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // 가로 스크롤을 감지하여 ViewPager 이벤트 차단
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                // 부모(ViewPager)에게 이벤트 전달 중단
                parent?.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.onInterceptTouchEvent(e)
    }
}
