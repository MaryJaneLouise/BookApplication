package com.mariejuana.bookapplication.helpers

import android.content.ClipData
import android.icu.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class TypeConverter {
    fun toFormattedDateTimeString(dateTime: String): String {
        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val localDateTime = LocalDateTime.parse(dateTime, parser)

        val dateFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
        val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
        return dateFormatter.format(date)
    }

    fun toFormattedDateString(date: String): String {
        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val localDateTime = LocalDateTime.parse(date, parser)

        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
        return dateFormatter.format(date)
    }
}
