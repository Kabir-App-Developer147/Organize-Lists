package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiService {
    private const val TAG = "AiService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Call the online Google Gemini API using direct REST.
     */
    suspend fun callGeminiApi(
        apiKey: String,
        model: String,
        prompt: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please set it in Settings/Profile or configure your Secrets panel in Google AI Studio."
        }

        val resolvedModel = if (model.isBlank()) "gemini-3.5-flash" else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$resolvedModel:generateContent?key=$apiKey"

        try {
            // Build Gemini request body
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini error response: $errBody")
                    return@withContext "Error calling Gemini (HTTP ${response.code}): ${response.message}\n$errBody"
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body"
                try {
                    val rootJson = JSONObject(responseBody)
                    val candidates = rootJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val contentObj = candidate.getJSONObject("content")
                        val parts = contentObj.getJSONArray("parts")
                        if (parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).getString("text")
                        }
                    }
                    return@withContext "Error: Could not parse response text from Gemini API."
                } catch (pe: Exception) {
                    Log.e(TAG, "Parsing error: ", pe)
                    return@withContext "Error parsing Gemini response: ${pe.message}"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error: ", e)
            return@withContext "Network error calling Gemini: ${e.message}"
        }
    }

    /**
     * Call local Ollama REST API.
     */
    suspend fun callOllamaApi(
        baseUrl: String,
        model: String,
        prompt: String
    ): String = withContext(Dispatchers.IO) {
        val resolvedUrl = if (baseUrl.isBlank()) "http://10.0.2.2:11434" else baseUrl.trim().removeSuffix("/")
        val apiEndpoint = "$resolvedUrl/api/generate"
        val resolvedModel = if (model.isBlank()) "gemma:2b" else model

        try {
            // Build Ollama request body
            val requestJson = JSONObject().apply {
                put("model", resolvedModel)
                put("prompt", prompt)
                put("stream", false)
            }

            val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(apiEndpoint)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Ollama error response: $errBody")
                    return@withContext "Error calling Ollama (HTTP ${response.code}): ${response.message}\n$errBody"
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body"
                try {
                    val rootJson = JSONObject(responseBody)
                    return@withContext rootJson.optString("response", "No response text field found in Ollama JSON.")
                } catch (pe: Exception) {
                    Log.e(TAG, "Ollama legacy parsing fallback: ", pe)
                    return@withContext "Response from Ollama: $responseBody"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ollama connectivity error: ", e)
            return@withContext "Ollama connection failed! Ensure your Ollama server is running at '$resolvedUrl' and has CORS/network access enabled.\nDetail: ${e.message}"
        }
    }

    /**
     * Process offline rule-based Generation.
     * Uses structural NLP heuristic extraction over list components and text to create cohesive,
     * beautiful personalized priority lists, productivity metrics, and indefinite goals locally without internet.
     */
    fun callLocalNativeEngine(
        selectedDocuments: List<Document>,
        presetType: String,
        customPrompt: String
    ): String {
        val filesHeader = if (selectedDocuments.isEmpty()) {
            "*No files or notes selected. Creating general analysis based on global profile settings.*"
        } else {
            "### Selected Repository Sources Analyzed:\n" + selectedDocuments.mapIndexed { idx, doc ->
                "  - **[${doc.type}] ${doc.title}** (${doc.content.length} chars)"
            }.joinToString("\n")
        }

        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        return when (presetType) {
            "PRIORITY_LIST" -> {
                buildString {
                    append("# 💡 AI Personalized Priorities & Action Plan\n")
                    append("> Generated Offline using Local Native NLP Suite\n")
                    append("> Timestamp: $timestamp\n\n")
                    append(filesHeader)
                    append("\n\n")
                    append("Based on the input sources, we extracted the following structured task priorities:\n\n")

                    // Look for key vectors or lines in selected docs
                    val keywords = mutableListOf<String>()
                    var lineCounter = 1
                    selectedDocuments.forEach { doc ->
                        doc.content.lines().forEach { line ->
                            val cleanLine = line.trim().removePrefix("-").trim()
                            if (cleanLine.isNotBlank() && cleanLine.length > 5 && keywords.size < 6) {
                                keywords.add(cleanLine)
                            }
                        }
                    }

                    if (keywords.isEmpty()) {
                        append("### 🎯 Immediate High-Impact Priorities\n")
                        append("1. **Verify workspace setup & environment variables** [Timeframe: TODAY, Priority: HIGH]\n")
                        append("   _Context: Essential for proper SDK compilation and debugging routines._\n")
                        append("2. **Refactor core planner state variables** [Timeframe: WEEK, Priority: HIGH]\n")
                        append("   _Context: Optimize state recompositions inside Jetpack Compose list views._\n")
                        append("3. **Map long-term learning goals** [Timeframe: ANYTIME, Priority: MEDIUM]\n")
                    } else {
                        append("### 🎯 Immediate High-Impact Priorities (Extracted)\n")
                        keywords.distinct().take(4).forEachIndexed { i, kw ->
                            val priority = if (i % 2 == 0) "HIGH" else "MEDIUM"
                            val frame = if (i == 0) "DAY" else if (i == 1) "WEEK" else "ANYTIME"
                            append("${i + 1}. **$kw** [Timeframe: $frame, Priority: $priority]\n")
                            append("   _Context: Auto-extracted from source files. Ready to compile into lists._\n\n")
                        }
                    }

                    append("\n### ⚡ Recommended Next Actions\n")
                    append("- [ ] Create a dedicated category folder for these new items.\n")
                    append("- [ ] Tap 'Save as Vault Note' below to keep this report forever.\n")
                    append("- [ ] Use the Task Extractor in the planner tab to instantiate these items on the board.\n")
                }
            }
            "EXECUTIVE_REPORT" -> {
                buildString {
                    append("# 📊 Executive Productivity & Progress Report\n")
                    append("> Generated Offline using Local Native NLP Suite\n")
                    append("> Timestamp: $timestamp\n\n")
                    append(filesHeader)
                    append("\n\n")
                    append("## 📈 Performance Summary & Analytics\n")
                    val totalChars = selectedDocuments.sumOf { it.content.length }
                    append("- **Total Vault Files Scanned:** ${selectedDocuments.size}\n")
                    append("- **Content Corpus Size:** $totalChars characters analyzed\n")
                    append("- **Recommended Work Pace:** Balanced to structured focus blocks\n")
                    append("- **Information Density Score:** ${if (totalChars > 500) "High (Rich documentation)" else "Compact"}\n\n")

                    append("## 🔍 Major Themes & Focus Areas Identified\n")
                    val foundFocus = selectedDocuments.any { it.content.contains("study", ignoreCase = true) || it.content.contains("learn", ignoreCase = true) }
                    val foundWork = selectedDocuments.any { it.content.contains("work", ignoreCase = true) || it.content.contains("project", ignoreCase = true) }
                    
                    if (foundFocus) {
                        append("- **Category 1: Academic & Skill Acquisition** (Study routines and literature logs)\n")
                    }
                    if (foundWork) {
                        append("- **Category 2: System Architecture & Business Logic** (Development timelines and deployment targets)\n")
                    }
                    if (!foundFocus && !foundWork) {
                        append("- **Category 1: Active Brainstorming** (General snippets, random document thoughts)\n")
                    }
                    append("- **Category 2: Personal Vision Mapping** (Long-term aspiration schedules)\n\n")

                    append("## 💡 Personalization Recommendations\n")
                    append("1. **Time-block 45 minute segments** for high density cognitive tasks mentioned in documents.\n")
                    append("2. **Group related checklist items** into dedicated folder categories under the Vault tab.\n")
                    append("3. **Commit to one micro-step daily** for your most recurring topic area.\n")
                }
            }
            "SOMEDAY_GOALS" -> {
                buildString {
                    append("# 🌌 Indefinite Goals & Aspirations Tracker (“Someday List”)\n")
                    append("> Generated Offline using Local Native NLP Suite\n")
                    append("> Timestamp: $timestamp\n\n")
                    append(filesHeader)
                    append("\n\n")
                    append("Here are proposed long-term, indefinite-timeframe goals created from your files to work towards indefinitely:\n\n")

                    var goalsCount = 0
                    selectedDocuments.forEach { doc ->
                        doc.content.lines().forEach { line ->
                            val clean = line.trim().removePrefix("-").trim()
                            if (clean.length > 10 && (clean.contains("goal", true) || clean.contains("want", true) || clean.contains("should", true) || clean.contains("learn", true) || goalsCount < 4)) {
                                if (goalsCount < 5) {
                                    append("### Focus Goal ${goalsCount + 1}: Indefinite Work\n")
                                    append("- **Objective:** $clean\n")
                                    append("- **Category/Theme:** Long-term Personal Growth\n")
                                    append("- **Timeframe:** ANYTIME (Goals list)\n")
                                    append("- **Suggested micro-habit:** Dedicate 2 hours per week with no deadlines.\n\n")
                                    goalsCount++
                                }
                            }
                        }
                    }

                    if (goalsCount == 0) {
                        append("### Focus Goal 1: Build robust localized AI tools\n")
                        append("- **Objective:** Master offline model endpoints (Ollama) and local text processing models.\n")
                        append("- **Category:** Technology Mastery\n")
                        append("- **Timeframe:** ANYTIME / Indefinite\n\n")
                        
                        append("### Focus Goal 2: Document comprehensive workflows\n")
                        append("- **Objective:** Write exhaustive study plans for your main hobby or interest area.\n")
                        append("- **Category:** Productivity Organization\n")
                        append("- **Timeframe:** ANYTIME / Indefinite\n")
                    }
                }
            }
            else -> {
                buildString {
                    append("# 🤖 Custom Query Response\n")
                    append("> Generated Offline using Local Native NLP Suite\n")
                    append("> Timestamp: $timestamp\n\n")
                    append("Custom Prompt: _\"$customPrompt\"_\n\n")
                    append(filesHeader)
                    append("\n\n")
                    append("## 📝 Local Parse Result\n")
                    append("We parsed your selected files and found ${selectedDocuments.size} matching sources. ")
                    append("Due to running in Offline Local Mode, we scanned the texts for core nouns:\n\n")
                    
                    val nouns = mutableListOf<String>()
                    selectedDocuments.forEach { doc ->
                        doc.content.split(' ', '\n').forEach { word ->
                            val w = word.trim().replace(Regex("[^a-zA-Z]"), "")
                            if (w.length > 5 && w.firstOrNull()?.isUpperCase() == true && nouns.size < 10) {
                                nouns.add(w)
                            }
                        }
                    }

                    if (nouns.distinct().isNotEmpty()) {
                        append("Identified Key Entities in scanned papers: ${nouns.distinct().joinToString(", ")}.\n\n")
                    } else {
                        append("No high-frequency entities scanned. Your document notes are fully private and safely cached offline.\n\n")
                    }
                    append("Provide an active OpenAI or Gemini API key online, or configure Ollama, for advanced language model comprehension!")
                }
            }
        }
    }
}
