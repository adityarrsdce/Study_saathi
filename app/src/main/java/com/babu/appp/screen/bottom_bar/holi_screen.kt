package com.babu.appp.screen

import com.babu.appp.R

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.babu.appp.json.fetchJsonFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

fun String.holiPdf(): Boolean = lowercase().endsWith(".pdf")
fun String.holiImage(): Boolean = lowercase().endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp")

fun <K, V> Map<K, V>.getInsensitive(key: K): V? {
    return entries.firstOrNull { it.key.toString().equals(key.toString(), ignoreCase = true) }?.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayScreen() {
    val isDark = isSystemInDarkTheme()
    val backgroundPainter = if (isDark) painterResource(R.drawable.pyq_dark) else painterResource(R.drawable.pyq_light)
    val appBarColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5D1B5)
    val appBarTextColor = if (isDark) Color.White else Color.Black
    val dropdownBgColor = if (isDark) Color(0xFF2C2C2C) else Color.White
    val dropdownTextColor = if (isDark) Color.White else Color.Black
    val errorTextColor = if (isDark) Color.White else Color.Black

    var courseMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedCourse by rememberSaveable { mutableStateOf("") }
    var selectedFileUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoadingFile by remember { mutableStateOf(false) }

    val holidayJsonUrl = "https://raw.githubusercontent.com/adityarrsdce/babubhaiya/refs/heads/main/Holiday/holiday.json"

    LaunchedEffect(Unit) {
        try {
            val jsonData = withContext(Dispatchers.IO) {
                fetchJsonFromUrl(holidayJsonUrl)
            }
            courseMap = Json.decodeFromString(jsonData.toString())
        } catch (e: Exception) {
            error = "Failed to load courses"
        }
    }

    LaunchedEffect(selectedCourse) {
        if (selectedCourse.isNotBlank()) {
            isLoadingFile = true
            selectedFileUrl = courseMap.getInsensitive(selectedCourse)
            isLoadingFile = false
        } else {
            selectedFileUrl = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Holiday Calendar", color = appBarTextColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = backgroundPainter,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (courseMap.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    error?.let {
                        Text(it, color = errorTextColor)
                    } ?: CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    CustomDropdown(
                        label = "Select Course",
                        options = courseMap.keys.toList(),
                        selected = selectedCourse,
                        onSelected = { selectedCourse = it },
                        backgroundColor = dropdownBgColor,
                        textColor = dropdownTextColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoadingFile -> CircularProgressIndicator()
                            selectedFileUrl != null -> {
                                when {
                                    selectedFileUrl!!.holiPdf() -> holiViewerWebView(selectedFileUrl!!)
                                    selectedFileUrl!!.holiImage() -> ZoomableImageViewer(selectedFileUrl!!)
                                    else -> Text("Unsupported file type", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    backgroundColor: Color,
    textColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (selected.isBlank()) label else selected, color = textColor)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(backgroundColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = textColor) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun holiViewerWebView(url: String) {
    val viewerUrl = "https://docs.google.com/gview?embedded=true&url=$url"
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                loadUrl(viewerUrl)
            }
        }
    )
}

@Composable
fun ZoomableImageViewer(url: String) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}
