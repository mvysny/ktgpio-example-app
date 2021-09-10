import io.ktgp.Closeable
import platform.posix.fflush
import platform.posix.fprintf

private val STDERR = platform.posix.fdopen(2, "w")

/**
 * Prints a message to stderr. Appends a newline automatically
 */
fun printErr(message: String) {
    fprintf(STDERR, message)
    fprintf(STDERR, "\n")
    fflush(STDERR)
}

fun Closeable.closeQuietly() {
    try {
        close()
    } catch (t: Throwable) {
        println()
        t.printStackTrace()
    }
}
