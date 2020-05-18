package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button

import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        var registerBtn: Button =findViewById(R.id.registerBtn)
        registerBtn.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }
        var loginBtn: Button =findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

}
