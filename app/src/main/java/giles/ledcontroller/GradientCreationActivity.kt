package giles.ledcontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import giles.views.GradientRectView
import kotlinx.android.synthetic.main.activity_gradient_creation.*

class GradientCreationActivity : AppCompatActivity() {

    val gradientView = GradientRectView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_creation)

        //Put the gradient view into the preview frame
        gradientView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout_gradient_preview.addView(gradientView)

        //Set up RecyclerView to display colors and "Add Color" button footer
    }
}
