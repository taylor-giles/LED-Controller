package giles.ledcontroller

import java.io.Serializable

class Pattern(
    var name: String,
    var layers: ArrayList<Layer> = ArrayList()
) : Comparable<Pattern>, Serializable {
    var duration: Int = 0

    override fun compareTo(other: Pattern): Int {
        return when {
            name != other.name -> name.compareTo(other.name)
            else -> layers.hashCode().compareTo(other.layers.hashCode())
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Pattern &&
                name == other.name &&
                layers == other.layers
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + layers.hashCode()
        return result
    }
}
