package giles.ledcontroller.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import giles.ledcontroller.Gradient

/**
 * A rectangular view which displays gradients
 */
class GradientRectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): View(context, attrs, defStyle) {
    private val paint = Paint()
    private var shader: LinearGradient? = null


    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun setGradientColors(colors: IntArray?){
        when {
            //If a null list was given, clear the gradient shader
            colors == null -> {
                shader = null
                paint.shader = shader
            }

            //If an empty list was given, convert to null
            colors.isEmpty() -> setGradientColors(null)

            //If only one color was given, then make a gradient of that color to the same color
            colors.size == 1 -> setGradientColors(intArrayOf(colors[0], colors[0]))

            else -> {
                //Make the gradient
                shader = LinearGradient(0f, 0f, width.toFloat(), 0f, colors, null, Shader.TileMode.REPEAT)
                paint.shader = shader
            }
        }
        invalidate()
    }

    fun displayGradient(gradient: Gradient?){
        if(gradient == null){
            setGradientColors(null)
        } else {
            setGradientColors(gradient.colors)
        }
    }
}