package web

import java.awt.Color
import java.awt.Point

internal val WIDTH = 800
internal val HEIGHT = 800
internal val CENTER: Point = Point(WIDTH / 2, HEIGHT / 2)
internal val MIN_TRAPPING_NET_CIRCLES_COUNT = 2
internal val MIN_SKELETON_DISTANCE_FROM_CENTER = (Math.min(HEIGHT, WIDTH) / 5)
internal val MIN_TRAPPING_NET_CIRCLE_DISTANCE = Math.min(WIDTH, HEIGHT) / 75
internal val FLY_SIZE = (Math.min(WIDTH, HEIGHT) / 50)
internal val MIN_SIDES = 10
internal val MAX_SIDES = 20
internal val MIN_FLIES_COUNT = 10
internal val MAX_FLIES_COUNT = 1000
internal val PART_OF_NORMAL_DISTRIBUTED_FLIES = 0.25f
internal val CAUGHT_FLY_COLOR = Color(94, 104, 205, 128)
internal val UNCAUGHT_FLY_COLOR = Color(37, 205, 7, 128)
internal val CHILDREN_COUNT = 3
internal val MAX_TRAPPING_NET_LENGTH_UPPER = 25 * 1000
internal val MAX_TRAPPING_NET_LENGTH_LOWER = 1 * 1000