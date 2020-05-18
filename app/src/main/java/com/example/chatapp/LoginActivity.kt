package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.find
import org.jetbrains.anko.indeterminateProgressDialog

class LoginActivity : AppCompatActivity() {
    lateinit var progressBar:ProgressDialog
    lateinit var authenticate:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val toolBar: androidx.appcompat.widget.Toolbar=findViewById(R.id.login_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Sign In"

        val loginEmail:EditText=findViewById(R.id.loginEmail)
        val loginPass:EditText=findViewById(R.id.loginPass)
        val loginBtn:Button=findViewById(R.id.signinBtn)

        loginBtn.setOnClickListener {
            val email:String=loginEmail.text.toString()
            val password:String=loginPass.text.toString()

            if(email.isEmpty() || password.isEmpty())Toast.makeText(this,"Please fill all of the required fields first.",Toast.LENGTH_SHORT).show()
            else {
                progressBar=indeterminateProgressDialog("Signing In.. Please Wait!")
                progressBar.setCanceledOnTouchOutside(false)
                progressBar.show()
                logInUser(email,password)
            }
        }
    }

    private fun logInUser(email: String, password: String) {
        authenticate=FirebaseAuth.getInstance()
        authenticate.signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if(it.isSuccessful){
                progressBar.dismiss()
                Toast.makeText(this,"Signed In Successfully",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
            else {
                progressBar.hide()
                Toast.makeText(this,"Error Occurred. Please try again with correct credentials or check your network!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
