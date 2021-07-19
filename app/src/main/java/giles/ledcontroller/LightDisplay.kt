package giles.ledcontroller

import java.io.Serializable

class LightDisplay(var numLights: Int) : Comparable<LightDisplay>, Serializable {

    override fun compareTo(other: LightDisplay): Int {
        return AppData.displays.indexOf(this) - AppData.displays.indexOf(other)
    }
}