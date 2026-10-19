package com.example.smartsilence

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun EditLocationDialog(
    location: LocationEntity,
    onDismiss: () -> Unit,
    onSave: (
        String,
        Double,
        Double,
        Float
    ) -> Unit,
    onRequestLocation: ((Double, Double) -> Unit) -> Unit
) {
    var name by remember {
        mutableStateOf(location.name)
    }

    var latitude by remember {
        mutableStateOf(location.latitude)
    }

    var longitude by remember {
        mutableStateOf(location.longitude)
    }

    var radius by remember {
        mutableStateOf(location.radius.toString())
    }

    var locationStatus by remember {
        mutableStateOf("Using saved location")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Edit Location",
                color = TextWhite
            )
        },

        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Location Name")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Button(
                    onClick = {
                        locationStatus =
                            "Getting current location..."

                        onRequestLocation {
                                lat, lon ->
                            latitude = lat
                            longitude = lon
                            locationStatus =
                                "New current location detected ✓"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = BackgroundNavy
                    )
                ) {
                    Text(
                        text = "📍 USE CURRENT LOCATION",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = locationStatus,
                    color = ElectricBlue,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Latitude: $latitude",
                    color = TextWhite,
                    fontSize = 12.sp
                )

                Text(
                    text = "Longitude: $longitude",
                    color = TextWhite,
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = radius,
                    onValueChange = { value ->
                        if (
                            value.all {
                                it.isDigit()
                            }
                        ) {
                            radius = value
                        }
                    },
                    label = {
                        Text("Radius (meters)")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    val radiusValue =
                        radius.toFloatOrNull()

                    if (
                        name.isNotBlank() &&
                        radiusValue != null &&
                        radiusValue > 0f
                    ) {
                        onSave(
                            name,
                            latitude,
                            longitude,
                            radiusValue
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = BackgroundNavy
                )
            ) {
                Text(
                    text = "UPDATE",
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("CANCEL")
            }
        }
    )
}