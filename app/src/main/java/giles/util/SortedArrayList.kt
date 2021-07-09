package giles.util

class SortedArrayList<T>(
    private val comparator: Comparator<in T>
    ) : ArrayList<T>() {

    override fun add(element: T): Boolean {
        var index: Int = this.binarySearch(element, comparator)
        if (index < 0) index = index.inv()
        super.add(index, element)
        return true
    }
}