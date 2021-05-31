package giles.util

import android.graphics.Color

object ColorUtils {
    fun getHue(color: Int): Float{
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[0]
    }
}