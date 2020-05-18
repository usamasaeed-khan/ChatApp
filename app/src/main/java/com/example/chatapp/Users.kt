package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class Users : AppCompatActivity() {

    private lateinit var database:DatabaseReference
    private lateinit var toolBar:androidx.appcompat.widget.Toolbar
    private lateinit var users:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        database=FirebaseDatabase.getInstance().reference.child("Users")

        toolBar=findViewById(R.id.user_app_bar)
        setSupportActionBar(toolBar)
        supportActionBar!!.title="All Users"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        users=findViewById(R.id.recycler_view_users)
        //users.setHasFixedSize(true)
        users.layoutManager=LinearLayoutManager(this)

    }

    override fun onStart() {
        super.onStart()



        val parameters=FirebaseRecyclerOptions.Builder<UsersClass>()
            .setQuery(database,UsersClass::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter=
            object:FirebaseRecyclerAdapter<UsersClass,UsersViewHolder>(parameters){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder
                    =UsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.users_layout,parent,false))

                override fun onBindViewHolder(p0: UsersViewHolder, position: Int, p2: UsersClass) {

                    //val currentUser=FirebaseAuth.getInstance().currentUser

                    /*if(getRef(position).key!=currentUser!!.uid) { */


                        p0.userNameView.text = p2.name
                        p0.allUsersStatus.text = p2.status

                        // Due to many users, we retrieve only their compressed\
                        // images that are thumbnails.

                        if (p2.thumb_image != "default") Picasso.get().load(p2.thumb_image).placeholder(R.drawable.male).into(
                            p0.imageTitle
                        )
                        else p0.imageTitle.setImageResource(R.drawable.male)


                        p0.itemView.setOnClickListener {
                            val userId = getRef(position).key

                            val sendToProfile = Intent(this@Users, UserProfile::class.java)
                            sendToProfile.putExtra("userId", userId)
                            startActivity(sendToProfile)
                        }
                    //}
                    //else {
                        //p0.itemView.visibility=View.GONE
                    //}
                }
            }

        users.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }


    class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameView: TextView =itemView.findViewById(R.id.all_users_name)
        val imageTitle: ImageView =itemView.findViewById(R.id.all_users_profile_img)
        val allUsersStatus:TextView=itemView.findViewById(R.id.all_users_status)
    }


}
