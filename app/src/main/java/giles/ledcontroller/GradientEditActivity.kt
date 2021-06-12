package giles.ledcontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.views.GradientRectView
import kotlinx.android.synthetic.main.activity_gradient_edit.*
import kotlinx.android.synthetic.main.item_gradient_color.view.*

class GradientEditActivity : AppCompatActivity() {

    private val gradientView = GradientRectView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_edit)

        //Put the gradient view into the preview frame
        gradientView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout_gradient_preview.addView(gradientView)

        //Set up RecyclerView to display colors and "Add Color" button footer
        layout_gradient_colors.layoutManager = LinearLayoutManager(this)
        //layout_gradient_colors.adapter = GradientColorViewAdapter(Array<Int>())
    }
}


/**
 * A RecyclerView adapter for representing colors used in the construction of a gradient
 */
class GradientColorViewAdapter(
    private val dataSet: Array<Int>
): RecyclerView.Adapter<GradientColorViewAdapter.GradientColorViewHolder>(){

    class GradientColorViewHolder constructor(
        val view: View
    ) : RecyclerView.ViewHolder(view) {
        val colorPreview = view.view_gradient_color
        val textDisplay = view.text_gradient_color

        init {
            setColor(getColor(view.context, android.R.color.black))
        }

        fun setColor(color: Int){
            colorPreview.setBackgroundColor(color)
            textDisplay.text = String.format("#%06X", 0xFFFFFF and color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientColorViewHolder {
        return GradientColorViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_gradient_color, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GradientColorViewHolder, position: Int) {
        //Set color of the view
        holder.setColor(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}
