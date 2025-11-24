package com.babu.appp.ads

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    val adUnitId = "ca-app-pub-3940256099942544/6300978111"

    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    MATCH_PARENT,
                    WRAP_CONTENT // ✅ Use WRAP_CONTENT for correct banner height
                )
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
