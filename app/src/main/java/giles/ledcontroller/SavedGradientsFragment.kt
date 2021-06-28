package giles.ledcontroller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import giles.ledcontroller.views.GradientRectView
import kotlinx.android.synthetic.main.item_gradient.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [SavedGradientsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedGradientsFragment : Fragment() {
    private lateinit var savedGradientsList: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_gradients, container, false)

        //Get components
        savedGradientsList = view.findViewById(R.id.layout_saved_gradients)
        val addButton = view.findViewById<FloatingActionButton>(R.id.fab_add_saved_gradient)

        //Note: RecyclerView adapter is set in onResume()

        //Set the add button action (open the GradientEditActivity for a new gradient)
        val gradientEditIntent = Intent(requireActivity(), GradientEditActivity::class.java)
        addButton.setOnClickListener{
            startActivityForResult(gradientEditIntent, R.integer.EDIT_GRADIENT_REQUEST)
        }

        return view
    }

    override fun onResume(){
        super.onResume()

        //Set up the RecyclerView with a LinearLayoutManager showing all gradients, sorted alphabetically by name
        val sortedSavedGradients = ArrayList(AppData.savedGradients.sortedBy{ gradient -> gradient.name })
        savedGradientsList.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = GradientViewAdapter(sortedSavedGradients)
        savedGradientsList.adapter = adapter
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


/**
 * A RecyclerView adapter for displaying gradients
 */

class GradientViewAdapter(
    private val dataSet: ArrayList<Gradient>
): RecyclerView.Adapter<GradientViewAdapter.GradientViewHolder>(){

    /**
     * A ViewHolder for views displaying gradient colors and the button used to add a new color to the gradient
     */
    class GradientViewHolder constructor(
        val view: View
    ) : RecyclerView.ViewHolder(view){
        private val preview: GradientRectView

        init {
            //TODO: Select this view by bringing up selection dialog when clicked
            view.setOnClickListener {
                Toast.makeText(view.context, "View was clicked", Toast.LENGTH_SHORT).show()
            }

            //Add the gradient preview
            preview = GradientRectView(view.context)
            view.layout_gradient_item_preview.addView(preview)
        }

        fun setGradient(gradient: Gradient){
            view.text_gradient_item_name.text = gradient.name
            preview.displayGradient(gradient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientViewHolder {
        //Create a new ViewHolder for a view inflated from the item_gradient layout
        return GradientViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_gradient, parent, false))
    }

    override fun onBindViewHolder(holder: GradientViewHolder, position: Int) {
        //Set gradient of the view
        holder.setGradient(dataSet[position])
    }

    //There is an item for every value in the data set
    override fun getItemCount() = dataSet.size
}