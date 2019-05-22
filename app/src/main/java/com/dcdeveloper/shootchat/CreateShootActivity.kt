package com.dcdeveloper.shootchat

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.jar.Manifest

class CreateShootActivity : AppCompatActivity() {

    var createShootImageView: ImageView? = null
    var messageEditText: EditText? = null
    val imageName = UUID.randomUUID().toString() + ".jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_shoot)

        createShootImageView = findViewById(R.id.createShootImageView)
        messageEditText = findViewById(R.id.messageEditText)
    }

    fun getPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    fun chooseImageClicked(view: View) {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            getPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


            val selectedImage = data?.data



        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                createShootImageView?.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto()
            }
        }
    }

    fun nextClicked(view: View){
        // Get the data from an ImageView as bytes
        createShootImageView?.isDrawingCacheEnabled = true
        createShootImageView?.buildDrawingCache()
        val bitmap = (createShootImageView?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()




        val ref = FirebaseStorage.getInstance().getReference().child("images").child(imageName)
        var uploadTask = ref.putBytes(data)

        val urlTask = uploadTask.continueWithTask(   Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    Toast.makeText(this , "Upload failed" , Toast.LENGTH_SHORT).show()
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this , ChooseUserActivity::class.java)
                intent.putExtra("imageUrl" , task.result.toString())
                intent.putExtra("imageName" ,imageName)
                intent.putExtra("message" , messageEditText?.text.toString())
                startActivity(intent)
            } else {
                // Handle failures
                // ...
            }
        }
    }



}


