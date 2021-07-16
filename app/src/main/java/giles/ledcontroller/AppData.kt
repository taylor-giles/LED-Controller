package giles.ledcontroller

import giles.util.SortedArrayList
import java.util.*

/**
 * Static object to store app data shared by multiple classes
 */
object AppData {
    //Set (no duplicates) to store saved colors
    var savedColors = HashSet<Int>()

    //ArrayList to store saved gradients
    var savedGradients = SortedArrayList<Gradient> { gradient: Gradient, gradient2: Gradient ->
        gradient.compareTo(gradient2)
    }

    //Set to store LED displays
    var displays = HashSet<LightDisplay>()
}