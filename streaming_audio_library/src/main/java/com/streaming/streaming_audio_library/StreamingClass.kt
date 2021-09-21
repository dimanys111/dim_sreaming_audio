package com.streaming.streaming_audio_library

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import java.io.IOException

class StreamingClass(private val activity: Activity, private val updateResults: (String) -> Unit,
                     private val changState: (Boolean) -> Unit)
{

    private val SERVER = "wss://dev.staging.kvint.io/api/audio"
    private val TIMEOUT = 10000

    private val BITRATE = 16000
    private val RECORD_BUFFER_TIME = 4f // seconds

    private val REQUEST_CODE_PERMISSION_RECORD_AUDIO = 0

    private var streaming = false

    private var record: AudioRecord? = null

    var ws: WebSocket? = null

    init {
        start()
    }

    fun changStreaming() {
        if (!streaming) {
            val permissionStatus =
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_PERMISSION_RECORD_AUDIO
                )
            }
        } else {
            streaming = false
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    startRecording()
                } else {
                    // permission denied
                }
                return
            }
        }
    }

    private fun connect(): WebSocket? {
        if (ws == null) {
            val factory = WebSocketFactory().setConnectionTimeout(TIMEOUT)
            try {
                val ws = factory.createSocket(SERVER)
                ws.addListener(WebSocketListener(this::onError, activity, updateResults))
                ws.connectAsynchronously()
                return ws
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        } else {
            ws?.connectAsynchronously()
            return ws
        }
    }

    fun onError() {
        Handler(Looper.getMainLooper()).postDelayed({
            ws = null
            start()
        }, 3000)
    }

    private fun start() {
        ws = connect()
    }

    private fun send(byteArray: ByteArray) {
        ws?.sendBinary(byteArray)
    }

    private fun startRecording() {
        Thread(object : Runnable {
            override fun run() {
                Log.v(this.javaClass.toString(), "Start recording... ")
                val minBufferSize = AudioRecord.getMinBufferSize(
                    BITRATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                var bufferSizeInBytes = (RECORD_BUFFER_TIME * BITRATE * 2).toInt()
                if (bufferSizeInBytes < minBufferSize) bufferSizeInBytes = minBufferSize
                record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    BITRATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes
                )
                if (record != null) {
                    record?.startRecording()
                    streaming = true
                    activity.runOnUiThread { changState(true) }
                    var pos = 0
                    val recordChunk = ByteArray(1366)
                    //var inStr = ByteArrayOutputStream()
                    while (streaming) {
                        pos += record!!.read(recordChunk, pos, recordChunk.size - pos)
                        if (pos < recordChunk.size) {
                            continue
                        } else {
                            pos = 0
                            send(recordChunk)
                        }
                    }
                    activity.runOnUiThread { changState(false) }
                    record?.stop()
                    record?.release()
                    record = null
                    Log.v(this.javaClass.toString(), "Stopped recording... ")
                }
            }
        }).start()
    }
}
