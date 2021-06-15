package giles.ledcontroller

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.views.GradientRectView
import kotlinx.android.synthetic.main.activity_gradient_edit.*
import kotlinx.android.synthetic.main.item_add_color_button.view.*
import kotlinx.android.synthetic.main.item_gradient_color.view.*


const val DEFAULT_COLOR_ID = android.R.color.black
class GradientEditActivity : AppCompatActivity() {

    private lateinit var gradientView: GradientRectView
    private val gradientColors = ArrayList<Int>()
    private lateinit var adapter: GradientColorViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_edit)

        //Put the gradient view into the preview frame
        gradientView = GradientRectView(this)
        gradientView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout_gradient_preview.addView(gradientView)

        //Set up RecyclerView to display colors and open activity to choose new color when "Add Color" button is clicked
        layout_gradient_colors.layoutManager = LinearLayoutManager(this)
        adapter = GradientColorViewAdapter(gradientColors, View.OnClickListener {
            val savedColorsIntent = Intent(this, SavedColorsActivity::class.java)
            startActivityForResult(savedColorsIntent, resources.getInteger(R.integer.CHOOSE_COLOR_REQUEST))
        })
        layout_gradient_colors.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_OK -> {
                when(requestCode){
                    //Choose color request - add the chosen color to the gradient
                    resources.getInteger(R.integer.CHOOSE_COLOR_REQUEST) -> {
                        val chosenColor = data!!.getIntExtra(getString(R.string.EXTRA_COLOR), 0)
                        gradientColors.add(chosenColor)
                        adapter.notifyItemInserted(gradientColors.indexOf(chosenColor))
                        gradientView.setGradientColors(gradientColors.toIntArray())
                    }
                }
            }
        }
    }
}


/**
 * A RecyclerView adapter for representing colors used in the construction of a gradient
 */
//View types
const val COLOR_TYPE = 1
const val BUTTON_TYPE = 2

class GradientColorViewAdapter(
    private val dataSet: ArrayList<Int>,
    private val addButtonListener: View.OnClickListener
): RecyclerView.Adapter<GradientColorViewAdapter.GradientColorViewHolder>(){

    /**
     * A ViewHolder for views displaying gradient colors and the button used to add a new color to the gradient
     */
    class GradientColorViewHolder constructor(
        val view: View,
        val color: Int = getColor(view.context, DEFAULT_COLOR_ID)
    ) : RecyclerView.ViewHolder(view){

        private val colorPreview: View? = view.view_gradient_color
        private val textDisplay: TextView? = view.text_gradient_color
        val addColorButton: View? = view.text_btn_add_color

        fun setColor(color: Int){
            colorPreview!!.setBackgroundColor(color)
            textDisplay!!.text = String.format("#%06X", 0xFFFFFF and color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientColorViewHolder {
        //If the type of this viewHolder is BUTTON_TYPE, then use the button layout, otherwise use the gradient color layout
        val layoutID = if(viewType == BUTTON_TYPE) R.layout.item_add_color_button else R.layout.item_gradient_color
        return GradientColorViewHolder(LayoutInflater.from(parent.context).inflate(layoutID, parent, false))
    }

    override fun onBindViewHolder(holder: GradientColorViewHolder, position: Int) {
        if(holder.itemViewType == BUTTON_TYPE){
            holder.addColorButton!!.setOnClickListener(addButtonListener)
        } else {
            //Set color of the view
            holder.setColor(dataSet[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dataSet.size) BUTTON_TYPE else COLOR_TYPE
    }

    override fun getItemCount() = dataSet.size + 1 //There is an item for every value in the dataset, plus one for the add button
}
