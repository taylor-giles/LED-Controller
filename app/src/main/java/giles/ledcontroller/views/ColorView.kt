package giles.ledcontroller.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.R
import giles.util.ColorUtils
import giles.views.WidthSquareView

const val COLOR_VIEW_PADDING = 30 //The amount of padding, in pixels, around the color/TextBox square
class ColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle){

    private var view = WidthSquareView(context)
    private var title = TextView(context)
    private var color: Int = 0
    var deselectedBackgroundColor = ContextCompat.getColor(context, android.R.color.transparent)
    var selectedBackgroundColor = ContextCompat.getColor(context, android.R.color.white)

    init {
        this.setPaddingRelative(COLOR_VIEW_PADDING, COLOR_VIEW_PADDING, COLOR_VIEW_PADDING, COLOR_VIEW_PADDING)
        title.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
        title.textAlignment = View.TEXT_ALIGNMENT_CENTER

        //Add ripple click effect
        foreground = ContextCompat.getDrawable(view.context, R.drawable.ripple_effect)
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
        title.text = ColorUtils.getHexString(color)
        title.typeface = Typeface.MONOSPACE
        title.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        title.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        title.textSize = 14f
    }

    fun getColor(): Int{
        return color
    }

    fun showAsSelected(){
        //Change frame background
        setBackgroundColor(selectedBackgroundColor)
    }

    fun showAsDeselected(){
        //Change frame background
        setBackgroundColor(deselectedBackgroundColor)
    }
}

/**
 * A RecyclerView adapter that creates square views to display colors
 */
class ColorViewAdapter @JvmOverloads constructor(
    var dataSet: Array<Int>,
    var changeBackgroundOnSelection: Boolean = true,
    private val onItemClickListener: View.OnClickListener? = null
): RecyclerView.Adapter<ColorViewAdapter.ColorViewHolder>(){

    private var selectedView: ColorView? = null
    var selectedColor: Int? = null

    class ColorViewHolder constructor(
        val view: ColorView,
        private val parentAdapter: ColorViewAdapter,
        onItemClickListener: View.OnClickListener?
    ) : RecyclerView.ViewHolder(view) {
        init {
            //Set the behavior when this item is clicked
            if(onItemClickListener == null){
                //By default, select this view when it is clicked
                view.setOnClickListener {
                    parentAdapter.selectView(view)
                }
            } else {
                view.setOnClickListener(onItemClickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = ColorView(WidthSquareView(parent.context))
        checkSelection(view)
        return ColorViewHolder(view, this, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        //Set color and selection status textDisplay of the view
        holder.view.setColor(dataSet[position])
        checkSelection(holder.view)
    }

    override fun getItemCount() = dataSet.size


    /**
     * Determines whether or not the given ColorView should be selected (based on the currently selected color)
     * and updates its background accordingly. Also updates the selectedView variable to ensure that the correct *color*
     * stays selected even when it gets recycled to a different *view*.
     */
    private fun checkSelection(view: ColorView){
        if(view.getColor() == selectedColor){
            if(changeBackgroundOnSelection) view.showAsSelected()
            selectedView = view
        } else {
            if(changeBackgroundOnSelection) view.showAsDeselected()
        }
    }

    /**
     * Marks the color of the given view as the currently selected color, and the given view as the currently
     * selected view. Updates the background of the given view and the previously selected view according to their new
     * selection status. If the given view was already selected, then it is deselected.
     */
    fun selectView(view: ColorView){
        //Change view background
        if(changeBackgroundOnSelection) view.showAsSelected()

        //Change the background of the previously selected view (un-select it)
        if(changeBackgroundOnSelection) selectedView?.showAsDeselected()

        //If this view is the same as the previously selected view, then this view was just de-selected
        if(view.getColor() == selectedColor){
            deselect()
        } else {
            //Set the selected view and color to match this view
            selectedView = view
            selectedColor = view.getColor()
        }
    }

    /**
     * If there is a currently selected view, de-select it.
     */
    fun deselect(){
        selectedColor = null
        if(selectedView != null){
            val prevSelectedView = selectedView!!
            selectedView = null
            checkSelection(prevSelectedView)
        }
    }
}