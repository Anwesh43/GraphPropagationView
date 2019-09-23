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
import android.util.Log
import java.util.*

val scGap : Float = 0.05f
val strokeFactor : Float = 90f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val sizeFactor : Float = 10f
val row : Int = 3
val totalNodes : Int = 15

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.x(i : Int) : Float = this * (i % row)
fun Float.y(i : Int) : Float = this * (i / row)

fun Canvas.drawBlock(x : Float, y : Float, size : Float, sc : Float, paint : Paint) {
    val sizeSc : Float = size * sc
    save()
    translate(x, y)
    drawRect(RectF(-sizeSc, -sizeSc, sizeSc, sizeSc), paint)
    restore()
}

fun Canvas.drawLinesToNeighbor(x1 : Float, y1 : Float, x2 : Float, y2 : Float, paint : Paint) {
    drawLine(x1, y1, x2, y2, paint)
}

fun Canvas.drawGraphNode(i : Int, scale : Float, neighbors : Set<GraphPropagationView.GraphNode>, paint : Paint) {
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (row + 1)
    val x : Float = gap + gap.x(i)
    val y : Float = gap + gap.y(i)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    paint.style = Paint.Style.FILL
    drawBlock(x, y, w / sizeFactor, sc1, paint)
    paint.style = Paint.Style.STROKE
    drawBlock(x, y, w / sizeFactor, 1f, paint)
    restore()
    neighbors.forEach {
        val x2 : Float = gap + gap.x(it.i)
        val y2 : Float = gap + gap.y(it.i)
        drawLinesToNeighbor(x, y, x + (x2 - x) * sc2, y + (y2 - y) * sc2, paint)
    }
}

class GraphPropagationView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class GraphNode(var i : Int, var visited : Boolean = false, val state : State = State()) {

        private val neighbors : HashSet<GraphNode> = HashSet<GraphNode>()

        fun populateNeighbors(nodes : List<GraphNode>) {
            if (i < nodes.size - 1) {
                neighbors.add(nodes[i + 1])
            }
            if (i < nodes.size - row) {
                neighbors.add(nodes[i + row])
            }
        }

        fun traverseNeighbors(cb : (GraphNode) -> Unit) {
            neighbors.forEach(cb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawGraphNode(i, state.scale, neighbors, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update {
                cb(it)
            }
        }
        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }
    }

    data class Graph(var i : Int) {

        private val nodes : ArrayList<GraphNode> = ArrayList()
        private val stack : Stack<GraphNode> = Stack()
        private var toAnimate : Int = 0

        init {
            for (i in 0..(totalNodes - 1)) {
                nodes.add(GraphNode(i))
            }
            nodes.forEach {
                it.populateNeighbors(nodes)
            }
            stack.push(nodes[0])

        }

        fun draw(canvas : Canvas, paint : Paint) {
            nodes.forEach {
                it.draw(canvas, paint)
            }
        }

        fun update(cb : (Float) -> Unit) {

            var curr: GraphNode = stack.peek()
            val stopCb : (Float) -> Unit = {
                curr.traverseNeighbors {
                    if (!it.visited) {
                        stack.push(it)
                        it.visited = true
                    }
                }
                cb(it)
            }
            curr.update {
                stack.pop()
                Log.d("${stack.size}", "${curr.i}")
                if (!stack.empty()) {
                    Log.d("calling update", "${stack.size}")
                    update(stopCb)
                } else {
                    stopCb(it)
                }

            }
//            Log.d("currI","${curr.i}")
//            val stopCb : (Float) -> Unit = {
//                curr.traverseNeighbors {
//                    if (!it.visited) {
//                        stack.push(it)
//                    }
//                }
//                toAnimate--
//                if (toAnimate == 0) {
//                    cb(it)
//                }
//            }
//            curr.update {
//                stack.pop()
//                if (!stack.empty()) {
//                    stack.peek().startUpdating {
//                        update {
//                            stopCb(it)
//                        }
//
//                    }
//                } else {
//                    stopCb(it)
//                }
//            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (!stack.empty()) {
                stack.peek().startUpdating {
                    cb()
                    toAnimate = stack.size
                }
            }
        }
    }

    data class Renderer(var view : GraphPropagationView) {

        private val animator : Animator = Animator(view)
        private val graph : Graph = Graph(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            graph.draw(canvas, paint)
            animator.animate {
                graph.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            graph.startUpdating {
                animator.start()

            }
        }
    }

    companion object {

        fun create(activity : Activity) : GraphPropagationView {
            val view : GraphPropagationView = GraphPropagationView(activity)
            activity.setContentView(view)
            return view
        }
    }
}