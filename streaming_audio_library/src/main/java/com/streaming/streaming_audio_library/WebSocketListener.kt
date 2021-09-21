package com.streaming.streaming_audio_library

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFrame
import org.json.JSONObject


class WebSocketListener(private val onError: () -> Unit, private val activity: Activity,
                        private val updateResults: (String) -> Unit) : WebSocketAdapter() {

    override fun onConnected(
        websocket: WebSocket?,
        headers: MutableMap<String, MutableList<String>>?
    ) {
        super.onConnected(websocket, headers)
    }

    override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
        super.onBinaryMessage(websocket, binary)
    }

    override fun onTextMessage(websocket: WebSocket?, text: String?) {
        super.onTextMessage(websocket, text)
        if (text != null) {
            output(text)
            Log.v(this.javaClass.toString(), text)
        }
    }

    override fun onTextMessage(websocket: WebSocket?, data: ByteArray?) {
        super.onTextMessage(websocket, data)
        output(data.toString())
        Log.v(this.javaClass.toString(), data.toString())
    }

    override fun onDisconnected(
        websocket: WebSocket?,
        serverCloseFrame: WebSocketFrame?,
        clientCloseFrame: WebSocketFrame?,
        closedByServer: Boolean
    ) {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
        onError()
    }

    override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
        super.onError(websocket, cause)
    }

    override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
        super.onConnectError(websocket, exception)
        onError()
    }

    private fun output(txt: String) {
        var jo = JSONObject(txt)
        if(jo.has("params")) {
            jo = jo.getJSONObject("params")
            if(jo.has("text")) {
                activity.runOnUiThread {
                    updateResults(jo.get("text").toString())
                }
            }
        }
    }

}