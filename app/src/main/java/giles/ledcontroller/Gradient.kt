package giles.ledcontroller

import android.content.Context
import giles.ledcontroller.views.GradientRectView
import java.io.Serializable

class Gradient @JvmOverloads constructor (
    var name: String,
    var colors: IntArray? = null,
    var positions: FloatArray? = null
) : Serializable{

    //TODO: function to get color at specified arbitrary position, between 1 and 0
}