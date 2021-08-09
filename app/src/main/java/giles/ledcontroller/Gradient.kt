package giles.ledcontroller

import android.graphics.Color
import java.io.Serializable

class Gradient @JvmOverloads constructor (
    var name: String,
    val colors: IntArray = IntArray(0),
    val positions: FloatArray
) : Serializable, Comparable<Gradient>{

    init{
        //Ensure that there is exactly one position per color
        if(colors.size != positions.size){
            throw IllegalArgumentException("Number of positions must match number of colors")
        }
    }

    /**
     * Constructor for generating the positions of the colors equally spaced apart
     */
    constructor(name: String, colors: IntArray = IntArray(0)): this(name, colors, FloatArray(colors.size)){
        val spacing: Float = 1f / colors.size
        for(i: Int in colors.indices){
            positions[i] = spacing * i
        }
    }


    /**
     * Determines and returns the color at a specific position [0..1] on the gradient
     */
    fun colorAtPosition(pos: Float): Int {
        return when(colors.size){
            0 -> 0
            1 -> colors[0]
            else -> {
                //Determine which positions the desired position is between
                var lowerPos = 0f
                var upperPos = 1f
                for(position: Float in positions){
                    if(position < pos && position > lowerPos){
                        lowerPos = position
                    }
                    if(position > pos && position < upperPos){
                        upperPos = position
                    }
                }

                //Calculate where the desired position lies as a percentage between the lower and upper
                val relPos: Float = (pos - lowerPos) / (upperPos - lowerPos)

                //Get closest colors (lower and upper)
                val lowerColor = colors[positions.asList().indexOf(lowerPos)]
                val upperColor = colors[positions.asList().indexOf(upperPos)]

                //Get color components at desired position
                val alpha = Color.alpha(lowerColor) + relPos * (Color.alpha(upperColor) - Color.alpha(lowerColor))
                val red = Color.red(lowerColor) + relPos * (Color.red(upperColor) - Color.red(lowerColor))
                val green = Color.green(lowerColor) + relPos * (Color.green(upperColor) - Color.green(lowerColor))
                val blue = Color.blue(lowerColor) + relPos * (Color.blue(upperColor) - Color.blue(lowerColor))

                //Build and return color
                Color.argb(alpha, red, green, blue)
            }
        }
    }


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

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + colors.contentHashCode()
        result = 31 * result + positions.contentHashCode()
        return result
    }
}