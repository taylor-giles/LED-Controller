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
            !colors.contentEquals(other.colors) -> colors.contentHashCode().compareTo(other.colors.contentHashCode())
            else -> positions.contentHashCode().compareTo(other.positions.contentHashCode())
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Gradient &&
                name == other.name &&
                colors.contentEquals(other.colors) &&
                positions.contentEquals(other.positions)
    }

    //TODO: function to get color at specified arbitrary position, between 1 and 0
}