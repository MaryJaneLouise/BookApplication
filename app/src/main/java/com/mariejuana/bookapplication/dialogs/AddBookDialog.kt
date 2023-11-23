package com.mariejuana.bookapplication.dialogs

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.mariejuana.bookapplication.databinding.DialogAddBookBinding
import com.mariejuana.bookapplication.realm.RealmDatabase
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class AddBookDialog : DialogFragment() {
    private lateinit var binding: DialogAddBookBinding
    lateinit var refreshDataCallback: RefreshDataInterface

    private var database = RealmDatabase()
    private val calendar = Calendar.getInstance()

    interface RefreshDataInterface {
        fun refreshData()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAddBookBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            editBookTitleDatePublishedButton.setOnClickListener {
                showDatePicker()
            }

            btnAddBook.setOnClickListener {
                if (editBookTitleName.text.isNullOrBlank() || editBookTitleName.text.isNullOrEmpty()) {
                    editBookTitleName.error = "Required"
                    return@setOnClickListener
                }
                if (editBookTitleAuthor.text.isNullOrBlank() || editBookTitleAuthor.text.isNullOrEmpty()) {
                    editBookTitleAuthor.error = "Required"
                    return@setOnClickListener
                }
                if (editBookTitlePages.text.isNullOrBlank() ||
                    editBookTitlePages.text.isNullOrEmpty() ||
                    !editBookTitlePages.text.isDigitsOnly()) {
                    editBookTitlePages.error = "Required"
                    return@setOnClickListener
                }
                if (editBookTitleDatePublished.text.isNullOrBlank() || editBookTitleDatePublished.text.isNullOrEmpty()) {
                    editBookTitleDatePublished.error = "Required"
                    return@setOnClickListener
                }

                val bookTitle = editBookTitleName.text.toString()
                val bookPages = editBookTitlePages.text.toString().toInt()
                val bookAuthor = editBookTitleAuthor.text.toString()
                val bookDatePublished = editBookTitleDatePublished.text.toString()

                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("addBookToRealm"))
                scope.launch(Dispatchers.IO) {
                    database.addBook(bookAuthor, bookTitle, bookDatePublished, bookPages)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Book has been added!", Toast.LENGTH_LONG).show()
                        refreshDataCallback.refreshData()
                        dialog?.dismiss()
                    }
                }
            }

            // Makes the dialog cancel
            btnCancel.setOnClickListener {
                dialog?.cancel()
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(), { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                binding.editBookTitleDatePublished.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

}