package giles.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * A rectangular view which displays the given shader
 */
class ShaderRectView @JvmOverloads constructor(
    context: Context,
    colors: IntArray? = null,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): View(context, attrs, defStyle) {
    private val paint = Paint()

    init {
        setShaderColors(colors)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    fun setShaderColors(colors: IntArray?){
        if(colors == null){
            paint.shader = null
        } else {
            for(color: Int in colors){
                Log.d("log", color.toString())
            }
            paint.shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), colors, null, Shader.TileMode.CLAMP)
        }
        invalidate()
    }
}