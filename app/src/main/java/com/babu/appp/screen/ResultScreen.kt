package com.babu.appp.screen

import com.babu.appp.R

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val backgroundImage = if (isDark) R.drawable.pyq_dark else R.drawable.pyq_light

    var regNo by rememberSaveable { mutableStateOf("") }
    var selectedCourse by rememberSaveable { mutableStateOf("") }
    var selectedSemester by rememberSaveable { mutableStateOf("") }
    var resultUrl by remember { mutableStateOf<String?>(null) }
    var courseLinks by remember { mutableStateOf<Map<String, Map<String, String>>>(emptyMap()) }

    var isRotating by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "rotation"
    )

    val jsonUrl = "https://raw.githubusercontent.com/adityarrsdce/babubhaiya/main/Resutl/result.json"

    fun updateResultUrl() {
        val semUrl = courseLinks[selectedCourse]?.get(selectedSemester)
        if (!semUrl.isNullOrEmpty() && regNo.isNotBlank()) {
            val romanSem = convertSemesterToRoman(selectedSemester)
            resultUrl = "$semUrl?Sem=$romanSem&RegNo=$regNo"
        }
    }

    LaunchedEffect(Unit) {
        courseLinks = fetchResultLinks(jsonUrl) ?: emptyMap()
    }

    val courses = courseLinks.keys.toList()
    val semesters = courseLinks[selectedCourse]?.keys?.toList() ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result Viewer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF1F1F1F) else Color(0xFFE5D1B5),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            isRotating = true
                            updateResultUrl()
                            Toast.makeText(context, "Reloading result...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                delay(600)
                                isRotating = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = Color.White,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = backgroundImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = regNo,
                        onValueChange = {
                            regNo = it
                            updateResultUrl()
                        },
                        label = { Text("Registration Number") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color.Black else Color.White,
                            unfocusedContainerColor = if (isDark) Color.Black else Color.White,
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedLabelColor = if (isDark) Color.White else Color.Black,
                            unfocusedLabelColor = if (isDark) Color.White else Color.Black
                        )
                    )
                    Column {
                        IconButton(onClick = {
                            regNo = (regNo.toLongOrNull()?.plus(1)).toString()
                            updateResultUrl()
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                        }
                        IconButton(onClick = {
                            regNo = (regNo.toLongOrNull()?.minus(1)).toString()
                            updateResultUrl()
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Course dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedCourse.ifBlank { "Select Course" },
                            onValueChange = {},
                            label = { Text("Course") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.Black else Color.White,
                                unfocusedContainerColor = if (isDark) Color.Black else Color.White,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedLabelColor = if (isDark) Color.White else Color.Black,
                                unfocusedLabelColor = if (isDark) Color.White else Color.Black
                            ),
                            modifier = Modifier.menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course) },
                                    onClick = {
                                        selectedCourse = course
                                        selectedSemester = ""
                                        resultUrl = null
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (selectedCourse.isNotBlank()) {
                        Button(onClick = {
                            navController.navigate("OtherResultScreen/${selectedCourse}")
                        }) {
                            Text("Other Result")
                        }
                    }
                }

                // Semester buttons
                if (selectedCourse.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Semester:", style = MaterialTheme.typography.titleMedium)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        semesters.forEach { sem ->
                            Button(
                                onClick = {
                                    selectedSemester = sem
                                    updateResultUrl()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sem == selectedSemester)
                                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Sem $sem")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WebView + Download
                resultUrl?.let { url ->
                    Button(
                        onClick = {
                            try {
                                val request = DownloadManager.Request(Uri.parse(url))
                                    .setTitle("Result_$regNo")
                                    .setDescription("Downloading Result")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        "Result_$regNo.pdf"
                                    )

                                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(request)

                                Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save PDF")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ResultWebView(url = url)
                }
            }
        }
    }
}

@Composable
fun ResultWebView(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                loadUrl(url)
            }
        },
        update = { webView -> webView.loadUrl(url) },
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    )
}

suspend fun fetchResultLinks(jsonUrl: String): Map<String, Map<String, String>>? {
    return withContext(Dispatchers.IO) {
        try {
            val jsonText = URL(jsonUrl).readText()
            val json = JSONObject(jsonText)
            val resultMap = mutableMapOf<String, Map<String, String>>()

            json.keys().forEach { course ->
                val courseObject = json.getJSONObject(course)
                val semObj = courseObject.getJSONObject("semesters")
                val semMap = mutableMapOf<String, String>()
                semObj.keys().forEach { sem ->
                    semMap[sem] = semObj.getString(sem)
                }
                resultMap[course] = semMap
            }
            resultMap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun convertSemesterToRoman(sem: String): String {
    return when (sem) {
        "1" -> "I"
        "2" -> "II"
        "3" -> "III"
        "4" -> "IV"
        "5" -> "V"
        "6" -> "VI"
        "7" -> "VII"
        "8" -> "VIII"
        else -> sem
    }
}
