package com.alisayar.videochat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.alisayar.videochat.databinding.ActivityCallBinding
import com.alisayar.videochat.models.MessageModel
import com.alisayar.videochat.util.NewMessageInterface

class CallActivity : AppCompatActivity(), NewMessageInterface {

    lateinit var binding: ActivityCallBinding
    private var username: String? = null

    private var socketRepository: SocketRepository? = null
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
    }

    override fun onNewMessage(message: MessageModel) {

    }
}