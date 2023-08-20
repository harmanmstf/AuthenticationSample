package com.example.authenticationsample.firestore.model

data class PhotoItem(
    val userId: String? = "",
    val photoUrl: String? = "",
    val username: String? = "",
    val comment: String? = ""
)
