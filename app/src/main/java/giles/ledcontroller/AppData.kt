package giles.ledcontroller

import java.util.*

/**
 * Static object to store app data shared by many classes
 */
object AppData {
    //Ordered set (no duplicates) to store saved colors
    var savedColors = HashSet<Int>()
}