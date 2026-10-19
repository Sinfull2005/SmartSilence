package com.example.smartsilence.security

/**
 * Actions that can be protected by Smart Silence
 * adaptive authentication.
 *
 * The sensitivity value is only the base risk
 * contribution of the action.
 *
 * The final risk is calculated dynamically using:
 *
 * Action Sensitivity
 * + Location Risk
 * + Failed Attempt Risk
 */
enum class SensitiveAction(
    val sensitivity: Int
) {

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    LOGIN(50),

    // ---------------------------------------------------------
    // TASKS
    // ---------------------------------------------------------

    VIEW_TASKS(5),
    ADD_TASK(10),
    EDIT_TASK(15),
    DELETE_TASK(20),

    // ---------------------------------------------------------
    // LOCATIONS
    // ---------------------------------------------------------

    ADD_LOCATION(15),
    EDIT_LOCATION(25),
    EDIT_LOCATION_DND(30),
    DELETE_LOCATION(35),

    // ---------------------------------------------------------
    // AUTO REPLY
    // ---------------------------------------------------------

    EDIT_AUTO_REPLY(30),
    ENABLE_DISABLE_AUTO_REPLY(30),

    // ---------------------------------------------------------
    // PRIORITY CONTACTS
    // ---------------------------------------------------------

    ADD_PRIORITY_CONTACT(40),
    EDIT_PRIORITY_CONTACT(40),
    DELETE_PRIORITY_CONTACT(50),
    DELETE_ALL_PRIORITY_CONTACTS(60),

    // ---------------------------------------------------------
    // DND / CALL SCREENING
    // ---------------------------------------------------------

    CHANGE_DND_RULES(60),
    CHANGE_CALL_SCREENING_BEHAVIOR(70),

    // ---------------------------------------------------------
    // SECURITY
    // ---------------------------------------------------------

    CHANGE_AUTHENTICATION_METHOD(80),
    DISABLE_SMART_SILENCE(90),

    RESET_SECURITY_SETTINGS(100),
    RESET_AUTHENTICATION_CREDENTIALS(100)
}