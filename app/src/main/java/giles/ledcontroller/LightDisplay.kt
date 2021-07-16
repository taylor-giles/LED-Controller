package giles.ledcontroller

class LightDisplay(var numLights: Int) : Comparable<LightDisplay> {
    override fun compareTo(other: LightDisplay): Int {
        return AppData.displays.indexOf(this) - AppData.displays.indexOf(other)
    }
}