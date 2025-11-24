package com.babu.appp.AnalyticsHelper

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

@Composable
fun AnalyticsNavObserver(navController: NavHostController) {
    val firebaseAnalytics = Firebase.analytics

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val screenName = destination.route ?: "unknown"
            val bundle = Bundle().apply {
                putString("screen_name", screenName)
            }
            firebaseAnalytics.logEvent("screen_view", bundle)
        }
    }
}
