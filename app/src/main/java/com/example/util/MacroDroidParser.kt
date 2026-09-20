package com.example.util

import com.example.data.models.TriggerType
import org.json.JSONArray
import org.json.JSONObject

data class ParsedMacroResult(
    val name: String? = null,
    val description: String? = null,
    val triggerType: TriggerType? = null,
    val notes: String? = null,
    val exportData: String,
    val formatDescription: String
)

object MacroDroidParser {

    fun parse(rawInput: String): ParsedMacroResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ParsedMacroResult(
                exportData = "",
                formatDescription = "Empty Input"
            )
        }

        // Try JSON parsing
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            val jsonResult = parseJson(trimmed)
            if (jsonResult != null) return jsonResult
        }

        // Try XML parsing
        if (trimmed.startsWith("<") && (trimmed.contains("<macro") || trimmed.contains("<MacroDroid") || trimmed.contains("<Macro"))) {
            val xmlResult = parseXml(trimmed)
            if (xmlResult != null) return xmlResult
        }

        // Try Key-Value Plain Text parsing (MacroDroid Share sheet format)
        if (trimmed.contains("Macro:") || trimmed.contains("Trigger:") || trimmed.contains("Action:") || trimmed.contains("Constraints:")) {
            val textResult = parsePlainText(trimmed)
            if (textResult != null) return textResult
        }

        // Fallback: Raw text storage
        val firstLine = trimmed.lines().firstOrNull()?.take(50)?.replace(Regex("[#*`_]"), "")?.trim()
        return ParsedMacroResult(
            name = if (!firstLine.isNullOrBlank() && firstLine.length <= 40) firstLine else null,
            description = null,
            triggerType = detectTriggerTypeFromText(trimmed),
            notes = null,
            exportData = trimmed,
            formatDescription = "Raw Text Snippet (Unparsed)"
        )
    }

    private fun parseJson(jsonStr: String): ParsedMacroResult? {
        return try {
            val obj = if (jsonStr.startsWith("[")) {
                val array = JSONArray(jsonStr)
                if (array.length() > 0) array.getJSONObject(0) else return null
            } else {
                JSONObject(jsonStr)
            }

            // MacroDroid uses keys like m_name, name, macroName, etc.
            val name = obj.optString("m_name", "")
                .ifEmpty { obj.optString("name", "") }
                .ifEmpty { obj.optString("macroName", "") }
                .ifEmpty { null }

            val description = obj.optString("m_description", "")
                .ifEmpty { obj.optString("description", "") }
                .ifEmpty { null }

            // Extract triggers
            var detectedTrigger: TriggerType? = null
            val triggersArray = obj.optJSONArray("m_triggerList")
                ?: obj.optJSONArray("triggers")
                ?: obj.optJSONArray("triggerList")

            val triggerDescriptions = mutableListOf<String>()
            if (triggersArray != null) {
                for (i in 0 until triggersArray.length()) {
                    val tObj = triggersArray.optJSONObject(i)
                    if (tObj != null) {
                        val tName = tObj.optString("m_name", tObj.optString("name", tObj.optString("type", "")))
                        if (tName.isNotEmpty()) {
                            triggerDescriptions.add(tName)
                            if (detectedTrigger == null) {
                                detectedTrigger = detectTriggerTypeFromText(tName + " " + tObj.toString())
                            }
                        }
                    } else {
                        val tStr = triggersArray.optString(i)
                        if (tStr.isNotEmpty()) {
                            triggerDescriptions.add(tStr)
                            if (detectedTrigger == null) {
                                detectedTrigger = detectTriggerTypeFromText(tStr)
                            }
                        }
                    }
                }
            }

            if (detectedTrigger == null) {
                detectedTrigger = detectTriggerTypeFromText(jsonStr)
            }

            val notesBuilder = StringBuilder()
            if (triggerDescriptions.isNotEmpty()) {
                notesBuilder.append("Triggers:\n").append(triggerDescriptions.joinToString("\n") { "• $it" })
            }

            val actionsArray = obj.optJSONArray("m_actionList") ?: obj.optJSONArray("actions")
            if (actionsArray != null && actionsArray.length() > 0) {
                if (notesBuilder.isNotEmpty()) notesBuilder.append("\n\n")
                notesBuilder.append("Actions:\n")
                for (i in 0 until actionsArray.length()) {
                    val aObj = actionsArray.optJSONObject(i)
                    val aName = aObj?.optString("m_name", aObj.optString("name", aObj.optString("type", ""))) ?: actionsArray.optString(i)
                    if (aName.isNotEmpty()) {
                        notesBuilder.append("• $aName\n")
                    }
                }
            }

            ParsedMacroResult(
                name = name,
                description = description,
                triggerType = detectedTrigger ?: TriggerType.EVENT,
                notes = notesBuilder.toString().trim().ifEmpty { null },
                exportData = jsonStr,
                formatDescription = "MacroDroid JSON (${triggersArray?.length() ?: 0} Triggers, ${actionsArray?.length() ?: 0} Actions)"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseXml(xmlStr: String): ParsedMacroResult? {
        return try {
            val nameRegex = Regex("<macro[^>]+name=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
            val name = nameRegex.find(xmlStr)?.groupValues?.get(1)
                ?: Regex("<name>([^<]+)</name>", RegexOption.IGNORE_CASE).find(xmlStr)?.groupValues?.get(1)

            val descRegex = Regex("<description>([^<]+)</description>", RegexOption.IGNORE_CASE)
            val description = descRegex.find(xmlStr)?.groupValues?.get(1)

            val triggerType = detectTriggerTypeFromText(xmlStr)

            val triggers = Regex("<trigger[^>]+name=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).findAll(xmlStr)
                .map { it.groupValues[1] }.toList()
            val actions = Regex("<action[^>]+name=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).findAll(xmlStr)
                .map { it.groupValues[1] }.toList()

            val notesBuilder = StringBuilder()
            if (triggers.isNotEmpty()) {
                notesBuilder.append("Triggers:\n").append(triggers.joinToString("\n") { "• $it" })
            }
            if (actions.isNotEmpty()) {
                if (notesBuilder.isNotEmpty()) notesBuilder.append("\n\n")
                notesBuilder.append("Actions:\n").append(actions.joinToString("\n") { "• $it" })
            }

            ParsedMacroResult(
                name = name,
                description = description,
                triggerType = triggerType,
                notes = notesBuilder.toString().trim().ifEmpty { null },
                exportData = xmlStr,
                formatDescription = "MacroDroid XML Export"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePlainText(text: String): ParsedMacroResult? {
        return try {
            var name: String? = null
            var description: String? = null
            val triggers = mutableListOf<String>()
            val actions = mutableListOf<String>()
            var currentSection: String? = null

            for (line in text.lines()) {
                val trimmedLine = line.trim()
                if (trimmedLine.startsWith("Macro:", ignoreCase = true)) {
                    name = trimmedLine.substringAfter(":").trim()
                } else if (trimmedLine.startsWith("Description:", ignoreCase = true)) {
                    description = trimmedLine.substringAfter(":").trim()
                } else if (trimmedLine.startsWith("Trigger:", ignoreCase = true) || trimmedLine.startsWith("Triggers:", ignoreCase = true)) {
                    currentSection = "trigger"
                    val inline = trimmedLine.substringAfter(":").trim()
                    if (inline.isNotEmpty()) triggers.add(inline)
                } else if (trimmedLine.startsWith("Action:", ignoreCase = true) || trimmedLine.startsWith("Actions:", ignoreCase = true)) {
                    currentSection = "action"
                    val inline = trimmedLine.substringAfter(":").trim()
                    if (inline.isNotEmpty()) actions.add(inline)
                } else if (trimmedLine.startsWith("Constraints:", ignoreCase = true)) {
                    currentSection = "constraint"
                } else if (trimmedLine.isNotEmpty()) {
                    if (currentSection == "trigger") triggers.add(trimmedLine.removePrefix("-").removePrefix("•").trim())
                    else if (currentSection == "action") actions.add(trimmedLine.removePrefix("-").removePrefix("•").trim())
                }
            }

            val triggerType = detectTriggerTypeFromText(triggers.joinToString(" ") + " " + text)

            val notesBuilder = StringBuilder()
            if (triggers.isNotEmpty()) {
                notesBuilder.append("Triggers:\n").append(triggers.joinToString("\n") { "• $it" })
            }
            if (actions.isNotEmpty()) {
                if (notesBuilder.isNotEmpty()) notesBuilder.append("\n\n")
                notesBuilder.append("Actions:\n").append(actions.joinToString("\n") { "• $it" })
            }

            ParsedMacroResult(
                name = name,
                description = description,
                triggerType = triggerType,
                notes = notesBuilder.toString().trim().ifEmpty { null },
                exportData = text,
                formatDescription = "MacroDroid Text Summary"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun detectTriggerTypeFromText(text: String): TriggerType {
        val lower = text.lowercase()
        return when {
            lower.contains("webhook") || lower.contains("http trigger") || lower.contains("incoming url") -> TriggerType.WEBHOOK
            lower.contains("time") || lower.contains("alarm") || lower.contains("cron") || lower.contains("schedule") || lower.contains("interval") || lower.contains("day of week") -> TriggerType.TIME_ALARM
            lower.contains("location") || lower.contains("geofence") || lower.contains("gps") || lower.contains("cell tower") -> TriggerType.LOCATION
            lower.contains("notification") || lower.contains("not_title") || lower.contains("status bar") -> TriggerType.NOTIFICATION
            lower.contains("button") || lower.contains("widget") || lower.contains("shortcut") || lower.contains("quick setting") || lower.contains("manual") || lower.contains("shake") -> TriggerType.MANUAL
            lower.contains("bluetooth") || lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("hotspot") || lower.contains("nfc") || lower.contains("airplane mode") -> TriggerType.CONNECTIVITY
            lower.contains("call") || lower.contains("sms") || lower.contains("mms") || lower.contains("dial") || lower.contains("contact") -> TriggerType.CALL_SMS
            lower.contains("battery") || lower.contains("power") || lower.contains("charger") || lower.contains("usb") || lower.contains("dock") -> TriggerType.BATTERY_POWER
            lower.contains("screen") || lower.contains("app launch") || lower.contains("volume") || lower.contains("sensor") || lower.contains("orientation") || lower.contains("event") -> TriggerType.EVENT
            else -> TriggerType.EVENT
        }
    }
}
