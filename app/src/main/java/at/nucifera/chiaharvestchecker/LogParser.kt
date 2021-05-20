package at.nucifera.chiaharvestchecker

import timber.log.Timber
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LogParser {

    private val dateFormat = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.ROOT)


    fun parseHarvesterInfoLine(line: String): HarvestAttempt? {


        try {

            val date = parseDate(line.substring(0, TIMESTAMP_PATTERN.length)) ?: return null


            val eligiblePlots =
                line.substring(TEXT_START, line.indexOf(" ", TEXT_START)).toIntOrNull()
                    ?: return null
            val proofsFound = line.substring(
                line.indexOf("Found", TEXT_START) + 6,
                line.indexOf("proofs", TEXT_START)
            )
                .trim().toIntOrNull() ?: return null


            return HarvestAttempt(date, eligiblePlots, proofsFound, 0, 0)

        } catch (e: Exception) {
            Timber.e(e)
        }

        return null

    }

    private fun parseDate(timestamp: String): Long? {
        SimpleDateFormat.getDateInstance()
        return try {
            dateFormat.parse(timestamp)?.time
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        const val TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        const val TEXT_START = 69

        const val HARVEST_LINE = "2021-05-17T14:28:55.947 harvester chia.harvester.harvester: INFO     1 plots were eligible for farming f50b6f76a8... Found 0 proofs. Time: 0.35500 s. Total 98 plots"

    }
}