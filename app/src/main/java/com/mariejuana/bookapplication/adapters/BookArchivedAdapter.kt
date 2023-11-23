package com.mariejuana.bookapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mariejuana.bookapplication.databinding.ContentBooksArchiveBinding
import com.mariejuana.bookapplication.databinding.ContentBooksBinding
import com.mariejuana.bookapplication.models.Book
import com.mariejuana.bookapplication.realm.RealmDatabase

class BookArchivedAdapter(private var bookList: ArrayList<Book>, private var context: Context, var bookArchivedAdapterCallback: BookArchivedAdapterInterface): RecyclerView.Adapter<BookArchivedAdapter.BookViewHolder>() {
    private var database = RealmDatabase()

    interface BookArchivedAdapterInterface {
        fun deleteBook(id: String)

        fun unArchiveBook(id: String)
    }

    inner class BookViewHolder(val binding: ContentBooksArchiveBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: Book) {
            with(binding) {
                val percentage = (itemData.pagesRead.toString().toDouble() / itemData.pages.toString().toDouble()) * 100

                txtBookName.text = String.format("%s", itemData.bookName)
                txtBookAuthor.text = String.format("%s", itemData.author)
                txtBookPagesRead.text = "Page ${itemData.pagesRead} of ${itemData.pages} (${String.format("%.2f", percentage.toString().toDouble())}%)"
                txtBookDatePublished.text = String.format("Published on %s", itemData.dateBookPublished)

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