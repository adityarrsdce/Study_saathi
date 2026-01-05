package com.babu.appp.util


import android.content.Context
import java.io.File
import java.net.URL

fun Context.getHolidayPdfFile(): File {
    val dir = File(filesDir, "pdf")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "holiday_calendar.pdf")
}

suspend fun Context.downloadPdf(url: String): File {
    val file = getHolidayPdfFile()
    URL(url).openStream().use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file
}

fun Context.isPdfCached(): Boolean {
    val file = getHolidayPdfFile()
    return file.exists() && file.length() > 0
}
