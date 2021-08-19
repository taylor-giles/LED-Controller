package giles.ledcontroller

import java.io.Serializable

/**
 * This class exists as a way to pair an effect with the lights that it affects.
 */
data class Layer(
    var effect: Effect,
    val lights: List<Int>
): Serializable