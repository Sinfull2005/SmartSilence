package com.example.smartsilence
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartsilence.ui.theme.SurfaceNavyLight

@Composable
fun BottomNavigationBar(

    selectedScreen:
    String,

    onScreenSelected:
        (String) -> Unit

) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(
                SurfaceNavyLight
            )
            .padding(
                vertical = 10.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceEvenly,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        NavigationItem(

            title =
                "HOME",

            selected =
                selectedScreen == "Home",

            onClick = {

                onScreenSelected(
                    "Home"
                )
            }
        )

        NavigationItem(

            title =
                "LOCATIONS",

            selected =
                selectedScreen == "Locations",

            onClick = {

                onScreenSelected(
                    "Locations"
                )
            }
        )

        NavigationItem(

            title =
                "TASKS",

            selected =
                selectedScreen == "Tasks",

            onClick = {

                onScreenSelected(
                    "Tasks"
                )
            }
        )

        NavigationItem(

            title =
                "MORE",

            selected =
                selectedScreen == "More",

            onClick = {

                onScreenSelected(
                    "More"
                )
            }
        )
    }
}