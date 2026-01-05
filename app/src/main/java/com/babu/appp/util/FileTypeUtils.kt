package com.babu.appp.util


fun String.isPdf(): Boolean =
    lowercase().endsWith(".pdf")

fun String.isImage(): Boolean =
    lowercase().endsWith(".jpg") ||
            lowercase().endsWith(".jpeg") ||
            lowercase().endsWith(".png") ||
            lowercase().endsWith(".webp")
