package com.anwesh.uiprojects.graphpropagationview

/**
 * Created by anweshmishra on 23/09/19.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val scGap : Float = 0.01f
val strokeFactor : Float = 90f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val sizeFactor : Float = 10f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawRect(x : Float, y : Float, size : Float, sc : Float, paint : Paint) {
    val sizeSc : Float = size * sc
    save()
    translate(x, y)
    drawRect(RectF(-sizeSc, -sizeSc, sizeSc, sizeSc), paint)
    restore()
}

fun Canvas.drawLinesToNeighbor(x1 : Float, y1 : Float, x2 : Float, y2 : Float, paint : Paint) {
    drawLine(x1, y1, x2, y2, paint)
}
