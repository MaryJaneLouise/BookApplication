package com.mariejuana.bookapplication.screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mariejuana.bookapplication.R
import com.mariejuana.bookapplication.adapters.BookAdapter
import com.mariejuana.bookapplication.databinding.ActivityBooksBinding
import com.mariejuana.bookapplication.dialogs.AddBookDialog
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

class BooksActivity : AppCompatActivity(), AddBookDialog.RefreshDataInterface, BookAdapter.BookAdapterInterface {
    private lateinit var binding: ActivityBooksBinding
    private lateinit var bookList: ArrayList<Book>
    private lateinit var adapter: BookAdapter

    private var  database = RealmDatabase()
    private var fabVisible = false

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookList = arrayListOf()

        adapter = BookAdapter(bookList, this, this)
        getBooks()

        val layoutManager = LinearLayoutManager(this)
        binding.rvBooks.layoutManager = layoutManager
        binding.rvBooks.adapter = adapter

        fabVisible = false

        binding.idFabOptions.setOnClickListener {
            if (!fabVisible) {
                binding.idFabAdd.show()
                binding.idFabArchive.show()
                binding.idFabFavorites.show()

                binding.idFabAdd.visibility = View.VISIBLE
                binding.idFabArchive.visibility = View.VISIBLE
                binding.idFabFavorites.visibility = View.VISIBLE

                binding.idFabOptions.setImageDrawable(resources.getDrawable(R.drawable.ic_close))

                fabVisible = true
            } else {
                binding.idFabAdd.hide()
                binding.idFabArchive.hide()
                binding.idFabFavorites.hide()

                binding.idFabAdd.visibility = View.GONE
                binding.idFabArchive.visibility = View.GONE
                binding.idFabFavorites.visibility = View.GONE

                binding.idFabOptions.setImageDrawable(resources.getDrawable(R.drawable.ic_options))

                fabVisible = false
            }
        }

        binding.idFabAdd.setOnClickListener {
            val addBookDialog = AddBookDialog()
            addBookDialog.refreshDataCallback = this
            addBookDialog.show(supportFragmentManager, null)
        }

        binding.idFabArchive.setOnClickListener {
            val intent = Intent(this, BooksArchivedActivity::class.java)
            startActivity(intent)
        }

        binding.idFabFavorites.setOnClickListener {
            val intent = Intent(this, FavoriteBooksActivity::class.java)
            startActivity(intent)
        }

        binding.idAllBookSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("SearchBooks"))
                val search = binding.idAllBookSearch.text.toString().lowercase()

                scope.launch(Dispatchers.IO) {
                    val result = database.getBooksByName(search)
                    bookList = arrayListOf()
                    bookList.addAll(
                        result.map {
                            mapBook(it)
                        }
                    )
                    withContext(Dispatchers.Main) {
                        adapter.updateBookList(bookList)
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

                if (position != RecyclerView.NO_POSITION && position < bookList.size) {
                    val builder = AlertDialog.Builder(this@BooksActivity)
                    builder.setMessage("Are you sure you want to archive this book?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes"){dialog, _ ->
                        val deletedBook: Book = bookList[viewHolder.adapterPosition]
                        bookList.removeAt(viewHolder.adapterPosition)
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                        adapter.bookAdapterCallback.archiveBook(deletedBook.id)
                        Toast.makeText(this@BooksActivity, "The swiped book has been archived.", Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") {dialog, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
        }).attachToRecyclerView(binding.rvBooks)
    }

    override fun onResume() {
        super.onResume()
        getBooks()
    }

    override fun refreshData() {
        getBooks()
    }

    override fun archiveBook(id: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("archiveBook"))
        scope.launch(Dispatchers.IO) {
            database.archiveBook(BsonObjectId(id))
            getBooks()
        }
    }

    override fun faveBook(id: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("faveBook"))
        scope.launch(Dispatchers.IO) {
            database.favoriteBook(BsonObjectId(id))
            getBooks()
        }
    }

    override fun updateBook(book: Book, author: String, bookName: String, datePublished: String, pages: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updateBook"))
        scope.launch(Dispatchers.IO) {
            database.updateBook(book, author, bookName, datePublished, pages)
            getBooks()
        }
    }

    override fun updateBookStatus(book: Book, pagesRead: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updateBookStatus"))
        scope.launch(Dispatchers.IO) {
            database.updateBookStatus(book, pagesRead)
            getBooks()
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

    private fun getBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getAllBooks()
            bookList = arrayListOf()
            bookList.addAll(
                books.map {
                    mapBook(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(bookList)
                if (bookList.isEmpty()) {
                    binding.rvBooks.visibility = View.GONE
                    binding.txtNoBooksAvailable.visibility = View.VISIBLE
                } else {
                    binding.txtNoBooksAvailable.visibility = View.GONE
                    binding.rvBooks.visibility = View.VISIBLE
                }
            }
        }
    }
}