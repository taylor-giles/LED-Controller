package giles.ledcontroller

import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.AppData.savedColors
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
        layout_saved_colors.adapter = ColorViewAdapter(savedColors.toTypedArray().sortedArray())
    }
}


class ColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle){

    private var view = SquareView(context)
    private var title = TextView(context)

    init {
        this.setPaddingRelative(25, 25, 25, 25)
        title.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
        title.textAlignment = View.TEXT_ALIGNMENT_CENTER
        Log.d("Log", "Here")
    }

    constructor(view: SquareView): this(view.context){
        this.view = view
        addView(view)
        addView(title)
    }

    fun setColor(color: Int){
        view.setBackgroundColor(color)
        title.text = String.format("#%06X", 0xFFFFFF and color)
        title.typeface = Typeface.MONOSPACE
        title.setBackgroundColor(getColor(context, android.R.color.white))
        title.setTextColor(getColor(context, android.R.color.black))
        title.textSize = 14f
    }
}

/**
 * A RecyclerView adapter that creates framed square views to display chosen colors
 */
class ColorViewAdapter(private val dataSet: Array<Int>):
    RecyclerView.Adapter<ColorViewAdapter.ColorViewHolder>(){

    class ColorViewHolder constructor(
        val view: ColorView
    ) : RecyclerView.ViewHolder(view) {

        init{
            //Define click listener for the view
            view.setOnClickListener { Toast.makeText(view.context, "View has been clicked", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = SquareView(parent.context)
        return ColorViewHolder(ColorView(view))
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        //Set color of the view
        holder.view.setColor(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}
