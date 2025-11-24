package com.babu.appp.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.babu.appp.R
import com.google.android.gms.ads.*


@Composable
fun HomeScreen(navController: NavController, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }

    // ✅ Back press handler
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // Exit the app
            (context as? Activity)?.finish()
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            )
    ) {
        // 🔰 App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.pyq_icon),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Study Saathi",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Study Materials", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(12.dp))

            CardGrid(
                items = listOf(
                    Triple("PYQs", R.drawable.pyq_icon, Color(0xFFFF8C00)),
                    Triple("Results", R.drawable.result_icon, Color(0xFFFFD100)),
                    Triple("Syllabus", R.drawable.syllabus_icon, Color(0xFF00B894)),
                    Triple("Important Questions", R.drawable.important_icon, Color(0xFF6C5CE7)),
                    //Triple("Diploma", R.drawable.diploma_icon, Color(0xFF3498DB)),
                    Triple("Feedback", R.drawable.feedback_icon, Color(0xFFE64A19)),
                    Triple("Competitive Exam", R.drawable.comp_icon, Color(0xFFEF476F)),
                    //Triple("Resume", R.drawable.resume_icon, Color(0xFF1E88E5))

                ),
                onItemClick = { label ->
                    when (label) {
                        "PYQs" -> navController.navigate("pyq")
                        "Syllabus" -> navController.navigate("syllabus")
                        "Results" -> navController.navigate("result")
                        "Important Questions" -> navController.navigate("imp")
                        "Feedback" -> navController.navigate("feedback")
                        "Competitive Exam" -> navController.navigate("comp_exam")
                        //"Diploma" -> navController.navigate("diploma")
                        //"Resume" -> navController.navigate("resume")

                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Show Banner Ad continuously
            ShowBannerAd(adUnitId = "ca-app-pub-4302526630220985/1663343480")

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CardGrid(
    items: List<Triple<String, Int, Color>>,
    onItemClick: (String) -> Unit
) {
    Column {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for ((label, iconRes, _) in rowItems) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .clickable { onItemClick(label) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ShowBannerAd(adUnitId: String) {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)

                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}
