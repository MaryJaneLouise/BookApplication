package com.mariejuana.bookapplication.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mariejuana.bookapplication.R
import com.mariejuana.bookapplication.adapters.FavoriteBookAdapter
import com.mariejuana.bookapplication.databinding.ActivityFavoriteBooksBinding
import com.mariejuana.bookapplication.models.Book
import com.mariejuana.bookapplication.realm.RealmDatabase
import com.mariejuana.bookapplication.realm.tables.BookRealm
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId

class FavoriteBooksActivity : AppCompatActivity(), FavoriteBookAdapter.BookFaveAdapterInterface {
    private lateinit var binding: ActivityFavoriteBooksBinding
    private lateinit var bookFaveList: ArrayList<Book>
    private lateinit var adapter: FavoriteBookAdapter

    private var  database = RealmDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookFaveList = arrayListOf()

        adapter = FavoriteBookAdapter(bookFaveList, this, this)
        getFaveBooks()

        val layoutManager = LinearLayoutManager(this)
        binding.rvBooksFavorite.layoutManager = layoutManager
        binding.rvBooksFavorite.adapter = adapter

        binding.idAllBookSearchFavorite.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("SearchFaveBooks"))
                val search = binding.idAllBookSearchFavorite.text.toString().lowercase()

                scope.launch(Dispatchers.IO) {
                    val result = database.getBooksByName(search)
                    bookFaveList = arrayListOf()
                    bookFaveList.addAll(
                        result.map {
                            mapBook(it)
                        }
                    )
                    withContext(Dispatchers.Main) {
                        adapter.updateBookList(bookFaveList)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nothing to do
            }
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position != RecyclerView.NO_POSITION && position < bookFaveList.size) {
                    val builder = AlertDialog.Builder(this@FavoriteBooksActivity)
                    builder.setMessage("Are you sure you want to archive this book?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes"){dialog, _ ->
                        val deletedBook: Book = bookFaveList[viewHolder.adapterPosition]
                        bookFaveList.removeAt(viewHolder.adapterPosition)
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                        adapter.bookFaveAdapterCallback.archiveBook(deletedBook.id)
                        Toast.makeText(this@FavoriteBooksActivity, "The swiped book has been archived.", Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") {dialog, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
        }).attachToRecyclerView(binding.rvBooksFavorite)
    }

    override fun onResume() {
        super.onResume()
        getFaveBooks()
    }

    override fun archiveBook(id: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("archiveBook"))
        scope.launch(Dispatchers.IO) {
            database.archiveBook(BsonObjectId(id))
            getFaveBooks()
        }
    }

    override fun unFaveBook(id: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("unFaveBook"))
        scope.launch(Dispatchers.IO) {
            database.unFavoriteBook(BsonObjectId(id))
            getFaveBooks()
        }
    }

    override fun updateBook(
        book: Book,
        author: String,
        bookName: String,
        datePublished: String,
        dateModified: String,
        pages: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updateBook"))
        scope.launch(Dispatchers.IO) {
            database.updateBook(book, author, bookName, datePublished, dateModified, pages)
            getFaveBooks()
        }
    }

    override fun updateBookStatus(book: Book, dateModified: String, pagesRead: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updateBookStatus"))
        scope.launch(Dispatchers.IO) {
            database.updateBookStatus(book, dateModified, pagesRead)
            getFaveBooks()
        }
    }

    private fun mapBook(book: BookRealm): Book {
        return Book(
            id = book.id.toHexString(),
            author = book.author,
            bookName = book.bookName,
            dateBookPublished = book.dateBookPublished,
            dateAdded = book.dateAdded.toString(),
            dateModified = book.dateModified.toString(),
            pages = book.pages,
            pagesRead = book.pagesRead,
            isFavorite = book.isFavorite,
            archived = book.archived
        )
    }

    private fun getFaveBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getAllBooksFavorite()
            bookFaveList = arrayListOf()
            bookFaveList.addAll(
                books.map {
                    mapBook(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(bookFaveList)
                if (bookFaveList.isEmpty()) {
                    binding.rvBooksFavorite.visibility = View.GONE
                    binding.txtNoBooksAvailable.visibility = View.VISIBLE
                } else {
                    binding.txtNoBooksAvailable.visibility = View.GONE
                    binding.rvBooksFavorite.visibility = View.VISIBLE
                }
            }
        }
    }
}