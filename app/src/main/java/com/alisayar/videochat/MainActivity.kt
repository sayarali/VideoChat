package com.alisayar.videochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alisayar.videochat.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usernameButton.setOnClickListener {

            if(binding.username.text.toString() != "") {
                val intent = Intent(this, CallActivity::class.java)
                    .putExtra("username", binding.username.text.toString())
                startActivity(intent)
            } else {
                Snackbar.make(binding.root, "Kullanıcı adınızı giriniz!", Snackbar.LENGTH_LONG).show()
            }



        }



    }
}