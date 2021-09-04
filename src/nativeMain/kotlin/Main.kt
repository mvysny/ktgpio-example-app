import io.ktgp.Gpio
import io.ktgp.gpio.PinState
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
        gpio.output(17).use { output ->
            // it's probably a good idea to hook the LED to the pin first,
            // before setting it to HIGH...?
            output.setState(PinState.HIGH)
            sleep(1000)
            output.setState(PinState.LOW)
        }
    }
}
