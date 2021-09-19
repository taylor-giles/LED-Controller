package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.LightDisplay
import giles.ledcontroller.Pattern
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.item_display.view.*
import kotlinx.android.synthetic.main.item_pattern.view.*

class DisplayView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var display: LightDisplay? = null
) : ConstraintLayout(context, attrs, defStyle) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        inflate(context, R.layout.item_display, this)
    }

    /**
     * A RecyclerView adapter for displaying [LightDisplay]s
     */
    class DisplayViewAdapter(
        internal var dataSet: List<LightDisplay>,
        private val onItemClickListener: View.OnClickListener? = null
    ): RecyclerView.Adapter<DisplayViewAdapter.DisplayViewHolder>(){

        private var selectedView: DisplayView? = null
        var selectedDisplay: LightDisplay? = null

        /**
         * A ViewHolder for views displaying [LightDisplay]s
         */
        class DisplayViewHolder constructor(
            val view: DisplayView,
            private val parentAdapter: DisplayViewAdapter,
            onItemClickListener: View.OnClickListener?
        ) : RecyclerView.ViewHolder(view){
            init {
                //Add ripple click effect to the view
                view.foreground = ContextCompat.getDrawable(view.context, R.drawable.ripple_effect)

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

            fun setDisplay(display: LightDisplay){
                view.text_display_name.text = display.name
                view.text_display_num_lights.text = display.numLights.toString()
                view.text_display_device_name.text = display.device.name
                view.display = display
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayViewHolder {
            //Create a new ViewHolder for a view inflated from the item_display layout
            val viewHolder = DisplayViewHolder(DisplayView(parent.context), this, onItemClickListener)
            checkSelection(viewHolder.view)
            return viewHolder
        }

        override fun onBindViewHolder(holder: DisplayViewHolder, position: Int) {
            //Set display of the view
            holder.setDisplay(dataSet[position])
            checkSelection(holder.view)
        }

        //There is an item for every value in the data set
        override fun getItemCount() = dataSet.size

        /**
         * Updates the selectedView variable to ensure that the correct display
         * stays selected even when it gets recycled to a different view.
         */
        private fun checkSelection(view: DisplayView){
            if(view.display == selectedDisplay){
                selectedView = view
            }
        }

        /**
         * Marks the display of the given view as the currently selected display, and the given view as the currently
         * selected view. If the given view was already selected, then it is deselected.
         */
        fun selectView(view: DisplayView){
            //If this view is the same as the previously selected view, then this view was just de-selected
            if(view.display == selectedDisplay){
                deselect()
            } else {
                //Set the selected view and display to match this view
                selectedView = view
                selectedDisplay = view.display
            }
        }

        /**
         * If there is a currently selected view, de-select it.
         */
        fun deselect(){
            selectedDisplay = null
            if(selectedView != null){
                val prevSelectedView = selectedView!!
                selectedView = null
                checkSelection(prevSelectedView)
            }
        }
    }
}