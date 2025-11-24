package com.babu.appp.screen

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babu.appp.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

// ---------------------------- DATA CLASSES ----------------------------
data class CourseList(val courses: Map<String, String>)
data class BranchList(val branches: Map<String, String>)
data class SemesterList(val semesters: Map<String, String>)
typealias SubjectYearMap = Map<String, Map<String, String>> // subject -> year -> pdf_url

// ---------------------------- MAIN SCREEN ----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dropdown states
    var courses by remember { mutableStateOf<List<String>>(emptyList()) }
    var branches by remember { mutableStateOf<List<String>>(emptyList()) }
    var semesters by remember { mutableStateOf<List<String>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var years by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedCourse by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }

    // Data storage
    var branchMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var semesterMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var subjectYearMap by remember { mutableStateOf<SubjectYearMap>(emptyMap()) }

    val isDark = isSystemInDarkTheme()
    val cardBgColor =
        if (isDark) Color(0xFF2C2C2C).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.96f)
    val textColor = if (isDark) Color.White else Color.Black
    val appBarColor = if (isDark) Color(0xFF1C1C1C) else Color(0xFFE5D1B5)
    val appBarTextColor = if (isDark) Color.White else Color.Black

    var showPdfViewer by remember { mutableStateOf(false) }
    var pdfUrlToView by remember { mutableStateOf("") }

    // ✅ Handle phone back button when PDF viewer is open
    BackHandler(enabled = showPdfViewer) {
        showPdfViewer = false
    }

    // Initialize Ads + load course list
    LaunchedEffect(Unit) {
        MobileAds.initialize(context)
        MobileAds.setAppMuted(true)

        coroutineScope.launch {
            try {
                val json =
                    fetchJsonFromUrl("https://raw.githubusercontent.com/adityarrsdce/babubhaiya/refs/heads/main/PYQ/Btech/Coures_list.json")
                val data = Gson().fromJson(json, CourseList::class.java)
                courses = data.courses.keys.toList()
                branchMap = data.courses
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showPdfViewer && pdfUrlToView.isNotEmpty()) {
        // WebView state with loading
        val pdfViewerUrl =
            "https://docs.google.com/gview?embedded=true&url=${Uri.encode(pdfUrlToView)}"
        val webViewState = rememberWebViewState(pdfViewerUrl)
        var isLoading by remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { showPdfViewer = false }) {
                            Icon(
                                painterResource(id = R.drawable.arrow),
                                contentDescription = "Back",
                                tint = appBarTextColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                WebView(
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
                        override fun onPageFinished(view: WebView, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Previous Year Questions", color = appBarTextColor) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                val backgroundPainter =
                    if (isDark) painterResource(id = R.drawable.pyq_dark)
                    else painterResource(id = R.drawable.pyq_light)

                Image(
                    painter = backgroundPainter,
                    contentDescription = "background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Download PYQ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text("All courses supported", fontSize = 14.sp, color = textColor)

                        // -------------------- DROPDOWNS --------------------
                        DropdownSelector("Select Course", courses, selectedCourse) { course ->
                            selectedCourse = course
                            selectedBranch = ""
                            selectedSemester = ""
                            selectedSubject = ""
                            selectedYear = ""
                            branches = emptyList()
                            semesters = emptyList()
                            subjects = emptyList()
                            years = emptyList()

                            coroutineScope.launch {
                                try {
                                    val json = fetchJsonFromUrl(branchMap[course]!!)
                                    val data = Gson().fromJson(json, BranchList::class.java)
                                    branchMap = data.branches
                                    branches = data.branches.keys.toList()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (branches.isNotEmpty()) DropdownSelector(
                            "Select Branch",
                            branches,
                            selectedBranch
                        ) { branch ->
                            selectedBranch = branch
                            selectedSemester = ""
                            selectedSubject = ""
                            selectedYear = ""
                            semesters = emptyList()
                            subjects = emptyList()
                            years = emptyList()

                            coroutineScope.launch {
                                try {
                                    val json = fetchJsonFromUrl(branchMap[branch]!!)
                                    val data = Gson().fromJson(json, SemesterList::class.java)
                                    semesterMap = data.semesters
                                    semesters = data.semesters.keys.toList()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (semesters.isNotEmpty()) DropdownSelector(
                            "Select Semester",
                            semesters,
                            selectedSemester
                        ) { sem ->
                            selectedSemester = sem
                            selectedSubject = ""
                            selectedYear = ""
                            subjects = emptyList()
                            years = emptyList()

                            coroutineScope.launch {
                                try {
                                    val json = fetchJsonFromUrl(semesterMap[sem]!!)
                                    val type =
                                        com.google.gson.reflect.TypeToken.getParameterized(
                                            Map::class.java,
                                            String::class.java,
                                            Map::class.java
                                        ).type
                                    @Suppress("UNCHECKED_CAST")
                                    subjectYearMap = Gson().fromJson(json, type) as SubjectYearMap
                                    subjects = subjectYearMap.keys.toList()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (subjects.isNotEmpty()) DropdownSelector(
                            "Select Subject",
                            subjects,
                            selectedSubject
                        ) { subj ->
                            selectedSubject = subj
                            years = subjectYearMap[subj]?.keys?.toList() ?: emptyList()
                            selectedYear = ""
                        }

                        if (years.isNotEmpty()) DropdownSelector(
                            "Select Year",
                            years,
                            selectedYear
                        ) { yr ->
                            selectedYear = yr
                        }

                        // -------------------- VIEW + DOWNLOAD BUTTONS --------------------
                        val pdfUrl =
                            subjectYearMap[selectedSubject]?.get(selectedYear) ?: ""

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ----------------- VIEW BUTTON -----------------
                            Button(
                                onClick = {
                                    when {
                                        selectedCourse.isEmpty() || selectedBranch.isEmpty() ||
                                                selectedSemester.isEmpty() || selectedSubject.isEmpty() || selectedYear.isEmpty() -> {
                                            Toast.makeText(
                                                context,
                                                "Please select all fields",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        pdfUrl.isEmpty() -> {
                                            Toast.makeText(
                                                context,
                                                "File not available",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {
                                            pdfUrlToView = pdfUrl
                                            showPdfViewer = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View")
                            }

                            // ----------------- DOWNLOAD BUTTON -----------------
                            // ----------------- DOWNLOAD BUTTON -----------------
                            Button(
                                onClick = {
                                    when {
                                        selectedCourse.isEmpty() || selectedBranch.isEmpty() ||
                                                selectedSemester.isEmpty() || selectedSubject.isEmpty() || selectedYear.isEmpty() -> {
                                            Toast.makeText(
                                                context,
                                                "Please select all fields",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        pdfUrl.isEmpty() -> {
                                            Toast.makeText(
                                                context,
                                                "File not available",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {
                                            val fileName =
                                                "${selectedSubject}_${selectedYear}.pdf".replace(" ", "_")

                                            val startDownload = {
                                                val request =
                                                    DownloadManager.Request(Uri.parse(pdfUrl)).apply {
                                                        setTitle(fileName)
                                                        setDescription("Downloading PDF...")
                                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                        setDestinationInExternalPublicDir(
                                                            Environment.DIRECTORY_DOWNLOADS,
                                                            fileName
                                                        )
                                                        setAllowedOverMetered(true)
                                                        setAllowedOverRoaming(true)
                                                    }
                                                val downloadManager =
                                                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                                downloadManager.enqueue(request)
                                                Toast.makeText(
                                                    context,
                                                    "Download started",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            // ✅ Pehle Toast show karein
                                            Toast.makeText(
                                                context,
                                                "Download will start after Ad",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // ✅ Ab Ad load kijiye
                                            val adRequest = AdRequest.Builder().build()
                                            InterstitialAd.load(
                                                context,
                                                "ca-app-pub-4302526630220985/2830135242",
                                                adRequest,
                                                object : InterstitialAdLoadCallback() {
                                                    override fun onAdLoaded(ad: InterstitialAd) {
                                                        ad.fullScreenContentCallback =
                                                            object : FullScreenContentCallback() {
                                                                override fun onAdDismissedFullScreenContent() {
                                                                    startDownload()
                                                                }

                                                                override fun onAdFailedToShowFullScreenContent(
                                                                    adError: AdError
                                                                ) {
                                                                    startDownload()
                                                                }
                                                            }
                                                        if (context is android.app.Activity) ad.show(context)
                                                        else startDownload()
                                                    }

                                                    override fun onAdFailedToLoad(adError: LoadAdError) {
                                                        startDownload()
                                                    }
                                                })
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Download")
                            }

                        }
                    }
                }
            }
        }
    }
}

// ---------------------------- DROPDOWN COMPOSABLE ----------------------------
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val menuBgColor = if (isDark) Color(0xFF2B2B2B) else Color.White
    val textColor = if (isDark) Color.White else Color.Black

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (selectedOption.isEmpty()) label else selectedOption, color = textColor)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = textColor)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(menuBgColor)
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = textColor) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ---------------------------- FETCH JSON ----------------------------
suspend fun fetchJsonFromUrl(url: String): String = withContext(Dispatchers.IO) {
    URL(url).readText()
}
