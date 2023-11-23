package com.mariejuana.bookapplication.realm

import com.mariejuana.bookapplication.models.Book
import com.mariejuana.bookapplication.realm.tables.BookRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.delete
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.lang.IllegalStateException
import java.time.Instant
import java.time.LocalDate

class RealmDatabase {
    private val realm: Realm by lazy {
        val configuration = RealmConfiguration
            .Builder(schema = setOf(BookRealm::class))
            .schemaVersion(1)
            .build()
        Realm.open(configuration)
    }

    // Gets the list of the books
    fun getAllBooks(): List<BookRealm> {
        return realm.query<BookRealm>().find()
    }

    // Search query for the books
    fun getBooksByName(name: String): List<BookRealm> {
        return realm.query<BookRealm>("bookName CONTAINS[c] $0", name).find()
    }

    // Adds the book in the database
    suspend fun addBook(
        author: String,
        bookName: String,
        datePublished: String,
        pages: Int) {
        withContext(Dispatchers.IO) {
            realm.write {
                val book = BookRealm().apply {
                    this.author = author
                    this.bookName = bookName
                    this.dateBookPublished = datePublished
                    this.dateAdded = LocalDate.now().toEpochDay()
                    this.pages = pages
                }
                copyToRealm(book)
            }
        }
    }

    // Favorite the book
    suspend fun favoriteBook(id: ObjectId) {
        withContext(Dispatchers.IO) {
            realm.write {
                val book = realm.query<BookRealm>("id == $0", id).first().find()

                if (book != null) {
                    findLatest(book)?.isFavorite = true
                }
            }
        }
    }

    // Update the book
    suspend fun updateBook(
        book: Book,
        author: String,
        bookName: String,
        datePublished: String,
        pages: Int) {
        withContext(Dispatchers.IO) {
            realm.write {
                val book: BookRealm? = realm.query<BookRealm>("id == $0", ObjectId(book.id)).first().find()

                if (book != null) {
                    val bookRealm = findLatest(book)

                    // Update the data
                    bookRealm?.apply {
                        this.author = author
                        this.bookName = bookName
                        this.dateBookPublished = datePublished
                        this.pages = pages
                    }
                }
            }
        }
    }

    // Archive the book
    suspend fun archiveBook(id: ObjectId) {
        withContext(Dispatchers.IO) {
            realm.write {
                val book = realm.query<BookRealm>("id == $0", id).first().find()

                if (book != null) {
                    findLatest(book)?.archived = true
                }
            }
        }
    }

    suspend fun deleteBook(id: ObjectId) {
        withContext(Dispatchers.IO) {
            realm.write {
                query<BookRealm>("id == $0", id)
                    .first()
                    .find()
                    ?. let { delete(it) }
                    ?: throw IllegalStateException("Book not found")
            }
        }
    }
}