package igrek.songbook.room

import java.util.concurrent.atomic.AtomicInteger

data class DiscoveryProgress(
        var all: AtomicInteger = AtomicInteger(0),
        var done: AtomicInteger = AtomicInteger(0),
)