package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.LedController
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.item_controller.view.*

class ControllerView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var controller: LedController? = null
) : ConstraintLayout(context, attrs, defStyle) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        inflate(context, R.layout.item_controller, this)
    }

    /**
     * A RecyclerView adapter for displaying [LedController]s
     */
    class ControllerViewAdapter(
        internal var dataSet: List<LedController>,
        private val onItemClickListener: View.OnClickListener? = null
    ): RecyclerView.Adapter<ControllerViewAdapter.ControllerViewHolder>(){

        private var selectedView: ControllerView? = null
        var selectedController: LedController? = null

        /**
         * A ViewHolder for views displaying [LedController]s
         */
        class ControllerViewHolder constructor(
            val view: ControllerView,
            private val parentAdapter: ControllerViewAdapter,
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

            fun setController(controller: LedController){
                view.text_controller_name.text = controller.name
                view.text_controller_num_lights.text = controller.numLights.toString()
                view.text_controller_device_name.text = controller.device.name
                view.controller = controller
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerViewHolder {
            //Create a new ViewHolder for a view inflated from the item_controller layout
            val viewHolder = ControllerViewHolder(ControllerView(parent.context), this, onItemClickListener)
            checkSelection(viewHolder.view)
            return viewHolder
        }

        override fun onBindViewHolder(holder: ControllerViewHolder, position: Int) {
            //Set controller of the view
            holder.setController(dataSet[position])
            checkSelection(holder.view)
        }

        //There is an item for every value in the data set
        override fun getItemCount() = dataSet.size

        /**
         * Updates the selectedView variable to ensure that the correct display
         * stays selected even when it gets recycled to a different view.
         */
        private fun checkSelection(view: ControllerView){
            if(view.controller == selectedController){
                selectedView = view
            }
        }

        /**
         * Marks the controller of the given view as the currently selected display, and the given view as the currently
         * selected view. If the given view was already selected, then it is deselected.
         */
        fun selectView(view: ControllerView){
            //If this view is the same as the previously selected view, then this view was just de-selected
            if(view.controller == selectedController){
                deselect()
            } else {
                //Set the selected view and controller to match this view
                selectedView = view
                selectedController = view.controller
            }
        }

        /**
         * If there is a currently selected view, de-select it.
         */
        fun deselect(){
            selectedController = null
            if(selectedView != null){
                val prevSelectedView = selectedView!!
                selectedView = null
                checkSelection(prevSelectedView)
            }
        }
    }
}