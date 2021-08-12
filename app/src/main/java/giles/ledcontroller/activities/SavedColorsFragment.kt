package giles.ledcontroller.activities

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import giles.ledcontroller.AppData
import giles.ledcontroller.R
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.ColorView
import giles.ledcontroller.views.ColorViewAdapter
import giles.util.ColorUtils
import kotlinx.android.synthetic.main.layout_selected_color_preview.view.*

/**
 * A [Fragment] subclass to display saved colors.
 * Use the [SavedColorsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedColorsFragment : Fragment() {

    private val colorViewSideLengthDp = 95.0
    private lateinit var adapter: ColorViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_saved_colors, container, false)

        //Get components
        val savedColorsList = fragmentView.findViewById<RecyclerView>(R.id.layout_saved_colors)
        val addButton = fragmentView.findViewById<FloatingActionButton>(R.id.fab_add_saved_color)

        //Get the maximum number of cols that can fit on the screen, where each col has the preferred size set by the sideLength val
        val screenWidthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        val maxCols = (screenWidthDp / colorViewSideLengthDp + 0.5).toInt()

        //Set up the preview dialog which shows when the user clicks on a color
        val previewDialogView = layoutInflater.inflate(R.layout.layout_selected_color_preview, container, false)
        val previewDialog: AlertDialog = AlertDialog.Builder(fragmentView.context)
            //Select button
            .setPositiveButton(R.string.select_color) { _, _ ->
                //Return from this activity with the selected color as an extra
                val returnIntent = Intent()
                returnIntent.putExtra(getString(R.string.EXTRA_COLOR), adapter.selectedColor)
                requireActivity().setResult(RESULT_OK, returnIntent)
                requireActivity().finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setView(previewDialogView)
            .setOnDismissListener {
                //When the dialog is dismissed, force the adapter to deselect all views
                adapter.deselect()
            }
            .create()

        //Set up the RecyclerView with a GridLayoutManager showing all colors in squares, sorted by hue
        val sortedSavedColors = AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray()
        savedColorsList.layoutManager = GridLayoutManager(fragmentView.context, maxCols)
        adapter = ColorViewAdapter(sortedSavedColors, false) { clickedView ->
            //Select the clicked view
            adapter.selectView(clickedView as ColorView)

            //Set the preview dialog color and title
            previewDialogView.view_color_preview_window.setBackgroundColor(adapter.selectedColor!!)
            previewDialog.setTitle(String.format("#%06X", 0xFFFFFF and adapter.selectedColor!!))

            //Set the behavior of the preview dialog delete button
            previewDialogView.btn_remove_saved_color.setOnClickListener {
                //Show a confirmation dialog
                AlertDialog.Builder(fragmentView.context)
                    .setTitle(R.string.remove_color)
                    .setMessage("Are you sure you want to remove " +
                            String.format("#%06X", 0xFFFFFF and adapter.selectedColor!!) + " from your saved colors list?")
                    .setPositiveButton(R.string.remove) { _, _ ->
                        //Delete the currently selected color and update the list
                        AppData.savedColors.remove(adapter.selectedColor!!)
                        AppData.saveColors(this.requireContext())
                        updateAdapter()

                        //Dismiss the dialog
                        previewDialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            //Display the dialog
            previewDialog.show()
        }

        //Set the adapter
        savedColorsList.adapter = adapter

        //Set the add button action (color picker dialog and add selected color)
        addButton.setOnClickListener {
            val builder = AlertDialog.Builder(fragmentView.context)

            //Create dialog to display color picker
            val dialogPicker = ColorPickerView(fragmentView.context, showSaveButton = false, showSavedColors = false)
            builder.setTitle("Save New Color")
                .setView(dialogPicker)
                .setPositiveButton(R.string.save_color) { _, _ ->
                    //Save the color and update the list
                    AppData.savedColors.add(dialogPicker.getColor())
                    AppData.saveColors(this.requireContext())
                    updateAdapter()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        return fragmentView
    }

    override fun onResume(){
        super.onResume()
        updateAdapter()
    }

    private fun updateAdapter(){
        //Re-make adapter dataset to update RecyclerView
        adapter.dataSet = AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray()
        adapter.notifyDataSetChanged()
        adapter.deselect()
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



