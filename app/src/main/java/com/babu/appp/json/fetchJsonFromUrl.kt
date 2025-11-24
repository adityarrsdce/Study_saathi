package com.babu.appp.json


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

suspend fun fetchJsonFromUrl(url: String): String = withContext(Dispatchers.IO) {
    URL(url).readText()
}
