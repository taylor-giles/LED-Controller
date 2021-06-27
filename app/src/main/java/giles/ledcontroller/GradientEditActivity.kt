package giles.ledcontroller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.views.GradientRectView
import giles.util.ItemTouchHelperAdapter
import giles.util.ItemTouchHelperCallback
import giles.util.OnDragStartListener
import kotlinx.android.synthetic.main.activity_gradient_edit.*
import kotlinx.android.synthetic.main.item_add_color_button.view.*
import kotlinx.android.synthetic.main.item_gradient_color.view.*

//It should be impossible for this color to be displayed
//The color given to a GradientColorView when a holder has been created but not yet given a color
const val DEFAULT_COLOR_ID = android.R.color.black

/**
 * Activity that allows a user to create or edit a gradient
 */
class GradientEditActivity : AppCompatActivity(), OnDragStartListener {
    private lateinit var gradientView: GradientRectView
    private val gradientColors = ArrayList<Int>()
    private lateinit var adapter: GradientColorViewAdapter
    private lateinit var touchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_edit)

        //Put the gradient view into the preview frame
        gradientView = GradientRectView(this)
        gradientView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout_gradient_preview.addView(gradientView)

        //TODO: Assign actions to the cancel and save buttons

        //Set up RecyclerView to display colors and open activity to choose new color when "Add Color" button is clicked
        val gradientColorsList = layout_gradient_colors
        gradientColorsList.layoutManager = LinearLayoutManager(this)
        adapter = GradientColorViewAdapter(gradientColors,
            //OnClickListener for "Add Color" button
            View.OnClickListener {
                val savedColorsIntent = Intent(this, SavedColorsActivity::class.java)
                startActivityForResult(savedColorsIntent, resources.getInteger(R.integer.CHOOSE_COLOR_REQUEST))
            },
            this, gradientView
        )
        gradientColorsList.adapter = adapter

        //Attach the ItemTouchHelper so that views can be dragged and reordered
        touchHelper = ItemTouchHelper(
            ItemTouchHelperCallback(adapter, isLongPressDraggable = false, isDraggable = true, isSwipable = false)
        )
        touchHelper.attachToRecyclerView(gradientColorsList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_OK -> {
                when(requestCode){
                    //Choose color request - add the chosen color to the gradient
                    resources.getInteger(R.integer.CHOOSE_COLOR_REQUEST) -> {
                        val chosenColor = data!!.getIntExtra(getString(R.string.EXTRA_COLOR), 0)
                        gradientColors.add(chosenColor)
                        adapter.notifyItemInserted(gradientColors.indexOf(chosenColor))
                        gradientView.setGradientColors(gradientColors.toIntArray())
                    }
                }
            }
        }
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }
}


/**
 * A RecyclerView adapter for representing colors used in the construction of a gradient
 */
//View types
const val COLOR_TYPE = 1
const val BUTTON_TYPE = 2
class GradientColorViewAdapter(
    private val dataSet: ArrayList<Int>,
    private val addButtonListener: View.OnClickListener,
    private val dragListener: OnDragStartListener,
    private val gradientView: GradientRectView
): RecyclerView.Adapter<GradientColorViewAdapter.GradientColorViewHolder>(), ItemTouchHelperAdapter{

    /**
     * A ViewHolder for views displaying gradient colors and the button used to add a new color to the gradient
     */
    class GradientColorViewHolder constructor(
        val view: View,
        val color: Int = getColor(view.context, DEFAULT_COLOR_ID)
    ) : RecyclerView.ViewHolder(view){

        private val colorPreview: View? = view.view_gradient_color
        private val textDisplay: TextView? = view.text_gradient_color
        val dragHandle: View? = view.image_gradient_color_drag_handle
        val removeButton: View? = view.image_gradient_color_remove

        val addColorButton: View? = view.text_btn_add_gradient_color

        fun setColor(color: Int){
            colorPreview!!.setBackgroundColor(color)
            textDisplay!!.text = String.format("#%06X", 0xFFFFFF and color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientColorViewHolder {
        //If the type of this viewHolder is BUTTON_TYPE, then use the button layout, otherwise use the gradient color layout
        val layoutID = if(viewType == BUTTON_TYPE) R.layout.item_add_color_button else R.layout.item_gradient_color
        return GradientColorViewHolder(LayoutInflater.from(parent.context).inflate(layoutID, parent, false))
    }

    override fun onBindViewHolder(holder: GradientColorViewHolder, position: Int) {
        if(holder.itemViewType == BUTTON_TYPE){
            holder.addColorButton!!.setOnClickListener(addButtonListener)
        } else {
            //Set color of the view
            holder.setColor(dataSet[position])

            //Give behavior to the remove button
            holder.removeButton!!.setOnClickListener{
                dataSet.remove(dataSet[holder.absoluteAdapterPosition])
                notifyItemRemoved(holder.absoluteAdapterPosition)
                gradientView.setGradientColors(dataSet.toIntArray())
            }

            //Attach the onDrag listener to the handle via onTouch
            holder.dragHandle!!.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    dragListener.onDragStart(holder)
                }
                false
            }
        }
    }

    //There is an item for every value in the data set, plus one for the add button, which is at the end
    override fun getItemCount() = dataSet.size + 1
    override fun getItemViewType(position: Int): Int {
        return if (position == dataSet.size) BUTTON_TYPE else COLOR_TYPE
    }

    /**
     * Called when an item is dragged and moved to a different position
     */
    override fun moveItem(fromPosition: Int, toPosition: Int) {
        val movedItem = dataSet[fromPosition]
        dataSet.remove(movedItem)
        dataSet.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        gradientView.setGradientColors(dataSet.toIntArray())
    }
}
