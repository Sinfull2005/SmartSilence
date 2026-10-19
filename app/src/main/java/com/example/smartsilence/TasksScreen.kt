package com.example.smartsilence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField

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
import com.example.smartsilence.data.TaskEntity

import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary


private val TextWhite =
    Color(0xFFF5F7FF)


@Composable
fun TasksScreen(

    tasks:
    List<TaskEntity>,

    locations:
    List<LocationEntity>,

    onAddTask:
        (String, String, Int?) -> Unit,

    onUpdateTask:
        (TaskEntity) -> Unit,

    onDeleteTask:
        (TaskEntity) -> Unit

) {

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var editingTask by remember {
        mutableStateOf<TaskEntity?>(null)
    }


    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
        ) {

            Spacer(
                modifier =
                    Modifier.height(25.dp)
            )

            Text(
                text =
                    "TASKS",

                color =
                    ElectricBlue,

                fontSize =
                    28.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    "Location and schedule based reminders",

                color =
                    TextSecondary,

                fontSize =
                    14.sp
            )

            Spacer(
                modifier =
                    Modifier.height(25.dp)
            )


            // ====================================================
            // ADD TASK
            // ====================================================

            Button(

                onClick = {

                    showAddDialog =
                        true
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            ElectricBlue,

                        contentColor =
                            BackgroundNavy
                    ),

                shape =
                    RoundedCornerShape(20.dp)
            ) {

                Text(
                    text =
                        "+ ADD TASK",

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )


            // ====================================================
            // TASK LIST
            // ====================================================

            if (tasks.isEmpty()) {

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
                                "No tasks yet",

                            color =
                                TextWhite,

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Create reminders based on location or time.",

                            color =
                                TextSecondary,

                            fontSize =
                                13.sp
                        )
                    }
                }

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize(),

                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    items(

                        items =
                            tasks,

                        key = {
                            it.id
                        }

                    ) { task ->

                        val location =
                            locations.find {
                                it.id ==
                                        task.locationId
                            }


                        Card(

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(24.dp),

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
                                        task.title,

                                    color =
                                        TextWhite,

                                    fontSize =
                                        18.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )


                                if (
                                    task.description.isNotBlank()
                                ) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(6.dp)
                                    )

                                    Text(
                                        text =
                                            task.description,

                                        color =
                                            TextSecondary,

                                        fontSize =
                                            13.sp
                                    )
                                }


                                Spacer(
                                    modifier =
                                        Modifier.height(10.dp)
                                )


                                Text(
                                    text =
                                        "📍 ${
                                            location?.name
                                                ?: "No location"
                                        }",

                                    color =
                                        ElectricBlue,

                                    fontSize =
                                        14.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )


                                // ====================================================
                                // UPDATE + DELETE
                                // ====================================================

                                Row(

                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                    OutlinedButton(

                                        onClick = {

                                            editingTask =
                                                task
                                        },

                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text =
                                                "UPDATE"
                                        )
                                    }


                                    OutlinedButton(

                                        onClick = {

                                            onDeleteTask(
                                                task
                                            )
                                        },

                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text =
                                                "DELETE"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // ============================================================
    // ADD TASK DIALOG
    // ============================================================

    if (showAddDialog) {

        AddTaskDialog(

            locations =
                locations,

            onDismiss = {

                showAddDialog =
                    false
            },

            onSave = {

                    title,
                    description,
                    locationId ->

                onAddTask(

                    title,

                    description,

                    locationId
                )

                showAddDialog =
                    false
            }
        )
    }


    // ============================================================
    // UPDATE TASK DIALOG
    // ============================================================

    editingTask?.let { task ->

        UpdateTaskDialog(

            task =
                task,

            locations =
                locations,

            onDismiss = {

                editingTask =
                    null
            },

            onSave = {

                    updatedTask ->

                onUpdateTask(
                    updatedTask
                )

                editingTask =
                    null
            }
        )
    }
}


// ================================================================
// ADD TASK DIALOG
// ================================================================

@Composable
private fun AddTaskDialog(

    locations:
    List<LocationEntity>,

    onDismiss:
        () -> Unit,

    onSave:
        (String, String, Int) -> Unit

) {

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var selectedLocation by remember {
        mutableStateOf<LocationEntity?>(null)
    }

    var locationMenuExpanded by remember {
        mutableStateOf(false)
    }


    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "ADD TASK"
            )
        },

        text = {

            Column {

                TextField(

                    value =
                        title,

                    onValueChange = {
                        title = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Task title"
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                TextField(

                    value =
                        description,

                    onValueChange = {
                        description = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Description"
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Box {

                    OutlinedButton(

                        onClick = {

                            locationMenuExpanded =
                                true
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                selectedLocation?.name
                                    ?: "Select location"
                        )
                    }


                    DropdownMenu(

                        expanded =
                            locationMenuExpanded,

                        onDismissRequest = {

                            locationMenuExpanded =
                                false
                        }
                    ) {

                        locations.forEach { location ->

                            DropdownMenuItem(

                                text = {

                                    Text(
                                        location.name
                                    )
                                },

                                onClick = {

                                    selectedLocation =
                                        location

                                    locationMenuExpanded =
                                        false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            TextButton(

                onClick = {

                    selectedLocation?.let {

                            location ->

                        if (
                            title.isNotBlank()
                        ) {

                            onSave(

                                title.trim(),

                                description.trim(),

                                location.id
                            )
                        }
                    }
                },

                enabled =
                    title.isNotBlank() &&
                            selectedLocation != null
            ) {

                Text(
                    text =
                        "SAVE"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "CANCEL"
                )
            }
        }
    )
}


// ================================================================
// UPDATE TASK DIALOG
// ================================================================

@Composable
private fun UpdateTaskDialog(

    task:
    TaskEntity,

    locations:
    List<LocationEntity>,

    onDismiss:
        () -> Unit,

    onSave:
        (TaskEntity) -> Unit

) {

    var title by remember(
        task.id
    ) {

        mutableStateOf(
            task.title
        )
    }

    var description by remember(
        task.id
    ) {

        mutableStateOf(
            task.description
        )
    }

    var selectedLocation by remember(
        task.id
    ) {

        mutableStateOf(

            locations.find {

                it.id ==
                        task.locationId
            }
        )
    }

    var locationMenuExpanded by remember {
        mutableStateOf(false)
    }


    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "UPDATE TASK"
            )
        },

        text = {

            Column {

                TextField(

                    value =
                        title,

                    onValueChange = {
                        title = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Task title"
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                TextField(

                    value =
                        description,

                    onValueChange = {
                        description = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Description"
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Box {

                    OutlinedButton(

                        onClick = {

                            locationMenuExpanded =
                                true
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                selectedLocation?.name
                                    ?: "Select location"
                        )
                    }


                    DropdownMenu(

                        expanded =
                            locationMenuExpanded,

                        onDismissRequest = {

                            locationMenuExpanded =
                                false
                        }
                    ) {

                        locations.forEach { location ->

                            DropdownMenuItem(

                                text = {

                                    Text(
                                        location.name
                                    )
                                },

                                onClick = {

                                    selectedLocation =
                                        location

                                    locationMenuExpanded =
                                        false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            TextButton(

                onClick = {

                    selectedLocation?.let {

                            location ->

                        if (
                            title.isNotBlank()
                        ) {

                            onSave(

                                task.copy(

                                    title =
                                        title.trim(),

                                    description =
                                        description.trim(),

                                    locationId =
                                        location.id
                                )
                            )
                        }
                    }
                },

                enabled =
                    title.isNotBlank() &&
                            selectedLocation != null
            ) {

                Text(
                    text =
                        "UPDATE"
                )
            }
        },

        dismissButton = {

            TextButton(

                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "CANCEL"
                )
            }
        }
    )
}