package giles.ledcontroller

import android.graphics.Color
import java.io.Serializable

class Pattern(
    var name: String,
    var layers: ArrayList<Layer> = ArrayList()
) : Comparable<Pattern>, Serializable {
    var duration: Int = 0
    private var length = 0

    //This is necessary since lambdas are not serializable
    companion object {
        class PatternComparator: Serializable, Comparator<Pattern>{
            override fun compare(o1: Pattern, o2: Pattern): Int {
                return o1.compareTo(o2)
            }
        }
    }

    init {
        //Determine the number of frames in the longest effect
        for(layer: Layer in layers){
            if(layer.effect.numFrames > length){
                length = layer.effect.numFrames
            }
        }

        //Calculate the duration of this pattern based on the length in frames
        duration = (length * MILLIS_BETWEEN_FRAMES) / 1000
    }

    fun generateFrameMatrix(totalNumLights: Int): List<IntArray>{
        //Start with a solid black base layer on all frames
        val output = ArrayList<IntArray>()
        for(i: Int in 0 until length){
            output.add(IntArray(totalNumLights))
        }

        //Get the frame matrices for all effects
        val effectMatrices = ArrayList<List<IntArray>>()
        for(layer: Layer in layers.reversed()){
            effectMatrices.add(layer.effect.generateFrameMatrix(layer.lights, totalNumLights))
        }

        //For every frame, iteratively blend all the lights in the corresponding frame of all effects
        for(frameNum: Int in 0 until length){
            for(effectMatrix: List<IntArray> in effectMatrices){
                val frame = effectMatrix[frameNum % effectMatrix.size]
                for(light: Int in 0 until totalNumLights){
                    val alpha = Color.alpha(frame[light]).toFloat() / 255f
                    val red = Color.red(output[frameNum][light]).toFloat() * (1f-alpha) + Color.red(frame[light]).toFloat() * alpha
                    val green = Color.green(output[frameNum][light]).toFloat() * (1f-alpha) + Color.green(frame[light]).toFloat() * alpha
                    val blue = Color.blue(output[frameNum][light]).toFloat() * (1f-alpha) + Color.blue(frame[light]).toFloat() * alpha
                    output[frameNum][light] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
                }
            }
        }
        return output
    }

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
