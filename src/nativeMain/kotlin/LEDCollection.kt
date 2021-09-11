import io.ktgp.Closeable
import io.ktgp.gpio.Gpio

/**
 * A basic collection of LEDs. See [LEDBoard] and [LEDBarGraph] for concrete implementations.
 */
interface LEDCollection : CompositeOutputDevice {
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

    override fun off() {
        leds.forEach { it.off() }
    }

    override fun on() {
        leds.forEach { it.on() }
    }

    override fun toggle() {
        leds.forEach { it.toggle() }
    }
}

/**
 * Utility class which owns a list of LEDs and closes them properly on [close].
 * Internal - not supposed to be used by the user directly.
 * @param activeHigh If `true` (the default), the [on] method will set all the associated pins to HIGH.
 * If `false`, the [on] method will set all pins to LOW (the [off] method always does the opposite).
 * @param initialValue If `false` (the default), all LEDs will be off initially.
 * If `true`, the device will be switched on initially.
 */
internal class CloseableLEDCollection(
    gpio: Gpio, pins: List<GpioPin>, val activeHigh: Boolean = true,
    initialValue: Boolean = false
) : LEDCollection, Closeable {
    private val _leds = mutableListOf<LED>()

    override val leds: List<LED>
        get() = _leds

    init {
        var initialized = false
        try {
            pins.toHashSet().forEach { _leds.add(LED(gpio, it, activeHigh, initialValue)) }
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

interface CompositeDevice : Closeable {
    /**
     * Shut down the device and release all associated resources (such as GPIO pins).
     *
     * This method is idempotent (can be called on an already closed device without any side-effects).
     * It is primarily intended for interactive use at the command line. It disables the device and releases its pin(s) for use by another device.
     *
     * You can attempt to do this simply by deleting an object, but unless you’ve cleaned up
     * all references to the object this may not work (even if you’ve cleaned up all references,
     * there’s still no guarantee the garbage collector will actually delete the object at that point).
     * By contrast, the close method provides a means of ensuring that the object is shut down.
     */
    override fun close()
}

/**
 * Extends [CompositeDevice] with [on], [off], and [toggle] methods for controlling subordinate output devices.
 */
interface CompositeOutputDevice : CompositeDevice {
    /**
     * Turn all the output devices off.
     */
    fun off()

    /**
     * Turn all the output devices on.
     */
    fun on()

    /**
     * Toggle all the output devices. For each device, if it’s on, turn it off; if it’s off, turn it on.
     */
    fun toggle()
}