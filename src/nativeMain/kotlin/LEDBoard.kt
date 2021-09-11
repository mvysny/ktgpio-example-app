import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.util.sleep

/**
 * Represents multiple LEDs on multiple PINs which can be controlled at the same time.
 */
class LEDBoard(gpio: Gpio, pins: List<GpioPin>) : LEDCollection, Closeable {
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

    /**
     * Turns on LEDs with given [indices]. If no indices are given, turns on all LEDs.
     */
    fun on(vararg indices: Int) {
        if (indices.isEmpty()) {
            _leds.forEach { it.on() }
        } else {
            indices.forEach { _leds[it].on() }
        }
    }

    /**
     * Turns off LEDs with given [indices]. If no indices are given, turns off all LEDs.
     */
    fun off(vararg indices: Int) {
        if (indices.isEmpty()) {
            _leds.forEach { it.off() }
        } else {
            indices.forEach { _leds[it].off() }
        }
    }

    /**
     * [LED.toggle]s all LEDs.
     */
    fun toggle(vararg indices: Int) {
        if (indices.isEmpty()) {
            _leds.forEach { it.toggle() }
        } else {
            indices.forEach { _leds[it].toggle() }
        }
    }

    /**
     * Blinks all the LEDs: turns it on for [onTimeMillis] (default 1000), then off for [offTimeMillis] (default 1000).
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

    /**
     * The [values] array represents the new LED state, one number for every LED.
     * 0 means off, any non-zero value means on.
     */
    fun setValue(vararg values: Int) {
        _leds.forEachIndexed { index, led ->
            val value = if (index in values.indices) values[index] else 0
            led.isLit = value != 0
        }
    }

    /**
     * Turns on LEDs in given [indices] range, turns off all other LEDs.
     */
    fun setValue(indices: IntRange) {
        _leds.forEachIndexed { index, led -> led.isLit = index in indices }
    }

    override fun toString(): String =
        "LEDBoard(${_leds.joinToString(", ") { "gpio${it.pin}=${if (it.isLit) "on" else "off"}" }})"
}

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(pins: List<GpioPin>) = LEDBoard(this, pins)

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(vararg pins: GpioPin) = ledboard(pins.toList())
