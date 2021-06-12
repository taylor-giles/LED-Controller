package giles.ledcontroller.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import giles.views.WidthSquareView

const val COLOR_VIEW_PADDING = 30 //The amount of padding, in pixels, around the color/textbox square

class ColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle){

    private var view = WidthSquareView(context)
    private var title = TextView(context)
    private var color: Int = 0

    init {
        this.setPaddingRelative(COLOR_VIEW_PADDING, COLOR_VIEW_PADDING, COLOR_VIEW_PADDING, COLOR_VIEW_PADDING)
        title.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
        title.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    constructor(view: WidthSquareView): this(view.context){
        this.view = view
        addView(view)
        addView(title)
    }

    constructor(context: Context, color: Int): this(context){
        this.view = WidthSquareView(context)
        addView(view)
        addView(title)
        setColor(color)
    }

    fun setColor(color: Int){
        this.color = color
        view.setBackgroundColor(color)
        title.text = String.format("#%06X", 0xFFFFFF and color)
        title.typeface = Typeface.MONOSPACE
        title.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        title.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        title.textSize = 14f
    }

    fun getColor(): Int{
        return color
    }
}