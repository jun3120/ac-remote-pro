package com.procool.remotecontrol.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class GestureHelper(
    private val onSwipeUp: (() -> Unit)? = null,
    private val onSwipeDown: (() -> Unit)? = null,
    private val onSwipeLeft: (() -> Unit)? = null,
    private val onSwipeRight: (() -> Unit)? = null
) : View.OnTouchListener {

    private val gestureDetector by lazy {
        GestureDetector(view?.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y

                if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                    if (dx > 100 && kotlin.math.abs(velocityX) > 100) {
                        onSwipeRight?.invoke()
                        return true
                    } else if (dx < -100 && kotlin.math.abs(velocityX) > 100) {
                        onSwipeLeft?.invoke()
                        return true
                    }
                } else {
                    if (dy > 100 && kotlin.math.abs(velocityY) > 100) {
                        onSwipeDown?.invoke()
                        return true
                    } else if (dy < -100 && kotlin.math.abs(velocityY) > 100) {
                        onSwipeUp?.invoke()
                        return true
                    }
                }
                return false
            }
        })
    }

    private var view: View? = null

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        this.view = v
        return gestureDetector.onTouchEvent(event ?: return false)
    }
}
