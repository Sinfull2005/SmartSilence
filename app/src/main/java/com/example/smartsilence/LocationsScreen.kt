package com.example.smartsilence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun LocationsScreen(

    locations:
    List<LocationEntity>,

    onAddLocation:
        (String, Double, Double, Float) -> Unit,

    onEditLocation:
        (
        LocationEntity,
        String,
        Double,
        Double,
        Float
    ) -> Unit,

    onDeleteLocation:
        (LocationEntity) -> Unit,

    onRequestLocation:
        ((Double, Double) -> Unit) -> Unit

) {

    var showAddLocationDialog by remember {
        mutableStateOf(false)
    }

    var locationToEdit by remember {
        mutableStateOf<LocationEntity?>(null)
    }


    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)

    ) {

        Spacer(
            modifier = Modifier.height(25.dp)
        )


        Text(
            text = "LOCATIONS",
            color = ElectricBlue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(6.dp)
        )


        Text(
            text = "Manage your Smart Silence locations",
            color = TextSecondary,
            fontSize = 14.sp
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ====================================================
        // ADD LOCATION
        // ====================================================

        Button(

            onClick = {
                showAddLocationDialog = true
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),

            shape =
                RoundedCornerShape(18.dp),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        ElectricBlue,

                    contentColor =
                        BackgroundNavy
                )
        ) {

            Text(
                text = "+ ADD LOCATION",
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ====================================================
        // LOCATION LIST
        // ====================================================

        if (locations.isEmpty()) {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor = SurfaceNavy
                    )

            ) {

                Column(

                    modifier =
                        Modifier.padding(20.dp)

                ) {

                    Text(
                        text = "No locations added",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    Text(
                        text =
                            "Add your current location where Smart Silence should automatically manage DND.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

        } else {

            Column(

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                locations.forEach { location ->

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

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
                                    location.name,

                                color =
                                    ElectricBlue,

                                fontSize =
                                    18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    "Latitude: ${location.latitude}",

                                color =
                                    TextWhite,

                                fontSize =
                                    13.sp
                            )


                            Text(
                                text =
                                    "Longitude: ${location.longitude}",

                                color =
                                    TextWhite,

                                fontSize =
                                    13.sp
                            )


                            Text(
                                text =
                                    "Radius: ${location.radius} m",

                                color =
                                    TextSecondary,

                                fontSize =
                                    13.sp
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )


                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)

                            ) {

                                Button(

                                    onClick = {
                                        locationToEdit =
                                            location
                                    },

                                    modifier =
                                        Modifier.weight(1f),

                                    colors =
                                        ButtonDefaults.buttonColors(

                                            containerColor =
                                                ElectricBlue,

                                            contentColor =
                                                BackgroundNavy
                                        )
                                ) {

                                    Text(
                                        text = "EDIT",
                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }


                                Button(

                                    onClick = {
                                        onDeleteLocation(
                                            location
                                        )
                                    },

                                    modifier =
                                        Modifier.weight(1f),

                                    colors =
                                        ButtonDefaults.buttonColors(

                                            containerColor =
                                                Color(0xFF331010),

                                            contentColor =
                                                TextWhite
                                        )
                                ) {

                                    Text(
                                        text = "DELETE"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // ========================================================
    // ADD LOCATION DIALOG
    // ========================================================

    if (showAddLocationDialog) {

        AddLocationDialog(

            onDismiss = {
                showAddLocationDialog =
                    false
            },

            onSave = {
                    name,
                    latitude,
                    longitude,
                    radius ->

                onAddLocation(
                    name,
                    latitude,
                    longitude,
                    radius
                )

                showAddLocationDialog =
                    false
            },

            onRequestLocation =
                onRequestLocation
        )
    }


    // ========================================================
    // EDIT LOCATION DIALOG
    // ========================================================

    locationToEdit?.let { location ->

        EditLocationDialog(

            location =
                location,

            onDismiss = {
                locationToEdit =
                    null
            },

            onSave = {
                    name,
                    latitude,
                    longitude,
                    radius ->

                onEditLocation(
                    location,
                    name,
                    latitude,
                    longitude,
                    radius
                )

                locationToEdit =
                    null
            },

            onRequestLocation =
                onRequestLocation
        )
    }
}