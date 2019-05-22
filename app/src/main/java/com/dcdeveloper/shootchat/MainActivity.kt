package com.dcdeveloper.shootchat

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jp.wasabeef.glide.transformations.BlurTransformation

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnKeyListener {

    var signUpModeActive: Boolean = true
    var loginTextView: TextView? = null
    var emailEditText: EditText? = null
    var passwordEditText: EditText? = null
    var logoView: ImageView? = null
    val mAuth = FirebaseAuth.getInstance()
    var backgroundView: ImageView? = null
    var backgroundLayout: ConstraintLayout? = null


    override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
            signUpClicked(view)
        }
        return false
    }

    override fun onClick(view: View) {
        if (view.id == R.id.loginTextView) {
            Log.i("Switch", "Was tapped")

            val signUpButton = findViewById(R.id.signUpButton) as Button

            if (signUpModeActive) {
                signUpModeActive = false
                signUpButton.text = "Login"
                loginTextView!!.setText("or, Sign up")
            } else {
                signUpModeActive = true
                signUpButton.text = "Sign up"
                loginTextView!!.setText("or, Login")
            }
        } else if (view.id == R.id.logoView || view.id == R.id.backgroundLayout) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if(inputMethodManager != null && currentFocus != null){
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //supportActionBar!!.hide()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        backgroundView = findViewById(R.id.backgroundView)

        loginTextView = findViewById(R.id.loginTextView)
        loginTextView!!.setOnClickListener(this)

        logoView = findViewById(R.id.logoView)
        backgroundLayout = findViewById(R.id.backgroundLayout)
        logoView!!.setOnClickListener(this)
        backgroundLayout?.setOnClickListener(this)
        passwordEditText?.setOnKeyListener(this)

        Glide.with(this).load(R.drawable.sep)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 1)))
            .into(backgroundView!!)

        if (mAuth.currentUser != null) {
            logIn()
        }

    }

    fun signUpClicked(view: View) {
        emailEditText = findViewById(R.id.emailEditText) as EditText
        passwordEditText = findViewById(R.id.passwordEditText) as EditText

        if (emailEditText?.getText().toString().matches("".toRegex())
            || passwordEditText?.getText().toString().matches("".toRegex())) {
            Toast.makeText(this, "A username and a password are required.", Toast.LENGTH_SHORT).show()
        } else {
            if (signUpModeActive) {

                //sign up the user
                mAuth.createUserWithEmailAndPassword(emailEditText?.text.toString(), passwordEditText?.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            //Add to database

                            FirebaseDatabase.getInstance().getReference().child("users").child(task.result!!.user.uid)
                                .child("email").setValue(emailEditText?.text.toString())

                            Toast.makeText(this, "Account succesfully created. Please log in.", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(this, "Sign-up Failed. Try Again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                //Login
                mAuth.signInWithEmailAndPassword(emailEditText?.text.toString(), passwordEditText?.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            logIn()
                        } else {
                            Toast.makeText(this, "Login Failed. Try Again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
    fun logIn() {
        // Move to next Activity
        val intent = Intent(this, ShootsActivity::class.java)
        startActivity(intent)
    }
}
