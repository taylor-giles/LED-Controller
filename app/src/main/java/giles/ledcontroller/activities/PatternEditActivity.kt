package giles.ledcontroller.activities

import android.content.Context
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
import giles.ledcontroller.*
import giles.ledcontroller.views.GradientRectView
import giles.util.ColorUtils
import giles.util.ItemTouchHelperAdapter
import giles.util.ItemTouchHelperCallback
import giles.util.OnDragStartListener
import kotlinx.android.synthetic.main.activity_pattern_edit.*
import kotlinx.android.synthetic.main.component_add_layer_button.view.*
import kotlinx.android.synthetic.main.item_layer.view.*

class PatternEditActivity : AppCompatActivity(), OnDragStartListener {
    private val patternLayers = ArrayList<Layer>()
    private lateinit var adapter: LayerViewAdapter
    private lateinit var touchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_edit)

        //Get the pattern to be edited (if there is one)
        val givenPattern = intent.getSerializableExtra(getString(R.string.EXTRA_PATTERN)) as Pattern?
        givenPattern?.layers?.toCollection(patternLayers)
        edit_text_pattern_name.setText(givenPattern?.name)

        //Set up RecyclerView to display colors and choose new color when "Add Color" button is clicked
        val patternLayersList = list_pattern_layers
        patternLayersList.layoutManager = LinearLayoutManager(this)
        adapter = LayerViewAdapter(patternLayers, this, AppData.currentController.numLights,
            //OnClickListener for "Add Color" button
            {
                //Store number of lights as intent extra
                val layerEditIntent = Intent(this, LayerEditActivity::class.java)
                layerEditIntent.putExtra(getString(R.string.EXTRA_NUMLIGHTS), AppData.currentController.numLights)

                //Open LayerEditActivity
                startActivityForResult(layerEditIntent, R.integer.EDIT_LAYER_REQUEST)
            },
            this
        )
        patternLayersList.adapter = adapter

        //Attach the ItemTouchHelper so that views can be dragged and reordered
        touchHelper = ItemTouchHelper(
            ItemTouchHelperCallback(adapter,
                isLongPressDraggable = false,
                isDraggable = true,
                isSwipable = false,
                viewTypes = arrayOf(COLOR_LAYER_TYPE, GRADIENT_LAYER_TYPE)
            )
        )
        touchHelper.attachToRecyclerView(patternLayersList)

        //Assign action to cancel button
        btn_cancel_pattern.setOnClickListener {
            finish()
        }

        //Assign action to save button
        btn_save_pattern.setOnClickListener {
            //Remove the old version of the edited pattern, if it exists
            if(givenPattern != null){
                for(pattern: Pattern in AppData.patterns){
                    if(pattern == givenPattern){
                        AppData.patterns.remove(pattern)
                        AppData.savePatterns(this)
                        break
                    }
                }
            }

            //TODO: Figure out why pattern editing ignores all changes
            //If the given pattern exists, then remove it
            if(givenPattern != null){
                for(pattern: Pattern in AppData.patterns){
                    if(pattern == givenPattern){
                        AppData.patterns.remove(pattern)
                        break
                    }
                }
            }

            //Get name
            var name = edit_text_pattern_name.text.toString()

            //Make default name if none is given
            if(name.isBlank() || name.isEmpty()){
                name = "Untitled Pattern"
            }

            //Keep incrementing the suffix number until the name is valid
            if(!checkName(name)) {
                var suffix = 1
                name += (suffix++).toString()
                while (!checkName(name)) {
                    name = (name.substring(0, name.length - (suffix - 1).toString().length)
                            ) + (suffix++).toString()
                }
            }

            //Save the new pattern
            AppData.patterns.add(Pattern(name, patternLayers))
            AppData.savePatterns(this)
            Toast.makeText(this, "Pattern saved as $name", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Checks whether or not the given String can be used as a name for a [Pattern].
     * A name is valid iff there are no [Pattern]s already saved with that name.
     *
     * @return true if the name is valid, false otherwise.
     */
    private fun checkName(name: String) : Boolean{
        for(pattern: Pattern in AppData.patterns){
            if(pattern.name == name){
                return false
            }
        }
        return true
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_OK -> {
                patternLayers.add(data?.getSerializableExtra(getString(R.string.EXTRA_LAYER)) as Layer)
                adapter.notifyItemInserted(patternLayers.size - 1)
            }
        }
    }
}


/**
 * A RecyclerView adapter for displaying layers in a pattern
 */
//View types
const val COLOR_LAYER_TYPE = 1
const val GRADIENT_LAYER_TYPE = 2
const val ADD_LAYER_BUTTON_TYPE = 3
class LayerViewAdapter(
    private val dataSet: ArrayList<Layer>,
    private val context: Context,
    private val numLights: Int,
    private val addButtonListener: View.OnClickListener,
    private val dragListener: OnDragStartListener
): RecyclerView.Adapter<LayerViewAdapter.LayerViewHolder>(),
    ItemTouchHelperAdapter {

    /**
     * A ViewHolder for views displaying pattern layers and the button used to add a new layer to the pattern
     */
    class LayerViewHolder constructor(
        val view: View
    ) : RecyclerView.ViewHolder(view){
        private val descriptionLayout: FrameLayout? = view.layout_effect_description
        val dragHandle: View? = view.image_layer_drag_handle
        val removeButton: View? = view.image_layer_remove
        val editButton: View? = view.image_layer_edit
        val titleText: TextView? = view.text_effect_title
        val contentText: TextView? = view.text_layer_content_description

        val addLayerButton: View? = view.text_btn_add_pattern_layer

        private val colorPreview = View(view.context)
        private val gradientView = GradientRectView(view.context)

        init{
            //Set up description views for both the color-type and gradient-type effects
            gradientView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            colorPreview.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            //All description-related views should be GONE by default
            gradientView.visibility = View.GONE
            colorPreview.visibility = View.GONE

            //Add all description-related views to layout
            descriptionLayout?.addView(gradientView)
            descriptionLayout?.addView(colorPreview)
        }

        fun setColor(color: Int){
            colorPreview.setBackgroundColor(color)
            contentText?.text = ColorUtils.getHexString(color)
            gradientView.visibility = View.GONE
            colorPreview.visibility = View.VISIBLE
        }

        fun setGradient(gradient: Gradient){
            gradientView.displayGradient(gradient)
            contentText?.text = gradient.name
            colorPreview.visibility = View.GONE
            gradientView.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        //If the type of this viewHolder is ADD_BUTTON_TYPE, then use the button layout, otherwise use the layer item layout
        val layoutID = if(viewType == ADD_LAYER_BUTTON_TYPE) R.layout.component_add_layer_button else R.layout.item_layer
        return LayerViewHolder(LayoutInflater.from(parent.context).inflate(layoutID, parent, false))
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        if (holder.itemViewType == ADD_LAYER_BUTTON_TYPE) {
            holder.addLayerButton!!.setOnClickListener(addButtonListener)
        } else {
            val effect = dataSet[position].effect

            //Set the title of the view
            holder.titleText!!.text = effect.title

            //Give behavior to the remove button
            holder.removeButton!!.setOnClickListener{
                dataSet.remove(dataSet[holder.absoluteAdapterPosition])
                notifyItemRemoved(holder.absoluteAdapterPosition)
            }

            //Give behavior to the edit button
            holder.editButton!!.setOnClickListener {
                //Open the LayerEditActivity to edit this layer
                val layerEditIntent = Intent(context, LayerEditActivity::class.java)
                layerEditIntent.putExtra(context.getString(R.string.EXTRA_LAYER), dataSet[position])
                layerEditIntent.putExtra(context.getString(R.string.EXTRA_NUMLIGHTS), numLights)
                context.startActivity(layerEditIntent)
            }

            //Attach the onDrag listener to the handle via onTouch
            holder.dragHandle!!.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    dragListener.onDragStart(holder)
                }
                false
            }

            //Update the description of the view
            when(holder.itemViewType){
                COLOR_LAYER_TYPE -> holder.setColor((effect as ColorEffect).color)
                GRADIENT_LAYER_TYPE -> holder.setGradient((effect as GradientEffect).gradient)
            }
        }
    }

    //There is an item for every value in the data set, plus one for the add button, which is at the end
    override fun getItemCount() = dataSet.size + 1
    override fun getItemViewType(position: Int): Int {
        return when {
            position == dataSet.size -> ADD_LAYER_BUTTON_TYPE
            dataSet[position].effect is ColorEffect -> COLOR_LAYER_TYPE
            else -> GRADIENT_LAYER_TYPE
        }
    }

    /**
     * Called when an item is dragged and moved to a different position
     */
    override fun moveItem(fromPosition: Int, toPosition: Int) {
        val movedItem = dataSet[fromPosition]
        dataSet.removeAt(fromPosition)
        dataSet.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
    }
}