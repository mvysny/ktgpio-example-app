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

/**
 * Closes quietly - catches any exception and prints it to stderr.
 */
fun Closeable.closeQuietly() {
    try {
        close()
    } catch (t: Throwable) {
        println()
        t.printStackTrace()
    }
}

/**
 * Represents a Raspberry PI GPIO pin. This number doesn't correspond to the physical
 * pin number. For example, GPIO pin 17 (GPIO17) is physical
 * pin number 11 (at least on on Raspberry PI 3B).
 * See https://www.raspberrypi.org/documentation/computers/os.html#gpio-pinout for more details.
 */
typealias GpioPin = Int
