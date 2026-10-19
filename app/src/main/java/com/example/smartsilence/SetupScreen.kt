package com.example.smartsilence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.BackgroundNavy2
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.TextSecondary

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun SetupScreen(
    onSetupStart: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundNavy,
                        BackgroundNavy2,
                        BackgroundNavy
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "SMART SILENCE",
                color = ElectricBlue,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Let's set up Smart Silence",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Smart Silence needs access to your location and contacts to provide location-based silence and priority contact features.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Button(
                onClick = onSetupStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = BackgroundNavy
                )
            ) {
                Text(
                    text = "START SETUP",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}