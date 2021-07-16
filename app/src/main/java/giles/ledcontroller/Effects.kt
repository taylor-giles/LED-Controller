package giles.ledcontroller

import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils.getText

val EFFECT_NAMES = arrayOf("Solid Color", "Solid Gradient", "Gradient Cycle", "Gradient Wave", "Timed Changes")
interface Effect {
    val title: String
    fun display(layer: Layer)
}

interface ColorEffect : Effect {
    val color: Int
}

interface GradientEffect : Effect {
    val gradient: Gradient
}


class SolidColorEffect(override val color: Int) : ColorEffect {
    override val title = "Solid Color"
    override fun display(layer: Layer){

    }
}


class SolidGradientEffect(
    override val gradient: Gradient,
    val start: Light,
    val end: Light
    ) : GradientEffect {

    override val title = "Solid Gradient"

    init{
        if(start > end){
            throw IllegalArgumentException("Start point must be before end point")
        }
    }

    override fun display(layer: Layer){

    }
}


class GradientCycleEffect(override val gradient: Gradient) : GradientEffect {
    override val title = "Gradient Cycle"
    override fun display(layer: Layer){

    }
}


class GradientWaveEffect(
    override val gradient: Gradient,
    val direction: GradientWaveDirection,
    val start: Light,
    val end: Light
    ) : GradientEffect{
    override val title = "Gradient Wave"
    init{
        if(start > end){
            throw IllegalArgumentException("Start point must be before end point")
        }
    }

    enum class GradientWaveDirection { START_TO_END, END_TO_START, CENTER_TO_EDGE, EDGE_TO_CENTER }

    override fun display(layer: Layer){

    }
}


class TimedChangeEffect(
    val effects: List<Effect>,
    val timers: List<Float>
    ) : Effect{
    override val title = "Timed Changes"
    override fun display(layer: Layer){

    }
}