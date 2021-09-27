import io.ktgp.gpio.Gpio

/**
 * Extends CompositeDevice and represents a generic motor connected to a
 * bi-directional motor driver circuit (i.e. an [H-bridge](https://en.wikipedia.org/wiki/H_bridge).
 *
 * Attach an [H-bridge](https://en.wikipedia.org/wiki/H_bridge) motor controller to your Pi;
 * connect a power source (e.g. a battery pack or the 5V pin) to the controller;
 * connect the outputs of the controller board to the two terminals of the motor;
 * connect the inputs of the controller board to two GPIO pins.
 * @param forward The GPIO pin that the forward input of the motor driver chip is connected to.
 * See [GpioPin] for valid pin numbers.
 * @param backward The GPIO pin that the backward input of the motor driver chip is
 * connected to. See [GpioPin] for valid pin numbers.
 */
class Motor(gpio: Gpio, forward: GpioPin, backward: GpioPin): CompositeDevice<DigitalOutputDevice> {
    private val forward = DigitalOutputDevice(gpio, forward, name = "Forward")
    private val backward = DigitalOutputDevice(gpio, backward, name = "Backward")

    override val devices: List<DigitalOutputDevice>
        get() = listOf(forward, backward)

    /**
     * Drive the motor backwards.
     */
    fun backward() {
        forward.off()
        backward.on()
    }

    /**
     * Drive the motor forwards.
     */
    fun forward() {
        backward.off()
        forward.on()
    }

    /**
     * Reverse the current direction of the motor. If the motor is currently idle this
     * does nothing. Otherwise, the motor’s direction will be reversed at the current speed.
     */
    fun reverse() {
        if (backward.isActive) {
            forward()
        } else if (forward.isActive) {
            backward()
        }
    }

    /**
     * Stop the motor.
     */
    fun stop() {
        backward.off()
        forward.off()
    }

    override fun toString(): String =
        "Motor($forward, $backward)"
}

/**
 * @param forward The GPIO pin that the forward input of the motor driver chip is connected to.
 * See [GpioPin] for valid pin numbers.
 * @param backward The GPIO pin that the backward input of the motor driver chip is
 * connected to. See [GpioPin] for valid pin numbers.
 */
fun Gpio.motor(forward: GpioPin, backward: GpioPin) = Motor(this, forward, backward)
