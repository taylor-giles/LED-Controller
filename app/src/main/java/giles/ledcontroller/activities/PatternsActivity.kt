package giles.ledcontroller.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import giles.ledcontroller.*
import giles.ledcontroller.views.PatternView
import giles.ledcontroller.views.PatternViewAdapter
import kotlinx.android.synthetic.main.activity_patterns.*
import kotlinx.android.synthetic.main.layout_selected_pattern_preview.view.*

class PatternsActivity : AppCompatActivity() {

    private lateinit var adapter: PatternViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patterns)

        //Set add button behavior
        val patternCreateIntent = Intent(this, PatternEditActivity::class.java)
        val addButton = fab_add_pattern
        addButton.setOnClickListener {
            startActivity(patternCreateIntent)
        }

        //Set up the preview dialog which shows when the user clicks on a pattern
        val previewDialogView = layoutInflater.inflate(R.layout.layout_selected_pattern_preview, ConstraintLayout(this), false)
        val previewDialog: AlertDialog = AlertDialog.Builder(this)
            //Select button
            .setPositiveButton(R.string.select_pattern) { _, _ ->
                //Return from this activity with the selected pattern as an extra
                val returnIntent = Intent()
                returnIntent.putExtra(getString(R.string.EXTRA_PATTERN), adapter.selectedPattern)
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
        adapter = PatternViewAdapter(AppData.patterns) { clickedView ->
            //Select the clicked view
            adapter.selectView(clickedView as PatternView)

            //Update the information in the preview display
            previewDialogView.text_pattern_preview_number_layers.text = adapter.selectedPattern!!.layers.size.toString()
            previewDialogView.text_pattern_preview_duration.text = adapter.selectedPattern!!.duration.toString()
            previewDialog.setTitle(adapter.selectedPattern?.name)

            //Make the list of layers to be displayed
            var layersText = ""
            var counter = 0
            for(layer: Layer in adapter.selectedPattern!!.layers){
                val content = when(layer.effect){
                    is GradientEffect -> (layer.effect as GradientEffect).gradient.name
                    is ColorEffect -> String.format("#%06X", 0xFFFFFF and (layer.effect as ColorEffect).color)
                    else -> ""
                }
                if(counter < 5 || adapter.selectedPattern!!.layers.size == counter + 1){
                    layersText = layersText + "• " + layer.effect.title + " (" + content + ")\n"
                } else {
                    layersText = layersText +  "• +" + (adapter.selectedPattern!!.layers.size - counter).toString() + " more"
                    break
                }
                counter++
            }
            previewDialogView.findViewById<TextView>(R.id.text_pattern_preview_layers).text = layersText

            //Set the behavior of the preview dialog delete button
            previewDialogView.btn_delete_selected_pattern.setOnClickListener {
                //Show a confirmation dialog
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete_pattern)
                    .setMessage("Are you sure you want to delete the pattern called " +
                            adapter.selectedPattern?.name + "?")
                    .setPositiveButton(R.string.delete) { _, _ ->
                        //Delete the currently selected pattern and update the list
                        AppData.patterns.remove(adapter.selectedPattern!!)
                        updateAdapter()

                        //Dismiss the dialog
                        previewDialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            //Set the behavior of the preview dialog edit button
            previewDialogView.btn_edit_selected_pattern.setOnClickListener {
                //Open the PatternEditActivity to edit this pattern
                val patternEditIntent = Intent(this, PatternEditActivity::class.java)
                patternEditIntent.putExtra(getString(R.string.EXTRA_PATTERN), adapter.selectedPattern)
                startActivity(patternEditIntent)
                previewDialog.dismiss()
            }

            //Display the dialog
            previewDialog.show()
        }
        list_saved_patterns.layoutManager = LinearLayoutManager(this)
        list_saved_patterns.adapter = adapter
    }

    override fun onResume(){
        super.onResume()
        updateAdapter()
    }

    private fun updateAdapter(){
        //Update adapter
        adapter.dataSet = AppData.patterns
        adapter.notifyDataSetChanged()
    }
}

