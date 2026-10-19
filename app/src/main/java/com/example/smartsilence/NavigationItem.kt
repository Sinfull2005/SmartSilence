package com.example.smartsilence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.TextSecondary

@Composable
fun NavigationItem(

    title:
    String,

    selected:
    Boolean,

    onClick:
        () -> Unit

) {

    Column(

        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(

            modifier = Modifier
                .size(
                    if (selected)
                        8.dp
                    else
                        6.dp
                )
                .background(

                    color =
                        if (selected)
                            ElectricBlue
                        else
                            TextSecondary,

                    shape =
                        RoundedCornerShape(50)
                )
        )

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(

            text =
                title,

            color =
                if (selected)
                    ElectricBlue
                else
                    TextSecondary,

            fontSize =
                10.sp,

            fontWeight =
                if (selected)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )
    }
}