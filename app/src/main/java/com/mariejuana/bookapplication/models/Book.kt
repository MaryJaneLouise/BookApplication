package com.mariejuana.bookapplication.models

class Book (
    val id: String,
    val author: String,
    val bookName: String,
    val dateBookPublished: String,
    val dateAdded: String,
    val dateModified: String,
    val pages: Int,
    val pagesRead: Int,
    val isFavorite: Boolean,
    val archived: Boolean
)