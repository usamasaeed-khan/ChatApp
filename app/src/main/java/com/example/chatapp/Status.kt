package com.example.chatapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.indeterminateProgressDialog

class Status : AppCompatActivity() {
    private lateinit var database:DatabaseReference
    lateinit var progressBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        var currentStatus:String=intent.getStringExtra("currentStatus")


        database=FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)

        val updateStatusInput:EditText=findViewById(R.id.status_update_input)
        val updateStatusBtn:Button=findViewById(R.id.status_update_btn)
        updateStatusInput.setText(intent.getStringExtra("currentStatus"))

        val toolBar: androidx.appcompat.widget.Toolbar=findViewById(R.id.edit_status_app_bar)
        setSupportActionBar(toolBar)
        supportActionBar!!.title="Update Status"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        updateStatusBtn.setOnClickListener {
            progressBar=indeterminateProgressDialog("Updating Status...")
            progressBar.setCanceledOnTouchOutside(false)
            progressBar.show()

            val updatedStatus:String=updateStatusInput.text.toString()
            database.child("status").setValue(updatedStatus).addOnCompleteListener {
                if(it.isSuccessful){
                    progressBar.dismiss()
                    Toast.makeText(this,"Status Updated Successfully!",Toast.LENGTH_LONG).show()
                    updateStatusInput.text.clear()
                }
                else {
                    Toast.makeText(this,"Update Status Failed, check connection and try again.",Toast.LENGTH_LONG).show()
                }
            }

        }

    }
}
