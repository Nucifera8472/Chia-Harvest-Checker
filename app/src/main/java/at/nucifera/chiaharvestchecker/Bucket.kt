package at.nucifera.chiaharvestchecker

class Bucket(val startTimeStamp: Long) {
    var harvestAttempts = 0
        private set
    var eligiblePlots = 0
        private set
    var foundProofs = 0
        private set
    var minPlots = -1
        private set
    var maxPlots = -1
        private set

    fun addAttempt(harvestAttempt: HarvestAttempt) {
        harvestAttempts++
        eligiblePlots += harvestAttempt.eligiblePlots
        foundProofs += harvestAttempt.proofsFound

        if (minPlots == -1 || harvestAttempt.totalPlots < minPlots) {
            minPlots = harvestAttempt.totalPlots
        }
        if (maxPlots == -1 || harvestAttempt.totalPlots > maxPlots) {
            maxPlots = harvestAttempt.totalPlots
        }
    }

    override fun toString(): String {
        return "$startTimeStamp totalAttempts:$harvestAttempts/${ChiaConstants.BUCKET_MAX_EXPECTED_ATTEMPTS} totalEligiblePlots:$eligiblePlots foundProofs:$foundProofs minPlots:$minPlots maxPlots:$maxPlots"
    }
}