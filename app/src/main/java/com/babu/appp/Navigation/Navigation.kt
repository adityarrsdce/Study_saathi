package com.babu.appp.Navigation


import android.app.Activity
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.babu.appp.AnalyticsHelper.AnalyticsNavObserver
import com.babu.appp.screen.*
import com.babu.appp.screen.bottom_bar.AboutScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    AnalyticsNavObserver(navController)

    val showBottomBar = currentRoute in listOf("home", "about", "ranking", "college", "events")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController = navController, paddingValues = innerPadding)
            }
            composable("pyq") { PyqScreen() }
            //composable("notes") { NotesScreen() }
            composable("result") { ResultScreen(navController = navController) }
            composable("syllabus") { SyllabusScreen() }
            composable("about") { AboutScreen() }
            composable("ranking") { RankingScreen() }
            composable("college") { HolidayScreen() }
            composable("events") { CalendarScreen() }

            composable("OtherResultScreen/{courseName}") { backStackEntry ->
                val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
                OtherResultScreen(courseName = courseName)
            }

            // ✅ NEW: Competitive Exam Screen
            composable("comp_exam") {
                CompetitiveExamScreenUI(navController = navController)
            }
            composable("imp") {
                ImpQScreen()
            }

            composable("diploma") {
                DiplomaScreenUI(navController = navController)
            }
            composable("resume") {
                ResumeScreen()
            }

            composable("feedback") {
                FeedbackScreen(navController = navController)
            }

        }
    }
}
