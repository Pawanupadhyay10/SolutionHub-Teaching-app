package model

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bawpawan.dev.yourssolution.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class Question_Adapter(
    val blueprintquestion: ArrayList<Question>,
    val itemClick: (Question) -> Unit
) :
    RecyclerView.Adapter<Question_Adapter.ViewHolder>() {
    val ref = FirebaseFirestore.getInstance().collection(QUESTION_REF)

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.question_lists, parent, false)
        return ViewHolder(v, itemClick)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(blueprintquestion[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return blueprintquestion.count()
    }

    //the class is hodling the list view
    inner class ViewHolder(itemView: View?, val itemClick: (Question) -> Unit) :
        RecyclerView.ViewHolder(itemView!!) {
        val context = itemView?.context
        val username_list = itemView?.findViewById<TextView>(R.id.List_username)
        val Date_list = itemView?.findViewById<TextView>(R.id.Date_text)
        val questiontxtList = itemView?.findViewById<TextView>(R.id.question_text_list)
        val likes_count = itemView?.findViewById<TextView>(R.id.NO_Likes)
        val Star = itemView?.findViewById<ImageView>(R.id.Star)
        val Questionpic = itemView?.findViewById<ImageView>(R.id.question_list_image)
        val num_comments = itemView?.findViewById<TextView>(R.id.NUM_commentsview)
        val subject = itemView?.findViewById<TextView>(R.id.Subject)
        val share = itemView?.findViewById<ImageView>(R.id.sharebtn)
        fun bindItems(question: Question) {
            username_list?.text = question.username
            questiontxtList?.text = question.Question
            likes_count?.text = question.Likes.toString()
            num_comments?.text = question.num_Comments.toString()
            subject?.text = question.subject
            val Dateformatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val DateString = Dateformatter.format(question.timestamp)
            Date_list?.text = DateString
            itemView.setOnClickListener { itemClick(question) }
            Picasso.get().load(question.images.toUri()).into(Questionpic)
            Star?.setOnClickListener {
                FirebaseFirestore.getInstance().collection(QUESTION_REF)
                    .document(question.documentid)
                    .update(LIKES, question.Likes + 1)
            }
            share?.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(
                    Intent.EXTRA_TEXT, ("Your Question\n" +
                            "Description/Chapter::\n" +
                            question.Question + "\n Username::\n"
                            + question.username + "\n" +
                            "Questionpic::\n" +
                            question.images + "\n" +
                            "Subject::\n" +
                            question.subject)
                )
                intent.type = "text/plain"
                context?.startActivity(Intent.createChooser(intent, "Send To"))
            }
        }
    }
}



