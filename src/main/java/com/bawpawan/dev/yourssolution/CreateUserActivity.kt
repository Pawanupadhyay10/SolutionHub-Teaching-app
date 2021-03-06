package com.bawpawan.dev.yourssolution

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_user.*
import model.TIMESTAMP
import model.USERNAME
import model.USERS

class CreateUserActivity : AppCompatActivity() {
    lateinit var Auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        Auth = FirebaseAuth.getInstance()
    }

    fun Clicked(view: View) {
        singup()
    }

    fun cancelclicked(view: View) {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
    }

    fun singup() {
        val emailsingup = Createloginemail.text.toString().trim()
        val passsinghup = CreateloginPassword.text.toString().trim()
        val username = Createusername.text.toString().trim()
        if (emailsingup.isNotEmpty() && passsinghup.isNotEmpty() && username.isNotEmpty()) {
            Auth.createUserWithEmailAndPassword(emailsingup, passsinghup)
                .addOnSuccessListener { result ->
                    // user created
                    hidekeyboard()
                    val changerequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    result.user?.updateProfile(changerequest)
                        ?.addOnFailureListener {
                            // Log.e("EXCEPTION", "failure user creation :${exception.message}")
                        }
                    val data = HashMap<String, Any>()
                    data.put(USERNAME, username)
                    data.put(TIMESTAMP, FieldValue.serverTimestamp())
                    result.user?.uid?.let {
                        FirebaseFirestore.getInstance().collection(USERS).document(it)
                    }?.set(data)
                        ?.addOnSuccessListener {
                            finish()
                            Toast.makeText(
                                this,
                                "Welcome and Remember your Passward",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }.addOnFailureListener {
                    //Log.e("EXCEPTION", "failure user creation :${exception.message}")
                }
        } else {
            Toast.makeText(
                this,
                "Fill the All Fields And make sure that Passward must be of Lenght Greater than Six",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    fun hidekeyboard() {
        val inputmanager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputmanager.isAcceptingText) {
            inputmanager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}