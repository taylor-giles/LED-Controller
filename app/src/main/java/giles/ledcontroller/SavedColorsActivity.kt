package giles.ledcontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.AppData.savedColors
import giles.ledcontroller.views.ColorView
import giles.views.SquareView
import kotlinx.android.synthetic.main.activity_saved_colors.*

const val COLOR_VIEW_SIDE_LENGTH_DP = 85.0
class SavedColorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_colors)

        //Get the maximum number of cols that can fit on the screen, where each col has preferred size COLOR_VIEW_SIDE_LENGTH_DP
        val screenWidthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        val maxCols = (screenWidthDp / COLOR_VIEW_SIDE_LENGTH_DP + 0.5).toInt()

        //Set up the RecyclerView with a GridLayoutManager showing all colors in squares
        layout_saved_colors.layoutManager = GridLayoutManager(this, maxCols)
        layout_saved_colors.adapter = ColorViewAdapter(savedColors.toTypedArray())
    }
}


/**
 * A RecyclerView adapter that creates framed square views to display chosen colors
 */
class ColorViewAdapter(private val dataSet: Array<Int>):
    RecyclerView.Adapter<ColorViewAdapter.ColorViewHolder>(){
    var selectedView: ColorView? = null
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
        val view = ColorView(SquareView(parent.context))
        checkSelection(view)
        return ColorViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        //Set color and selection status display of the view
        holder.view.setColor(dataSet[position])
        checkSelection(holder.view)
    }

    override fun getItemCount() = dataSet.size


    /**
     * Determines whether or not the current ColorView should be selected and updates its background accordingly.
     * Also updates the selectedView variable to ensure that the correct color stays selected even when it gets
     * recycled to a different view.
     */
    private fun checkSelection(view: ColorView){
        if(view.getColor() == selectedColor){
            view.setBackgroundColor(getColor(view.context, android.R.color.white))
            selectedView = view
        } else {
            view.setBackgroundColor(getColor(view.context, android.R.color.transparent))
        }
    }

    fun selectView(view: ColorView){
        //Change view background
        view.setBackgroundColor(getColor(view.context, android.R.color.white))

        //Change the background of the previously selected view (un-select it)
        selectedView?.setBackgroundColor(getColor(view.context, android.R.color.transparent))

        //If this view is the same as the previously selected view, then this view was just de-selected
        if(view.getColor() == selectedColor){
            selectedView = null
            selectedColor = null
        } else {
            //Set the selected view and color to match this view
            selectedView = view
            selectedColor = view.getColor()
        }
    }
}
