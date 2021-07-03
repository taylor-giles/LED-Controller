package giles.ledcontroller

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.GradientRectView
import giles.util.ItemTouchHelperAdapter
import giles.util.ItemTouchHelperCallback
import giles.util.OnDragStartListener
import kotlinx.android.synthetic.main.activity_gradient_edit.*
import kotlinx.android.synthetic.main.component_add_color_button.view.*
import kotlinx.android.synthetic.main.item_gradient_color.view.*


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

        //Get the gradient to be edited (if there is one)
        val givenGradient = intent.getSerializableExtra(getString(R.string.EXTRA_GRADIENT)) as Gradient?

        //Put the gradient view into the preview frame
        gradientView = GradientRectView(this)
        gradientView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout_gradient_preview.addView(gradientView)

        //Display the gradient to be edited, if applicable
        gradientView.displayGradient(givenGradient)
        givenGradient?.colors?.toCollection(gradientColors)
        text_edit_gradient_name.setText(givenGradient?.name)

        //Set up RecyclerView to display colors and choose new color when "Add Color" button is clicked
        val gradientColorsList = layout_gradient_colors
        gradientColorsList.layoutManager = LinearLayoutManager(this)
        adapter = GradientColorViewAdapter(gradientColors,
            //OnClickListener for "Add Color" button
            {
                //Open color picker dialog with option for saved colors
                val builder = AlertDialog.Builder(this)

                //Create dialog to display color picker
                val dialogPicker = ColorPickerView(this, showSaveButton = true, showSavedColors = true)
                builder.setTitle("Add Color to Gradient")
                    .setView(dialogPicker)
                    .setPositiveButton(R.string.add_color) { _, _ ->
                        //Add the selected color to the gradient
                        gradientColors.add(dialogPicker.getColor())
                        adapter.notifyItemInserted(gradientColors.size - 1)
                        gradientView.setGradientColors(gradientColors.toIntArray())
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            },
            this, gradientView
        )
        gradientColorsList.adapter = adapter

        //Attach the ItemTouchHelper so that views can be dragged and reordered
        touchHelper = ItemTouchHelper(
            ItemTouchHelperCallback(adapter, isLongPressDraggable = false, isDraggable = true, isSwipable = false)
        )
        touchHelper.attachToRecyclerView(gradientColorsList)

        //Assign action to cancel button
        btn_cancel_gradient.setOnClickListener {
            finish()
        }

        //Assign action to save button
        btn_save_gradient.setOnClickListener {
            //Get name
            var name = text_edit_gradient_name.text.toString()

            //Make default name if none is given
            if(name.isBlank() || name.isEmpty()){
                name = "New Gradient"
            }

            //Keep incrementing the suffix number until the name is valid
            if(!checkName(name)){
                var suffix = 1
                name += (suffix++).toString()
                while(!checkName(name)){
                    name = (name.substring(0, name.length - (suffix-1).toString().length)) + (suffix++).toString()
                }
            }


            //Save the gradient and finish the activity
            AppData.savedGradients.remove(givenGradient)
            AppData.savedGradients.add(Gradient(name, gradientColors.toIntArray()))
            Toast.makeText(this, "Gradient saved as $name", Toast.LENGTH_SHORT).show()
            finish()
        }
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
                        adapter.notifyItemInserted(gradientColors.size - 1)
                        gradientView.setGradientColors(gradientColors.toIntArray())
                    }
                }
            }
        }
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    /**
     * Checks whether or not the given String can be used as a name for a [Gradient].
     * A name is valid iff there are no [Gradient]s already saved with that name.
     *
     * @return true if the name is valid, false otherwise.
     */
    private fun checkName(name: String) : Boolean{
        for(gradient: Gradient in AppData.savedGradients){
            if(gradient.name == name){
                return false
            }
        }
        return true
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
        val view: View
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
        val layoutID = if(viewType == BUTTON_TYPE) R.layout.component_add_color_button else R.layout.item_gradient_color
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
        dataSet.removeAt(fromPosition)
        dataSet.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        gradientView.setGradientColors(dataSet.toIntArray())
    }
}
