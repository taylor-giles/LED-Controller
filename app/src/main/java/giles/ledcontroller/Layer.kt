package giles.ledcontroller

import java.io.Serializable

class Layer(
    var effect: Effect,
    val lights: List<Light>
): Serializable {

    init {
        //Find earliest and latest lights in selection
    }
}