import io.ktgp.Closeable
import io.ktgp.gpio.Gpio
import io.ktgp.gpio.Output
import io.ktgp.gpio.PinState
import io.ktgp.util.sleep

/**
 * Represents a single device of any type; GPIO-based, SPI-based, I2C-based, etc.
 * This is the base class of the device hierarchy. It defines the basic services
 * applicable to all devices (specifically the [isActive] property and the [close] method).
 */
interface Device : Closeable {
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

    /**
     * Returns `true` if the device is closed (see the [close] method). Once a device
     * is closed you can no longer use any other methods or properties to control or query the device.
     */
    val isClosed: Boolean

    /**
     * Returns `true` if the device is currently active and `false` otherwise. This
     * property is usually derived from value. Unlike value, this is always a boolean.
     */
    val isActive: Boolean
}

/**
 * Represents a generic GPIO device and provides the services common to all
 * single-pin GPIO devices (like ensuring two GPIO devices do not share a pin).
 */
interface GPIODevice : Device {
    /**
     * The Pin that the device is connected to. The GPIO pin number, see [GpioPin] for more details.
     */
    val pin: GpioPin
}

/**
 * Represents a generic GPIO output device.
 *
 * This class extends [GPIODevice] to add facilities common to GPIO output devices:
 * an [on] method to switch the device on, a corresponding [off] method, and a [toggle] method.
 */
interface OutputDevice : GPIODevice {
    /**
     * Turns the device off.
     */
    fun off()

    /**
     * Turns the device on.
     */
    fun on()

    /**
     * Reverse the state of the device. If it’s on, turn it off; if it’s off, turn it on.
     */
    fun toggle() {
        if (isActive) {
            off()
        } else {
            on()
        }
    }
}

/**
 * Represents a generic output device with typical on/off behaviour.
 *
 * This class extends [OutputDevice] with a [blink] method which uses an optional
 * background thread to handle toggling the device state without further interaction.
 * @param activeHigh If `true` (the default), the LED will operate normally with the circuit described above.
 * If `false` you should wire the cathode to the GPIO pin, and the anode to a 3V3 pin (via a limiting resistor).
 * @param initialValue if `false` (the default), the LED will be off initially.
 * If `true`, the LED will be switched on initially.
 */
open class DigitalOutputDevice(
    gpio: Gpio,
    override final val pin: GpioPin,
    val activeHigh: Boolean = true,
    initialValue: Boolean = false,
    val name: String
) : OutputDevice {

    init {
        pin.requireInGpioRange()
    }

    protected val output: Output = gpio.output(pin, if (initialValue) PinState.HIGH else PinState.LOW, !activeHigh)

    /**
     * Turns the device on and off repeatedly:
     * turns it on for [onTimeMillis] (default 1000), then off for [offTimeMillis] (default 1000).
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

    override final var isClosed: Boolean = false
        private set

    override final var isActive: Boolean = initialValue
        set(value) {
            field = value
            output.setState(if (value) PinState.HIGH else PinState.LOW)
        }

    override fun close() {
        isActive = false
        isClosed = true
        output.close()
    }

    override fun toString() = "${name}(${if (activeHigh) "active_high" else "active_low"}; gpio${pin}=${if (isActive) "on" else "off"})"

    /**
     * Turns the device on.
     */
    override fun on() {
        isActive = true
    }

    /**
     * Turns the device off.
     */
    override fun off() {
        isActive = false
    }
}

/**
 * Represents a device composed of multiple [devices] like simple HATs, H-bridge motor controllers,
 * robots composed of multiple motors, etc.
 */
interface CompositeDevice<D: Device> : Device {
    /**
     * The devices.
     */
    val devices: List<D>

    /**
     * Valid device indices range.
     */
    val indices: IntRange get() = devices.indices

    override fun close() {
        devices.forEach { it.closeQuietly() }
    }

    override val isClosed: Boolean
        get() = devices.any { it.isClosed }

    /**
     * Composite devices are considered “active” if any of their constituent devices is active.
     */
    override val isActive: Boolean
        get() = devices.any { it.isActive }
}

/**
 * Extends [CompositeDevice] with [on], [off], and [toggle] methods for controlling subordinate output devices.
 */
interface CompositeOutputDevice<D: OutputDevice> : CompositeDevice<D> {
    /**
     * Turn all the output devices off.
     */
    fun off() {
        devices.forEach { it.off() }
    }

    /**
     * Turn all the output devices on.
     */
    fun on() {
        devices.forEach { it.on() }
    }

    /**
     * Toggle all the output devices. For each device, if it’s on, turn it off; if it’s off, turn it on.
     */
    fun toggle() {
        devices.forEach { it.toggle() }
    }
}
