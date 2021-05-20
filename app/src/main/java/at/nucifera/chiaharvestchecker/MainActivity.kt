package at.nucifera.chiaharvestchecker

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val parser = LogParser()

    private var totalPlots: Int = 0
    private var showProofCheer = false

    private val targetPath
        get() = filesDir.absolutePath + LOCAL_PATH

    private val targetFile
        get() = targetPath + TARGET_FILE_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        File(targetPath).mkdirs()
        val logFile = File(targetFile)
        if (!logFile.exists())
            logFile.createNewFile()
    }

    fun startParse(view: View) {

        lifecycleScope.launch(Dispatchers.IO) {
            parseLog()


        }

    }

    private suspend fun parseLog() {

        try {
            val start = System.currentTimeMillis()
            Timber.d("CHIA start parsing")

            copyFileFromShare()

            var message = ""

            val targetFile = File(targetFile).useLines {

                var currentBucket: Bucket? = null
                it.forEach { line ->

                    if (!line.regionMatches(
                            24,
                            HARVESTER_LINE_PATTERN,
                            0,
                            HARVESTER_LINE_PATTERN.length
                        )
                    )
                        return@forEach

                    val attempt = parser.parseHarvesterInfoLine(line)

                    attempt?.run {
                        val bucketTime: Long =
                            timeStamp - timeStamp % (TimeUnit.SECONDS.toMillis(ChiaConstants.BUCKET_SECONDS)) // modulo 900 seconds (quarter hour)
                        if (currentBucket?.startTimeStamp != bucketTime) {
                            if (currentBucket != null) {
                                // write to db
                                message += "CHIA BUCKET ${currentBucket.toString()}\n"
                                Timber.d("CHIA BUCKET ${currentBucket.toString()}")
                            }
                            // new bucket with current time
                            currentBucket = Bucket(bucketTime)
                        }
                        currentBucket?.addAttempt(this)
                        if (currentBucket?.foundProofs ?: 0 > 0)
                            showProofCheer = true
                    }

//                    Timber.d(attempt.toString())
                }
            }

            val end = System.currentTimeMillis()

            Timber.d("CHIA end parsing. Time: ${end - start}ms")

            withContext(Dispatchers.Main) {
                if(showProofCheer)
                    findViewById<TextView>(R.id.proofText).visibility = View.VISIBLE
                val textView = findViewById<TextView>(R.id.textView)
                textView.text = message
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

    }

    private suspend fun copyFileFromShare() {

        val config = SmbConfig.builder()
            .withTimeout(
                120,
                TimeUnit.SECONDS
            ) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
            .withSoTimeout(
                180,
                TimeUnit.SECONDS
            ) // Socket Timeout (default is 0 seconds, blocks forever)
            .build()
        val client = SMBClient(config)

        client.connect("192.168.1.12").use { connection ->
            val info = connection.connectionInfo
            val ac =
                AuthenticationContext("", "".toCharArray(), ".")
            val session: Session = connection.authenticate(ac)

            val conn = session.connection


            (session.connectShare("Users") as? DiskShare?)?.apply {

                val exists = fileExists("Nucifera/.chia/mainnet/log/debug.log")
                Timber.d("CHIA log file found: $exists")


                val inputFile = openFile(
                    "Nucifera/.chia/mainnet/log/debug.log",
                    setOf(
                        AccessMask.FILE_READ_DATA,
                        AccessMask.FILE_READ_ATTRIBUTES,
                        AccessMask.FILE_READ_EA
                    ),
                    null,
                    null,
                    SMB2CreateDisposition.FILE_OPEN,
                    null
                )


                val id = inputFile.fileId
                val info = inputFile.fileInformation

                Timber.d("CHIA file size ${info.standardInformation.allocationSize}")

                val inputStream = inputFile.inputStream
                val outputStream = File(targetFile).outputStream()


                try {

                    val copied = inputStream.copyTo(outputStream)
                    Timber.d("CHIA copied bytes: $copied")
                } catch (e: Exception) {
                    Timber.e(e)
                } finally {
                    inputStream.close()
                    outputStream.close()
                    inputFile.close()
                }


//
//                val result = File(SHARE_PATH).copyTo

            }
        }
    }

    companion object {
        const val LOCAL_PATH = "/tmp"
        const val TARGET_FILE_NAME = "/debug.log"
        const val HARVESTER_LINE_PATTERN = "harvester chia.harvester.harvester"
    }
}

