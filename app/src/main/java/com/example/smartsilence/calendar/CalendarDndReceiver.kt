package com.example.smartsilence.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartsilence.DndManager

class CalendarDndReceiver : BroadcastReceiver() {

    companion object {

        private const val ACTION_CALENDAR_DND_START =
            "com.example.smartsilence.CALENDAR_DND_START"

        private const val ACTION_CALENDAR_DND_END =
            "com.example.smartsilence.CALENDAR_DND_END"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val dndManager =
            DndManager(context)

        when (intent.action) {

            ACTION_CALENDAR_DND_START -> {

                if (dndManager.hasDndAccess()) {

                    dndManager.enableDnd()
                }
            }

            ACTION_CALENDAR_DND_END -> {

                /*
                 * Calendar event has ended.
                 *
                 * For this test there are no active saved
                 * locations, so Smart Silence can safely
                 * turn DND off.
                 */
                if (dndManager.hasDndAccess()) {

                    dndManager.disableDnd()
                }
            }
        }
    }
}

