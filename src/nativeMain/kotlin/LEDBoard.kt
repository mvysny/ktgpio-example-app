import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.gpio.Output
import io.ktgp.gpio.PinState
import io.ktgp.util.sleep

class LEDBoard : Closeable {
    override fun close() {
        TODO("Not yet implemented")
    }
}

/**
 * Represents a Raspberry PI GPIO pin.
 * @property gpio the GPIO pin number. E.g. passing in 17 will target GPIO17 which is
 * pin number 11 (at least on on Raspberry PI 3B).
 * See https://www.raspberrypi.org/documentation/computers/os.html#gpio-pinout for more details.
 */
value class Pin(val gpio: Int)

/**
 * A LED on given [pin]. The LED is off by default.
 */
class LED(gpio: Gpio, val pin: Pin) : Closeable {
    /**
     * Whether the LED is on or not.
     */
    var on: Boolean = false
        set(value) {
            field = value
            output.setState(if (value) PinState.HIGH else PinState.LOW)
        }

    private val output: Output = gpio.output(pin.gpio)

    override fun close() {
        output.close()
    }

    override fun toString() = "LED($pin, ${if(on) "on" else "off"})"

    fun on() {
        on = true
    }

    fun off() {
        on = false
    }

    fun blink(delayMillis: Long = 1000) {
        on()
        sleep(delayMillis)
        off()
        sleep(delayMillis)
    }
}

fun Gpio.led(pin: Pin) = LED(this, pin)
