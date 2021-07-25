package giles.ledcontroller

import java.io.Serializable

interface Effect : Serializable {
    val title: String
    fun display(lights: List<Light>)
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
    override fun display(lights: List<Light>){

    }
}


class SolidGradientEffect(
    override val gradient: Gradient,
    ) : GradientEffect {

    override val title = "Solid Gradient"

    override fun display(lights: List<Light>){

    }
}


class GradientCycleEffect(override val gradient: Gradient) : GradientEffect {
    override val title = "Gradient Cycle"
    override fun display(lights: List<Light>){

    }
}


class GradientWaveEffect(
    override val gradient: Gradient,
    val direction: EffectDirection
    ) : GradientEffect{
    override val title = "Gradient Wave"

    override fun display(lights: List<Light>){

    }
}