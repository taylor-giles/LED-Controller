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
import giles.ledcontroller.views.ControllerView
import kotlinx.android.synthetic.main.activity_controller_select.*
import kotlinx.android.synthetic.main.layout_selected_controller_preview.view.*

class ControllerSelectActivity : AppCompatActivity() {
    private lateinit var adapter: ControllerView.ControllerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller_select)

        //Set add button behavior
        val controllerCreateIntent = Intent(this, ControllerEditActivity::class.java)
        val addButton = fab_add_controller
        addButton.setOnClickListener {
            startActivity(controllerCreateIntent)
        }

        //Set up the preview dialog which shows when the user clicks on a controller
        val previewDialogView = layoutInflater.inflate(R.layout.layout_selected_controller_preview, ConstraintLayout(this), false)
        val previewDialog: AlertDialog = AlertDialog.Builder(this)
            //Select button
            .setPositiveButton(R.string.select_controller) { _, _ ->
                AppData.currentController = adapter.selectedController!!
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
        adapter = ControllerView.ControllerViewAdapter(AppData.savedControllers) { clickedView ->
            //Select the clicked view
            adapter.selectView(clickedView as ControllerView)

            //Update the information in the preview controller
            previewDialogView.text_controller_preview_name.text =
                adapter.selectedController!!.name
            previewDialogView.text_controller_preview_num_lights.text =
                adapter.selectedController!!.numLights.toString()
            previewDialogView.text_controller_preview_device.text =
                adapter.selectedController!!.device.name
            previewDialog.setTitle(getString(R.string.controller))

            //Set the behavior of the preview dialog delete button
            previewDialogView.btn_delete_selected_controller.setOnClickListener {
                //Show a confirmation dialog
                AlertDialog.Builder(this)
                    .setTitle(R.string.remove_controller)
                    .setMessage(
                        "Are you sure you want to remove the controller called " +
                                adapter.selectedController?.name + " from your saved controllers?"
                    )
                    .setPositiveButton(R.string.delete) { _, _ ->
                        //Delete the currently selected controller and update the list
                        AppData.savedControllers.remove(adapter.selectedController!!)
                        updateAdapter()

                        //Dismiss the dialog
                        previewDialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            //Set the behavior of the preview dialog edit button
            previewDialogView.btn_edit_selected_controller.setOnClickListener {
                //Open the ControllerEditActivity to edit this controller
                val controllerEditIntent = Intent(this, ControllerEditActivity::class.java)
//                controllerEditIntent.putExtra(
//                    getString(R.string.EXTRA_CONTROLLER),
//                    adapter.selectedDisplay
//                )
                startActivity(controllerEditIntent)
                previewDialog.dismiss()
            }

            //Display the dialog
            previewDialog.show()
        }
        recycler_controller_select.adapter = adapter
        val manager = LinearLayoutManager(this)
        manager.isAutoMeasureEnabled = false
        recycler_controller_select.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume(){
        super.onResume()
        updateAdapter()
    }

    private fun updateAdapter(){
        //Update adapter
        adapter.dataSet = AppData.savedControllers
        adapter.notifyDataSetChanged()
    }
}