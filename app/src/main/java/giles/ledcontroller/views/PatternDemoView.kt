package giles.ledcontroller.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import giles.ledcontroller.MILLIS_BETWEEN_FRAMES
import giles.ledcontroller.Pattern
import kotlin.concurrent.thread

class PatternDemoView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    init {
        this.orientation = HORIZONTAL
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 100)
        this.setBackgroundColor(Color.WHITE)
    }

    /**
     * Run the demo by changing the colors of the views in the layout based on the frame matrix
     */
    fun demo(pattern: Pattern, numLights: Int){
        val frameMatrix = pattern.generateFrameMatrix(numLights)

        //Create thread to run demo until this view is no longer on the screen
        thread{
            val views = ArrayList<View>()
            this.removeAllViews()

            //Build the views to be shown and add them to the layout
            for(i in 0 until numLights){
                val view = View(context)
                view.layoutParams = LayoutParams(1, LayoutParams.MATCH_PARENT, 1f)
                views.add(view)
                this.addView(view)
            }

            Log.d("-----------------", "Here in the demo thread!")
            do {
                for (frame in frameMatrix) {
                    for (light in 0 until numLights) {
                        views[light].setBackgroundColor(frame[light])
                    }
                    Thread.sleep(MILLIS_BETWEEN_FRAMES.toLong())
                    if (!this.isShown) {
                        break
                    }
                }
            }while(this.isShown)
        }
    }
}