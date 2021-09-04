package giles.ledcontroller.activities

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.AppData
import giles.ledcontroller.Gradient
import giles.ledcontroller.R
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.GradientRectView
import giles.util.ColorUtils
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
            //If the given gradient exists, then remove it
            if(givenGradient != null){
                for(gradient: Gradient in AppData.savedGradients){
                    if(gradient == givenGradient){
                        AppData.savedGradients.remove(gradient)
                        AppData.saveGradients(this)
                        break
                    }
                }
            }
            
            //Get name
            var name = text_edit_gradient_name.text.toString()

            //Make default name if none is given
            if(name.isBlank() || name.isEmpty()){
                name = "Untitled Gradient"
            }

            //Keep incrementing the suffix number until the name is valid
            if(!checkName(name)){
                var suffix = 1
                name += (suffix++).toString()
                while(!checkName(name)){
                    name = (name.substring(0, name.length - (suffix-1).toString().length)) + (suffix++).toString()
                }
            }

            //Make and save the new gradient
            AppData.savedGradients.add(Gradient(name, gradientColors.toIntArray()))
            AppData.saveGradients(this)
            Toast.makeText(this, "Gradient saved as $name", Toast.LENGTH_SHORT).show()
            finish()
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
const val ADD_GRADIENT_COLOR_TYPE = 2
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
        val opacityBar: SeekBar? = view.slider_gradient_color_opacity
        val opacityText: EditText? = view.text_gradient_color_opacity

        val addColorButton: View? = view.text_btn_add_gradient_color

        fun setColor(color: Int){
            colorPreview!!.setBackgroundColor(color)
            textDisplay!!.text = ColorUtils.getHexString(color)
            opacityText!!.setText(Color.alpha(color).toString())
            opacityBar!!.progress = Color.alpha(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientColorViewHolder {
        //If the type of this viewHolder is BUTTON_TYPE, then use the button layout, otherwise use the gradient color layout
        val layoutID = if(viewType == ADD_GRADIENT_COLOR_TYPE) R.layout.component_add_color_button else R.layout.item_gradient_color
        return GradientColorViewHolder(LayoutInflater.from(parent.context).inflate(layoutID, parent, false))
    }

    override fun onBindViewHolder(holder: GradientColorViewHolder, position: Int) {
        if(holder.itemViewType == ADD_GRADIENT_COLOR_TYPE){
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

            //Give behavior to the opacity bar
             holder.opacityBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                val color = dataSet[holder.absoluteAdapterPosition]

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val newColor = Color.argb(progress, Color.red(color), Color.green(color), Color.blue(color))
                    holder.setColor(newColor)
                    dataSet[holder.absoluteAdapterPosition] = newColor
                    gradientView.setGradientColors(dataSet.toIntArray())
                }
            })
        }
    }

    //There is an item for every value in the data set, plus one for the add button, which is at the end
    override fun getItemCount() = dataSet.size + 1
    override fun getItemViewType(position: Int): Int {
        return if (position == dataSet.size) ADD_GRADIENT_COLOR_TYPE else COLOR_TYPE
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
