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
    var savedGradients = SortedArrayList<Gradient> { gradient1: Gradient, gradient2: Gradient ->
        gradient1.compareTo(gradient2)
    }

    //Set to store LED displays
    var displays = HashSet<LightDisplay>()

    //ArrayList to store saved patterns
    var patterns = SortedArrayList<Pattern> { pattern1: Pattern, pattern2: Pattern ->
        pattern1.compareTo(pattern2)
    }
}