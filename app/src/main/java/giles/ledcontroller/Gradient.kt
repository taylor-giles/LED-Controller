package giles.ledcontroller

class Gradient @JvmOverloads constructor (
    var name: String,
    var colors: IntArray? = null,
    var positions: FloatArray? = null
) {
    //TODO: function to get color at specified arbitrary position, between 1 and 0
}