package giles.ledcontroller

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import giles.ledcontroller.views.GradientRectView
import giles.ledcontroller.views.GradientView
import giles.ledcontroller.views.GradientViewAdapter
import kotlinx.android.synthetic.main.item_gradient.view.*
import kotlinx.android.synthetic.main.layout_selected_color_preview.view.*
import kotlinx.android.synthetic.main.layout_selected_gradient_preview.*
import kotlinx.android.synthetic.main.layout_selected_gradient_preview.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [SavedGradientsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedGradientsFragment : Fragment() {

    private lateinit var savedGradientsList: RecyclerView
    private lateinit var adapter: GradientViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_saved_gradients, container, false)

        //Get components
        savedGradientsList = fragmentView.findViewById(R.id.layout_saved_gradients)
        val addButton = fragmentView.findViewById<FloatingActionButton>(R.id.fab_add_saved_gradient)

        //Set the add button action (open the GradientEditActivity for a new gradient)
        val gradientEditIntent = Intent(requireActivity(), GradientEditActivity::class.java)
        addButton.setOnClickListener{
            startActivityForResult(gradientEditIntent, R.integer.EDIT_GRADIENT_REQUEST)
        }

        //Set up the preview dialog which shows when the user clicks on a gradient
        val previewDialogView = layoutInflater.inflate(R.layout.layout_selected_gradient_preview, container, false)
        val previewDialog: AlertDialog = AlertDialog.Builder(fragmentView.context)
            //Select button
            .setPositiveButton(R.string.select_gradient) {_, _ ->
                //Return from this activity with the selected gradient as an extra
                val returnIntent = Intent()
                returnIntent.putExtra(getString(R.string.EXTRA_GRADIENT), adapter.selectedGradient)
                requireActivity().setResult(Activity.RESULT_OK, returnIntent)
                requireActivity().finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setView(previewDialogView)
            .setOnDismissListener {
                //When the dialog is dismissed, force the adapter to deselect all views
                adapter.deselect()
            }
            .create()

        //Set up the RecyclerView with a LinearLayoutManager showing all gradients, sorted alphabetically by name
        val sortedSavedGradients = ArrayList(AppData.savedGradients.sortedBy{ gradient -> gradient.name })
        savedGradientsList.layoutManager = LinearLayoutManager(requireActivity())
        adapter = GradientViewAdapter(sortedSavedGradients) { clickedView ->
            //Select the clicked view
            adapter.selectView(clickedView as GradientView)

            //Create a GradientRectView to show in the preview dialog
            val gradientPreview = GradientRectView(clickedView.context)
            previewDialogView.preview_selected_gradient.addView(gradientPreview)

            //Assign a gradient to the display preview and title
            gradientPreview.displayGradient(adapter.selectedGradient)
            previewDialog.setTitle(adapter.selectedGradient?.name)

            //Set the behavior of the preview dialog delete button
            previewDialogView.btn_delete_selected_gradient.setOnClickListener {
                //Show a confirmation dialog
                AlertDialog.Builder(fragmentView.context)
                    .setTitle(R.string.remove_gradient)
                    .setMessage("Are you sure you want to delete the gradient called " +
                            adapter.selectedGradient?.name + "?")
                    .setPositiveButton(R.string.delete) { _, _ ->
                        //Delete the currently selected gradient and update the list
                        AppData.savedGradients.remove(adapter.selectedGradient!!)
                        updateAdapter()

                        //Dismiss the dialog
                        previewDialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            //Set the behavior of the preview dialog edit button
            previewDialogView.btn_edit_selected_gradient.setOnClickListener {
                //Open the GradientEditActivity to edit this gradient
                gradientEditIntent.putExtra(getString(R.string.EXTRA_GRADIENT), adapter.selectedGradient)
                startActivityForResult(gradientEditIntent, R.integer.EDIT_GRADIENT_REQUEST)
            }

            //Display the dialog
            previewDialog.show()
        }
        savedGradientsList.adapter = adapter

        return fragmentView
    }

    override fun onResume(){
        super.onResume()
        updateAdapter()
    }

    private fun updateAdapter(){
        //Update adapter
        adapter.dataSet = ArrayList(AppData.savedGradients.sortedBy{ gradient -> gradient.name })
        adapter.notifyDataSetChanged()
        adapter.deselect()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SavedGradientsFragment.
         */
        @JvmStatic
        fun newInstance() = SavedGradientsFragment()
    }
}