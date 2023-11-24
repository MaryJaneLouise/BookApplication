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
import com.mariejuana.bookapplication.adapters.BookArchivedAdapter
import com.mariejuana.bookapplication.databinding.ActivityBooksArchivedBinding
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

class BooksArchivedActivity : AppCompatActivity(), BookArchivedAdapter.BookArchivedAdapterInterface {
    private lateinit var binding: ActivityBooksArchivedBinding
    private lateinit var bookArchivedList: ArrayList<Book>
    private lateinit var adapter: BookArchivedAdapter

    private var database = RealmDatabase()
    private var fabVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookArchivedList = arrayListOf()

        adapter = BookArchivedAdapter(bookArchivedList, this, this)
        getArchivedBooks()

        val layoutManager = LinearLayoutManager(this)
        binding.rvBooksArchived.layoutManager = layoutManager
        binding.rvBooksArchived.adapter = adapter

        fabVisible = false

        binding.idFabOptions.setOnClickListener {
            if (!fabVisible) {
                binding.idFabUnarchive.show()
                binding.idFabDelete.show()

                binding.idFabUnarchive.visibility = View.VISIBLE
                binding.idFabDelete.visibility = View.VISIBLE

                binding.idFabOptions.setImageDrawable(resources.getDrawable(R.drawable.ic_close))

                fabVisible = true
            } else {
                binding.idFabUnarchive.hide()
                binding.idFabDelete.hide()

                binding.idFabUnarchive.visibility = View.GONE
                binding.idFabDelete.visibility = View.GONE

                binding.idFabOptions.setImageDrawable(resources.getDrawable(R.drawable.ic_options))

                fabVisible = false
            }
        }

        binding.idFabUnarchive.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to unarchive all of the books?")
            builder.setTitle("Warning!")
            builder.setPositiveButton("Yes") { dialog, _ ->
                if (bookArchivedList.size > 0) {
                    unarchiveAllBook()
                    getArchivedBooks()
                    Toast.makeText(this, "All of the books have been unarchived.", Toast.LENGTH_SHORT).show()
                } else {
                    getArchivedBooks()
                    Toast.makeText(this, "There is no books to be unarchived.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        binding.idFabDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to delete all of the books?")
            builder.setTitle("Warning!")
            builder.setPositiveButton("Yes") { dialog, _ ->
                if (bookArchivedList.size > 0) {
                    deleteAllBook()
                    getArchivedBooks()
                    Toast.makeText(this, "All of the books have been deleted.", Toast.LENGTH_SHORT).show()
                } else {
                    getArchivedBooks()
                    Toast.makeText(this, "There is no books to be deleted.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        binding.idAllBookSearchArchived.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("SearchArchivedBooks"))
                val search = binding.idAllBookSearchArchived.text.toString().lowercase()

                scope.launch(Dispatchers.IO) {
                    val result = database.getBooksByName(search)
                    bookArchivedList = arrayListOf()
                    bookArchivedList.addAll(
                        result.map {
                            mapBook(it)
                        }
                    )
                    withContext(Dispatchers.Main) {
                        adapter.updateBookList(bookArchivedList)
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

                if (position != RecyclerView.NO_POSITION && position < bookArchivedList.size) {
                    val builder = AlertDialog.Builder(this@BooksArchivedActivity)
                    builder.setMessage("Are you sure you want to delete this book? This cannot be undone.")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        val deletedBook: Book = bookArchivedList[viewHolder.adapterPosition]
                        bookArchivedList.removeAt(viewHolder.adapterPosition)
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                        adapter.bookArchivedAdapterCallback.deleteBook(deletedBook.id)
                        Toast.makeText(this@BooksArchivedActivity, "The swiped book has been deleted.", Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") {dialog, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
        }).attachToRecyclerView(binding.rvBooksArchived)
    }

    override fun onResume() {
        super.onResume()
        getArchivedBooks()

        binding.idFabUnarchive.hide()
        binding.idFabDelete.hide()

        binding.idFabUnarchive.visibility = View.GONE
        binding.idFabDelete.visibility = View.GONE

        binding.idFabOptions.setImageDrawable(resources.getDrawable(R.drawable.ic_options))

        fabVisible = false
    }

    override fun deleteBook(id: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deleteBook"))
        scope.launch(Dispatchers.IO) {
            database.deleteBook(BsonObjectId(id))
            getArchivedBooks()
        }
    }

    override fun deleteAllBook() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deleteAllBook"))
        scope.launch(Dispatchers.IO) {
            database.deleteAllBooksInArchive()
            getArchivedBooks()
        }
    }

    override fun unArchiveBook(id: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("unArchiveBook"))
        scope.launch(Dispatchers.IO) {
            database.unArchiveBook(BsonObjectId(id))
            getArchivedBooks()
        }
    }

    override fun unarchiveAllBook() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("unArchiveAllBook"))
        scope.launch(Dispatchers.IO) {
            database.unarchiveAllBook()
            getArchivedBooks()
        }
    }

    private fun mapBook(book: BookRealm): Book {
        return Book(
            id = book.id.toHexString(),
            author = book.author,
            bookName = book.bookName,
            dateBookPublished = book.dateBookPublished,
            dateAdded = book.dateAdded,
            dateModified = book.dateModified,
            pages = book.pages,
            pagesRead = book.pagesRead,
            isFavorite = book.isFavorite,
            archived = book.archived
        )
    }

    private fun getArchivedBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllArchivedBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getAllBooksArchived()
            bookArchivedList = arrayListOf()
            bookArchivedList.addAll(
                books.map {
                    mapBook(it)
                }
            )

            withContext(Dispatchers.Main) {
                adapter.updateBookList(bookArchivedList)
                if (bookArchivedList.isEmpty()) {
                    binding.rvBooksArchived.visibility = View.GONE
                    binding.txtNoBooksAvailable.visibility = View.VISIBLE
                } else {
                    binding.txtNoBooksAvailable.visibility = View.GONE
                    binding.rvBooksArchived.visibility = View.VISIBLE
                }
            }
        }
    }
}