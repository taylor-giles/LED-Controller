package giles.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * A rectangular view which displays the given shader
 */
class ShaderRectView @JvmOverloads constructor(
    context: Context,
    shader: Shader? = null,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): View(context, attrs, defStyle) {
    private val paint = Paint()

    init {
        setShader(shader)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat(), paint)
    }

    fun setShader(shader: Shader?){
        paint.shader = shader
        invalidate()
    }
}