import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.gpio.Output
import io.ktgp.gpio.PinState
import io.ktgp.util.sleep

/**
 * Represents multiple LEDs on multiple PINs which can be controlled at the same time.
 */
class LEDBoard(gpio: Gpio, pins: List<GpioPin>) : Closeable {
    private val leds = mutableListOf<LED>()

    init {
        var initialized = false
        try {
            pins.forEach { leds.add(LED(gpio, it)) }
            initialized = true
        } finally {
            if (!initialized) {
                close()
            }
        }
    }

    override fun close() {
        leds.forEach { it.closeQuietly() }
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
     * Returns true if any of the LEDs is lit. Setting this to true or false
     * will turn on or off all LEDs.
     */
    var isLit: Boolean
        get() = leds.any { it.isLit }
        set(value) {
            leds.forEach { it.isLit = value }
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
        "LEDBoard(${leds.joinToString(", ") { "${it.pin}=${if (it.isLit) "on" else "off"}" }})"
}

/**
 * Represents a Raspberry PI GPIO pin. This number doesn't correspond to the physical
 * pin number. For example, GPIO pin 17 (GPIO17) is physical
 * pin number 11 (at least on on Raspberry PI 3B).
 * See https://www.raspberrypi.org/documentation/computers/os.html#gpio-pinout for more details.
 */
typealias GpioPin = Int

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

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(pins: List<GpioPin>) = LEDBoard(this, pins)

/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(vararg pins: GpioPin) = ledboard(pins.toList())
