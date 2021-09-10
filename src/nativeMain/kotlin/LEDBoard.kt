import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.gpio.Output
import io.ktgp.gpio.PinState
import io.ktgp.util.sleep

/**
 * Represents multiple LEDs on multiple PINs which can be controlled at the same time.
 */
class LEDBoard(gpio: Gpio, pins: List<Pin>) : Closeable {
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
     * Turns on all LEDs.
     */
    fun on() {
        leds.forEach { it.on() }
    }

    /**
     * Turns on LEDs with given indices, turns off all others.
     */
    fun light(vararg indices: Int) {
        leds.forEachIndexed { index, led -> led.on = index in indices }
    }

    /**
     * Turns off all LEDs.
     */
    fun off() {
        leds.forEach { it.off() }
    }

    /**
     * [LED.toggle]s all LEDs.
     */
    fun toggle() {
        leds.forEach { it.toggle() }
    }

    /**
     * Blinks all LEDs: turns it on, then off after given [delayMillis].
     */
    fun blink(delayMillis: Long = 1000) {
        on()
        sleep(delayMillis)
        off()
        sleep(delayMillis)
    }

    override fun toString(): String = "LEDBoard(${leds.joinToString(", ") { "${it.pin.gpio}=${if(it.on) "on" else "off"}" }})"

    val indices: IntRange get() = leds.indices
}

/**
 * Represents a Raspberry PI GPIO pin.
 * @property gpio the GPIO pin number. E.g. passing in 17 will target GPIO17 which is
 * pin number 11 (at least on on Raspberry PI 3B).
 * See https://www.raspberrypi.org/documentation/computers/os.html#gpio-pinout for more details.
 */
value class Pin(val gpio: Int) {
    init {
        require(gpio in 0..27) { "Invalid gpio number $gpio: must be 0..27" }
    }
}

/**
 * A single LED on given [pin]. The LED is off by default.
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
        on = false
        output.close()
    }

    override fun toString() = "LED(${pin.gpio}=${if(on) "on" else "off"})"

    /**
     * Turns the LED on.
     */
    fun on() {
        on = true
    }

    /**
     * Turns the LED off.
     */
    fun off() {
        on = false
    }

    /**
     * Toggles the LED: turns it off if it was on, and vice versa.
     */
    fun toggle() {
        on = !on
    }

    /**
     * Blinks the LED: turns it on, then off after given [delayMillis].
     */
    fun blink(delayMillis: Long = 1000) {
        on()
        sleep(delayMillis)
        off()
        sleep(delayMillis)
    }
}

/**
 * Controls a LED on given [pin]. Don't forget to close the [LED] afterwards.
 */
fun Gpio.led(pin: Pin) = LED(this, pin)
/**
 * Controls multiple LEDs on given [pins]. Don't forget to close the [LEDBoard] afterwards.
 */
fun Gpio.ledboard(pins: List<Pin>) = LEDBoard(this, pins)
/**
 * Controls multiple LEDs on given [gpioPins]. Don't forget to close the [LEDBoard] afterwards.
 * @param gpioPins the GPIO pin number. E.g. passing in 17 will target GPIO17 which is
 * pin number 11 (at least on on Raspberry PI 3B).
 */
fun Gpio.ledboard(vararg gpioPins: Int) = LEDBoard(this, gpioPins.map { Pin(it) })
