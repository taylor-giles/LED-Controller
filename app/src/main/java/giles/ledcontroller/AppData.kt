package giles.ledcontroller

import java.util.*

/**
 * Static object to store app data shared by multiple classes
 */
object AppData {
    //Set (no duplicates) to store saved colors
    var savedColors = HashSet<Int>()
}