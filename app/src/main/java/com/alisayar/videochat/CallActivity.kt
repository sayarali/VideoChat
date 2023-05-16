package com.alisayar.videochat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.alisayar.videochat.databinding.ActivityCallBinding
import com.alisayar.videochat.models.IceCandidateModel
import com.alisayar.videochat.models.MessageModel
import com.alisayar.videochat.util.NewMessageInterface
import com.alisayar.videochat.util.PeerConnectionObserver
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.webrtc.*

class CallActivity : AppCompatActivity(), NewMessageInterface {

    lateinit var binding: ActivityCallBinding
    private var username: String? = null

    private var socketRepository: SocketRepository? = null

    private var rtcClient: RTCClient? = null

    private var targetUsername: String = ""
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
                rtcClient?.addIceCandidate(p0)
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )
                socketRepository?.sendMessageToSocket(
                    MessageModel(
                        type = "ice_candidate",
                        name = username,
                        target = targetUsername,
                        data = candidate
                    )
                )
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
            }
        })

        binding.apply {
            callButton.setOnClickListener {
                if(targetUsernameEt.text.toString() != ""){
                    targetUsername = targetUsernameEt.text.toString()
                    socketRepository?.sendMessageToSocket(MessageModel(
                        type = "start_call",
                        name = username,
                        target = targetUsername,
                        data = null
                    ))
                } else {
                    Toast.makeText(
                        binding.root.context,
                        "Aranacak kişinin kullanıcı adını giriniz!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        }

//        rtcClient?.initializeSurfaceView(binding.localView)
//        rtcClient?.startLocalVideo(binding.localView)

    }

    override fun onNewMessage(message: MessageModel) {
        println("onNewMessage: $message")
        when(message.type){
            "call_response" -> {
                if(message.data == "offline"){
                    runOnUiThread {
                        Toast.makeText(
                            binding.root.context,
                            "Kullanıcı çevrimdışı, arama yapılamaz!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    //aranabilir
                    runOnUiThread {
                        setWhoToCallLayoutGone()
                        setCallLayoutVisible()
                        binding.apply {
                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                            rtcClient?.call(targetUsernameEt.text.toString())
                        }
                    }
                }
            }
            "offer_received" -> {
                runOnUiThread {
                    setIncomingCallLayoutVisible()
                    binding.incomingNameTv.text = "${message.name.toString()} arıyor..."
                    binding.acceptButton.setOnClickListener {
                        setIncomingCallLayoutGone()
                        setWhoToCallLayoutGone()
                        setCallLayoutVisible()

                        binding.apply {
                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                            targetUsername = message.name!!
                        }

                        val session = SessionDescription(
                            SessionDescription.Type.OFFER,
                            message.data.toString()
                        )
                        rtcClient?.onRemoteSessionReceived(session)
                        rtcClient?.answer(message.name!!)

                    }
                    binding.rejectButton.setOnClickListener {
                        setIncomingCallLayoutGone()
                    }
                    binding.remoteViewLoading.visibility = View.GONE
                }


            }
            "answer_received" -> {
                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                binding.remoteViewLoading.visibility = View.GONE
            }

            "ice_candidate" -> {
                runOnUiThread{
                    try {
                        val receivingCandidate = Gson().fromJson(
                            Gson().toJson(message.data),
                            IceCandidateModel::class.java
                        )
                        rtcClient?.addIceCandidate(
                            IceCandidate(
                                receivingCandidate.sdpMid,
                                Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),
                                receivingCandidate.sdpCandidate
                            )
                        )
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun setIncomingCallLayoutGone(){
        binding.incomingCallLayout.visibility = View.GONE
    }
    private fun setIncomingCallLayoutVisible(){
        binding.incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone(){
        binding.callLayout.visibility = View.GONE
    }
    private fun setCallLayoutVisible(){
        binding.callLayout.visibility = View.VISIBLE
    }

    private fun setWhoToCallLayoutGone(){
        binding.whoToCallLayout.visibility = View.GONE
    }
    private fun setWhoToCallLayoutVisible(){
        binding.whoToCallLayout.visibility = View.VISIBLE
    }
}