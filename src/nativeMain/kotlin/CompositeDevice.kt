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
