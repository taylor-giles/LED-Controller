package giles.ledcontroller

import java.io.Serializable

val EFFECT_NAMES = arrayOf("Solid Color", "Solid Gradient", "Gradient Cycle", "Gradient Wave", "Timed Changes")
interface Effect : Serializable {
    val title: String
    fun display(layer: List<Light>)
}

enum class EffectDirection { START_TO_END, END_TO_START, CENTER_TO_EDGE, EDGE_TO_CENTER }

interface ColorEffect : Effect {
    val color: Int
}

interface GradientEffect : Effect {
    val gradient: Gradient
}


class SolidColorEffect(override val color: Int) : ColorEffect {
    override val title = "Solid Color"
    override fun display(layer: List<Light>){

    }
}


class SolidGradientEffect(
    override val gradient: Gradient,
    ) : GradientEffect {

    override val title = "Solid Gradient"

    override fun display(layer: List<Light>){

    }
}


class GradientCycleEffect(override val gradient: Gradient) : GradientEffect {
    override val title = "Gradient Cycle"
    override fun display(layer: List<Light>){

    }
}


class GradientWaveEffect(
    override val gradient: Gradient,
    val direction: EffectDirection
    ) : GradientEffect{
    override val title = "Gradient Wave"

    override fun display(layer: List<Light>){

    }
}


class TimedChangeEffect(
    val effects: List<Effect>,
    val timers: List<Float>
    ) : Effect{
    override val title = "Timed Changes"
    override fun display(layer: List<Light>){

    }
}