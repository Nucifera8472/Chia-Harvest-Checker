package at.nucifera.chiaharvestchecker

import kotlin.math.floor
import kotlin.math.roundToInt

object ChiaConstants {
    const val BUCKET_SECONDS = 900L
    // chia supposedly sends 64 challenges per 10 minutes, so we expect this amount of attempts to
    // happen within our bucket period
    val BUCKET_MAX_EXPECTED_ATTEMPTS = floor(BUCKET_SECONDS / (600.0 / 64)).roundToInt()
}