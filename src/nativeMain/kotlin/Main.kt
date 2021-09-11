import io.ktgp.Gpio
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
        // we're going to control three LEDs on adjacent GPIO ports: 17, 27 and 22.
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
}
