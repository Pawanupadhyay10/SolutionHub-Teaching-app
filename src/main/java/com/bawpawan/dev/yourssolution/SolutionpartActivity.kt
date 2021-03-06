package com.bawpawan.dev.yourssolution

import Commentspack.Comments
import Commentspack.CommentsAdapter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_solutionpart.*
import model.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class SolutionpartActivity : AppCompatActivity(), View.OnClickListener {
    val Channel_id = "channelid"
    val channelname = "ChannelName"
    val Notificationid = 0
    var path: Uri? = null
    val uniqueId = UUID.randomUUID().toString()
    val firebaseStore = FirebaseStorage.getInstance()
    val storageReference = firebaseStore.getReference()
    var questiondocid: String? = null
    val PICK_IMAGE_REQUEST = 1
    lateinit var commentsAdapter: CommentsAdapter
    var Commentss = arrayListOf<Comments>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solutionpart)
        supportActionBar?.title = "SOLUTION"
        createNotificationChannel()

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        questiondocid = intent.getStringExtra(DOCUMENT_ID_KEY)
        // val txt = intent.getStringArrayExtra(QUESTIONTXT)
        //val pic = intent.getStringArrayExtra(IMAGEURI)
        solutionpicuploadbtn.setOnClickListener(this)
        solutiontextSendbtn.setOnClickListener(this)
        commentsAdapter = CommentsAdapter(Commentss)
        SolutionRecyclerView.adapter = commentsAdapter
        val layoutManager = LinearLayoutManager(this)
        SolutionRecyclerView.layoutManager = layoutManager
    }

    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channelname)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Channel_id, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun gallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onClick(v: View?) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingintent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notifaction = NotificationCompat.Builder(this, Channel_id)
            .setContentTitle("New Answer ")
            .setContentText("Go and Check Solution And Like the Post")
            .setSmallIcon(R.drawable.notify)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingintent)
            .build()
        val noyificationmanager = NotificationManagerCompat.from(this)
        when (v?.id) {
            R.id.solutionpicuploadbtn -> {//images
                noyificationmanager.notify(Notificationid, notifaction)
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        PICK_IMAGE_REQUEST
                    )
                } else {
                    Toast.makeText(this, "Pls Wait IT May Take Few Seconds", Toast.LENGTH_SHORT)
                        .show()
                    gallery()
                }
                RETeriveimagestochatbox()
            }
            R.id.solutiontextSendbtn -> {//text msgs
                noyificationmanager.notify(Notificationid, notifaction)
                Toast.makeText(
                    this, "Pls Wait IT May Take Few Seconds" +
                            "And empty fields not allowed", Toast.LENGTH_SHORT
                ).show()
                addUploadRecordToDb("")
                RETeriveimagestochatbox()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gallery()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        RETeriveimagestochatbox()
    }

    private fun RETeriveimagestochatbox() {
        FirebaseFirestore.getInstance().collection(QUESTION_REF)
            .document(questiondocid.toString()).collection(COMMENT_REF)
            .orderBy(TIMESTAMP, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Log.e("EXception", "Could not add reterive${exception.localizedMessage}")
                }
                if (snapshot != null) {
                    Commentss.clear()
                    for (document in snapshot.documents) {
                        val data = document.data
                        val username = data?.get(USERNAME) as String
                        val imagechat = data.get(SOLUTIONIMAGES)
                        val timestamp = data.get(TIMESTAMP) as Timestamp
                        val commentstxt = data.get(COMMENT_TXT)
                        val newComment = Commentspack.Comments(
                            username,
                            commentstxt.toString(),
                            timestamp.toDate(),
                            imagechat.toString()
                        )
                        Commentss.add(newComment)
                    }
                    commentsAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timmer1()
        if (data == null || data.data == null) {
            return
        }
        path = data.data
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                // To handle deprication use
                val source = path?.let { ImageDecoder.createSource(contentResolver, it) }
                val bitmap = ImageDecoder.decodeBitmap(source!!)
                solutionpostimages.setImageBitmap(bitmap)
            } else {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, path)
                solutionpostimages.setImageBitmap(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        storageimages()
    }

    fun storageimages() {
        if (path != null) {
            val refStore = storageReference.child("MediaCapture/" + uniqueId + ".jpeg")
            val uploadTask = refStore.putFile(path!!)

            val urlTask =
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation refStore.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val pic: String? = downloadUri.toString()
                        addUploadRecordToDb(pic)
                    }
                }.addOnFailureListener {
                    // Log.e("EXCEPTION","error ${e.localizedMessage}")
                }
        }
    }

    fun addUploadRecordToDb(picsolution: String?) {
        val commenttxt: String = Givesolution.text.toString().trim()
        if (commenttxt.isNotEmpty()) {
            val questionref = FirebaseFirestore.getInstance().collection(QUESTION_REF)
                .document(questiondocid.toString())
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val solquestion = transaction.get(questionref)
                val Numcomments = solquestion.getLong(NUM_COMMENTS)?.plus(1)
                transaction.update(questionref, NUM_COMMENTS, Numcomments)
                val newCommentref = FirebaseFirestore.getInstance().collection(QUESTION_REF)
                    .document(questiondocid.toString())
                    .collection(COMMENT_REF).document()
                val data = HashMap<String, Any>()
                data.put(COMMENT_TXT, commenttxt)
                data.put(SOLUTIONIMAGES, picsolution!!)
                data.put(TIMESTAMP, FieldValue.serverTimestamp())
                data.put(USERNAME, FirebaseAuth.getInstance().currentUser?.displayName.toString())
                transaction.set(newCommentref, data)
            }.addOnSuccessListener {
                Givesolution.setText(" ")
                hidekeyboard()
            }.addOnFailureListener {
                // Log.e("EXception", "Could not add comment${exception.localizedMessage}")
                //Log.e("EXCEPTION","error ${e.localizedMessage}")
            }
        } else if (commenttxt.isEmpty() && picsolution!!.isNotEmpty()) {
            val questionref = FirebaseFirestore.getInstance().collection(QUESTION_REF)
                .document(questiondocid.toString())
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val solquestion = transaction.get(questionref)
                val Numcomments = solquestion.getLong(NUM_COMMENTS)?.plus(1)
                transaction.update(questionref, NUM_COMMENTS, Numcomments)
                val newCommentref = FirebaseFirestore.getInstance().collection(QUESTION_REF)
                    .document(questiondocid.toString())
                    .collection(COMMENT_REF).document()
                val data = HashMap<String, Any>()
                data.put(COMMENT_TXT, commenttxt)
                data.put(SOLUTIONIMAGES, picsolution)
                data.put(TIMESTAMP, FieldValue.serverTimestamp())
                data.put(USERNAME, FirebaseAuth.getInstance().currentUser?.displayName.toString())
                transaction.set(newCommentref, data)
            }.addOnSuccessListener {
                Givesolution.setText(" ")
                hidekeyboard()
            }.addOnFailureListener {
                // Log.e("EXception", "Could not add comment${exception.localizedMessage}")
                //Log.e("EXCEPTION","error ${e.localizedMessage}")
            }
        } else {
            Toast.makeText(
                this,
                "Empty Messages Not Allowed and Pic Not Selected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun hidekeyboard() {
        val inputmanager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputmanager.isAcceptingText) {
            inputmanager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun Timmer1() {
        var counter = 10
        Showtimmer.visibility = View.VISIBLE
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Showtimmer.text = counter.toString()
                counter--
            }

            override fun onFinish() {
                Showtimmer.text = getString(R.string.done__)
                Showtimmer.visibility = View.INVISIBLE
            }
        }.start()
    }
}


