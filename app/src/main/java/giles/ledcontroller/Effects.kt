package giles.ledcontroller

import java.io.Serializable

const val MILLIS_BETWEEN_FRAMES = 200
const val MIN_EFFECT_DURATION = 15 //Minimum duration of any effect, in seconds

enum class EffectDirection { START_TO_END, END_TO_START }

interface Effect : Serializable {
    val title: String
    val numFrames: Int
    fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray>
}
interface ColorEffect : Effect {
    val color: Int
}
interface GradientEffect : Effect {
    val gradient: Gradient
}


class SolidColorEffect(
    override val color: Int
) : ColorEffect {

    override val title = "Solid Color"
    override val numFrames = 1

    override fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray> {
        val output = ArrayList<IntArray>()
        val frame = IntArray(totalNumLights)

        //Build the only frame in this effect (solid color effects have only one change)
        for(light in 0 until totalNumLights){
            if(lights.contains(light)){
                frame[light] = color
            } else {
                frame[light] = 0x7F000000
            }
        }

        //Solid color effects have only one change
        output.add(frame)
        return output
    }
}


class SolidGradientEffect(
    override val gradient: Gradient
) : GradientEffect {

    override val title = "Solid Gradient"
    override val numFrames = 1

    override fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray> {
        val output = ArrayList<IntArray>()
        val frame = IntArray(totalNumLights)

        //Build the only frame in this effect (solid gradient effects have only one change)
        for(light in 0 until totalNumLights){
            if(lights.contains(light)){
                frame[light] = gradient.colorAtPosition(light.toFloat()/totalNumLights.toFloat())
            } else {
                frame[light] = 0x7F000000
            }
        }

        //Solid gradient effects have only one change
        output.add(frame)
        return output
    }
}


class GradientCycleEffect(
    override val gradient: Gradient,
    val duration: Int //In seconds
) : GradientEffect {

    override val title = "Gradient Cycle"
    override val numFrames: Int = duration * 1000 / MILLIS_BETWEEN_FRAMES

    override fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray> {
        val output = ArrayList<IntArray>()
        val frame = IntArray(totalNumLights)

        //Iteratively build the frames in this effect
        var frameNum = 0
        while(frameNum < numFrames){
            for(light in 0 until totalNumLights){
                if(lights.contains(light)){
                    frame[light] = gradient.colorAtPosition(frameNum.toFloat() / numFrames.toFloat())
                } else {
                    frame[light] = 0x7F000000
                }
            }
            output.add(frame.copyOf())
            frameNum++
        }

        return output
    }
}


class GradientWaveEffect(
    override val gradient: Gradient,
    val duration: Int, //In seconds
    val direction: EffectDirection
) : GradientEffect{

    override val title = "Gradient Wave"
    override val numFrames: Int = duration * 1000 / MILLIS_BETWEEN_FRAMES

    override fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray> {
        val output = ArrayList<IntArray>()
        val frame = IntArray(totalNumLights)

        //Iteratively build the frames in this effect
        var frameNum = 0
        while(frameNum < numFrames){
            for(light in 0 until totalNumLights){
                if(lights.contains(light)){
                    val position = when(direction){
                        EffectDirection.START_TO_END -> (light + frameNum) % totalNumLights
                        EffectDirection.END_TO_START -> (totalNumLights + (light - frameNum)) % totalNumLights
                    }
                    frame[light] = gradient.colorAtPosition(position.toFloat() / totalNumLights.toFloat())
                } else {
                    frame[light] = 0x7F000000
                }
            }
            output.add(frame.copyOf())
            frameNum++
        }

        return output
    }
}