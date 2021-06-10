package giles.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

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

    fun setGradientColors(colors: IntArray?){
        this.colors = colors
        if(colors == null){
            paint.shader = null
        } else {
            for(color: Int in colors){
                Log.d("log", color.toString())
            }
            paint.shader = LinearGradient(0f, 0f, width.toFloat(), 0f, colors, null, Shader.TileMode.REPEAT)
        }
        invalidate()
    }
}