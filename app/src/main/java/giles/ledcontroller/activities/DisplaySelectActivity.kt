package giles.ledcontroller.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import giles.ledcontroller.AppData
import giles.ledcontroller.R
import giles.ledcontroller.views.DisplayView
import kotlinx.android.synthetic.main.activity_display_select.*
import kotlinx.android.synthetic.main.layout_selected_display_preview.view.*

class DisplaySelectActivity : AppCompatActivity() {
    private lateinit var adapter: DisplayView.DisplayViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_select)

        //Set add button behavior
        val displayCreateIntent = Intent(this, DisplayEditActivity::class.java)
        val addButton = fab_add_display
        addButton.setOnClickListener {
            startActivity(displayCreateIntent)
        }

        //Set up the preview dialog which shows when the user clicks on a display
        val previewDialogView = layoutInflater.inflate(R.layout.layout_selected_display_preview, ConstraintLayout(this), false)
        val previewDialog: AlertDialog = AlertDialog.Builder(this)
            //Select button
            .setPositiveButton(R.string.select_display) { _, _ ->
                AppData.currentDisplay = adapter.selectedDisplay!!
                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setView(previewDialogView)
            .setOnDismissListener {
                //When the dialog is dismissed, force the adapter to deselect all views
                adapter.deselect()
            }
            .create()

        //Set up RecyclerView
        adapter = DisplayView.DisplayViewAdapter(AppData.savedDisplays) { clickedView ->
            //Select the clicked view
            adapter.selectView(clickedView as DisplayView)

            //Update the information in the preview display
            previewDialogView.text_display_preview_name.text =
                adapter.selectedDisplay!!.name
            previewDialogView.text_display_preview_num_lights.text =
                adapter.selectedDisplay!!.numLights.toString()
            previewDialogView.text_display_preview_device.text =
                adapter.selectedDisplay!!.device.name
            previewDialog.setTitle(getString(R.string.display))

            //Set the behavior of the preview dialog delete button
            previewDialogView.btn_delete_selected_display.setOnClickListener {
                //Show a confirmation dialog
                AlertDialog.Builder(this)
                    .setTitle(R.string.remove_display)
                    .setMessage(
                        "Are you sure you want to remove the display called " +
                                adapter.selectedDisplay?.name + " from your saved displays?"
                    )
                    .setPositiveButton(R.string.delete) { _, _ ->
                        //Delete the currently selected display and update the list
                        AppData.savedDisplays.remove(adapter.selectedDisplay!!)
                        updateAdapter()

                        //Dismiss the dialog
                        previewDialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            //Set the behavior of the preview dialog edit button
            previewDialogView.btn_edit_selected_display.setOnClickListener {
                //Open the DisplayEditActivity to edit this display
                val displayEditIntent = Intent(this, DisplayEditActivity::class.java)
//                displayEditIntent.putExtra(
//                    getString(R.string.EXTRA_DISPLAY),
//                    adapter.selectedDisplay
//                )
                startActivity(displayEditIntent)
                previewDialog.dismiss()
            }

            //Display the dialog
            previewDialog.show()
        }
        recycler_display_select.adapter = adapter
        val manager = LinearLayoutManager(this)
        manager.isAutoMeasureEnabled = false
        recycler_display_select.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume(){
        super.onResume()
        updateAdapter()
    }

    private fun updateAdapter(){
        //Update adapter
        adapter.dataSet = AppData.savedDisplays
        adapter.notifyDataSetChanged()
    }
}