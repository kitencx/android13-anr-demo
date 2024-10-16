
package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Transformation
import android.widget.FrameLayout

class IDCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleId: Int = 0
) : FrameLayout(context, attrs, defStyleId) {


    private val matrix = Matrix()

    init {
        setBackgroundColor(Color.TRANSPARENT)
        inflate(context, R.layout.view_idcard, this)
        rotationX = 15F
    }

    override fun getChildStaticTransformation(child: View, t: Transformation): Boolean {
        matrix.setPolyToPoly(
            floatArrayOf(0F, 0F, child.width.toFloat(), 0F, 0F, child.height.toFloat(), child.width.toFloat(), child.height.toFloat()),
            0,
            floatArrayOf(
                0F, 0F,
                child.width.toFloat(), -dp(14)
                , 3.5F, child.height.toFloat(),
                child.width.toFloat(), child.height.toFloat() - dp(14)
            ),
            0,
            4
        )
        matrix.postScale(0.95F, 0.95F)
        t.matrix.set(matrix)
        return false
    }

    private fun dp(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }
}