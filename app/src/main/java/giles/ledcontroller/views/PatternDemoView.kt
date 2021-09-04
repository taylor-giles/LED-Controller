package giles.ledcontroller.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import giles.ledcontroller.MILLIS_BETWEEN_FRAMES
import giles.ledcontroller.Pattern


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
        val views = ArrayList<View>()
        this.removeAllViews()

        //Build the views to be shown and add them to the layout
        for(i in 0 until numLights){
            val view = View(context)
            view.layoutParams = LayoutParams(1, LayoutParams.MATCH_PARENT, 1f)
            views.add(view)
            this.addView(view)
        }

        //Create thread to run demo until this view is no longer on the screen
        val thread = Thread {
            do {
                //Generate matrix
                val frameMatrix = pattern.generateFrameMatrix(numLights)

                //Iterate over frames
                for (frame in frameMatrix) {
                    //Iterate over lights in frame
                    for (light in 0 until numLights) {
                        //Change color of view corresponding to light
                        views[light].post{ views[light].setBackgroundColor(frame[light]) }
                    }

                    //Wait
                    try {
                        Thread.sleep(MILLIS_BETWEEN_FRAMES.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    if (!this.isShown) {
                        break
                    }
                }
            } while(this.isShown)
        }
        thread.start()
    }
}