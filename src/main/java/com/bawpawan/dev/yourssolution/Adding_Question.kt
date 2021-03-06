package com.bawpawan.dev.yourssolution

import android.app.*
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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_adding__question.*
import model.*
import java.io.IOException
import java.util.*
import java.util.Locale.getDefault
import kotlin.collections.HashMap

class Adding_Question : AppCompatActivity() {
    val Channel_id = "channelid"
    val channelname = "ChannelName"
    val Notificationid = 0

    val uniqueId = UUID.randomUUID().toString()
    val ref = FirebaseFirestore.getInstance().collection(QUESTION_REF)

   //a Uri object to store file path
    private var filePath: Uri? = null
    val firebaseStore = FirebaseStorage.getInstance()
    val storageReference = firebaseStore.reference

    //a constant to track the file chooser intent
    private val PICK_IMAGE_REQUEST = 234
    var category = GATE
    private lateinit var mAdView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding__question)
        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingintent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notifaction = NotificationCompat.Builder(this, Channel_id)
            .setContentTitle("New Post Added By A User")
            .setContentText("Go and Give Solution,Contribute And be Popular")
            .setSmallIcon(R.drawable.notify)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingintent)
            .build()
        val noyificationmanager = NotificationManagerCompat.from(this)
        send_btn.setOnClickListener {
            noyificationmanager.notify(Notificationid, notifaction)
            Toast.makeText(this, "Wait IT May Take Few Seconds", Toast.LENGTH_SHORT).show()
            Timmer1()
            hidekeyboard()
            Uploadfile()
        }
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder()
            .build()
        mAdView.loadAd(adRequest)
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


    fun add_question_image(view: View) {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                PICK_IMAGE_REQUEST
            )
        } else {
            showFileChooser()
        }
    }

    fun category_gate_add(view: View) {
        if (category == GATE) {
            addGatebtn.isChecked = true
            return
        }
        addApptitudebtn.isChecked = false
        category = GATE
    }

    fun category_apptitude_add(view: View) {
        if (category == APPTITUDE) {
            addApptitudebtn.isChecked = true
            return
        }
        addGatebtn.isChecked = false
        category = APPTITUDE
    }

    //method to show file chooser
    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    //handling the image chooser activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }
            filePath = data.data
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    // To handle deprication use
                    val source = filePath?.let { ImageDecoder.createSource(contentResolver, it) }
                    val bitmap = ImageDecoder.decodeBitmap(source!!)
                    postImage.setImageBitmap(bitmap)
                } else {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)
                    postImage.setImageBitmap(bitmap)
                }
            } catch (e: IOException) {
                e.printStackTrace()
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
                showFileChooser()
            }
        }
    }

    //this method will upload the file
    fun Uploadfile() {
        if (filePath != null) {
            val refStore = storageReference.child("images/" + uniqueId + ".jpeg")
            val uploadTask = refStore.putFile(filePath!!)

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
                        addUploadRecordToDb(downloadUri.toString())
                    }
                }.addOnFailureListener {

                }
        }
    }

    fun addUploadRecordToDb(uri: String) {
        val nameuser = addusername.text.toString()
        val sub = Typequestion.text.toString()
        if (nameuser.isNotEmpty() && sub.isNotEmpty()) {
            val data = HashMap<String, Any>()
            data[IMAGEURI] = uri
            data[CATEGORY_SELECT] = category
            data[USERNAME] = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            data[SUBJECT] = nameuser.toLowerCase(getDefault())
            data[QUESTIONTXT] = sub
            data[IMAGEURL] = uniqueId
            data[TIMESTAMP] = FieldValue.serverTimestamp()
            data[LIKES] = 0
            data[NUM_COMMENTS] = 0
            ref.add(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        this, "Question Posted Check in Which Section You have Posted" +
                                "GATE or APPTITUDE", Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error to Post Question", Toast.LENGTH_LONG).show()
                    // Log.e("Exception","error ::${e.localizedMessage}")
                }
        } else {
            Toast.makeText(this, "Empty Feilds Are Not Allowed", Toast.LENGTH_SHORT).show()
        }
    }

    fun hidekeyboard() {
        val inputmanager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputmanager.isAcceptingText) {
            inputmanager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun Timmer1() {
        var counter = 15
        postingtimmer.visibility = View.VISIBLE
        object : CountDownTimer(16000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                postingtimmer.text = counter.toString()
                counter--
            }

            override fun onFinish() {
                postingtimmer.text = getString(R.string.done__)
                postingtimmer.visibility = View.INVISIBLE
            }
        }.start()
    }
}
