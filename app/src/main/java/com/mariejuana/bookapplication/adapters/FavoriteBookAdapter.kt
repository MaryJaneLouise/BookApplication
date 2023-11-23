package com.mariejuana.bookapplication.adapters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.mariejuana.bookapplication.R
import com.mariejuana.bookapplication.databinding.ContentBooksBinding
import com.mariejuana.bookapplication.databinding.ContentBooksFaveBinding
import com.mariejuana.bookapplication.databinding.DialogEditBookBinding
import com.mariejuana.bookapplication.databinding.DialogEditBookReadBinding
import com.mariejuana.bookapplication.models.Book
import com.mariejuana.bookapplication.realm.RealmDatabase
import java.util.Calendar
import java.util.Locale

class FavoriteBookAdapter(private var bookList: ArrayList<Book>, private var context: Context, var bookFaveAdapterCallback: BookFaveAdapterInterface): RecyclerView.Adapter<FavoriteBookAdapter.BookViewHolder>() {
    private var database = RealmDatabase()
    private val calendar = Calendar.getInstance()

    interface BookFaveAdapterInterface {

        fun unFaveBook(id: String)

        fun updateBook(book: Book, author: String, bookName: String, datePublished: String, pages: Int)

        fun archiveBook(id: String)

        fun updateBookStatus(book: Book, pagesRead: Int)
    }

    inner class BookViewHolder(val binding: ContentBooksFaveBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: Book) {
            with(binding) {
                val percentage = (itemData.pagesRead.toString().toDouble() / itemData.pages.toString().toDouble()) * 100

                txtBookName.text = String.format("%s", itemData.bookName)
                txtBookAuthor.text = String.format("%s", itemData.author)
                txtBookPagesRead.text = "Page ${itemData.pagesRead} of ${itemData.pages} (${String.format("%.2f", percentage.toString().toDouble())}%)"

                txtBookDatePublished.text = String.format("Published on %s", itemData.dateBookPublished)
                txtBookDatePublished.visibility = View.GONE

//                if (itemData.dateAdded != itemData.dateModified) {
//                    txtBookDateModified.visibility = View.VISIBLE
//                    txtBookDateModified.text = String.format("%s", itemData.dateModified)
//                }


                // Removes the book into favorites
                btnUnfave.setOnClickListener {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to remove this book to the favorites?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        bookList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        bookFaveAdapterCallback.unFaveBook(itemData.id)
                        Toast.makeText(context, "The selected book has been removed in favorites.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Edits the details of the book
                btnEditBook.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.dialog_edit_book, null)
                    val bindingEdit = DialogEditBookBinding.bind(view)

                    builder.setView(view)

                    val inputTitle = bindingEdit.editBookTitleName
                    val inputAuthor = bindingEdit.editBookTitleAuthor
                    val inputPages = bindingEdit.editBookTitlePages
                    val inputDatePublished = bindingEdit.editBookTitleDatePublished
                    val buttonCalendar = bindingEdit.editBookTitleDatePublishedButton

                    inputTitle.setText("${itemData.bookName}")
                    inputAuthor.setText("${itemData.author}")
                    inputDatePublished.setText("${itemData.dateBookPublished}")
                    inputPages.setText("${itemData.pages}")

                    buttonCalendar.setOnClickListener {
                        val datePickerDialog = DatePickerDialog(
                            context, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                                val selectedDate = Calendar.getInstance()
                                selectedDate.set(year, monthOfYear, dayOfMonth)

                                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                val formattedDate = dateFormat.format(selectedDate.time)
                                bindingEdit.editBookTitleDatePublished.setText(formattedDate)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                    }

                    builder.setCancelable(false)

                    builder.setPositiveButton("Update details") { dialog, _ ->
                        val newTitle = inputTitle.text.toString()
                        val newAuthor = inputAuthor.text.toString()
                        val newPages = inputPages.text.toString()
                        val newDatePublished = inputDatePublished.text.toString()

                        if ((newTitle.isNullOrEmpty() && newTitle.isNullOrEmpty()) ||
                            (newAuthor.isNullOrEmpty() && newAuthor.isNullOrBlank()) ||
                            (newPages.isNullOrEmpty() && newPages.isNullOrBlank() || (newPages < itemData.pagesRead.toString())) ||
                            (newDatePublished.isNullOrEmpty() && newDatePublished.isNullOrBlank())) {
                            Toast.makeText(context, "Book details has been updated unsuccessfully. Please check again.", Toast.LENGTH_SHORT).show()
                            dialog.cancel()
                        } else {
                            bookFaveAdapterCallback.updateBook(itemData, newAuthor, newTitle, newDatePublished, newPages.toInt())
                            Toast.makeText(context, "Book details has been updated.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Archives the book
                btnRemove.setOnClickListener {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to archive this book?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        bookList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        bookFaveAdapterCallback.archiveBook(itemData.id)
                        Toast.makeText(context, "The selected book has been archived.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Updates the book read status
                btnUpdatePagesRead.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.dialog_edit_book_read, null)
                    val bindingEdit = DialogEditBookReadBinding.bind(view)

                    builder.setView(view)

                    val inputPageRead = bindingEdit.editBookTitlePageRead
                    inputPageRead.setText("${itemData.pagesRead}")

                    builder.setCancelable(false)

                    builder.setPositiveButton("Update details") { dialog, _ ->
                        val newPage = inputPageRead.text.toString().toInt()

                        if (newPage > itemData.pages || (newPage.toString().isNullOrEmpty() || newPage.toString().isNullOrBlank())) {
                            Toast.makeText(context, "Book page read has been updated unsuccessfully. Please check again.", Toast.LENGTH_SHORT).show()
                            dialog.cancel()
                        } else {
                            bookFaveAdapterCallback.updateBookStatus(itemData, newPage)
                            Toast.makeText(context, "Book page read has been updated.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ContentBooksFaveBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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