
package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.measureTime


class HandPainterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleId: Int = 0
) : View(context, attrs, defStyleId) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL // Change from STROKE to FILL
        strokeCap = Paint.Cap.ROUND
    }
    // private val strokePaint = Paint(circlePaint) // If you need a separate stroke paint
    private val path = Path()
    private lateinit var tracedPath: Path
    private val drawnPath = Path()
    private val pathMeasure = PathMeasure()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = width / 2f
        val centerY = height / 2f
        val rX = width / 4f // Half of the view's width
        val rY = height / 4f // Half of the view's height
        path.reset()
        path.addHighlight(RectF(centerX - rX, centerY - rY, centerX + rX, centerY + rY))
        tracedPath = path.taperPath(4f, 10f)
    }


    override fun onDraw(canvas: Canvas) {
        measureTime {
            super.onDraw(canvas)
            canvas.drawPath(tracedPath, circlePaint)
            // Optionally draw a stroke if you want an outline
            // strokePaint.style = Paint.Style.STROKE
            // canvas.drawPath(tracedPath, strokePaint)
        }.apply {
            Log.d("HandPainterView", "onDraw time: $this")
        }
    }
}

// 新增一个方法，采用双边路径算法，将一条Path转换成一个两端变细的Path
fun Path.taperPath(minWidth: Float, maxWidth: Float, taperStartRatio: Float = 0.1f, taperEndRatio: Float = 0.8f): Path {
    val taperedPath = Path()
    val pathMeasure = PathMeasure(this, false)
    val length = pathMeasure.length

    if (length == 0f) {
        return taperedPath // Return empty path if original is empty
    }

    val numSteps = (length / 2f).toInt().coerceAtLeast(100) // Increase steps for smoother curve based on length
    val stepLength = length / numSteps

    val pos = FloatArray(2)
    val tan = FloatArray(2)

    for (i in 0..numSteps) {
        val currentDistance = i * stepLength
        pathMeasure.getPosTan(currentDistance, pos, tan)

        val currentRatio = currentDistance / length

        val strokeWidth = when {
            currentRatio < taperStartRatio -> {
                // Start taper
                val fraction = currentRatio / taperStartRatio
                minWidth + (maxWidth - minWidth) * fraction
            }
            currentRatio > taperEndRatio -> {
                // End taper
                val fraction = (currentRatio - taperEndRatio) / (1f - taperEndRatio)
                maxWidth - (maxWidth - minWidth) * fraction
            }
            else -> {
                // Middle section
                maxWidth
            }
        }.coerceIn(minWidth, maxWidth)

        val halfWidth = strokeWidth / 2f

        // Calculate perpendicular vector
        val dx = -tan[1] * halfWidth
        val dy = tan[0] * halfWidth

        if (i == 0) {
            taperedPath.moveTo(pos[0] + dx, pos[1] + dy)
        } else {
            taperedPath.lineTo(pos[0] + dx, pos[1] + dy)
        }
    }

    // Draw the other side of the tapered path by iterating backwards
    for (i in numSteps downTo 0) {
        val currentDistance = i * stepLength
        pathMeasure.getPosTan(currentDistance, pos, tan)

        val currentRatio = currentDistance / length
        val strokeWidth = when {
            currentRatio < taperStartRatio -> minWidth + (maxWidth - minWidth) * (currentRatio / taperStartRatio)
            currentRatio > taperEndRatio -> maxWidth - (maxWidth - minWidth) * ((currentRatio - taperEndRatio) / (1f - taperEndRatio))
            else -> maxWidth
        }.coerceIn(minWidth, maxWidth)

        val halfWidth = strokeWidth / 2f
        val dx = -tan[1] * halfWidth
        val dy = tan[0] * halfWidth

        taperedPath.lineTo(pos[0] - dx, pos[1] - dy)
    }

    taperedPath.close() // Close the path to form a shape
    return taperedPath
}



fun Path.addHighlight(
    bounds: RectF,
    startAngle: Double = -120.0,
    totalDegrees: Double = 400.0,
) {
    val numPoints = 360

    val a = bounds.width() / 2f // 椭圆长半轴
    val b = bounds.height() / 2f // 椭圆短半轴
    val angleStep = (Math.toRadians(totalDegrees) / numPoints).toFloat()

    var theta = Math.toRadians(startAngle).toFloat() // 当前角度（弧度），从-120°开始

    for (i in 0..numPoints) {
        val scale = 1f + 0.2f * (theta / (2 * Math.PI.toFloat())) // scale随theta增加

        val currentA: Float = a * scale
        val currentB: Float = b * scale

        // 椭圆的参数方程
        val x = (bounds.centerX() + currentA * cos(theta.toDouble())).toFloat()
        val y = (bounds.centerY() + currentB * sin(theta.toDouble())).toFloat()

        if (isEmpty) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }

        theta += angleStep
    }
}


