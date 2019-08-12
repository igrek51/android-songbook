package igrek.songbook.util

class ListMover<T>(private val items: MutableList<T>) {

    fun move(position: Int, step: Int): MutableList<T> {
        var targetPosition = position + step
        if (targetPosition < 0)
            targetPosition = 0
        if (targetPosition >= items.size)
            targetPosition = items.size - 1

        if (targetPosition == position)
            return items

        var pos = position
        while (pos < targetPosition) {
            swapElements(pos, pos+1)
            pos++
        }
        while (pos > targetPosition) {
            swapElements(pos, pos-1)
            pos--
        }
        return items
    }

    private fun swapElements(pos1: Int, pos2: Int) {
        val tmp = items[pos1]
        items[pos1] = items[pos2]
        items[pos2] = tmp
    }

}