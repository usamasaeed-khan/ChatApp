package com.example.chatapp

import android.app.Activity
import android.content.AbstractThreadedSyncAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var authenticate:FirebaseAuth
    private lateinit var pagerAdapter:PagerAdapter
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authenticate=FirebaseAuth.getInstance()

        var toolBar: androidx.appcompat.widget.Toolbar=findViewById(R.id.main_page_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar!!.title="ChatApp"


        //Set Fragments Pager Adapter

        viewPager=findViewById(R.id.view_pager)
        pagerAdapter= PagerAdapter(supportFragmentManager)

        viewPager.adapter=pagerAdapter

        tabLayout=findViewById(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onStart() {
        super.onStart()
        var currentUser:FirebaseUser?=authenticate.currentUser

        //current user is null if the user is not logged in.

        if(currentUser==null){
            changeActivityToLogIn()
        }
    }

    private fun changeActivityToLogIn() {
        startActivity(Intent(this,StartActivity::class.java))

        //In this case, we have to go to the StartActivity to login the user.

        finish()

    }


    // Options menu contains multiple items(logout,acc settings etc) which are declared in the
    // main_menu.xml file.

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu,menu)
        return true

    }


    // When user select the options menu from the app bar.

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        if(item!!.itemId==R.id.logout_options_btn){
            FirebaseAuth.getInstance().signOut()
            changeActivityToLogIn()
        }
        if(item.itemId==R.id.acc_options_btn){
            startActivity(Intent(this,AccountSettings::class.java))
        }
        if(item.itemId==R.id.users_options_btn){
            startActivity(Intent(this,Users::class.java))
        }
        return true
    }

}



// For each section of viewpager used for fragments.

class PagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {


    override fun getItem(fragmentPosition: Int): Fragment {
        return when(fragmentPosition){
            0-> FriendRequests()
            1-> Chats()
            2-> Friends()
            else-> null!!
        }
    }

    override fun getCount(): Int {

        //Return number of fragments we have.

        return 3
    }


    // For the title of each fragment.
    override fun getPageTitle(fragmentPosition: Int):CharSequence?{
        return when(fragmentPosition){
            0-> "FRIEND REQUESTS"
            1-> "CHATS"
            2-> "FRIENDS"
            else-> null
        }
    }

}
