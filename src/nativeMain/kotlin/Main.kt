import io.ktgp.Gpio
import io.ktgp.gpio.Gpio
import io.ktgp.use
import io.ktgp.util.sleep

fun main() {
    println("Hello, Kotlin/Native!")
    // you'll probably need to run this app with sudo, in order to be able to access
    // /dev/mem and /dev/gpiomem. See
    // https://raspberrypi.stackexchange.com/questions/40105/access-gpio-pins-without-root-no-access-to-dev-mem-try-running-as-root
    // for more details.
    Gpio().use { gpio ->
        // remember that the pin parameter of the output() function refers
        // to the GPIO number. E.g. passing in 17 will target GPIO17 which is
        // pin number 11 (at least on on Raspberry PI 3B).
        // See https://www.raspberrypi.org/documentation/computers/os.html#gpio-pinout for more details.
        //
        example1(gpio)
//        example2(gpio)
    }
}

/**
 * We're going to control three LEDs on adjacent GPIO ports: 17, 27 and 22.
 */
private fun example1(gpio: Gpio) {
    gpio.ledBarGraph(17, 27, 22).use { leds ->
        // it's probably a good idea to hook the LED to the pin first,
        // before setting it to HIGH...?
        println(leds)
        repeat(10) {
            leds.litCountRange.forEach {
                leds.litCount = it
                sleep(200)
            }
        }
    }
}

/**
 * Reads a text from stdin and animates it on a 7-segment character display.
 */
private fun example2(gpio: Gpio) {
    println("Type in text to display, then press Enter. Empty line ends the program.")
    gpio.ledCharDisplay(17, 27, 22, 13, 19, 26, 6, activeHigh = false).use { leds ->
        val lines = generateSequence { var line = readLine(); if (line.isNullOrBlank()) line = null; line }
        lines.forEach { leds.show(it) }
    }
}
