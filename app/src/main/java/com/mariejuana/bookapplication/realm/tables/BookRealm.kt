package com.mariejuana.bookapplication.realm.tables

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class BookRealm : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var author: String? = null
    var bookName: String? = null
    var dateBookPublished: Long? = null
    var dateAdded: Long? = null
    var dateModified: Long? = null
    var pages: Int? = null
    var pagesRead: Int? = null
    var isFavorite: Boolean = false
    var archived: Boolean = false
}