package com.alisayar.videochat

import android.util.Log
import com.alisayar.videochat.models.MessageModel
import com.alisayar.videochat.util.NewMessageInterface
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class SocketRepository(private val messageInterface: NewMessageInterface) {

    private var webSocket: WebSocketClient? = null
    private var username: String? = null

    private val TAG = "SocketRepository"

    fun initSocket(username: String){
        this.username = username

        webSocket = object: WebSocketClient(URI("ws://10.0.2.2:2208")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                sendMessageToSocket(
                    MessageModel(
                    type = "store_user",
                    name = username,
                    target = null,
                    data = null
                    )
                )
            }

            override fun onMessage(message: String?) {

                try {
                    messageInterface.onNewMessage(Gson().fromJson(message, MessageModel::class.java))
                } catch (e: Exception){
                    e.printStackTrace()
                }

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.d(TAG, "onError: $ex")
            }
        }
        webSocket?.connect()
    }

    fun sendMessageToSocket(message: MessageModel){
        try {
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception){
            e.printStackTrace()
        }
    }



}