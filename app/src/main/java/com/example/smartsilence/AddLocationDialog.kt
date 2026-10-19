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
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.TextSecondary

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun AddLocationDialog(

    onDismiss: () -> Unit,

    onSave:
        (String, Double, Double, Float) -> Unit,

    onRequestLocation:
        ((Double, Double) -> Unit) -> Unit

) {

    var name by remember {
        mutableStateOf("")
    }

    var latitude by remember {
        mutableStateOf<Double?>(null)
    }

    var longitude by remember {
        mutableStateOf<Double?>(null)
    }

    var radius by remember {
        mutableStateOf("100")
    }

    var locationStatus by remember {
        mutableStateOf("Location not selected")
    }


    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text = "Add Location",
                color = TextWhite
            )
        },

        text = {

            Column {

                OutlinedTextField(

                    value =
                        name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text("Location Name")
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                )


                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                Button(

                    onClick = {

                        locationStatus =
                            "Getting current location..."

                        onRequestLocation {

                                lat,
                                lon ->

                            latitude =
                                lat

                            longitude =
                                lon

                            locationStatus =
                                "Current location detected ✓"
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                ElectricBlue,

                            contentColor =
                                BackgroundNavy
                        )
                ) {

                    Text(

                        text =
                            "📍 USE CURRENT LOCATION",

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                Text(

                    text =
                        locationStatus,

                    color =
                        if (
                            latitude != null &&
                            longitude != null
                        )
                            ElectricBlue
                        else
                            TextSecondary,

                    fontSize =
                        13.sp
                )


                if (
                    latitude != null &&
                    longitude != null
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(

                        text =
                            "Latitude: $latitude",

                        color =
                            TextWhite,

                        fontSize =
                            12.sp
                    )

                    Text(

                        text =
                            "Longitude: $longitude",

                        color =
                            TextWhite,

                        fontSize =
                            12.sp
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                OutlinedTextField(

                    value =
                        radius,

                    onValueChange = { value ->

                        if (
                            value.all {
                                it.isDigit()
                            }
                        ) {

                            radius =
                                value
                        }
                    },

                    label = {
                        Text("Radius (meters)")
                    },

                    supportingText = {
                        Text("Example: 100 = 100 meters")
                    },

                    modifier =
                        Modifier.fillMaxWidth()
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

                        latitude != null &&

                        longitude != null &&

                        radiusValue != null &&

                        radiusValue > 0f

                    ) {

                        onSave(

                            name,

                            latitude!!,

                            longitude!!,

                            radiusValue
                        )
                    }
                },

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            ElectricBlue,

                        contentColor =
                            BackgroundNavy
                    )
            ) {

                Text(

                    text =
                        "SAVE",

                    fontWeight =
                        FontWeight.Bold
                )
            }
        },

        dismissButton = {

            Button(
                onClick =
                    onDismiss
            ) {

                Text(
                    "CANCEL"
                )
            }
        }
    )
}