package giles.ledcontroller.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import giles.ledcontroller.AppData
import giles.ledcontroller.R
import giles.ledcontroller.views.GradientView
import giles.ledcontroller.views.GradientViewAdapter

class GradientSelectionActivity : AppCompatActivity() {

    private lateinit var addButton: FloatingActionButton
    private lateinit var gradientsList: RecyclerView
    private lateinit var adapter: GradientViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_selection)

        //Get components
        gradientsList = this.findViewById(R.id.layout_gradients_to_select)
        addButton = this.findViewById(R.id.fab_add_gradient_from_select)

        //Set the add button action (open the GradientEditActivity for a new gradient)
        val gradientEditIntent = Intent(this, GradientEditActivity::class.java)
        addButton.setOnClickListener{
            startActivity(gradientEditIntent)
        }

        //Set up the RecyclerView with a LinearLayoutManager showing all gradients, sorted alphabetically by name
        gradientsList.layoutManager = LinearLayoutManager(this)
        adapter = GradientViewAdapter(AppData.savedGradients) { clickedView ->
            //Select the clicked view
            adapter.selectView(clickedView as GradientView)

            //Return from this activity with the selected gradient as an extra
            val returnIntent = Intent()
            returnIntent.putExtra(getString(R.string.EXTRA_GRADIENT), adapter.selectedGradient)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        gradientsList.adapter = adapter
    }

    override fun onResume(){
        super.onResume()
        updateAdapter()
    }

    private fun updateAdapter(){
        //Update adapter
        adapter.dataSet = AppData.savedGradients
        adapter.notifyDataSetChanged()
        adapter.deselect()
    }
}