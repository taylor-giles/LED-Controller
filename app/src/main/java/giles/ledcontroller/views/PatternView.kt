package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.Pattern
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.item_pattern.view.*

class PatternView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var pattern: Pattern? = null
) : ConstraintLayout(context, attrs, defStyle) {

    init {
        inflate(context, R.layout.item_pattern, this)
    }
}


/**
 * A RecyclerView adapter for displaying gradients
 */
class PatternViewAdapter(
    var dataSet: ArrayList<Pattern>,
    private val onItemClickListener: View.OnClickListener? = null
): RecyclerView.Adapter<PatternViewAdapter.PatternViewHolder>(){

    private var selectedView: PatternView? = null
    var selectedPattern: Pattern? = null

    /**
     * A ViewHolder for views displaying gradient colors and the button used to add a new color to the gradient
     */
    class PatternViewHolder constructor(
        val view: PatternView,
        private val parentAdapter: PatternViewAdapter,
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

        fun setPattern(pattern: Pattern){
            view.text_pattern_item_name.text = pattern.name
            view.text_pattern_item_duration.text = pattern.duration.toString()
            view.text_pattern_item_number_layers.text = pattern.layers.size.toString()
            view.pattern = pattern
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternViewHolder {
        //Create a new ViewHolder for a view inflated from the item_gradient layout
        val viewHolder = PatternViewHolder(PatternView(parent.context), this, onItemClickListener)
        checkSelection(viewHolder.view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: PatternViewHolder, position: Int) {
        //Set gradient of the view
        holder.setPattern(dataSet[position])
        checkSelection(holder.view)
    }

    //There is an item for every value in the data set
    override fun getItemCount() = dataSet.size

    /**
     * Updates the selectedView variable to ensure that the correct pattern
     * stays selected even when it gets recycled to a different view.
     */
    private fun checkSelection(view: PatternView){
        if(view.pattern == selectedPattern){
            selectedView = view
        }
    }

    /**
     * Marks the pattern of the given view as the currently selected pattern, and the given view as the currently
     * selected view. If the given view was already selected, then it is deselected.
     */
    fun selectView(view: PatternView){
        //If this view is the same as the previously selected view, then this view was just de-selected
        if(view.pattern == selectedPattern){
            deselect()
        } else {
            //Set the selected view and pattern to match this view
            selectedView = view
            selectedPattern = view.pattern
        }
    }

    /**
     * If there is a currently selected view, de-select it.
     */
    fun deselect(){
        selectedPattern = null
        if(selectedView != null){
            val prevSelectedView = selectedView!!
            selectedView = null
            checkSelection(prevSelectedView)
        }
    }
}