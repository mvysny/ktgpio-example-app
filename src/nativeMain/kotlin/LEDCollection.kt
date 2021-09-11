import io.ktgp.Closeable
import io.ktgp.gpio.Gpio

/**
 * A basic collection of LEDs. See [LEDBoard] and [LEDBarGraph] for concrete implementations.
 */
interface LEDCollection {
    /**
     * All leds contained within this collection.
     */
    val leds: List<LED>

    /**
     * The number of LEDs on the bar graph actually lit up.
     */
    val litCount: Int
        get() = leds.count { it.isLit }

    /**
     * The number of LEDs controlled by this bar.
     */
    val count: Int get() = leds.size

    /**
     * Returns true if any of the LEDs is lit. Setting this to true or false
     * will turn on or off all LEDs.
     */
    var isLit: Boolean
        get() = leds.any { it.isLit }
        set(value) {
            leds.forEach { it.isLit = value }
        }

    /**
     * Valid LED indices range.
     */
    val indices: IntRange get() = leds.indices
}

/**
 * Utility class which owns a list of LEDs and closes them properly on [close].
 * Internal - not supposed to be used by the user directly.
 */
internal class CloseableLEDCollection(gpio: Gpio, pins: List<GpioPin>) : LEDCollection, Closeable {
    private val _leds = mutableListOf<LED>()

    override val leds: List<LED>
        get() = _leds

    init {
        var initialized = false
        try {
            pins.forEach { _leds.add(LED(gpio, it)) }
            initialized = true
        } finally {
            if (!initialized) {
                close()
            }
        }
    }

    override fun close() {
        _leds.forEach { it.closeQuietly() }
        _leds.clear()
    }

    override fun toString(): String =
        leds.joinToString(", ") { "gpio${it.pin}" }
}
