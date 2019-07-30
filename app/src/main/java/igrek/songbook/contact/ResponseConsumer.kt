package igrek.songbook.contact

@FunctionalInterface
interface ResponseConsumer<T> {

    fun accept(response: T)

}