package com.mariejuana.bookapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mariejuana.bookapplication.databinding.ContentBooksBinding
import com.mariejuana.bookapplication.models.Book
import com.mariejuana.bookapplication.realm.RealmDatabase

class BookAdapter(private var bookList: ArrayList<Book>, private var context: Context, var bookAdapterCallback: BooktAdapterInterface): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    private var database = RealmDatabase()

    interface BooktAdapterInterface {
        fun deleteBook(id: String)

        fun archiveBook(id: String)
    }

    inner class BookViewHolder(val binding: ContentBooksBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: Book) {
            with(binding) {
                txtBookName.text = String.format("%s", itemData.bookName)
                txtBookAuthor.text = String.format("%s", itemData.author)
                txtBookPages.text = String.format("%s pages", itemData.pages)
                txtBookDatePublished.text = String.format("Published on %s", itemData.dateBookPublished)
                txtBookDateModified.visibility = View.GONE

//                if (itemData.dateAdded != itemData.dateModified) {
//                    txtBookDateModified.visibility = View.VISIBLE
//                    txtBookDateModified.text = String.format("%s", itemData.dateModified)
//                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ContentBooksBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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