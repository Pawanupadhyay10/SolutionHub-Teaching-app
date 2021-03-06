package com.bawpawan.dev.yourssolution

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import model.*

class MainActivity : AppCompatActivity() {
    private lateinit var mInterstitialAd: InterstitialAd
    private var category = GATE
    lateinit var Qadapter: Question_Adapter
    var questions = arrayListOf<Question>()
    val questionfetching = FirebaseFirestore.getInstance().collection(QUESTION_REF)
    lateinit var Questionlistner: ListenerRegistration
    lateinit var Auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "ADD YOUR QUESTION BY CLICKING CIRCULAR ADD BUTTON", Toast.LENGTH_LONG)
            .show()
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                //Log.d("TAG", "The interstitial wasn't loaded yet.")
            }
            val newQuestionAdd = Intent(this, Adding_Question::class.java)
            startActivity(newQuestionAdd)
        }
        Qadapter = Question_Adapter(questions) { question ->
            val i = Intent(this, SolutionpartActivity::class.java)
            i.putExtra(DOCUMENT_ID_KEY, question.documentid)
            // i.putExtra(QUESTIONTXT, question.Question)
            //i.putExtra(IMAGEURI, question.images)
            startActivity(i)
        }
        recyclershow.adapter = Qadapter
        val layoutManager = LinearLayoutManager(this)
        recyclershow.layoutManager = layoutManager
        Auth = FirebaseAuth.getInstance()
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-9719530186912484/2588810838"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

    }

    override fun onResume() {
        super.onResume()
        UpdateUi()
    }

    override fun onKeyDown(keycode: Int, event: KeyEvent?): Boolean {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
        }
        return super.onKeyDown(keycode, event)
    }

    fun setListener() = if (category == POPULAR) {
        // show popular docs.
        Questionlistner = questionfetching.orderBy(LIKES, Query.Direction.DESCENDING)
            .addSnapshotListener(this) { snapshot, exception ->
                if (exception != null) {
                    //  Log.e("Exception", "Could not reterive:$exception")
                } else {
                    // parsing data
                    if (snapshot != null) {
                        questions.clear()
                        for (document: DocumentSnapshot in snapshot.documents) {
                            val data = document.data
                            val images = data?.get(IMAGEURI)
                            val cat = data?.get(CATEGORY_SELECT)
                            val name = data?.get(USERNAME)
                            val questiontxt = data?.get(QUESTIONTXT)
                            val likes = data?.get(LIKES)
                            val num_comments = data?.get(NUM_COMMENTS) as? Long
                            val timestamp = data?.get(TIMESTAMP) as Timestamp
                            val sub = data.get(SUBJECT)
                            val documentid = document.id

                            val newQuestion = Question(
                                cat.toString(),
                                name.toString(),
                                questiontxt.toString(),
                                documentid,
                                likes as Long,
                                timestamp.toDate(),
                                images.toString(),
                                num_comments,
                                sub.toString()
                            )
                            questions.add(newQuestion)
                        }
                        Qadapter.notifyDataSetChanged()
                    }
                }
            }
    } else {
        Questionlistner = questionfetching.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .whereEqualTo(CATEGORY_SELECT, category)
            .addSnapshotListener(this) { snapshot, exception ->
                if (exception != null) {
                    // Log.e("Exception", "Could not reterive:$exception")
                } else {
                    // parsing data
                    if (snapshot != null) {
                        questions.clear()
                        for (document: DocumentSnapshot in snapshot.documents) {
                            val data = document.data
                            val images = data?.get(IMAGEURI)
                            val cat = data?.get(CATEGORY_SELECT)
                            val name = data?.get(USERNAME)
                            val questiontxt = data?.get(QUESTIONTXT)
                            val likes = data?.get(LIKES)
                            val num_comments = data?.get(NUM_COMMENTS) as? Long
                            val timestamp = data?.get(TIMESTAMP) as Timestamp
                            val sub = data.get(SUBJECT)
                            val documentid = document.id

                            val newQuestion = Question(
                                cat.toString(),
                                name.toString(),
                                questiontxt.toString(),
                                documentid,
                                likes as Long,
                                timestamp.toDate(),
                                images.toString(),
                                num_comments,
                                sub.toString()
                            )
                            questions.add(newQuestion)
                        }
                        Qadapter.notifyDataSetChanged()
                    }
                }
            }
    }

    fun mainGatebtn(view: View) {
        if (category == GATE) {
            mainGatebtn.isChecked = true
            return
        }
        mainApptitudebtn.isChecked = false
        mainPopularbtn.isChecked = false
        category = GATE
        Questionlistner.remove()
        setListener()
    }

    fun mainApptitudebtn(view: View) {
        if (category == APPTITUDE) {
            mainApptitudebtn.isChecked = true
            return
        }
        mainPopularbtn.isChecked = false
        mainGatebtn.isChecked = false
        category = APPTITUDE
        Questionlistner.remove()
        setListener()
    }

    fun mainpopularbtn(view: View) {

        if (category == POPULAR) {
            mainPopularbtn.isChecked = true
            return
        }
        mainGatebtn.isChecked = false
        mainApptitudebtn.isChecked = false
        category = POPULAR
        Questionlistner.remove()
        setListener()
    }

    fun searchget(query: String) {
        questionfetching.whereEqualTo(SUBJECT, query)
            .get()
            .addOnCompleteListener(OnCompleteListener { task ->
                questions.clear()
                for (document: DocumentSnapshot in task.result!!) {
                    val data = document.data
                    val images = data?.get(IMAGEURI)
                    val cat = data?.get(CATEGORY_SELECT)
                    val name = data?.get(USERNAME)
                    val questiontxt = data?.get(QUESTIONTXT)
                    val likes = data?.get(LIKES)
                    val num_comments = data?.get(NUM_COMMENTS) as? Long
                    val timestamp = data?.get(TIMESTAMP) as Timestamp
                    val sub = data.get(SUBJECT)
                    val documentid = document.id

                    val newQuestion = Question(
                        cat.toString(),
                        name.toString(),
                        questiontxt.toString(),
                        documentid,
                        likes as Long,
                        timestamp.toDate(),
                        images.toString(),
                        num_comments,
                        sub.toString()
                    )
                    questions.add(newQuestion)
                }
                Qadapter = Question_Adapter(questions) { question ->
                    val i = Intent(this, SolutionpartActivity::class.java)
                    i.putExtra(DOCUMENT_ID_KEY, question.documentid)
                    startActivity(i)
                }
                recyclershow.adapter = Qadapter


            }).addOnFailureListener {
                // Log.e("EXCeption","Not Search ${exception.localizedMessage}")
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val menusearch = menu.findItem(R.id.search)
        if (menusearch != null) {
            val searchview = menusearch.actionView as SearchView
            searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    Toast.makeText(
                        applicationContext,
                        "Search by subject CaseSensitive",
                        Toast.LENGTH_LONG
                    ).show()
                    if (query!!.isNotEmpty()) {
                        searchget(query)
                    }
                    Toast.makeText(
                        applicationContext,
                        "CaseSensitive " + " If Question not found,then GO Back",
                        Toast.LENGTH_LONG
                    ).show()
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuitem = menu?.getItem(0)
        if (Auth.currentUser == null) {
            menuitem?.title = "Login"
        } else {
            menuitem?.title = "Logout"
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            if (Auth.currentUser == null) {
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
            } else {
                Auth.signOut()
                UpdateUi()
                finish()
            }
            return true
        }
        return false
    }

    fun UpdateUi() {
        if (Auth.currentUser == null) {
            mainGatebtn.isEnabled = false
            mainApptitudebtn.isEnabled = false
            mainPopularbtn.isEnabled = false
            fab.isEnabled = false
            questions.clear()
            Qadapter.notifyDataSetChanged()
        } else {
            mainGatebtn.isEnabled = true
            mainApptitudebtn.isEnabled = true
            mainPopularbtn.isEnabled = true
            fab.isEnabled = true
            setListener()
        }
    }
//    fun hidekeyboard(){
//        val inputmanager=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        if(inputmanager.isAcceptingText){
//            inputmanager.hideSoftInputFromWindow(currentFocus?.windowToken,0)
//        }
//    }
}

