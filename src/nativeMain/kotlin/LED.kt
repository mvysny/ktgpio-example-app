import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.gpio.Output
import io.ktgp.gpio.PinState
import io.ktgp.util.sleep

/**
 * A single LED on given [pin]. The LED is off by default.
 */
class LED(gpio: Gpio, val pin: GpioPin) : Closeable {
    init {
        require(pin in 0..27) { "Invalid gpio number $gpio: must be 0..27" }
    }

    /**
     * Whether the LED is on or not.
     */
    var isLit: Boolean = false
        set(value) {
            field = value
            output.setState(if (value) PinState.HIGH else PinState.LOW)
        }

    private val output: Output = gpio.output(pin)

    override fun close() {
        isLit = false
        output.close()
    }

    override fun toString() = "LED(${pin}=${if (isLit) "on" else "off"})"

    /**
     * Turns the LED on.
     */
    fun on() {
        isLit = true
    }

    /**
     * Turns the LED off.
     */
    fun off() {
        isLit = false
    }

    /**
     * Toggles the LED: turns it off if it was on, and vice versa.
     */
    fun toggle() {
        isLit = !isLit
    }

    /**
     * Blinks the LED: turns it on for [onTimeMillis] (default 1000), then off for [offTimeMillis] (default 1000).
     * Repeats [repeatTimes] (defaults to 1).
     */
    fun blink(
        onTimeMillis: Long = 1000,
        offTimeMillis: Long = 1000,
        repeatTimes: Int = 1
    ) {
        repeat(repeatTimes) {
            on()
            sleep(onTimeMillis)
            off()
            sleep(offTimeMillis)
        }
    }
}

/**
 * Controls a LED on given [pin]. Don't forget to close the [LED] afterwards.
 */
fun Gpio.led(pin: GpioPin) = LED(this, pin)
