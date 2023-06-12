package igrek.songbook.util

class FibonacciCounter {

    private var penultimate = 0 // last but one
    private var last = 1

    fun next(): Int {
        val oldLast = last
        last += penultimate
        penultimate = oldLast
        return oldLast
    }

    fun reset() {
        penultimate = 0
        last = 1
    }
}