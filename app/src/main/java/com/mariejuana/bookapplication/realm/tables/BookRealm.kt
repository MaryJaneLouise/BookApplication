package com.mariejuana.bookapplication.realm.tables

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class BookRealm : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var author: String = ""
    var bookName: String = ""
    var dateBookPublished: String = ""
    var dateAdded: Long? = null
    var dateModified: Long? = null
    var pages: Int = 0
    var pagesRead: Int = 0
    var isFavorite: Boolean = false
    var archived: Boolean = false
}