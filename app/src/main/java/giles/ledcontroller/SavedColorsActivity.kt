package giles.ledcontroller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.ColorView
import giles.util.ColorUtils
import giles.views.WidthSquareView
import kotlinx.android.synthetic.main.activity_saved_colors.*


const val COLOR_VIEW_SIDE_LENGTH_DP = 95.0
class SavedColorsActivity : AppCompatActivity() {
    private lateinit var adapter: ColorViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_colors)

        val optionsPanel = layout_saved_colors_buttons
        val selectedColorTextDisplay = text_selected_saved_color_display
        val selectedColorWindow = view_selected_saved_color_window

        //Hide the options panel until a selection is made
        optionsPanel.visibility = GONE

        //Get the maximum number of cols that can fit on the screen, where each col has preferred size COLOR_VIEW_SIDE_LENGTH_DP
        val screenWidthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        val maxCols = (screenWidthDp / COLOR_VIEW_SIDE_LENGTH_DP + 0.5).toInt()

        //Set up the RecyclerView with a GridLayoutManager showing all colors in squares, sorted by hue
        layout_saved_colors.layoutManager = GridLayoutManager(this, maxCols)
        adapter = ColorViewAdapter(
            AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray(),
            selectedColorTextDisplay, selectedColorWindow, optionsPanel)
        layout_saved_colors.adapter = adapter

        //Set the delete button action (confirmation dialog and delete behavior)
        btn_delete_saved_color.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Remove Color")
                .setMessage("Are you sure you want to remove " +
                        String.format("#%06X", 0xFFFFFF and adapter.selectedColor!!) + " from your saved colors list?")
                .setPositiveButton(R.string.delete) { _, _ ->
                    //Delete the currently selected color
                    AppData.savedColors.remove(adapter.selectedColor!!)

                    //Re-make adapter to update RecyclerView
                    adapter = ColorViewAdapter(
                        AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray(),
                        selectedColorTextDisplay, selectedColorWindow, optionsPanel)
                    layout_saved_colors.adapter = adapter

                    //De-select view in adapter
                    adapter.deselect()
                }
                .setNegativeButton(android.R.string.no, null).show()
        }

        //Set the select button action (store the selected color and return to previous activity)
        btn_select_saved_color.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(getString(R.string.EXTRA_COLOR), adapter.selectedColor)
            setResult(RESULT_OK, returnIntent)
            finish()
        }

        //Set the add button action (color picker dialog and add selected color)
        fab_add_saved_color.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            //Create dialog to display color picker
            val dialogPicker = ColorPickerView(this, false)
            builder.setTitle("Save New Color")
                .setView(dialogPicker)
                .setPositiveButton(R.string.save_color) { _, _ ->
                    //Save the color
                    AppData.savedColors.add(dialogPicker.getColor())

                    //Re-make adapter to update RecyclerView
                    adapter = ColorViewAdapter(
                        AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray(),
                        selectedColorTextDisplay, selectedColorWindow, optionsPanel)
                    layout_saved_colors.adapter = adapter
                    adapter.deselect()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }
    }
}


/**
 * A RecyclerView adapter that creates square views to textDisplay colors
 */
class ColorViewAdapter(
    private val dataSet: Array<Int>,
    private val textDisplay: TextView,
    private val colorWindow: View,
    private val panel: View
): RecyclerView.Adapter<ColorViewAdapter.ColorViewHolder>(){

    private var selectedView: ColorView? = null
    var selectedColor: Int? = null

    class ColorViewHolder constructor(
        val view: ColorView,
        private val parentAdapter: ColorViewAdapter
    ) : RecyclerView.ViewHolder(view) {
        init {
            //Select this view when it is clicked
            view.setOnClickListener {
                parentAdapter.selectView(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = ColorView(WidthSquareView(parent.context))
        checkSelection(view)
        return ColorViewHolder(view, this)
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
            view.setBackgroundColor(getColor(view.context, android.R.color.white))
            selectedView = view
        } else {
            view.setBackgroundColor(getColor(view.context, android.R.color.transparent))
        }
    }

    /**
     * Marks the color of the given view as the currently selected color, and the given view as the currently
     * selected view. Updates the background of the given view and the previously selected view according to their new
     * selection status.
     */
    fun selectView(view: ColorView){
        //Change view background
        view.setBackgroundColor(getColor(view.context, android.R.color.white))

        //Change the background of the previously selected view (un-select it)
        selectedView?.setBackgroundColor(getColor(view.context, android.R.color.transparent))

        //If this view is the same as the previously selected view, then this view was just de-selected
        if(view.getColor() == selectedColor){
            deselect()
        } else {
            //Set the selected view and color to match this view
            selectedView = view
            selectedColor = view.getColor()

            //A selection has been made - make sure the options panel is visible
            panel.visibility = VISIBLE

            //Update the displays to match this color
            textDisplay.text = String.format("#%06X", 0xFFFFFF and selectedColor!!)
            colorWindow.setBackgroundColor(selectedColor!!)
        }
    }

    /**
     * If there is a currently selected view, de-select it.
     * Hide the button panel (delete, select, etc.) since no selection is made
     */
    fun deselect(){
        selectedColor = null
        if(selectedView != null){
            val prevSelectedView = selectedView!!
            selectedView = null
            checkSelection(prevSelectedView)
        }

        //No selection is made - hide the options panel
        panel.visibility = GONE
    }
}
