package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.Gradient
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.item_gradient.view.*

class GradientView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    val gradient: Gradient? = null
) : ConstraintLayout(context, attrs, defStyle) {

    init {
        inflate(context, R.layout.item_gradient, this)
    }
}


/**
 * A RecyclerView adapter for displaying gradients
 */
class GradientViewAdapter(
    var dataSet: ArrayList<Gradient>,
    private val onItemClickListener: View.OnClickListener? = null
): RecyclerView.Adapter<GradientViewAdapter.GradientViewHolder>(){

    private var selectedView: GradientView? = null
    var selectedGradient: Gradient? = null

    /**
     * A ViewHolder for views displaying gradient colors and the button used to add a new color to the gradient
     */
    class GradientViewHolder constructor(
        val view: GradientView,
        private val parentAdapter: GradientViewAdapter,
        onItemClickListener: View.OnClickListener?
    ) : RecyclerView.ViewHolder(view){
        private val preview: GradientRectView

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

            //Add the gradient preview
            preview = GradientRectView(view.context)
            view.layout_gradient_item_preview.addView(preview)
        }

        fun setGradient(gradient: Gradient){
            view.text_gradient_item_name.text = gradient.name
            preview.displayGradient(gradient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientViewHolder {
        //Create a new ViewHolder for a view inflated from the item_gradient layout
        val viewHolder = GradientViewHolder(GradientView(parent.context), this, onItemClickListener)
        checkSelection(viewHolder.view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: GradientViewHolder, position: Int) {
        //Set gradient of the view
        holder.setGradient(dataSet[position])
        checkSelection(holder.view)
    }

    //There is an item for every value in the data set
    override fun getItemCount() = dataSet.size

    /**
     * Updates the selectedView variable to ensure that the correct gradient
     * stays selected even when it gets recycled to a different view.
     */
    private fun checkSelection(view: GradientView){
        if(view.gradient == selectedGradient){
            selectedView = view
        }
    }

    /**
     * Marks the gradient of the given view as the currently selected gradient, and the given view as the currently
     * selected view. If the given view was already selected, then it is deselected.
     */
    fun selectView(view: GradientView){
        //If this view is the same as the previously selected view, then this view was just de-selected
        if(view.gradient == selectedGradient){
            deselect()
        } else {
            //Set the selected view and gradient to match this view
            selectedView = view
            selectedGradient = view.gradient
        }
    }

    /**
     * If there is a currently selected view, de-select it.
     */
    fun deselect(){
        selectedGradient = null
        if(selectedView != null){
            val prevSelectedView = selectedView!!
            selectedView = null
            checkSelection(prevSelectedView)
        }
    }
}