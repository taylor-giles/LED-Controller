package giles.ledcontroller

import java.io.Serializable

class Layer(
    var effect: Effect,
    val lights: List<Int>
): Serializable {

}