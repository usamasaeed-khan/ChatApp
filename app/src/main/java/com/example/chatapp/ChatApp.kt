package com.example.chatapp

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso


// For firebase offline data.

class ChatApp:Application(){
    override fun onCreate() {
        super.onCreate()


        //For keeping string data synced.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)


        //For picasso(images).
        val builder:Picasso.Builder=Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this,Int.MAX_VALUE.toLong()))
        val built:Picasso=builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled=true
        Picasso.setSingletonInstance(built)
    }
}
