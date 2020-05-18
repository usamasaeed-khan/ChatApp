package com.example.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

class AccountSettings : AppCompatActivity() {
    private lateinit var database:DatabaseReference
    private lateinit var currentUser:FirebaseUser

    private lateinit var profileImage:CircleImageView
    private lateinit var userName:TextView
    private lateinit var userStatus:TextView
    private lateinit var imageBtn:Button
    private val REQUEST_CODE=1
    private lateinit var progressDialog:ProgressDialog

    private lateinit var imgUrl:Task<Uri>

    //Storage reference.
    private lateinit var storage:StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)


        //storage pointing towards root of the firebase storage currently.
        storage=FirebaseStorage.getInstance().reference

        val updateStatusActivity:Button=findViewById(R.id.status_btn)
        updateStatusActivity.setOnClickListener {
            val i=Intent(this,Status::class.java)
            i.putExtra("currentStatus",userStatus.text.toString())
            startActivity(i)
        }


        profileImage=findViewById(R.id.profile_image)
        userName=findViewById(R.id.user_name)
        userStatus=findViewById(R.id.user_status)
        imageBtn=findViewById(R.id.image_btn)

        currentUser= FirebaseAuth.getInstance().currentUser!!
        database=FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)
        database.keepSynced(true)
        database.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dbSnapShot: DataSnapshot) {

                userName.text=dbSnapShot.child("name").value.toString()
                userStatus.text=dbSnapShot.child("status").value.toString()
                if(dbSnapShot.child("thumb_image").value.toString()!="default"){
                    //Picasso.get().load(dbSnapShot.child("thumb_image").value.toString()).placeholder(R.drawable.male).into(profileImage)

                    //For offline image.
                    Picasso.get().load(dbSnapShot.child("thumb_image").value.toString()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.male).into(profileImage,object:com.squareup.picasso.Callback{
                        override fun onSuccess() {
                            // Do nothing if the offline image is loaded successfully.
                        }

                        override fun onError(e: Exception?) {

                            // Load online image if offline feature fails.

                            Picasso.get().load(dbSnapShot.child("thumb_image").value.toString()).placeholder(R.drawable.male).into(profileImage)

                        }

                    })
                }
                else profileImage.setImageResource(R.drawable.male)
            }
        })

        imageBtn.setOnClickListener {
            // 1- Create your own intent.
            // Lets user select from documents.

            val selectImgActivity=Intent()
            selectImgActivity.type="thumb_image/*"
            selectImgActivity.action=Intent.ACTION_GET_CONTENT

            //createChooser is used to let the user select thumb_image
            // from the documents of his phone.
            startActivityForResult(Intent.createChooser(selectImgActivity,"Select Profile Picture"),REQUEST_CODE)

            // 2- Use the library to get cropped photo.
            // Lets user select from all all default gallery apps.

           /* CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)*/
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK){
            val imageUri:Uri?=data?.data

            CropImage.activity(imageUri).setAspectRatio(1,1).start(this)

        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val croppedPhoto:CropImage.ActivityResult=CropImage.getActivityResult(data)
            progressDialog=indeterminateProgressDialog("Uploading Image.. Please Wait!")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            if(resultCode== Activity.RESULT_OK){
                val resultImgUri:Uri?=croppedPhoto.uri

                //Compressing thumb_image

                //get file from uri.
                val thumbnailFile=File(resultImgUri!!.path)

                //MaxWidth,MaxHeight,Quality can be changed, refer to the git link of the lib.

                val compressedImageBitmap=Compressor(this)
                    .setMaxWidth(200) //200 pixels.
                    .setMaxHeight(200)
                    .setQuality(75) //75% quality.
                    .compressToBitmap(thumbnailFile)

                //Upload to firebase.

                val byteArrayOutputStream=ByteArrayOutputStream()
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
                val byteArray=byteArrayOutputStream.toByteArray()

                // Path to upload the thumbnail thumb_image.
                val thumbnailPath:StorageReference=storage.child("user_profile_pictures").child("thumbnails").child("${currentUser.uid}.jpg")

//                val uploadTask:UploadTask=thumbnailPath.putBytes(byteArray)
//                uploadTask.addOnCompleteListener {
//                    if(it.isSuccessful){
//
//                    }
//                }



                val dirPath:StorageReference=storage.child("user_profile_pictures").child("${currentUser.uid}.jpg")


                //Upload thumb_image.
                dirPath.putFile(resultImgUri).continueWithTask {
                    dirPath.downloadUrl
                }.addOnSuccessListener { Uri ->
                    val imageUrl=Uri.toString() //get download url of thumb_image.


                    //Upload thumbnail.
                    val uploadTask:UploadTask=thumbnailPath.putBytes(byteArray)
                    uploadTask.continueWithTask {
                        thumbnailPath.downloadUrl
                    }.addOnSuccessListener {
                        val thumbUrl=Uri.toString()

                        // Creating a map to update thumb_image and thumbnail in the database.

                        val updateDatabaseMap= mutableMapOf("thumb_image" to imageUrl,"thumb_image" to thumbUrl)

                        database.updateChildren(updateDatabaseMap as Map<String, Any>).addOnCompleteListener {
                            if(it.isSuccessful){
                                progressDialog.dismiss()
                                Toast.makeText(this,"Image Uploaded Successfully!",Toast.LENGTH_LONG).show()

                            }
                        }


                    }.addOnFailureListener {
                        Toast.makeText(this,"Error Uploading Thumbnail Image! Check your connection and try again.",Toast.LENGTH_LONG).show()
                        progressDialog.dismiss()
                    }


//                    database.child("thumb_image").setValue(imageUrl).addOnCompleteListener {
//                        if(it.isSuccessful){
//                            progressDialog.dismiss()
//                            Toast.makeText(this,"Image Uploaded Successfully!",Toast.LENGTH_LONG).show()
//                        }
//                    }
                }.addOnFailureListener {
                    Toast.makeText(this,"Error Uploading Image! Check your connection and try again.",Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val e:Exception=croppedPhoto.error
                Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }
        }
    }
}
