package com.aura.ai.domain.action

import com.aura.ai.data.model.AuraAction
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Parses an assistant response and extracts a structured [AuraAction].
 *
 * AURA uses a function-call convention via a JSON code block. If the response
 * contains a ```json block with an "action" key, it is parsed as an action.
 * Otherwise the full text is returned as a plain [AuraAction.Reply].
 *
 * Expected JSON shape:
 * ```json
 * {
 *   "action": "make_call" | "send_sms" | "set_reminder" | "launch_app",
 *   "contact": "Mom",            // for call / sms
 *   "number": "+15551234567",    // optional resolved number
 *   "message": "I'll be late",  // for sms
 *   "title": "Doctor appt",     // for reminder
 *   "description": "...",       // for reminder (optional)
 *   "datetime": "2024-09-15 09:00",  // for reminder (yyyy-MM-dd HH:mm)
 *   "app_name": "Spotify",      // for launch_app
 *   "package_name": "com.spotify.music", // for launch_app (optional)
 *   "reply": "Calling Mom now!" // spoken reply to user
 * }
 * ```
 */
object ActionParser {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun parse(response: String): Pair<AuraAction, String?> {
        val jsonBlock = extractJsonBlock(response) ?: return AuraAction.Reply(response) to null
        return try {
            val json = JSONObject(jsonBlock)
            val replyText = json.optString("reply", "").ifBlank { null }
            val action = when (json.optString("action")) {
                "make_call" -> AuraAction.MakeCall(
                    contact = json.getString("contact"),
                    number = json.optString("number").ifBlank { null }
                )
                "send_sms" -> AuraAction.SendSms(
                    contact = json.getString("contact"),
                    number = json.optString("number").ifBlank { null },
                    message = json.getString("message")
                )
                "set_reminder" -> {
                    val datetime = json.getString("datetime")
                    val epochMillis = runCatching { dateFormat.parse(datetime)?.time }
                        .getOrNull() ?: (System.currentTimeMillis() + 60_000)
                    AuraAction.SetReminder(
                        title = json.getString("title"),
                        description = json.optString("description"),
                        epochMillis = epochMillis
                    )
                }
                "launch_app" -> AuraAction.LaunchApp(
                    appName = json.getString("app_name"),
                    packageName = json.optString("package_name").ifBlank { null }
                )
                else -> AuraAction.Reply(replyText ?: response)
            }
            action to replyText
        } catch (e: Exception) {
            AuraAction.Reply(response) to null
        }
    }

    private fun extractJsonBlock(text: String): String? {
        val startMarker = "```json"
        val endMarker = "```"
        val start = text.indexOf(startMarker)
        if (start == -1) return null
        val contentStart = start + startMarker.length
        val end = text.indexOf(endMarker, contentStart)
        if (end == -1) return null
        return text.substring(contentStart, end).trim()
    }
}
