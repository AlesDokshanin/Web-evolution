package web_kotlin

object WebConfig {
    private val normalDistribution = true
    internal var maxTrappingNetLength = 100 * 1000

    var drawFlies = false
    var dynamicFlies = false
    var normalFliesDistribution = true

    var sidesCount = 15
        @Throws(IllegalArgumentException::class)
        set(value) =
        when (value) {
            in MIN_SIDES..MAX_SIDES -> sidesCount = value
            else -> throw IllegalArgumentException("Invalid web sides count: $value (should be in range [${MIN_SIDES}, ${MAX_SIDES}].")
        }

    var fliesCount = 100
        @Throws(IllegalArgumentException::class)
        set(count) {
            if (count < MIN_FLIES_COUNT || count > MAX_FLIES_COUNT) {
                throw IllegalArgumentException("Flies count should be in range [${MIN_FLIES_COUNT}, ${MAX_FLIES_COUNT}]")
            }
            fliesCount = count
        }

    internal val minAngleBetweenSkeletonLines = Math.PI / WebConfig.sidesCount

}