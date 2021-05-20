package at.nucifera.chiaharvestchecker

import org.junit.Test

import org.junit.Assert.*

class LogParserTest {

    private val sut = LogParser()

    @Test
    fun parseHarvesterInfoLine() {

        val attempt =
            sut.parseHarvesterInfoLine("2021-05-17T14:28:55.947 harvester chia.harvester.harvester: INFO     6 plots were eligible for farming f50b6f76a8... Found 2 proofs. Time: 0.35500 s. Total 98 plots")

        assertNotNull(attempt)
        assertEquals(6, attempt?.eligiblePlots)
        assertEquals(2, attempt?.proofsFound)
        assertEquals(98, attempt?.totalPlots)
    }
}