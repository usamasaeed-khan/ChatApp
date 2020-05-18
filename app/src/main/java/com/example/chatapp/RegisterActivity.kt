package com.example.chatapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.indeterminateProgressDialog


class RegisterActivity : AppCompatActivity() {

    lateinit var authenticate:FirebaseAuth
    lateinit var database:DatabaseReference
    private lateinit var progressBar:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        authenticate=FirebaseAuth.getInstance()


        val registerName:EditText=findViewById(R.id.registerName)
        val registerEmail:EditText=findViewById(R.id.registerEmail)
        val registerPass:EditText=findViewById(R.id.registerPass)
        val registerBtn:Button=findViewById(R.id.signupBtn)

        val toolBar:Toolbar=findViewById(R.id.register_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar!!.title="Create Account"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        registerBtn.setOnClickListener {

            val userName:String=registerName.text.toString()
            val userEmail:String=registerEmail.text.toString()
            val userPass:String=registerPass.text.toString()

            if(userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty())Toast.makeText(this,"Please fill all of the required fields first.",Toast.LENGTH_SHORT).show()
            else {
                progressBar=indeterminateProgressDialog("Registering User... Please Wait!")
                progressBar.show()
                progressBar.setCanceledOnTouchOutside(false)
                progressBar.show()
                registerUser(userName,userEmail,userPass)
            }
        }
    }

    private fun registerUser(userName: String, userEmail: String, userPass: String) {

        authenticate.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener { view ->
            if(view.isSuccessful){
                val currentUser: FirebaseUser? =FirebaseAuth.getInstance().currentUser
                val userId= currentUser?.uid

                //  database is the root key-value pair of our database, its child will be users.

                // Child of users is the user id of each user representing the specific user data.

                database=FirebaseDatabase.getInstance().reference.child("Users").child(userId!!)

                // Fill the user data map with user info.
                var userData=mutableMapOf("name" to userName,"status" to "Hey there! I'm using WhatsApp","thumb_image" to "default","thumb_image" to "default")

                database.setValue(userData).addOnCompleteListener {
                    if(it.isSuccessful){
                        progressBar.dismiss()
                        Toast.makeText(this,"Signed Up Successfully",Toast.LENGTH_SHORT).show()
                        var i=Intent(this,MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }
            }
            else {
                progressBar.hide()
                Toast.makeText(this,"Error Occurred. Please Try Again!",Toast.LENGTH_SHORT).show()
            }
        }

    }
}
