package com.mariejuana.bookapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mariejuana.bookapplication.R
import com.mariejuana.bookapplication.databinding.ContentBooksArchiveBinding
import com.mariejuana.bookapplication.databinding.ContentBooksBinding
import com.mariejuana.bookapplication.databinding.DialogShowDetailsBookBinding
import com.mariejuana.bookapplication.helpers.TypeConverter
import com.mariejuana.bookapplication.models.Book
import com.mariejuana.bookapplication.realm.RealmDatabase

class BookArchivedAdapter(private var bookList: ArrayList<Book>, private var context: Context, var bookArchivedAdapterCallback: BookArchivedAdapterInterface): RecyclerView.Adapter<BookArchivedAdapter.BookViewHolder>() {
    private var database = RealmDatabase()
    private val typeConverter = TypeConverter()
    private var buttonVisible = false

    interface BookArchivedAdapterInterface {
        fun deleteBook(id: String)

        fun unArchiveBook(id: String)

        fun unarchiveAllBook()

        fun deleteAllBook()
    }

    inner class BookViewHolder(val binding: ContentBooksArchiveBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: Book) {
            with(binding) {
                val percentage = (itemData.pagesRead.toString().toDouble() / itemData.pages.toString().toDouble()) * 100

                val convertedDateAdded = typeConverter.toFormattedDateTimeString(itemData.dateAdded)
                val convertedDateModified = typeConverter.toFormattedDateTimeString(itemData.dateModified)

                buttonVisible = false

                txtBookName.text = String.format("%s", itemData.bookName)
                txtBookAuthor.text = String.format("%s", itemData.author)
                txtBookPagesRead.text = "Page ${itemData.pagesRead} of ${itemData.pages} (${String.format("%.2f", percentage.toString().toDouble())}%)"

                btnSeeDetails.visibility = View.GONE
                btnUnarchiveDelete.visibility = View.GONE

                cvForecast.setOnClickListener {
                    if (!buttonVisible) {
                        btnSeeDetails.visibility = View.VISIBLE
                        btnUnarchiveDelete.visibility = View.VISIBLE

                        buttonVisible = true
                    } else {
                        btnSeeDetails.visibility = View.GONE
                        btnUnarchiveDelete.visibility = View.GONE

                        buttonVisible = false
                    }
                }

                // Unarchives the book
                btnUnarchive.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to unarchive this book?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        bookList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        bookArchivedAdapterCallback.unArchiveBook(itemData.id)
                        Toast.makeText(context, "The selected book has been unarchived.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Deletes the book permanently
                btnDelete.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to delete this book? This cannot be undone.")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        bookList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        bookArchivedAdapterCallback.deleteBook(itemData.id)
                        Toast.makeText(context, "The selected book has been deleted.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Shows the full details of the book
                btnSeeDetails.setOnClickListener {
                    val builder = android.app.AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.dialog_show_details_book, null)
                    val bindingEdit = DialogShowDetailsBookBinding.bind(view)

                    builder.setView(view)

                    val inputTitle = bindingEdit.editBookTitleName
                    val inputAuthor = bindingEdit.editBookTitleAuthor
                    val inputPages = bindingEdit.editBookTitlePages
                    val inputDatePublished = bindingEdit.editBookTitleDatePublished
                    val inputDateModified = bindingEdit.editBookTitleDateModified
                    val inputDateAdded = bindingEdit.editBookTitleDateAdded


                    inputTitle.setText("${itemData.bookName}")
                    inputAuthor.setText("${itemData.author}")
                    inputDatePublished.setText("${itemData.dateBookPublished}")
                    inputPages.setText("${itemData.pages}")
                    inputDateModified.setText(convertedDateModified)
                    inputDateAdded.setText(convertedDateAdded)

                    builder.setCancelable(false)

                    builder.setPositiveButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ContentBooksArchiveBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val bookData = bookList[position]
        holder.bind(bookData)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    fun updateBookList(bookList: ArrayList<Book>){
        this.bookList = arrayListOf()
        notifyDataSetChanged()
        this.bookList = bookList
        this.notifyItemInserted(this.bookList.size)
    }
}