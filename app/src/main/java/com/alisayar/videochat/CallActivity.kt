package com.alisayar.videochat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.alisayar.videochat.databinding.ActivityCallBinding
import com.alisayar.videochat.models.MessageModel
import com.alisayar.videochat.util.NewMessageInterface
import com.alisayar.videochat.util.PeerConnectionObserver
import org.webrtc.*

class CallActivity : AppCompatActivity(), NewMessageInterface {

    lateinit var binding: ActivityCallBinding
    private var username: String? = null

    private var socketRepository: SocketRepository? = null

    private var rtcClient: RTCClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        username = intent.getStringExtra("username")
        socketRepository = SocketRepository(this)
        username?.let {
            socketRepository?.initSocket(it)

        }
        rtcClient = RTCClient(application, username!!, socketRepository!!, object: PeerConnectionObserver(){
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
            }
        })

//        rtcClient?.initializeSurfaceView(binding.localView)
//        rtcClient?.startLocalVideo(binding.localView)

    }

    override fun onNewMessage(message: MessageModel) {

    }
}