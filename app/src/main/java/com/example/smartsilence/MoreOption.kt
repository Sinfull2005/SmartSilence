package com.example.smartsilence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun MoreOption(

    title: String,

    description: String,

    onClick: () -> Unit

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    SurfaceNavy
            )
    ) {

        Column(

            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(

                text =
                    title,

                color =
                    TextWhite,

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(

                text =
                    description,

                color =
                    TextSecondary,

                fontSize =
                    13.sp
            )
        }
    }
}