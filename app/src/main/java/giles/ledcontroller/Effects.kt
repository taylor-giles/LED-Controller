package giles.ledcontroller

import java.io.Serializable

const val MILLIS_BETWEEN_FRAMES = 200
const val MIN_EFFECT_DURATION = 15 //Minimum duration of any effect, in seconds

enum class EffectDirection { START_TO_END, END_TO_START }

interface Effect : Serializable {
    val title: String
    val delay: Float
    fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray>
}
interface ColorEffect : Effect {
    val color: Int
}
interface GradientEffect : Effect {
    val gradient: Gradient
}
interface DynamicEffect : Effect {
    val duration: Int //The duration of this effect, in seconds
}


class SolidColorEffect(
    override val color: Int,
    override val delay: Float
) : ColorEffect {

    override val title = "Solid Color"
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
    override val gradient: Gradient,
    override val delay: Float
) : GradientEffect {

    override val title = "Solid Gradient"

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
    override val delay: Float,
    override val duration: Int
) : GradientEffect, DynamicEffect {

    override val title = "Gradient Cycle"

    override fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray> {
        val output = ArrayList<IntArray>()
        val frame = IntArray(totalNumLights)

        //Iteratively build the frames in this effect
        var frameTime = 0
        while(frameTime < duration){
            for(light in 0 until totalNumLights){
                if(lights.contains(light)){
                    frame[light] = gradient.colorAtPosition(frameTime.toFloat() / duration.toFloat())
                } else {
                    frame[light] = 0x7F000000
                }
            }
            output.add(frame.copyOf())
            frameTime += MILLIS_BETWEEN_FRAMES
        }

        return output
    }
}


class GradientWaveEffect(
    override val gradient: Gradient,
    override val delay: Float,
    override val duration: Int,
    val direction: EffectDirection
) : GradientEffect, DynamicEffect{

    override val title = "Gradient Wave"

    override fun generateFrameMatrix(lights: List<Int>, totalNumLights: Int): List<IntArray> {
        val output = ArrayList<IntArray>()
        val frame = IntArray(totalNumLights)

        //Iteratively build the frames in this effect
        var frameTime = 0
        while(frameTime < duration){
            for(light in 0 until totalNumLights){
                if(lights.contains(light)){
                    val offset = (frameTime / MILLIS_BETWEEN_FRAMES)
                    val position = when(direction){
                        EffectDirection.START_TO_END -> (light + offset) % totalNumLights
                        EffectDirection.END_TO_START -> (totalNumLights + (light - offset)) % totalNumLights
                    }
                    frame[light] = gradient.colorAtPosition(position.toFloat() / totalNumLights.toFloat())
                } else {
                    frame[light] = 0x7F000000
                }
            }
            output.add(frame.copyOf())
            frameTime += MILLIS_BETWEEN_FRAMES
        }

        return output
    }
}