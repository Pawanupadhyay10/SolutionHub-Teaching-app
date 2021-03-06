package Commentspack

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
import model.QUESTION_REF
import java.text.SimpleDateFormat
import java.util.*


class CommentsAdapter(val commentsolution: ArrayList<Comments>) :
    RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    val ref = FirebaseFirestore.getInstance().collection(QUESTION_REF)

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.comments_list_view, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindComments(commentsolution[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return commentsolution.count()
    }

    //the class is hodling the list view
    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val username_list = itemView?.findViewById<TextView>(R.id.Commentusername)
        val Date_list = itemView?.findViewById<TextView>(R.id.CommentDate)
        val questiontxtList = itemView?.findViewById<TextView>(R.id.Commentfield)
        val imagemsg = itemView?.findViewById<ImageView>(R.id.showimagemsgs)
        // val likes_count = itemView?.findViewById<TextView>(R.id.NO_Likes)
        //val Star = itemView?.findViewById<ImageView>(R.id.Star)
        //val Questionpic = itemView?.findViewById<ImageView>(R.id.question_list_image)

        fun bindComments(comments: Comments) {
            username_list?.text = comments.username
            val Dateformatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val DateString = Dateformatter.format(comments.timestamp)
            Date_list?.text = DateString
            questiontxtList?.text = comments.commenttxt
            Picasso.get().load(comments.imagecommenturi?.toUri()).into(imagemsg)
        }
    }
}

