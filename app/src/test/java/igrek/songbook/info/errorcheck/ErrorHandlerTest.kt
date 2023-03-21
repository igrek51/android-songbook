package igrek.songbook.info.errorcheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ErrorHandlerTest {

    @Test
    fun testFormatErrorMessageCauseEmpty() {
        try {
            try {
                throw java.lang.IllegalStateException()
            } catch (t: Throwable) {
                throw RuntimeException(t)
            }
        } catch (t: Throwable) {
            val msg = formatErrorMessage(t)
            assertThat(msg).isEqualTo("java.lang.IllegalStateException: IllegalStateException")
        }

        try {
            try {
                throw RuntimeException()
            } catch (t: Throwable) {
                throw AssertionError("context", t)
            }
        } catch (t: Throwable) {
            val msg = formatErrorMessage(t)
            assertThat(msg).isEqualTo("context: RuntimeException")
        }
    }

    @Test
    fun testFormatErrorMessageContext() {
        try {
            try {
                throw RuntimeException("Dupa")
            } catch (t: Throwable) {
                throw ContextError("liftoff", t)
            }
        } catch (t: Throwable) {
            val msg = formatErrorMessage(t)
            assertThat(msg).isEqualTo("liftoff: Dupa")
        }
    }

}