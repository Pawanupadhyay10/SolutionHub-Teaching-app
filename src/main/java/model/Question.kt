package model

import java.util.*

data class Question(
    val Category: String,
    val username: String,
    val Question: String,
    val documentid: String,
    val Likes: Long,
    val timestamp: Date,
    val images: String,
    val num_Comments: Long?,
    val subject: String?
)
