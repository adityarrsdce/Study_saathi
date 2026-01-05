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
data class BranchWrapper(val branches: Map<String, String>)
data class SemesterWrapper(val semesters: Map<String, String>)
typealias SubjectMap = Map<String, Map<String, String>>

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var branches by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var semesters by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var subjects by remember { mutableStateOf<SubjectMap>(emptyMap()) }

    var selectedBranch by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    var showPdfViewer by remember { mutableStateOf(false) }
    var pdfUrlToView by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val appBarColor = if (isDark) Color(0xFF1C1C1C) else Color(0xFFE5D1B5)
    val appBarTextColor = if (isDark) Color.White else Color.Black

    BackHandler(enabled = showPdfViewer) { showPdfViewer = false }

    // 🔹 Load branch list directly (course removed)
    LaunchedEffect(Unit) {
        try {
            val json = fetchJson(
                "https://raw.githubusercontent.com/adityarrsdce/babubhaiya/refs/heads/main/syllabus/btech/branch_list.json"
            )
            val wrapper = Gson().fromJson(json, BranchWrapper::class.java)
            branches = wrapper.branches
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

            // -------------------- BRANCH --------------------
            Dropdown("Select Branch", branches.keys.toList(), selectedBranch) { branch ->
                selectedBranch = branch
                selectedSemester = ""
                selectedSubject = null
                semesters = emptyMap()
                subjects = emptyMap()

                coroutineScope.launch {
                    val semJson = fetchJson(branches[branch]!!)
                    val wrapper = Gson().fromJson(semJson, SemesterWrapper::class.java)
                    semesters = wrapper.semesters
                }
            }

            Spacer(Modifier.height(8.dp))

            // -------------------- SEMESTER --------------------
            if (semesters.isNotEmpty()) {
                Dropdown("Select Semester", semesters.keys.toList(), selectedSemester) { sem ->
                    selectedSemester = sem
                    selectedSubject = null
                    subjects = emptyMap()

                    coroutineScope.launch {
                        val subjJson = fetchJson(semesters[sem]!!)
                        val type = object : TypeToken<SubjectMap>() {}.type
                        subjects = Gson().fromJson(subjJson, type)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // -------------------- SUBJECT --------------------
            if (subjects.isNotEmpty()) {
                Dropdown("Select Subject", subjects.keys.toList(), selectedSubject ?: "") {
                    selectedSubject = it
                }
            }

            Spacer(Modifier.height(16.dp))

            // -------------------- CONTENT --------------------
            selectedSubject?.let { subjectName ->
                val detail = subjects[subjectName] ?: return@let

                Text("📗 $subjectName", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                val pdfUrl = detail["url"] ?: ""
                val contentType = detail["type"]

                if (contentType == "pdf" && pdfUrl.isNotEmpty()) {
                    if (showPdfViewer) {
                        val viewerUrl =
                            "https://docs.google.com/gview?embedded=true&url=${Uri.encode(pdfUrlToView)}"
                        val webViewState = rememberWebViewState(viewerUrl)
                        var loading by remember { mutableStateOf(true) }

                        Box(Modifier.fillMaxSize()) {
                            AccompanistWebView(
                                state = webViewState,
                                modifier = Modifier.fillMaxSize(),
                                onCreated = {
                                    it.settings.javaScriptEnabled = true
                                    it.settings.loadWithOverviewMode = true
                                    it.settings.useWideViewPort = true
                                    it.settings.builtInZoomControls = true
                                    it.settings.displayZoomControls = false
                                },
                                client = object :
                                    com.google.accompanist.web.AccompanistWebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String?) {
                                        loading = false
                                    }
                                }
                            )

                            if (loading) {
                                Box(
                                    Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    pdfUrlToView = pdfUrl
                                    showPdfViewer = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("View PDF") }

                            Button(
                                onClick = {
                                    val fileName =
                                        "${subjectName.replace(" ", "_")}.pdf"
                                    val request =
                                        DownloadManager.Request(Uri.parse(pdfUrl))
                                            .setTitle(fileName)
                                            .setDescription("Downloading PDF...")
                                            .setNotificationVisibility(
                                                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                            )
                                            .setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS,
                                                fileName
                                            )

                                    val dm =
                                        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    dm.enqueue(request)
                                    Toast.makeText(
                                        context,
                                        "Download started",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Download PDF") }
                        }
                    }
                } else if (contentType == "text") {
                    LazyColumn {
                        item {
                            Text(
                                text = parseBoldText(detail["text"] ?: ""),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            BannerAd("ca-app-pub-4302526630220985/1663343480")
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
            Icon(Icons.Filled.ArrowDropDown, null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = { onSelect(it); expanded = false }
                )
            }
        }
    }
}

// ---------------------------- BANNER AD ----------------------------
@Composable
fun BannerAd(adUnitId: String) {
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

// ---------------------------- HELPERS ----------------------------
suspend fun fetchJson(url: String): String =
    withContext(Dispatchers.IO) { URL(url).readText() }

@Composable
fun parseBoldText(input: String): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        while (i < input.length) {
            if (i + 1 < input.length && input[i] == '*' && input[i + 1] == '*') {
                i += 2
                val start = i
                while (i + 1 < input.length && !(input[i] == '*' && input[i + 1] == '*')) i++
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(input.substring(start, i))
                pop()
                i += 2
            } else {
                append(input[i++])
            }
        }
    }
