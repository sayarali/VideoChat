package com.alisayar.videochat

import android.app.Application
import android.view.SurfaceView
import com.alisayar.videochat.models.MessageModel
import org.webrtc.*

class RTCClient(
    private val application: Application,
    private val username: String,
    private val socketRepository: SocketRepository,
    private val observer: PeerConnection.Observer
) {
    private val eglContext = EglBase.create()

    private val peerConnectionFactory by lazy {
        createPeerConnectionFactory()
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478")
            .createIceServer()
    )

    private val peerConnection by lazy {
        createPeerConnection(observer)
    }

    private val localVideoSource by lazy {
        peerConnectionFactory.createVideoSource(false)
    }
    private val localAudioSource by lazy {
        peerConnectionFactory.createAudioSource(MediaConstraints())
    }

    init {
        initPeerConnectionFactory(application)
    }

    private fun initPeerConnectionFactory(application: Application){
        val peerConnectionOption = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(peerConnectionOption)
    }

    private fun createPeerConnectionFactory() : PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(eglContext.eglBaseContext, true, true)
            )
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglContext.eglBaseContext)
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }


    fun initializeSurfaceView(surface: SurfaceViewRenderer){
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext.eglBaseContext, null)
        }
    }

    fun startLocalVideo(surface: SurfaceViewRenderer){
        val surfaceTextureHelper = SurfaceTextureHelper
            .create(Thread.currentThread().name, eglContext.eglBaseContext)
        val videoCapturer = getVideoCapturer(application)
        videoCapturer.initialize(surfaceTextureHelper, surface.context, localVideoSource.capturerObserver)
        videoCapturer.startCapture(320, 240, 30)
        val localVideoTrack = peerConnectionFactory.createVideoTrack("local_track", localVideoSource)
        localVideoTrack.addSink(surface)
        val localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio_track", localAudioSource)
        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)


        peerConnection?.addStream(localStream)
    }

    private fun getVideoCapturer(application: Application): VideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw java.lang.IllegalStateException()
        }
    }

    fun call(targetUsername: String) {
        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        peerConnection?.createOffer(object: SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection?.setLocalDescription(object: SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {

                    }

                    override fun onSetSuccess() {
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )

                        socketRepository.sendMessageToSocket(MessageModel(
                            type = "create_offer",
                            name = username,
                            target = targetUsername,
                            data = offer
                        ))
                    }

                    override fun onCreateFailure(p0: String?) {

                    }

                    override fun onSetFailure(p0: String?) {

                    }

                }, desc)
            }

            override fun onSetSuccess() {

            }

            override fun onCreateFailure(p0: String?) {

            }

            override fun onSetFailure(p0: String?) {

            }

        }, mediaConstraints)
    }

    fun onRemoteSessionReceived(session: SessionDescription) {
        peerConnection?.setRemoteDescription(object: SdpObserver{
            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetFailure(p0: String?) {
            }

        }, session)


    }

    fun answer(targetUsername: String) {
        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        peerConnection?.createAnswer(object: SdpObserver{
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection?.setLocalDescription(object: SdpObserver{
                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }

                    override fun onSetSuccess() {
                        val answer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )
                        socketRepository.sendMessageToSocket(
                            MessageModel(
                                type = "create_answer",
                                name = username,
                                target = targetUsername,
                                data = answer
                            )
                        )
                    }

                    override fun onCreateFailure(p0: String?) {
                    }

                    override fun onSetFailure(p0: String?) {
                    }

                }, desc)
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetFailure(p0: String?) {
            }

        }, mediaConstraints)
    }

    fun addIceCandidate(p0: IceCandidate?) {
        peerConnection?.addIceCandidate(p0)
    }

}