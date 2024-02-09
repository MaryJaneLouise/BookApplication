package com.mariejuana.bookapplication.realm.tables

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ArchiveBookRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    var author: String = ""
    var bookName: String = ""
    var dateBookPublished: String = ""
    var dateAdded: String = ""
    var dateModified: String = ""
    var pages: Int = 0
    var pagesRead: Int = 0
}