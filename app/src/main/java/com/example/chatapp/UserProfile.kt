package com.example.chatapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.firebase.ui.database.paging.FirebaseDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import org.jetbrains.anko.indeterminateProgressDialog
import java.text.DateFormat
import java.util.*

class UserProfile : AppCompatActivity() {
    lateinit var progressBar: ProgressDialog
    private var friendshipStatus:String="not friends"
    //lateinit var currentUser:FirebaseUser

    //Database reference for friend requests data.

    lateinit var requestsDatabase:DatabaseReference
    lateinit var friendsDatabase:DatabaseReference
    lateinit var notificationsDatabase:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        progressBar=indeterminateProgressDialog("Loading User Profile...")
        progressBar.setCanceledOnTouchOutside(false)
        progressBar.show()



        friendsDatabase=FirebaseDatabase.getInstance().reference.child("Friends")
        notificationsDatabase=FirebaseDatabase.getInstance().reference.child("Notifications")
        val currentUser= FirebaseAuth.getInstance().currentUser!!
        val userProfileName=findViewById<TextView>(R.id.user_profile_name)
        val userProfileStatus=findViewById<TextView>(R.id.user_profile_status)
        val userProfileImage=findViewById<ImageView>(R.id.user_profile_image)
        val userProfileFriends=findViewById<TextView>(R.id.user_profile_friends)
        val sendFriendRequest=findViewById<Button>(R.id.user_profile_send_request)
        val declineReqBtn=findViewById<Button>(R.id.user_profile_decline_request)

        //Id of the user whose profile the current user is visiting.
        val profileId=intent.getStringExtra("userId")


        declineReqBtn.visibility= View.INVISIBLE
        declineReqBtn.isEnabled=false



        //create a reference to get current user's profile info.
        val database=FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        requestsDatabase=FirebaseDatabase.getInstance().reference.child("FriendRequests")
        database.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {


            }

            override fun onDataChange(p0: DataSnapshot) {

                val userProfileNameDB=p0.child("name").value.toString()
                val userProfileStatusDB=p0.child("status").value.toString()
                val userProfileImageDB=p0.child("image").value.toString()

                userProfileName.text=userProfileNameDB
                userProfileStatus.text=userProfileStatusDB
                if(userProfileImageDB!="default")Picasso.get().load(userProfileImageDB).placeholder(R.drawable.male).into(userProfileImage)
                else userProfileImage.setImageResource(R.drawable.male)

                //Check friendship status by searching.

                requestsDatabase.child(currentUser.uid).addListenerForSingleValueEvent(
                    object:ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {

                            //check if current user has visited profile's id or nit.
                            if(p0.hasChild(profileId)){
                                val requestType:String=p0.child(profileId).child("request_type").value.toString()
                                if(requestType == "received"){


                                    friendshipStatus="req_received"
                                    sendFriendRequest.text="ACCEPT REQUEST"

                                    declineReqBtn.visibility= View.VISIBLE
                                    declineReqBtn.isEnabled=true

                                }
                                else if(requestType=="sent"){
                                    friendshipStatus="req_state"
                                    sendFriendRequest.text="CANCEL REQUEST"
                                    declineReqBtn.visibility= View.INVISIBLE
                                    declineReqBtn.isEnabled=false
                                }

                                progressBar.dismiss()

                            }
                            else {

                                friendsDatabase.child(currentUser.uid).addListenerForSingleValueEvent(object:ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot) {

                                        if(p0.hasChild(profileId)){

                                            sendFriendRequest.isEnabled=true
                                            friendshipStatus="friends"

                                            declineReqBtn.visibility= View.INVISIBLE
                                            declineReqBtn.isEnabled=false

                                            FirebaseDatabase.getInstance().reference.child("Users").child(profileId).child("name").addListenerForSingleValueEvent(object:ValueEventListener{
                                                override fun onDataChange(p0: DataSnapshot) {

                                                    val currentUserName=p0.value.toString()
                                                    val currentUserFirstName=currentUserName.split(" ")
                                                    sendFriendRequest.text="UNFRIEND ${currentUserFirstName[0]}"


                                                }

                                                override fun onCancelled(p0: DatabaseError) {


                                                }
                                            })


                                        }
                                        progressBar.dismiss()

                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                        progressBar.dismiss()

                                    }
                                })

                            }



                        }

                        override fun onCancelled(p0: DatabaseError) {


                        }
                    }
                )




            }
        })


        sendFriendRequest.setOnClickListener {
            sendFriendRequest.isEnabled=false



            // When both users are not friends.
            if(friendshipStatus=="not friends"){


                //Create a child of the visited user in the current user's friend req list.
                requestsDatabase.child(currentUser.uid).child(profileId).child("request_type").setValue("sent").addOnCompleteListener { task ->

                    if(task.isSuccessful){

                        //Create child of the current user in the visited user's profile.
                        requestsDatabase.child(profileId).child(currentUser.uid).child("request_type").setValue("received").addOnCompleteListener {
                            if(it.isSuccessful){

                                //Create a child of visited user's profile in order to send him notification.

                                var notificationsMap= mutableMapOf<String,String>(
                                    "from" to currentUser.uid,

                                    // if you want to add multiple types of notifications you can just change the type child.
                                    "type" to "request"

                                )

                                //.push() creates a random id for every new notification.
                                notificationsDatabase.child(profileId).push().setValue(notificationsMap).addOnSuccessListener {

                                    friendshipStatus="req_sent"
                                    sendFriendRequest.text="CANCEL REQUEST"

                                    declineReqBtn.visibility= View.INVISIBLE
                                    declineReqBtn.isEnabled=false

                                }.addOnFailureListener {
                                    Toast.makeText(this,"Unable to send request, check your connection and try again!",Toast.LENGTH_LONG).show()
                                }





                                Toast.makeText(this,"Request Sent Successfully!",Toast.LENGTH_LONG).show()
                            }

                        }

                    }
                    else {
                        Toast.makeText(this,"Couldn't Send Request. Check your connection and try again!",Toast.LENGTH_LONG).show()
                    }

                    sendFriendRequest.isEnabled=true

                }
            }


            // Cancel Friend Request.
            if(friendshipStatus=="req_sent"){

                requestsDatabase.child((currentUser.uid)).child(profileId).removeValue().addOnCompleteListener {

                    if(it.isSuccessful){

                        requestsDatabase.child(profileId).child(currentUser.uid).removeValue().addOnCompleteListener {


                            friendshipStatus="not friends"
                            sendFriendRequest.text="SEND REQUEST"

                            declineReqBtn.visibility= View.INVISIBLE
                            declineReqBtn.isEnabled=false

                        }

                    }

                    sendFriendRequest.isEnabled=true

                }

            }

            // Accept Friend Request.

            if(friendshipStatus=="req_received"){



                val currentDate:String=DateFormat.getDateInstance().format(Date())
                friendsDatabase.child(currentUser.uid).child(profileId).setValue(currentDate).addOnCompleteListener { task ->

                    if(task.isSuccessful){

                        friendsDatabase.child(profileId).child(currentUser.uid).setValue(currentDate).addOnCompleteListener { task2 ->

                            if(task2.isSuccessful){
                                requestsDatabase.child(currentUser.uid).child(profileId).removeValue().addOnCompleteListener { task3 ->

                                    if(task3.isSuccessful){

                                        requestsDatabase.child(profileId).child(currentUser.uid).removeValue().addOnCompleteListener {

                                            if(it.isSuccessful){
                                                sendFriendRequest.isEnabled=true
                                                friendshipStatus="friends"

                                                FirebaseDatabase.getInstance().reference.child("Users").child(profileId).child("name").addListenerForSingleValueEvent(object:ValueEventListener{
                                                    override fun onDataChange(p0: DataSnapshot) {

                                                        val currentUserName=p0.value.toString()
                                                        val currentUserFirstName=currentUserName.split(" ")
                                                        sendFriendRequest.text="UNFRIEND ${currentUserFirstName[0]}"

                                                        declineReqBtn.visibility= View.INVISIBLE
                                                        declineReqBtn.isEnabled=false

                                                    }

                                                    override fun onCancelled(p0: DatabaseError) {


                                                    }
                                                })

                                                Toast.makeText(this,"Friend Added Successfully!",Toast.LENGTH_LONG).show()

                                            }


                                        }

                                    }


                                }
                            }

                        }
                    }

                }

            }

        }
    }
}
