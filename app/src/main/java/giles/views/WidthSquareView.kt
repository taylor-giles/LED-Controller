package giles.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/**
 * A View that will always be square, with side length
 * equal to the original width
 */
class WidthSquareView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Re-measure to be a square with side length equal to the original width (ignore height)
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

/**
 * A View that will always be square, with side length
 * equal to the original height
 */
class HeightSquareView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Re-measure to be a square with side length equal to the original height (ignore width)
        super.onMeasure(heightMeasureSpec, heightMeasureSpec)
    }
}