package giles.ledcontroller

class LEDDisplay(var numLights: Int) : Comparable<LEDDisplay> {
    override fun compareTo(other: LEDDisplay): Int {
        return AppData.displays.indexOf(this) - AppData.displays.indexOf(other)
    }
}