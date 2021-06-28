package giles.ledcontroller.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import giles.ledcontroller.Gradient

/**
 * A rectangular view which displays a gradient of the given colors from left to right
 */
class GradientRectView @JvmOverloads constructor(
    context: Context,
    private var colors: IntArray? = null,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): View(context, attrs, defStyle) {
    private val paint = Paint()
    private var shader: LinearGradient? = null

    init {
        setGradientColors(colors)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setGradientColors(colors)
    }

    fun setGradientColors(newColors: IntArray?){
        this.colors = newColors
        when {
            colors == null -> {
                shader = null
                paint.shader = shader
            }
            colors!!.isEmpty() -> setGradientColors(null)
            else -> {
                //If only one color was given, then make a gradient of that color to the same color
                if(newColors!!.size == 1){
                    this.colors = intArrayOf(newColors[0], newColors[0])
                }

                //Make the gradient
                shader = LinearGradient(0f, 0f, width.toFloat(), 0f, colors!!, null, Shader.TileMode.REPEAT)
                paint.shader = shader
            }
        }
        invalidate()
    }

    fun displayGradient(gradient: Gradient){
        setGradientColors(gradient.colors)
    }
}