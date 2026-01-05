package com.babu.appp.util

package com.babu.appp.screen.bottom_bar

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.babu.appp.R
import com.babu.appp.json.fetchJsonFromUrl
import com.babu.appp.util.getHolidayCache
import com.babu.appp.util.saveHolidayCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

// ---------------- INTERNET CHECK ----------------

fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// ---------------- MAIN SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayScreen() {

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val bgImage =
        if (isDark) painterResource(R.drawable.pyq_dark)
        else painterResource(R.drawable.pyq_light)

    val appBarColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5D1B5)

    var holidayUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val holidayJsonUrl =
        "https://raw.githubusercontent.com/adityarrsdce/babubhaiya/main/Holiday/holiday.json"

    // ---------------- DATA LOAD LOGIC ----------------
    LaunchedEffect(Unit) {

        // 1️⃣ LOAD FROM CACHE FIRST
        val cachedJson = getHolidayCache(context)
        if (cachedJson != null) {
            try {
                val map = Json.decodeFromString<Map<String, String>>(cachedJson)
                holidayUrl = map.values.firstOrNull()
                error = null
                isLoading = false
            } catch (_: Exception) {}
        }

        // 2️⃣ LOAD FROM INTERNET
        if (isInternetAvailable(context)) {
            try {
                val freshJson = withContext(Dispatchers.IO) {
                    fetchJsonFromUrl(holidayJsonUrl)
                }

                saveHolidayCache(context, freshJson)

                val map = Json.decodeFromString<Map<String, String>>(freshJson)
                holidayUrl = map.values.firstOrNull()

                error = null
                isLoading = false

            } catch (e: Exception) {
                if (holidayUrl == null) {
                    error = "Failed to load holiday calendar"
                    isLoading = false
                }
            }
        } else {
            if (holidayUrl == null) {
                error = "No internet & no cached data"
                isLoading = false
            }
        }
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
                painter = bgImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )

                    holidayUrl != null -> {
                        PdfViewer(
                            url = holidayUrl!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(0.dp))
                        )
                    }
                }
            }
        }
    }
}

// ---------------- PDF VIEWER ----------------

@Composable
fun PdfViewer(url: String, modifier: Modifier = Modifier) {
    val viewerUrl = "https://docs.google.com/gview?embedded=true&url=$url"

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                webViewClient = WebViewClient()
                loadUrl(viewerUrl)
            }
        }
    )
}
