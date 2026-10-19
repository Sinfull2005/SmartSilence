package com.example.smartsilence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SmartSilenceSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.smart_silence_logo
            ),
            contentDescription = "Smart Silence logo",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
    }
}
