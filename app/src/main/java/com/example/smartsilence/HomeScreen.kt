package com.example.smartsilence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.smartsilence.location.SmartSilenceTTS
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.SurfaceNavyDark
import com.example.smartsilence.ui.theme.SurfaceNavyLight
import com.example.smartsilence.ui.theme.TextPrimary
import com.example.smartsilence.ui.theme.TextSecondary


private val TextWhite =
    Color(0xFFF5F7FF)


@Composable
fun HomeScreen(
    onDndRuleChange: (() -> Unit) -> Unit
) {

    val context =
        LocalContext.current


    // =========================================================
    // TTS
    // =========================================================

    val tts =
        remember {
            SmartSilenceTTS(
                context
            )
        }

    DisposableEffect(Unit) {

        onDispose {
            tts.shutdown()
        }
    }


    // =========================================================
    // DND MANAGER
    // =========================================================

    val dndManager =
        remember {
            DndManager(
                context
            )
        }

    var dndEnabled by remember {
        mutableStateOf(
            dndManager.isDndEnabled()
        )
    }

    var statusMessage by remember {

        mutableStateOf(

            if (dndEnabled)

                "Do Not Disturb is currently active"

            else

                "Do Not Disturb is currently inactive"
        )
    }


    // =========================================================
    // UI
    // =========================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    BackgroundNavy
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                )
    ) {


        // =====================================================
        // HEADER
        // =====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "SMART SILENCE",

                    color =
                        TextWhite,

                    fontSize =
                        26.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        "Context-aware notification management",

                    color =
                        TextSecondary,

                    fontSize =
                        13.sp
                )
            }

            Card(

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            SurfaceNavyDark
                    )
            ) {

                Text(
                    text =
                        "◉",

                    color =
                        ElectricBlue,

                    fontSize =
                        22.sp,

                    modifier =
                        Modifier.padding(
                            12.dp
                        )
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    22.dp
                )
        )


        // =====================================================
        // DND STATUS
        // =====================================================

        Card(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        175.dp
                    ),

            shape =
                RoundedCornerShape(
                    24.dp
                ),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        SurfaceNavy
                )
        ) {

            Row(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            22.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "DND STATUS",

                        color =
                            TextSecondary,

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                10.dp
                            )
                    )

                    Text(

                        text =
                            if (dndEnabled)

                                "ACTIVE"

                            else

                                "INACTIVE",

                        color =
                            ElectricBlue,

                        fontSize =
                            29.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                7.dp
                            )
                    )

                    Text(
                        text =
                            statusMessage,

                        color =
                            TextPrimary,

                        fontSize =
                            13.sp
                    )
                }

                Card(

                    shape =
                        RoundedCornerShape(
                            60.dp
                        ),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                SurfaceNavyLight
                        )
                ) {

                    Box(

                        modifier =
                            Modifier.size(
                                85.dp
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "⌁",

                            color =
                                ElectricBlue,

                            fontSize =
                                38.sp
                        )
                    }
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        // =====================================================
        // SMART SILENCE BUTTON
        // =====================================================

        Button(

            onClick = {

                // =================================================
                // DND ACCESS CHECK
                // =================================================

                if (
                    !dndManager.hasDndAccess()
                ) {

                    dndManager.openDndSettings()

                    statusMessage =
                        "Please grant DND access in Android Settings."

                    return@Button
                }


                // =================================================
                // PROTECTED DND RULE CHANGE
                // =================================================

                onDndRuleChange {

                    // =================================================
                    // DISABLE SMART SILENCE
                    // =================================================

                    if (dndEnabled) {

                        val disabled =
                            dndManager.disableDnd()

                        if (disabled) {

                            dndEnabled =
                                false

                            statusMessage =
                                "Do Not Disturb has been disabled."

                            tts.speak(
                                "Do Not Disturb has been disabled."
                            )

                        } else {

                            statusMessage =
                                "Unable to disable Do Not Disturb."
                        }

                        return@onDndRuleChange
                    }


                    // =================================================
                    // ENABLE SMART SILENCE
                    // =================================================

                    statusMessage =
                        "Smart Silence is enabling..."


                    /*
                     * IMPORTANT:
                     *
                     * DND can suppress audio streams.
                     *
                     * Therefore the acknowledgement is spoken FIRST.
                     *
                     * DND is enabled only after TTS reports that the
                     * acknowledgement has finished.
                     */

                    tts.speak(

                        "Do Not Disturb has been enabled.",

                        onDone = {

                            val enabled =
                                dndManager.enableDnd()

                            if (enabled) {

                                dndEnabled =
                                    true

                                statusMessage =
                                    "Do Not Disturb has been enabled."

                            } else {

                                dndEnabled =
                                    dndManager.isDndEnabled()

                                statusMessage =

                                    if (dndEnabled)

                                        "Do Not Disturb has been enabled."

                                    else

                                        "Unable to enable Do Not Disturb."
                            }
                        }
                    )
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        54.dp
                    ),

            shape =
                RoundedCornerShape(
                    18.dp
                ),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        ElectricBlue,

                    contentColor =
                        TextWhite
                )
        ) {

            Text(

                text =
                    if (dndEnabled)

                        "DISABLE SMART SILENCE"

                    else

                        "ENABLE SMART SILENCE",

                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    22.dp
                )
        )


        // =====================================================
        // CURRENT CONTEXT
        // =====================================================

        Text(
            text =
                "CURRENT CONTEXT",

            color =
                ElectricBlue,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            ContextCard(

                title =
                    "LOCATION",

                value =
                    "Unknown",

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            ContextCard(

                title =
                    "ACTIVITY",

                value =
                    "Still",

                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )


        // =====================================================
        // QUICK ACTIONS
        // =====================================================

        Text(
            text =
                "QUICK ACTIONS",

            color =
                ElectricBlue,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            QuickActionCard(

                title =
                    "Add\nLocation",

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            QuickActionCard(

                title =
                    "Priority\nContacts",

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            QuickActionCard(

                title =
                    "Settings",

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            QuickActionCard(

                title =
                    "Tasks",

                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }
    }
}