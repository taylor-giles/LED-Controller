package giles.ledcontroller

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.ColorView
import giles.util.ColorUtils
import giles.views.WidthSquareView
import kotlinx.android.synthetic.main.fragment_saved_colors.*

/**
 * A simple [Fragment] subclass.
 * Use the [SavedColorsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
const val COLOR_VIEW_SIDE_LENGTH_DP = 95.0
class SavedColorsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_colors, container, false)

        //Get components
        val optionsPanel = view.findViewById<View>(R.id.layout_saved_colors_buttons)
        val selectedColorTextDisplay = view.findViewById<TextView>(R.id.text_selected_saved_color_display)
        val selectedColorWindow = view.findViewById<View>(R.id.view_selected_saved_color_window)
        val savedColorsList = view.findViewById<RecyclerView>(R.id.layout_saved_colors)
        val deleteButton = view.findViewById<Button>(R.id.btn_delete_saved_color)
        val selectButton = view.findViewById<Button>(R.id.btn_select_saved_color)
        val addButton = view.findViewById<FloatingActionButton>(R.id.fab_add_saved_color)

        //Hide the options panel until a selection is made
        optionsPanel.visibility = View.GONE

        //Get the maximum number of cols that can fit on the screen, where each col has preferred size COLOR_VIEW_SIDE_LENGTH_DP
        val screenWidthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        val maxCols = (screenWidthDp / COLOR_VIEW_SIDE_LENGTH_DP + 0.5).toInt()

        //Set up the RecyclerView with a GridLayoutManager showing all colors in squares, sorted by hue
        val sortedSavedColors = AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray()
        savedColorsList.layoutManager = GridLayoutManager(view.context, maxCols)
        var adapter = ColorViewAdapter(sortedSavedColors, selectedColorTextDisplay, selectedColorWindow, optionsPanel)
        savedColorsList.adapter = adapter

        //Set the delete button action (confirmation dialog and delete behavior)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(view.context)
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
                    savedColorsList.adapter = adapter

                    //De-select view in adapter
                    adapter.deselect()
                }
                .setNegativeButton(android.R.string.cancel, null).show()
        }

        //Set the select button action (store the selected color and return to previous activity)
        selectButton.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(getString(R.string.EXTRA_COLOR), adapter.selectedColor)
            requireActivity().setResult(RESULT_OK, returnIntent)
            requireActivity().finish()
        }

        //Set the add button action (color picker dialog and add selected color)
        addButton.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)

            //Create dialog to display color picker
            val dialogPicker = ColorPickerView(view.context, false)
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
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SavedColorsFragment.
         */
        @JvmStatic
        fun newInstance() = SavedColorsFragment()
    }
}



/**
 * A RecyclerView adapter that creates square views to display colors
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
            panel.visibility = View.VISIBLE

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
        panel.visibility = View.GONE
    }
}