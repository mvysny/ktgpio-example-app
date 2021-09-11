import io.ktgp.Closeable
import io.ktgp.gpio.Gpio

/**
 * Controls a line of LEDs representing a bar graph. Positive [value] (0 to 1) light the LEDs from first to last.
 * Alternatively set [litCount] to control how many LEDs are lit.
 */
class LEDBarGraph(gpio: Gpio, pins: List<GpioPin>) : LEDCollection, Closeable {
    private val ledboard = LEDBoard(gpio, pins)

    override val leds: List<LED>
        get() = ledboard.leds

    override var litCount: Int
        get() = ledboard.litCount
        set(value) {
            require(value in (0..count)) { "The value must be in range ${ledboard.indices}" }
            ledboard.setValue(0 until value)
        }

    /**
     * The value of the LED bar graph. When no LEDs are lit, the value is 0. When all LEDs are lit, the value is 1.
     * Values between 0 and 1 light LEDs linearly from first to last.
     */
    var value: Float
        get() = litCount.toFloat() / ledboard.count
        set(value) {
            require(value in 0f..1f) { "The value must be in range 0..1" }
            litCount = (value * ledboard.count).toInt()
        }

    override fun close() {
        ledboard.close()
    }

    override fun toString(): String =
        "LEDBarGraph(${litCount} lit out of ${leds.map { it.pin }})"
}

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBarGraph] afterwards.
 */
fun Gpio.ledBarGraph(pins: List<GpioPin>) = LEDBarGraph(this, pins)

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBarGraph] afterwards.
 */
fun Gpio.ledBarGraph(vararg pins: GpioPin) = ledBarGraph(pins.toList())
