package com.babu.appp.screen

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.babu.appp.R
import com.google.accompanist.web.WebView as AccompanistWebView
import com.google.accompanist.web.rememberWebViewState
import com.google.android.gms.ads.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// ---------------------------- DATA CLASSES ----------------------------
data class CourseWrapper(val courses: Map<String, String>)
data class BranchWrapper(val branches: Map<String, String>)
data class SemesterWrapper(val semesters: Map<String, String>)
typealias SubjectMap = Map<String, Map<String, String>> // subject -> details (url/type/text)

// ---------------------------- MAIN SCREEN ----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var courses by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var branches by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var semesters by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var subjects by remember { mutableStateOf<SubjectMap>(emptyMap()) }

    var selectedCourse by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    var showPdfViewer by remember { mutableStateOf(false) }
    var pdfUrlToView by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val appBarColor = if (isDark) Color(0xFF1C1C1C) else Color(0xFFE5D1B5)
    val appBarTextColor = if (isDark) Color.White else Color.Black

    BackHandler(enabled = showPdfViewer) { showPdfViewer = false }

    // Load course list
    LaunchedEffect(Unit) {
        try {
            val json = fetchJson("https://raw.githubusercontent.com/adityarrsdce/babubhaiya/refs/heads/main/syllabus/btech/course%20_list.json")
            val wrapper = Gson().fromJson(json, CourseWrapper::class.java)
            courses = wrapper.courses
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Syllabus", color = appBarTextColor, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // -------------------- DROPDOWNS --------------------
            Dropdown("Select Course", courses.keys.toList(), selectedCourse) { course ->
                selectedCourse = course
                selectedBranch = ""
                selectedSemester = ""
                selectedSubject = null
                branches = emptyMap()
                semesters = emptyMap()
                subjects = emptyMap()

                coroutineScope.launch {
                    try {
                        val branchJson = fetchJson(courses[course]!!)
                        val wrapper = Gson().fromJson(branchJson, BranchWrapper::class.java)
                        branches = wrapper.branches
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (branches.isNotEmpty()) {
                Dropdown("Select Branch", branches.keys.toList(), selectedBranch) { branch ->
                    selectedBranch = branch
                    selectedSemester = ""
                    selectedSubject = null
                    semesters = emptyMap()
                    subjects = emptyMap()

                    coroutineScope.launch {
                        try {
                            val semJson = fetchJson(branches[branch]!!)
                            val wrapper = Gson().fromJson(semJson, SemesterWrapper::class.java)
                            semesters = wrapper.semesters
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (semesters.isNotEmpty()) {
                Dropdown("Select Semester", semesters.keys.toList(), selectedSemester) { sem ->
                    selectedSemester = sem
                    selectedSubject = null
                    subjects = emptyMap()

                    coroutineScope.launch {
                        try {
                            val subjJson = fetchJson(semesters[sem]!!)
                            val type = object : TypeToken<SubjectMap>() {}.type
                            subjects = Gson().fromJson(subjJson, type)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (subjects.isNotEmpty()) {
                Dropdown("Select Subject", subjects.keys.toList(), selectedSubject ?: "") { subj ->
                    selectedSubject = subj
                }
            }

            Spacer(Modifier.height(16.dp))

            // -------------------- CONTENT DISPLAY --------------------
            selectedSubject?.let { subjectName ->
                val detail = subjects[subjectName]
                detail?.let {
                    Text("📗 $subjectName", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    // Decide PDF URL and type
                    val pdfUrl = it["url"] ?: ""
                    val contentType = it["type"]

                    if (contentType == "pdf" && pdfUrl.isNotEmpty()) {
                        if (showPdfViewer) {
                            val pdfViewerUrl = "https://docs.google.com/gview?embedded=true&url=${Uri.encode(pdfUrlToView)}"
                            val webViewState = rememberWebViewState(pdfViewerUrl)
                            var isLoading by remember { mutableStateOf(true) }

                            BackHandler(enabled = showPdfViewer) { showPdfViewer = false }

                            Box(Modifier.fillMaxSize()) {
                                AccompanistWebView(
                                    state = webViewState,
                                    modifier = Modifier.fillMaxSize(),
                                    onCreated = { webView ->
                                        webView.settings.javaScriptEnabled = true
                                        webView.settings.loadWithOverviewMode = true
                                        webView.settings.useWideViewPort = true
                                        webView.settings.builtInZoomControls = true
                                        webView.settings.displayZoomControls = false
                                    },
                                    client = object : com.google.accompanist.web.AccompanistWebViewClient() {
                                        override fun onPageFinished(view: WebView, url: String?) { isLoading = false }
                                    }
                                )
                                if (isLoading) {
                                    Box(
                                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) { CircularProgressIndicator(color = Color.White) }
                                }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { pdfUrlToView = pdfUrl; showPdfViewer = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("View PDF") }

                                Button(
                                    onClick = {
                                        val fileName = "${subjectName.replace(" ", "_")}.pdf"
                                        val request = DownloadManager.Request(Uri.parse(pdfUrl))
                                            .setTitle(fileName)
                                            .setDescription("Downloading PDF...")
                                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                            .setAllowedOverMetered(true)
                                            .setAllowedOverRoaming(true)

                                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        downloadManager.enqueue(request)
                                        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Download PDF") }
                            }
                        }

                    } else if (contentType == "text") {
                        val rawText = it["text"] ?: "No content available."

                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text(
                                    text = parseBoldText(rawText),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Text("❗ Type missing or invalid")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ✅ Banner Ad
            BannerAd(adUnitId = "ca-app-pub-4302526630220985/1663343480")
        }
    }
}

// ---------------------------- DROPDOWN ----------------------------
@Composable
fun Dropdown(label: String, items: List<String>, selectedItem: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedItem.isEmpty()) label else selectedItem)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item); expanded = false })
            }
        }
    }
}

// ---------------------------- BANNER AD ----------------------------
@Composable
fun BannerAd(adUnitId: String) {
    var isAdVisible by remember { mutableStateOf(true) }

    if (isAdVisible) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )
    }
}

// ---------------------------- FETCH JSON ----------------------------
suspend fun fetchJson(url: String): String = withContext(Dispatchers.IO) {
    URL(url).readText()
}

// ---------------------------- PARSE BOLD TEXT ----------------------------
@Composable
fun parseBoldText(input: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < input.length) {
            if (i + 1 < input.length && input[i] == '*' && input[i + 1] == '*') {
                i += 2
                val start = i
                while (i + 1 < input.length && !(input[i] == '*' && input[i + 1] == '*')) {
                    i++
                }
                val boldText = input.substring(start, i)
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(boldText)
                pop()
                i += 2 // skip **
            } else {
                append(input[i])
                i++
            }
        }
    }
}
