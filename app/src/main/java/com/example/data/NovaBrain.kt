package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NovaBrainResult(
    val reply: String,
    val intent: String,
    val suggestions: List<String>,
    val actionUrl: String? = null,
    val actionLabel: String? = null,
    val appQuery: String? = null
)

object NovaBrain {
    private const val MAX_LENGTH = 500

    fun processQuery(transcript: String, userName: String = "Operator"): NovaBrainResult {
        val clean = transcript.trim().replace("\\s+".toRegex(), " ").take(MAX_LENGTH)
        val lower = clean.lowercase(Locale.ROOT)
        val nameSuffix = if (userName.isNotBlank()) ", $userName" else ""

        if (clean.isBlank()) {
            return NovaBrainResult(
                reply = "I didn't catch that$nameSuffix. Please tap the microphone and speak or type your question.",
                intent = "general",
                suggestions = listOf("Open Camera", "What time is it?", "Open YouTube")
            )
        }

        // 0. Voice Sign-Out Command
        if (hasAny(lower, listOf("sign out", "log out", "logout", "sign me out", "goodbye nova"))) {
            return NovaBrainResult(
                reply = "Understood$nameSuffix. Signing you out securely now.",
                intent = "sign_out",
                suggestions = listOf("Sign in with Google again")
            )
        }

        // 1. Battery Query
        if (hasAny(lower, listOf("battery", "battery percentage", "battery level", "how much battery", "power level"))) {
            return NovaBrainResult(
                reply = "BATTERY_CHECK",
                intent = "battery",
                suggestions = listOf("Open Settings", "What time is it?", "Open Camera")
            )
        }

        // 2. Time & Date Queries
        if (hasAny(lower, listOf("what time is it", "current time", "what's the time", "tell me the time", "what time", "the time"))) {
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return NovaBrainResult(
                reply = "It is currently $timeStr.",
                intent = "time",
                suggestions = listOf("What is today's date?", "Open YouTube", "Open Camera")
            )
        }

        if (hasAny(lower, listOf("date", "today's date", "what day is today", "what is the date", "what is today"))) {
            val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return NovaBrainResult(
                reply = "Today is $dateStr.",
                intent = "time",
                suggestions = listOf("What time is it?", "Open Google", "Open Camera")
            )
        }

        // 3. Web Search Queries
        if (lower.startsWith("search google for ") || lower.startsWith("google ") || lower.startsWith("search for ")) {
            val query = lower
                .replaceFirst("^search google for ".toRegex(), "")
                .replaceFirst("^google ".toRegex(), "")
                .replaceFirst("^search for ".toRegex(), "")
                .trim()
            if (query.isNotEmpty()) {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                return NovaBrainResult(
                    reply = "Searching Google for '$query'.",
                    intent = "open_url",
                    suggestions = listOf("Open YouTube", "What time is it?", "Open Camera"),
                    actionUrl = "https://www.google.com/search?q=$encoded",
                    actionLabel = "Search: $query"
                )
            }
        }

        if (lower.startsWith("search youtube for ") || lower.startsWith("youtube ")) {
            val query = lower
                .replaceFirst("^search youtube for ".toRegex(), "")
                .replaceFirst("^youtube ".toRegex(), "")
                .trim()
            if (query.isNotEmpty()) {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                return NovaBrainResult(
                    reply = "Searching YouTube for '$query'.",
                    intent = "open_url",
                    suggestions = listOf("Open YouTube", "What time is it?", "Open Camera"),
                    actionUrl = "https://www.youtube.com/results?search_query=$encoded",
                    actionLabel = "YouTube: $query"
                )
            }
        }

        if (lower.startsWith("wikipedia ") || lower.startsWith("search wikipedia for ")) {
            val query = lower
                .replaceFirst("^search wikipedia for ".toRegex(), "")
                .replaceFirst("^wikipedia ".toRegex(), "")
                .trim()
            if (query.isNotEmpty()) {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                return NovaBrainResult(
                    reply = "Looking up '$query' on Wikipedia.",
                    intent = "open_url",
                    suggestions = listOf("Open Google", "What time is it?", "Open Camera"),
                    actionUrl = "https://en.wikipedia.org/wiki/Special:Search?search=$encoded",
                    actionLabel = "Wiki: $query"
                )
            }
        }

        // 4. Note taking task
        if (lower.startsWith("take a note ") || lower.startsWith("note that ") || lower.startsWith("write note ")) {
            val noteContent = clean
                .replaceFirst("(?i)^take a note ".toRegex(), "")
                .replaceFirst("(?i)^note that ".toRegex(), "")
                .replaceFirst("(?i)^write note ".toRegex(), "")
                .trim()
            return NovaBrainResult(
                reply = "Note saved: \"$noteContent\".",
                intent = "note",
                suggestions = listOf("Open Calendar", "What time is it?", "Open Notes")
            )
        }

        // 5. Open ANY App by Name (e.g. "open camera", "open spotify", "open whatsapp", "open chrome", "open settings", etc.)
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val appQuery = lower
                .replaceFirst("^open ".toRegex(), "")
                .replaceFirst("^launch ".toRegex(), "")
                .replaceFirst("^start ".toRegex(), "")
                .trim()

            if (appQuery.isNotEmpty()) {
                val title = appQuery.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                return NovaBrainResult(
                    reply = "Opening $title for you now.",
                    intent = "open_app",
                    appQuery = appQuery,
                    actionLabel = "Open $title",
                    actionUrl = "app:$appQuery",
                    suggestions = listOf("Open Camera", "Open Settings", "What time is it?")
                )
            }
        }

        // 6. Security Questions
        if (hasAny(lower, listOf("security", "secure", "privacy", "safe mode", "protected", "encryption"))) {
            return NovaBrainResult(
                reply = "Nova runs with encrypted communication, on-device audio transcription, strict least-privilege permissions, and authenticated sessions.",
                intent = "security",
                suggestions = listOf("What can you do?", "What time is it?", "Open Camera")
            )
        }

        // 7. Assistant Identity & Capabilities
        if (hasAny(lower, listOf("who are you", "what are you", "tell me about nova", "introduce yourself", "about you"))) {
            return NovaBrainResult(
                reply = "I am Nova, your high-performance AI voice agent. I can launch any app on your device, check the battery, search the web, take notes, tell time, and execute tasks on voice command.",
                intent = "general",
                suggestions = listOf("Open Camera", "What's my battery level?", "Open YouTube")
            )
        }

        if (hasAny(lower, listOf("help", "commands", "what can you do", "capabilities", "features"))) {
            return NovaBrainResult(
                reply = "You can speak any app name to open it (e.g., 'Open Camera', 'Open Chrome', 'Open Settings'), check battery, search Google, take notes, check time, or enter Live Mode. Tap the orb anytime.",
                intent = "general",
                suggestions = listOf("Open Camera", "What's my battery level?", "Open Settings")
            )
        }

        if (hasAny(lower, listOf("joke", "tell me a joke", "make me laugh"))) {
            val jokes = listOf(
                "Why do programmers prefer dark mode? Because light attracts bugs!",
                "There are 10 types of people in the world: those who understand binary, and those who don't.",
                "Why did the smartphone get glasses? Because it lost all its contacts!",
                "An algorithm walks into a bar and says: 'I'll have what everyone else is having, but 20% faster!'"
            )
            return NovaBrainResult(
                reply = jokes.random(),
                intent = "general",
                suggestions = listOf("Tell me another joke", "What time is it?", "Open YouTube")
            )
        }

        // 8. Single-word app shortcuts (e.g. user just says "camera", "spotify", "settings", "calculator")
        val directApps = listOf("camera", "settings", "spotify", "calculator", "clock", "calendar", "maps", "chrome", "whatsapp", "youtube", "netflix")
        if (directApps.contains(lower)) {
            val title = lower.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return NovaBrainResult(
                reply = "Launching $title.",
                intent = "open_app",
                appQuery = lower,
                actionLabel = "Open $title",
                actionUrl = "app:$lower",
                suggestions = listOf("Open Camera", "What time is it?", "Open Settings")
            )
        }

        // 9. Intelligent Conversational Fallback
        return NovaBrainResult(
            reply = "I heard: '$clean'. I can launch any app on your device, check battery, search Google, or answer your commands.",
            intent = "general",
            suggestions = listOf("Open Camera", "Check battery", "Tell me a joke")
        )
    }

    private fun hasAny(text: String, phrases: List<String>): Boolean {
        return phrases.any { text.contains(it) }
    }
}
