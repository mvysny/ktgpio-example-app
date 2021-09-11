/**
 * A collection of LEDs.
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
