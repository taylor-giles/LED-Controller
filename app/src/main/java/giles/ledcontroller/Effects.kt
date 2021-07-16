package giles.ledcontroller

val EFFECT_NAMES = arrayOf("Solid Color", "Solid Gradient", "Gradient Cycle", "Gradient Wave", "Timed Changes")
interface Effect {
    fun display(layer: Layer)
}


class SolidColorEffect(val color: Int) : Effect {
    override fun display(layer: Layer){

    }
}


class SolidGradientEffect(val gradient: Gradient, val start: Light, val end: Light) : Effect {

    init{
        if(start > end){
            throw IllegalArgumentException("Start point must be before end point")
        }
    }

    override fun display(layer: Layer){

    }
}


class GradientCycleEffect(val gradient: Gradient) : Effect {
    override fun display(layer: Layer){

    }
}


class GradientWaveEffect(
    val gradient: Gradient,
    val direction: GradientWaveDirection,
    val start: Light,
    val end: Light
    ) : Effect{

    init{
        if(start > end){
            throw IllegalArgumentException("Start point must be before end point")
        }
    }

    enum class GradientWaveDirection { START_TO_END, END_TO_START, CENTER_TO_EDGE, EDGE_TO_CENTER }

    override fun display(layer: Layer){

    }
}


class TimedChangeEffect(val effects: List<Effect>, val timers: List<Float>) : Effect{
    override fun display(layer: Layer){

    }
}