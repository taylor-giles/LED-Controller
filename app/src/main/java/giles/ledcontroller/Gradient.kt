package giles.ledcontroller

import android.content.Context
import giles.ledcontroller.views.GradientRectView
import java.io.Serializable

class Gradient @JvmOverloads constructor (
    var name: String,
    var colors: IntArray? = null,
    var positions: FloatArray? = null
) : Serializable, Comparable<Gradient>{
    override fun compareTo(other: Gradient): Int {
        return when {
            name != other.name -> name.compareTo(other.name)
            positions.contentEquals(other.positions) -> colors.hashCode().compareTo(other.colors.hashCode())
            else -> positions.hashCode().compareTo(other.positions.hashCode())
        }
    }

    //TODO: function to get color at specified arbitrary position, between 1 and 0
}