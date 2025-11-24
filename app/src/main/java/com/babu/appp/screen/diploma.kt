package com.babu.appp.screen

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.babu.appp.R
import com.babu.appp.json.fetchJsonFromUrl
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ---------------------------- DATA CLASSES ----------------------------
@Serializable
data class DiplomaPdf(val title: String, val fileUrl: String)

@Serializable
data class DiplomaSemesterList(val semesters: Map<String, String>) // semester name -> JSON URL

@Serializable
data class DiplomaSubjectList(val subjects: Map<String, String>) // subject name -> JSON URL

// ---------------------------- DOWNLOAD & OPEN PDF ----------------------------
fun downloadDiplomaPdf(context: Context, fileUrl: String, fileName: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle(fileName)
            setDescription("Downloading PDF...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun openPdfInViewer(context: Context, fileUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(fileUrl), "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
    }
}

// ---------------------------- MAIN SCREEN ----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiplomaScreenUI(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isDark = isSystemInDarkTheme()
    val background: Painter = if (isDark) painterResource(R.drawable.pyq_dark) else painterResource(R.drawable.pyq_light)
    val commonThumbnail = painterResource(R.drawable.pdf_icon)

    val appBarColor = if (isDark) Color(0xFF1C1C1C) else Color(0xFFE5D1B5)
    val appBarTextColor = if (isDark) Color.White else Color.Black
    val titleTextColor = if (isDark) Color.White else Color.Black

    // Dropdown states
    var semesterMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedSemester by remember { mutableStateOf<String?>(null) }
    var subjectMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var pdfList by remember { mutableStateOf(listOf<DiplomaPdf>()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Ads
    var mInterstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    val interstitialAdUnitId = "ca-app-pub-4302526630220985/2830135242"
    val bannerAdUnitId = "ca-app-pub-4302526630220985/1663343480"

    fun loadInterstitialAd(callback: (() -> Unit)? = null) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialAdUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { mInterstitialAd = ad; callback?.invoke() }
            override fun onAdFailedToLoad(adError: LoadAdError) { mInterstitialAd = null; callback?.invoke() }
        })
    }

    // ---------------------------- INITIAL LOAD ----------------------------
    LaunchedEffect(Unit) {
        MobileAds.initialize(context)
        MobileAds.setAppMuted(true)
        loadInterstitialAd()

        isLoading = true
        try {
            val json = withContext(Dispatchers.IO) { fetchJsonFromUrl("https://raw.githubusercontent.com/adityarrsdce/babubhaiya/main/Diploma/Master_JSON.json") }
            val parsed = Json.decodeFromString<DiplomaSemesterList>(json)
            semesterMap = parsed.semesters
        } catch (e: Exception) {
            errorMessage = "Failed to load semester list"
        } finally {
            isLoading = false
        }
    }

    fun loadSubjects(url: String) {
        isLoading = true
        errorMessage = null
        subjectMap = emptyMap()
        selectedSubject = null
        pdfList = emptyList()

        coroutineScope.launch {
            try {
                val json = withContext(Dispatchers.IO) { fetchJsonFromUrl(url) }
                val parsed = Json.decodeFromString<DiplomaSubjectList>(json)
                subjectMap = parsed.subjects
            } catch (_: Exception) {
                errorMessage = "Failed to load subjects"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadPdfs(url: String) {
        isLoading = true
        errorMessage = null
        pdfList = emptyList()

        coroutineScope.launch {
            try {
                val json = withContext(Dispatchers.IO) { fetchJsonFromUrl(url) }
                pdfList = Json.decodeFromString(json)
            } catch (_: Exception) {
                errorMessage = "Failed to load PDF data"
            } finally {
                isLoading = false
            }
        }
    }

    // ---------------------------- UI ----------------------------
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Diploma", color = appBarTextColor) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Image(painter = background, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            Column(modifier = Modifier.padding(16.dp)) {

                // ---------- SEMESTER DROPDOWN ----------
                if (semesterMap.isNotEmpty()) {
                    var expandedSem by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedSem,
                        onExpandedChange = { expandedSem = !expandedSem },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedSemester ?: "Select Semester",
                            onValueChange = {},
                            label = { Text("Select Semester") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSem) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.Black else Color.White,
                                unfocusedContainerColor = if (isDark) Color.Black else Color.White,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )
                        ExposedDropdownMenu(expanded = expandedSem, onDismissRequest = { expandedSem = false }) {
                            semesterMap.forEach { (semName, url) ->
                                DropdownMenuItem(text = { Text(semName) }, onClick = {
                                    selectedSemester = semName
                                    expandedSem = false
                                    loadSubjects(url)
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ---------- SUBJECT DROPDOWN ----------
                if (subjectMap.isNotEmpty()) {
                    var expandedSub by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedSub,
                        onExpandedChange = { expandedSub = !expandedSub },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedSubject ?: "Select Subject",
                            onValueChange = {},
                            label = { Text("Select Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.Black else Color.White,
                                unfocusedContainerColor = if (isDark) Color.Black else Color.White,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )
                        ExposedDropdownMenu(expanded = expandedSub, onDismissRequest = { expandedSub = false }) {
                            subjectMap.forEach { (subName, url) ->
                                DropdownMenuItem(text = { Text(subName) }, onClick = {
                                    selectedSubject = subName
                                    expandedSub = false
                                    loadPdfs(url)
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ---------- PDF LIST ----------
                when {
                    isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    errorMessage != null -> Text(errorMessage ?: "", color = Color.Red)
                    pdfList.isNotEmpty() -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            pdfList.forEach { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { openPdfInViewer(context, item.fileUrl) },
                                    border = BorderStroke(1.dp, Color.Black)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Image(painter = commonThumbnail, contentDescription = null, modifier = Modifier.size(40.dp), contentScale = ContentScale.Crop)
                                        Text(text = item.title, color = titleTextColor, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                                        IconButton(onClick = {
                                            val fileName = item.title.replace(" ", "_") + ".pdf"
                                            val startDownload = { downloadDiplomaPdf(context, item.fileUrl, fileName); loadInterstitialAd() }

                                            if (mInterstitialAd != null && activity != null) {
                                                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                                    override fun onAdDismissedFullScreenContent() { startDownload() }
                                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) { startDownload() }
                                                }
                                                mInterstitialAd?.show(activity)
                                            } else startDownload()
                                        }) { Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.Red) }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                DipBannerAd(adUnitId = bannerAdUnitId)
            }
        }
    }
}

// ---------------------------- BANNER AD ----------------------------
@Composable
fun DipBannerAd(adUnitId: String) {
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
