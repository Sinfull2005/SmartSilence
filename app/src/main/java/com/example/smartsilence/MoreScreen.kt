
package com.example.smartsilence

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsilence.data.SettingsEntity
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun MoreScreen(
    settings: SettingsEntity?,
    onSaveSettings: (SettingsEntity) -> Unit,
    onRequestSmsPermission: ((Boolean) -> Unit) -> Unit,
    onPriorityContactsClick: () -> Unit,
    onCalendarSyncClick: () -> Unit,
    onSecuritySettingsClick: () -> Unit,
    onLockApp: () -> Unit
) {
    val context = LocalContext.current

    val dndManager = remember {
        DndManager(context)
    }

    var dndAccessGranted by remember {
        mutableStateOf(dndManager.hasDndAccess())
    }

    var autoReplyEnabled by remember {
        mutableStateOf(settings?.autoReplyEnabled ?: false)
    }

    var autoReplyMessage by remember {
        mutableStateOf(
            settings?.autoReplyMessage
                ?: "Hi, I'm currently unavailable. I'll get back to you soon."
        )
    }

    LaunchedEffect(settings) {
        if (settings != null) {
            autoReplyEnabled = settings.autoReplyEnabled
            autoReplyMessage = settings.autoReplyMessage
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "MORE",
            color = ElectricBlue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(25.dp))

        // DND ACCESS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceNavy
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Do Not Disturb Access",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (dndAccessGranted) {
                        "Smart Silence can control Do Not Disturb."
                    } else {
                        "Permission is required to automatically control Do Not Disturb."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (dndAccessGranted) {
                        "✓ DND ACCESS GRANTED"
                    } else {
                        "DND ACCESS NOT GRANTED"
                    },
                    color = if (dndAccessGranted) {
                        ElectricBlue
                    } else {
                        TextSecondary
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!dndAccessGranted) {
                    Button(
                        onClick = {
                            dndManager.openDndSettings()
                            dndAccessGranted = dndManager.hasDndAccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = BackgroundNavy
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "GRANT DND ACCESS",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AUTOMATIC SMS REPLY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceNavy
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Automatic SMS Reply",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Send an automatic SMS when Smart Silence silences a call.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!autoReplyEnabled) {
                            onRequestSmsPermission { granted ->
                                if (granted) {
                                    onSaveSettings(
                                        SettingsEntity(
                                            id = settings?.id ?: 1,
                                            smartSilenceEnabled =
                                                settings?.smartSilenceEnabled
                                                    ?: false,
                                            autoReplyEnabled = true,
                                            autoReplyMessage = autoReplyMessage,
                                            batteryOptimizationEnabled =
                                                settings?.batteryOptimizationEnabled
                                                    ?: true
                                        )
                                    )
                                }
                            }
                        } else {
                            onSaveSettings(
                                SettingsEntity(
                                    id = settings?.id ?: 1,
                                    smartSilenceEnabled =
                                        settings?.smartSilenceEnabled
                                            ?: false,
                                    autoReplyEnabled = false,
                                    autoReplyMessage = autoReplyMessage,
                                    batteryOptimizationEnabled =
                                        settings?.batteryOptimizationEnabled
                                            ?: true
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (autoReplyEnabled) {
                            ElectricBlue
                        } else {
                            BackgroundNavy
                        },
                        contentColor = if (autoReplyEnabled) {
                            BackgroundNavy
                        } else {
                            TextWhite
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (autoReplyEnabled) {
                            "AUTOMATIC SMS: ON"
                        } else {
                            "AUTOMATIC SMS: OFF"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = autoReplyMessage,
                    onValueChange = {
                        autoReplyMessage = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("SMS Message")
                    },
                    placeholder = {
                        Text("Enter automatic reply")
                    },
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveSettings(
                            SettingsEntity(
                                id = settings?.id ?: 1,
                                smartSilenceEnabled =
                                    settings?.smartSilenceEnabled
                                        ?: false,
                                autoReplyEnabled = autoReplyEnabled,
                                autoReplyMessage = autoReplyMessage,
                                batteryOptimizationEnabled =
                                    settings?.batteryOptimizationEnabled
                                        ?: true
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = BackgroundNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "SAVE SMS SETTINGS",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PRIORITY CONTACTS
        MoreOption(
            title = "Priority Contacts",
            description = "Allow selected contacts during DND",
            onClick = onPriorityContactsClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CALENDAR SYNC
        MoreOption(
            title = "Calendar Sync",
            description = "View your upcoming calendar events",
            onClick = onCalendarSyncClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SETTINGS
        MoreOption(
            title = "Settings",
            description = "Manage Smart Silence security settings",
            onClick = onSecuritySettingsClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // APP LOCK
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceNavy
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "App Security",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Lock Smart Silence and require your app credential again.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLockApp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = BackgroundNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "LOCK SMART SILENCE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
