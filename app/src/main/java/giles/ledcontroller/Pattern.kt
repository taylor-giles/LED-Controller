package giles.ledcontroller

import android.graphics.Color
import java.io.Serializable

class Pattern(
    var name: String,
    var layers: ArrayList<Layer> = ArrayList()
) : Comparable<Pattern>, Serializable {
    var duration: Int = 0

    fun generateFrameMatrix(totalNumLights: Int): List<IntArray>{
        //Determine the number of frames in the longest effect
        var length = 0
        for(layer: Layer in layers){
            if(layer.effect.numFrames > length){
                length = layer.effect.numFrames
            }
        }
        duration = (length * MILLIS_BETWEEN_FRAMES) / 1000

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
                    val alpha = Color.alpha(frame[light]) / 255
                    val red = Color.red(output[frameNum][light]) * (1-alpha) + Color.red(frame[light]) * alpha
                    val green = Color.green(output[frameNum][light]) * (1-alpha) + Color.green(frame[light]) * alpha
                    val blue = Color.blue(output[frameNum][light]) * (1-alpha) + Color.blue(frame[light]) * alpha
                    output[frameNum][light] = Color.rgb(red, green, blue)
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
