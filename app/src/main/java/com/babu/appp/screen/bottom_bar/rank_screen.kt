package com.babu.appp.screen

import com.babu.appp.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.babu.appp.json.fetchJsonFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CollegeRankData(
    val rank: Int,
    val image: String? = null,
    val website: String? = null
)

data class CollegeRanking(
    val name: String,
    val course: String,
    val rank: Int,
    val image: String? = null,
    val website: String? = null
)

fun <V> Map<String, V>.getrankInsensitive(key: String): V? {
    return entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen() {
    val isDark = isSystemInDarkTheme()

    val backgroundPainter = if (isDark) {
        painterResource(id = R.drawable.pyq_dark)
    } else {
        painterResource(id = R.drawable.pyq_light)
    }

    val topBarColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5D1B5)
    val dropdownBgColor =if (isDark) Color(0xFF2C2C2C) else Color(0xFFFFFFFF)
    val dropdownTextColor = if (isDark) Color.White else Color.Black

    var courseMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedCourse by remember { mutableStateOf("BTECH") }
    var rankings by remember { mutableStateOf<List<CollegeRanking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val jsonData = withContext(Dispatchers.IO) {
                fetchJsonFromUrl("https://raw.githubusercontent.com/adityarrsdce/babubhaiya/refs/heads/main/Rank/course_list.json")
            }
            courseMap = Json.decodeFromString(jsonData.toString())
        } catch (e: Exception) {
            error = "Failed to load courses"
        }
    }

    LaunchedEffect(selectedCourse, courseMap) {
        if (selectedCourse.isBlank() || courseMap.isEmpty()) return@LaunchedEffect
        isLoading = true
        try {
            val courseUrl = courseMap.getrankInsensitive(selectedCourse)
                ?: throw Exception("No URL found for $selectedCourse")

            val json = withContext(Dispatchers.IO) { fetchJsonFromUrl(courseUrl) }
            val data = Json.decodeFromString<Map<String, CollegeRankData>>(json.toString())

            rankings = data.entries
                .sortedBy { it.value.rank }
                .map {
                    CollegeRanking(
                        name = it.key,
                        course = selectedCourse,
                        rank = it.value.rank,
                        image = it.value.image,
                        website = it.value.website
                    )
                }

            error = null
        } catch (e: Exception) {
            error = "Data not found"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Rank") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = dropdownTextColor
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // 🖼 Background
            Image(
                painter = backgroundPainter,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (courseMap.isNotEmpty()) {
                    rankDropdown(
                        label = "Select Course",
                        options = courseMap.keys.toList(),
                        selected = selectedCourse,
                        onSelected = {
                            selectedCourse = it
                            rankings = emptyList()
                        },
                        backgroundColor = dropdownBgColor,
                        textColor = dropdownTextColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        error != null -> Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        rankings.isNotEmpty() -> LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(rankings) { college ->
                                RankingCard(college)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rankDropdown(
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
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            options.forEach { course ->
                DropdownMenuItem(
                    text = { Text(course, color = textColor) },
                    onClick = {
                        onSelected(course)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RankingCard(college: CollegeRanking) {
    val uriHandler = LocalUriHandler.current

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clickable(enabled = !college.website.isNullOrBlank()) {
                college.website?.let { uriHandler.openUri(it) }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            college.image?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${college.rank} - ${college.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = college.course,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
