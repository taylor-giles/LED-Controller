package giles.ledcontroller

class Light(
    val display: LEDDisplay,
    val index: Int
) : Comparable<Light> {
    init {
        //Bounds checking on index
        if(index > display.numLights){
            throw IllegalArgumentException("Index of light cannot be greater than number of lights")
        } else if(index < 0){
            throw IllegalArgumentException("Index cannot be negative")
        }
    }

    /**
     * Two [Light]s are equal if their displays are the same and their indices are the same
     */
    override fun equals(other: Any?): Boolean {
        return other is Light &&
                other.display == this.display &&
                other.index == this.index
    }

    /**
     * A [Light]'s [hashCode] is its index appended to the [hashCode] of its display
     */
    override fun hashCode(): Int {
        return (display.hashCode().toString() + index.toString()).toInt()
    }

    /**
     * A [Light] is said to be "less than" another light if it comes earlier in the total light display array.
     * A [Light] comes earlier than another light if it is in the same display at a lower index, or
     * in an earlier display.
     */
    override fun compareTo(other: Light): Int {
        return when (this.display) {
            other.display -> this.index - other.index
            else -> this.display.compareTo(other.display)
        }
    }
}