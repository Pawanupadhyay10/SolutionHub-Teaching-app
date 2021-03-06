package com.bawpawan.dev.yourssolution

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var Auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(findViewById(R.id.toolbar))
        Auth = FirebaseAuth.getInstance()
        forgetpasswardbtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            val view = layoutInflater.inflate(R.layout.dilog_box, null)
            val recoveremail = view.findViewById<EditText>(R.id.recoverd_email)
            builder.setView(view)
            builder.setPositiveButton("Reset", DialogInterface.OnClickListener { _, _ ->
                val emailrec = recoveremail.text.toString().trim()
                forgotPassword(emailrec)
            })
            builder.setNegativeButton("close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }
    }

    fun forgotPassword(emailrec: String) {
        Auth.sendPasswordResetEmail(emailrec)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email sent.Check Your Inbox", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                //  Log.e("EC","ERoRR:${exception.localizedMessage}")
            }
    }

    override fun onStart() {
        super.onStart()
        val currentuser = Auth.currentUser
        if (currentuser != null) {
            UpadateUi(currentuser)
        }
    }

    fun CREateUSER(view: View) {
        val i = Intent(this, CreateUserActivity::class.java)
        startActivity(i)
    }

    fun LOGINUSER(view: View) {
        hidekeyboard()
        login()
    }

    private fun login() {
        val loginemail = loginemail.text.toString()
        val passlogin = loginPassword.text.toString()
        if (loginemail.isNotEmpty() && passlogin.isNotEmpty()) {
            Auth.signInWithEmailAndPassword(loginemail, passlogin)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = Auth.currentUser
                        UpadateUi(user!!)
                    } else {
                        Toast.makeText(
                            this,
                            "Go And Create Account" + "OR Enter Correct Value Pair",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    //  Log.e("EXCEPTION", "failure user creation :${exception.message}")
                }
        } else {
            Toast.makeText(
                this, "Enter Correct Value Pair",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun UpadateUi(firebaseUser: FirebaseUser?) {
        val i = Intent(this, SplashActivity::class.java)
        startActivity(i)
    }

    private fun hidekeyboard() {
        val inputmanager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputmanager.isAcceptingText) {
            inputmanager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        Timmer1()
    }

    fun Timmer1() {
        var counter = 2
        logintimmer.visibility = View.VISIBLE
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                logintimmer.text = counter.toString()
                counter--
            }

            override fun onFinish() {
                logintimmer.text = getString(R.string.done__)
                logintimmer.visibility = View.INVISIBLE
            }
        }.start()
    }
}