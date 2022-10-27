package igrek.songbook.kotlin

import kotlinx.coroutines.*
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext

class CoroutinesTest {

    @Test
    fun testRunBlockingDeadlock() {
        val context: CoroutineContext = CoroutineName("launchMe")
        val scope = CoroutineScope(context)

        println("entering runBlocking 1")
        runBlocking {
            runBlocking {
                runBlocking(context) {
                    scope.launch {
                        println("entered")
                    }
                }
            }
        }
        println("exited runblocking 1")
    }

    @Test
    fun maxCoroutinesCount() {
        val counter = AtomicLong(0)

        repeat(100) {
            GlobalScope.launch(Dispatchers.IO) {
                counter.incrementAndGet()
                delay(1000)
            }
        }

        runBlocking {
            delay(100)
        }

        check(counter.get() == 100L)
    }

//    @Ignore
//    @Test
//    fun deadlockOnBlockingMainDispatcher() {
//        println("starting")
//        runBlocking(Dispatchers.Main) {
//            println("entered")
//        }
//        println("exited")
//    }
//
//    @Ignore
//    @Test
//    fun deadlockOnMainContextInsideBlocking() {
//        println("entering runBlocking 1")
//        runBlocking { // or Dispatchers.IO
//            println("changing context")
//            withContext(Dispatchers.Main) {
//                println("entered")
//            }
//        }
//        println("exited runblocking 1")
//    }

    @Test
    fun exceptionInSuspendFunction() {
        runBlocking {
            try {
                attempt()
            } catch (t: RuntimeException) {
                println("caught")
            }
        }
    }

    private suspend fun attempt() {
        delay(1)
        throw RuntimeException("fatality")
    }

}
