import io.ktgp.gpio.Gpio
import io.ktgp.gpio.Output
import io.ktgp.gpio.PinState

/**
 * A single LED on given [pin]. The LED is off by default.
 *
 * Connect the cathode (short leg, flat side) of the LED to a ground pin; connect the anode
 * (longer leg) to a limiting resistor; connect the other side of the limiting resistor to a
 * GPIO pin (the limiting resistor can be placed either side of the LED).
 * @param activeHigh If `true` (the default), the LED will operate normally with the circuit described above.
 * If `false` you should wire the cathode to the GPIO pin, and the anode to a 3V3 pin (via a limiting resistor).
 * @param initialValue if `false` (the default), the LED will be off initially.
 * If `true`, the LED will be switched on initially.
 */
class LED(
    gpio: Gpio,
    override val pin: GpioPin,
    val activeHigh: Boolean = true,
    initialValue: Boolean = false
) : DigitalOutputDevice {

    init {
        require(pin in 0..27) { "Invalid gpio number $gpio: must be 0..27" }
    }

    private val output: Output = gpio.output(pin, if (initialValue) PinState.HIGH else PinState.LOW, !activeHigh)

    /**
     * Whether the LED is on or not.
     */
    var isLit: Boolean = initialValue
        set(value) {
            field = value
            output.setState(if (value) PinState.HIGH else PinState.LOW)
        }

    override var isClosed: Boolean = false
        private set

    override val isActive: Boolean
        get() = isLit

    override fun close() {
        isLit = false
        isClosed = true
        output.close()
    }

    override fun toString() = "LED(${if (activeHigh) "active_high" else "active_low"}; gpio${pin}=${if (isLit) "on" else "off"})"

    /**
     * Turns the LED on.
     */
    override fun on() {
        isLit = true
    }

    /**
     * Turns the LED off.
     */
    override fun off() {
        isLit = false
    }
}

/**
 * Controls a LED on given [pin]. Don't forget to close the [LED] afterwards.
 */
fun Gpio.led(pin: GpioPin) = LED(this, pin)
