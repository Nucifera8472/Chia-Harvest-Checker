package at.nucifera.chiaharvestchecker

data class HarvestAttempt(
    val timeStamp: Long,
    val eligiblePlots: Int,
    val proofsFound: Int,
    val totalPlots: Int,
    val time: Int
) {

    override fun toString(): String {
        var text = "$timeStamp - $eligiblePlots eligible plots. "
        if(eligiblePlots > 0)
            text += " $proofsFound proofs found."
        return text
    }
}
