import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.util.sleep

/**
 * Represents multiple LEDs which can be controlled at the same time.
 * @param pins connect to LEDs on all of these pins.
 * @param activeHigh If `true` (the default), the [on] method will set all the associated pins to HIGH.
 * If `false`, the [on] method will set all pins to LOW (the [off] method always does the opposite).
 * @param initialValue If `false` (the default), all LEDs will be off initially.
 * If `true`, the device will be switched on initially.
 */
class LEDBoard(
    gpio: Gpio, pins: List<GpioPin>,
    val activeHigh: Boolean = true,
    initialValue: Boolean = false
) : LEDCollection, Closeable {
    private val _leds = CloseableLEDCollection(gpio, pins, activeHigh, initialValue)

    override val leds: List<LED>
        get() = _leds.leds

    override fun close() {
        _leds.close()
    }

    /**
     * Turns on LEDs with given [indices]. If no indices are given, turns on all LEDs.
     */
    fun on(vararg indices: Int) {
        if (indices.isEmpty()) {
            leds.forEach { it.on() }
        } else {
            indices.forEach { leds[it].on() }
        }
    }

    /**
     * Turns off LEDs with given [indices]. If no indices are given, turns off all LEDs.
     */
    fun off(vararg indices: Int) {
        if (indices.isEmpty()) {
            leds.forEach { it.off() }
        } else {
            indices.forEach { leds[it].off() }
        }
    }

    /**
     * [LED.toggle]s all LEDs.
     */
    fun toggle(vararg indices: Int) {
        if (indices.isEmpty()) {
            leds.forEach { it.toggle() }
        } else {
            indices.forEach { leds[it].toggle() }
        }
    }

    /**
     * Blinks all the LEDs: turns it on for [onTimeMillis] (default 1000), then off for [offTimeMillis] (default 1000).
     * Repeats [times] (defaults to 1).
     */
    fun blink(
        onTimeMillis: Long = 1000,
        offTimeMillis: Long = 1000,
        times: Int = 1
    ) {
        repeat(times) {
            on()
            sleep(onTimeMillis)
            off()
            sleep(offTimeMillis)
        }
    }

    /**
     * The [values] array represents the new LED state, one number for every LED.
     * 0 means off, any non-zero value means on.
     */
    fun setValue(vararg values: Int) {
        leds.forEachIndexed { index, led ->
            val value = if (index in values.indices) values[index] else 0
            led.isLit = value != 0
        }
    }

    /**
     * Turns on LEDs in given [indices] range, turns off all other LEDs.
     */
    fun setValue(indices: IntRange) {
        leds.forEachIndexed { index, led -> led.isLit = index in indices }
    }

    override fun toString(): String =
        "LEDBoard(${leds.joinToString(", ") { "gpio${it.pin}=${if (it.isLit) "on" else "off"}" }})"
}

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(pins: List<GpioPin>) = LEDBoard(this, pins)

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(vararg pins: GpioPin) = ledboard(pins.toList())
