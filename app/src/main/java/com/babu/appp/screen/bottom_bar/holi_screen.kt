package com.babu.appp.screen

import com.babu.appp.R
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.babu.appp.json.fetchJsonFromUrl
import com.babu.appp.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val backgroundPainter =
        if (isDark) painterResource(R.drawable.pyq_dark)
        else painterResource(R.drawable.pyq_light)

    val appBarColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5D1B5)

    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val jsonUrl =
        "https://raw.githubusercontent.com/adityarrsdce/babubhaiya/refs/heads/main/Holiday/holiday.json"

    // ---------------- LOAD DATA ----------------
    LaunchedEffect(Unit) {
        try {
            val jsonText = withContext(Dispatchers.IO) {
                fetchJsonFromUrl(jsonUrl)
            }
            val map: Map<String, String> = Json.decodeFromString(jsonText)
            val pdfUrl = map.values.first()

            if (context.isPdfCached()) {
                pdfFile = context.getHolidayPdfFile()
            } else if (isInternetAvailable(context)) {
                pdfFile = context.downloadPdf(pdfUrl)
            } else {
                error = "Internet required first time to download holiday calendar"
            }

        } catch (e: Exception) {
            error = "Failed to load holiday calendar"
        }
        isLoading = false
    }

    // ---------------- UI ----------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Holiday Calendar") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor)
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = backgroundPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()

                    error != null -> Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error
                    )

                    pdfFile != null -> {
                        PdfLocalViewer(
                            context = context,
                            pdfFile = pdfFile!!
                        )
                    }

                }
            }
        }
    }
}
