package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ListRepository(
        database.profileDao(),
        database.listCategoryDao(),
        database.listItemDao(),
        database.documentDao()
    )

    // Profile state
    val profile: StateFlow<Profile?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Categories state
    val categories: StateFlow<List<ListCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Items state
    val allItems: StateFlow<List<ListItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Documents/Vault state
    val allDocuments: StateFlow<List<Document>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state filters
    private val _selectedCategory = MutableStateFlow<String?>("All") // "All" means all categories
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow<String>("DAY") // "DAY", "WEEK", "MONTH", "YEAR", "ANYTIME", "ALL"
    val selectedTimeframe = _selectedTimeframe.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("PRIORITY") // "PRIORITY", "DATE", "NAME", "STATUS"
    val sortBy = _sortBy.asStateFlow()

    // Filtered & Sorted items
    val filteredItems: StateFlow<List<ListItem>> = combine(
        allItems,
        _selectedCategory,
        _selectedTimeframe,
        _searchQuery,
        _sortBy
    ) { items, cat, time, query, sort ->
        var list = items

        // Category filter
        if (cat != null && cat != "All") {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }

        // Timeframe filter
        if (time != "ALL") {
            list = list.filter { it.timeframe.equals(time, ignoreCase = true) }
        }

        // Search query filter
        if (query.isNotEmpty()) {
            list = list.filter { it.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true) }
        }

        // Sort
        when (sort) {
            "PRIORITY" -> {
                list = list.sortedWith(compareByDescending<ListItem> { 
                    when (it.priority) {
                        "HIGH" -> 3
                        "MEDIUM" -> 2
                        "LOW" -> 1
                        else -> 0
                    }
                }.thenBy { it.status == "DID_IT" }) // Incomplete items on top
            }
            "DATE" -> {
                list = list.sortedByDescending { it.timestampCreated }
            }
            "NAME" -> {
                list = list.sortedBy { it.title.lowercase() }
            }
            "STATUS" -> {
                list = list.sortedBy { it.status == "DID_IT" } // "WANNA_DO" comes first
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Item for Focus Mode
    private val _activeFocusItem = MutableStateFlow<ListItem?>(null)
    val activeFocusItem = _activeFocusItem.asStateFlow()

    fun startFocusSession(item: ListItem) {
        _activeFocusItem.value = item
    }

    fun endFocusSession() {
        _activeFocusItem.value = null
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setTimeframeFilter(timeframe: String) {
        _selectedTimeframe.value = timeframe
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    // Completion / Onboarding submissions
    fun completeOnboarding(name: String, priority: String, pace: String, type: String) {
        viewModelScope.launch {
            val userProfile = Profile(
                userName = name,
                priorityTopic = priority,
                pace = pace,
                preferredType = type,
                isOnboardingCompleted = true
            )
            repository.insertProfile(userProfile)

            // Setup default categories based on user's onboarding choice!
            val defaultCategories = mutableListOf(
                ListCategory("General Tasks", isSystem = true, iconName = "task"),
                ListCategory("Daily Routine", isSystem = true, iconName = "task")
            )

            when (type) {
                "Movies" -> {
                    defaultCategories.add(ListCategory("Movies to Watch", isSystem = true, iconName = "movie"))
                    defaultCategories.add(ListCategory("TV Series list", isSystem = true, iconName = "movie"))
                }
                "Books" -> {
                    defaultCategories.add(ListCategory("Books to Read", isSystem = true, iconName = "book"))
                    defaultCategories.add(ListCategory("Articles/Papers", isSystem = true, iconName = "book"))
                }
                "Travels" -> {
                    defaultCategories.add(ListCategory("Destinations", isSystem = true, iconName = "travel"))
                    defaultCategories.add(ListCategory("Bucket List", isSystem = true, iconName = "travel"))
                }
                else -> {
                    defaultCategories.add(ListCategory("Shopping List", isSystem = true, iconName = "list"))
                    defaultCategories.add(ListCategory("My Movies", isSystem = true, iconName = "movie"))
                    defaultCategories.add(ListCategory("My Books", isSystem = true, iconName = "book"))
                }
            }

            repository.insertCategories(defaultCategories)

            // Also let's seed a couple of helper starter items to guide them!
            val starterItems = listOf(
                ListItem(
                    title = "Organize my workspace",
                    category = "General Tasks",
                    priority = "HIGH",
                    timeframe = "DAY",
                    notes = "Clear clutter to maintain peaceful focus.",
                    lifeArea = "Career"
                ),
                ListItem(
                    title = "Set up reading priorities for this month",
                    category = "General Tasks",
                    priority = "MEDIUM",
                    timeframe = "WEEK",
                    notes = "Take 10 minutes to plan books/articles.",
                    lifeArea = "Personal Growth"
                ),
                ListItem(
                    title = "Watch a thoughtful movie tonight",
                    category = if (type == "Movies") "Movies to Watch" else "General Tasks",
                    priority = "LOW",
                    timeframe = "DAY",
                    notes = "Enjoy distraction-free entertainment.",
                    lifeArea = "Leisure"
                )
            )
            for (item in starterItems) {
                repository.insertItem(item)
            }
        }
    }

    // Core list item modifications
    fun addItem(title: String, category: String, priority: String, timeframe: String, notes: String, lifeArea: String = "Personal Growth") {
        viewModelScope.launch {
            val item = ListItem(
                title = title,
                category = category,
                priority = priority,
                timeframe = timeframe,
                notes = notes,
                lifeArea = lifeArea
            )
            repository.insertItem(item)
        }
    }

    fun toggleItemStatus(item: ListItem) {
        viewModelScope.launch {
            val updated = if (item.status == "WANNA_DO") {
                item.copy(status = "DID_IT", timestampCompleted = System.currentTimeMillis())
            } else {
                item.copy(status = "WANNA_DO", timestampCompleted = null)
            }
            repository.insertItem(updated)
            // If it is the current focus item, update focus state
            if (_activeFocusItem.value?.id == item.id) {
                _activeFocusItem.value = updated
            }
        }
    }

    fun deleteItem(item: ListItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            if (_activeFocusItem.value?.id == item.id) {
                _activeFocusItem.value = null
            }
        }
    }

    fun addCustomCategory(name: String, iconName: String) {
        viewModelScope.launch {
            repository.insertCategory(ListCategory(name = name, iconName = iconName))
        }
    }

    fun deleteCategory(category: ListCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            if (_selectedCategory.value == category.name) {
                _selectedCategory.value = "All"
            }
        }
    }

    // Document/Snippet/File actions
    fun addDocument(
        title: String,
        content: String,
        type: String,
        filePath: String? = null,
        fileSize: String? = null,
        fileMimeType: String? = null,
        autoParse: Boolean = false
    ) {
        viewModelScope.launch {
            val doc = Document(
                title = title,
                content = content,
                type = type,
                filePath = filePath,
                fileSize = fileSize,
                fileMimeType = fileMimeType
            )
            repository.insertDocument(doc)
            if (autoParse && content.isNotBlank()) {
                parseTextToTasks(title, content)
            }
        }
    }

    fun updateDocument(doc: Document) {
        viewModelScope.launch {
            repository.updateDocument(doc)
        }
    }

    fun deleteDocument(doc: Document) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
        }
    }

    fun parseTextToTasks(title: String, text: String) {
        viewModelScope.launch {
            val lines = text.split("\n")
            for (line in lines) {
                var cleanLine = line.trim()
                if (cleanLine.isEmpty()) continue

                // Check and remove common bullets / checkboxes
                if (cleanLine.startsWith("-") || cleanLine.startsWith("*") || cleanLine.startsWith("+")) {
                    cleanLine = cleanLine.substring(1).trim()
                }
                while (cleanLine.startsWith("[") && cleanLine.contains("]")) {
                    val closeIdx = cleanLine.indexOf("]")
                    if (closeIdx != -1 && closeIdx < cleanLine.length) {
                        cleanLine = cleanLine.substring(closeIdx + 1).trim()
                    } else {
                        break
                    }
                }
                if (cleanLine.lowercase().startsWith("todo:")) {
                    cleanLine = cleanLine.substring(5).trim()
                }

                if (cleanLine.isEmpty()) continue

                val lower = cleanLine.lowercase()

                // 1. Timeframe mapping
                val timeframe = when {
                    lower.contains("goal") || lower.contains("indefinitely") || lower.contains("indefinite") || lower.contains("someday") || lower.contains("anytime") || lower.contains("always") -> "ANYTIME"
                    lower.contains("today") || lower.contains("tonight") || lower.contains("daily") -> "DAY"
                    lower.contains("week") || lower.contains("weekend") -> "WEEK"
                    lower.contains("month") -> "MONTH"
                    lower.contains("year") -> "YEAR"
                    else -> "DAY" // Default to Day for immediate focus
                }

                // 2. Priority mapping
                val priority = when {
                    lower.contains("high") || lower.contains("urgent") || lower.contains("priority") || lower.contains("must") || lower.contains("important") -> "HIGH"
                    lower.contains("low") || lower.contains("whenever") || lower.contains("maybe") || lower.contains("someday") -> "LOW"
                    else -> "MEDIUM"
                }

                // 3. Category grouping
                val category = when {
                    lower.contains("movie") || lower.contains("watch") || lower.contains("film") || lower.contains("netflix") || lower.contains("series") || lower.contains("episode") -> {
                        val hasWatchGroup = categories.value.any { it.name.contains("Movie") || it.name.contains("Watch") }
                        if (hasWatchGroup) {
                            categories.value.first { it.name.contains("Movie") || it.name.contains("Watch") }.name
                        } else {
                            "General Tasks"
                        }
                    }
                    lower.contains("book") || lower.contains("read") || lower.contains("article") || lower.contains("novel") -> {
                        val hasReadGroup = categories.value.any { it.name.contains("Book") || it.name.contains("Read") }
                        if (hasReadGroup) {
                            categories.value.first { it.name.contains("Book") || it.name.contains("Read") }.name
                        } else {
                            "General Tasks"
                        }
                    }
                    else -> "General Tasks"
                }

                repository.insertItem(
                    ListItem(
                        title = cleanLine,
                        category = category,
                        priority = priority,
                        timeframe = timeframe,
                        notes = "Auto-compiled from: \"$title\""
                    )
                )
            }
        }
    }

    // AI Copilot Actions
    private val _aiResultText = MutableStateFlow<String>("")
    val aiResultText = _aiResultText.asStateFlow()

    private val _isAiGenerating = MutableStateFlow<Boolean>(false)
    val isAiGenerating = _isAiGenerating.asStateFlow()

    fun generateAiReport(
        apiKey: String,
        modelSource: String, // "ONLINE_GEMINI", "OLLAMA", "OFFLINE_LOCAL"
        modelName: String,
        ollamaUrl: String,
        selectedDocs: List<Document>,
        presetType: String, // "PRIORITY_LIST", "EXECUTIVE_REPORT", "SOMEDAY_GOALS", "CUSTOM"
        customPrompt: String
    ) {
        viewModelScope.launch {
            _isAiGenerating.value = true
            _aiResultText.value = "AI models compiling personalized report...\nAnalyzing ${selectedDocs.size} context source files."

            val prompt = buildAiPrompt(selectedDocs, presetType, customPrompt)

            val result = when (modelSource) {
                "OFFLINE_LOCAL" -> {
                    AiService.callLocalNativeEngine(selectedDocs, presetType, customPrompt)
                }
                "OLLAMA" -> {
                    AiService.callOllamaApi(ollamaUrl, modelName, prompt)
                }
                else -> { // ONLINE_GEMINI
                    val finalKey = if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                        apiKey
                    } else {
                        com.example.BuildConfig.GEMINI_API_KEY
                    }
                    AiService.callGeminiApi(finalKey, modelName, prompt)
                }
            }

            _aiResultText.value = result
            _isAiGenerating.value = false
        }
    }

    private fun buildAiPrompt(selectedDocs: List<Document>, presetType: String, customPrompt: String): String {
        val contextBuilder = StringBuilder()
        if (selectedDocs.isEmpty()) {
            contextBuilder.append("No context files selected.\n")
        } else {
            contextBuilder.append("Analyze the following sources to generate results:\n")
            selectedDocs.forEachIndexed { idx, doc ->
                contextBuilder.append("=== SOURCE FILE #${idx + 1} [Type: ${doc.type}]: ${doc.title} ===\n")
                contextBuilder.append("${doc.content}\n")
                contextBuilder.append("=========================================\n")
            }
        }

        return when (presetType) {
            "PRIORITY_LIST" -> {
                "You are an offline/online productivity personalized intelligence agent. " +
                "Deconstruct the user's provided document inputs and draft a beautifully organized priority checklist. " +
                "Group the tasks into: Today, Week, Month, Year, and Anytime Goals. " +
                "Give bullet details for each action. Do not reference raw formatting parameters.\n\n" +
                "Context Sources:\n$contextBuilder\n\n" +
                "Format: Markdown checklists."
            }
            "EXECUTIVE_REPORT" -> {
                "You are a stellar productivity analyst. " +
                "Compile an executive-level summary and progress evaluation of the selected documentation. " +
                "Spot bottlenecks, map workflows, and outline actionable personal strategies.\n\n" +
                "Context Sources:\n$contextBuilder\n\n" +
                "Format: Markdown report with header tags."
            }
            "SOMEDAY_GOALS" -> {
                "You are a long-term goal planning assistant. " +
                "Filter current documents for aspirational, undefined, or 'Someday' long-term goals that can be worked on indefinitely. " +
                "Format them cleanly with associated micro-habits the user can build without hard timelines.\n\n" +
                "Context Sources:\n$contextBuilder\n\n" +
                "Format: Markdown list."
            }
            else -> {
                "Write a comprehensive markdown answer addressing: \"$customPrompt\"\n\n" +
                "Using details from context files:\n$contextBuilder"
            }
        }
    }

    fun saveResultAsDocument(title: String, content: String) {
        viewModelScope.launch {
            if (content.isNotBlank()) {
                val docTitle = if (title.isBlank()) "AI Report" else title
                addDocument(
                    title = docTitle,
                    content = content,
                    type = "DOCUMENT",
                    autoParse = false
                )
            }
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            // Delete all list items, categories, documents, and profile to start fresh
            val userProfile = Profile(isOnboardingCompleted = false)
            repository.insertProfile(userProfile)
            
            val itemsList = allItems.value
            for (i in itemsList) {
                repository.deleteItem(i)
            }
            
            val catsList = categories.value
            for (c in catsList) {
                repository.deleteCategory(c)
            }

            val docsList = allDocuments.value
            for (d in docsList) {
                repository.deleteDocument(d)
            }
        }
    }

    // --- JSON Backup Export/Import Feature ---
    fun exportBackupToJson(): String {
        val backupObj = org.json.JSONObject()

        try {
            // 1. Profile
            val profileVal = profile.value
            if (profileVal != null) {
                val pObj = org.json.JSONObject()
                pObj.put("id", profileVal.id)
                pObj.put("userName", profileVal.userName)
                pObj.put("priorityTopic", profileVal.priorityTopic)
                pObj.put("pace", profileVal.pace)
                pObj.put("preferredType", profileVal.preferredType)
                pObj.put("isOnboardingCompleted", profileVal.isOnboardingCompleted)
                backupObj.put("profile", pObj)
            }

            // 2. Categories
            val catsArr = org.json.JSONArray()
            for (cat in categories.value) {
                val cObj = org.json.JSONObject()
                cObj.put("name", cat.name)
                cObj.put("isSystem", cat.isSystem)
                cObj.put("iconName", cat.iconName)
                catsArr.put(cObj)
            }
            backupObj.put("categories", catsArr)

            // 3. ListItems
            val itemsArr = org.json.JSONArray()
            for (item in allItems.value) {
                val iObj = org.json.JSONObject()
                iObj.put("id", item.id)
                iObj.put("title", item.title)
                iObj.put("category", item.category)
                iObj.put("status", item.status)
                iObj.put("priority", item.priority)
                iObj.put("timeframe", item.timeframe)
                iObj.put("notes", item.notes)
                iObj.put("timestampCreated", item.timestampCreated)
                if (item.timestampCompleted != null) {
                    iObj.put("timestampCompleted", item.timestampCompleted)
                } else {
                    iObj.put("timestampCompleted", org.json.JSONObject.NULL)
                }
                iObj.put("lifeArea", item.lifeArea)
                itemsArr.put(iObj)
            }
            backupObj.put("items", itemsArr)

            // 4. Documents
            val docsArr = org.json.JSONArray()
            for (doc in allDocuments.value) {
                val dObj = org.json.JSONObject()
                dObj.put("id", doc.id)
                dObj.put("title", doc.title)
                dObj.put("content", doc.content)
                dObj.put("type", doc.type)
                dObj.put("timestampCreated", doc.timestampCreated)
                dObj.put("filePath", doc.filePath ?: org.json.JSONObject.NULL)
                dObj.put("fileSize", doc.fileSize ?: org.json.JSONObject.NULL)
                dObj.put("fileMimeType", doc.fileMimeType ?: org.json.JSONObject.NULL)
                docsArr.put(dObj)
            }
            backupObj.put("documents", docsArr)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return backupObj.toString(2)
    }

    fun importBackupFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val backupObj = org.json.JSONObject(jsonString)

                // 1. Profile
                if (backupObj.has("profile")) {
                    val pObj = backupObj.getJSONObject("profile")
                    val importedProfile = Profile(
                        id = pObj.optInt("id", 0),
                        userName = pObj.optString("userName", ""),
                        priorityTopic = pObj.optString("priorityTopic", "Focus"),
                        pace = pObj.optString("pace", "Balanced"),
                        preferredType = pObj.optString("preferredType", "Tasks"),
                        isOnboardingCompleted = pObj.optBoolean("isOnboardingCompleted", false)
                    )
                    repository.insertProfile(importedProfile)
                }

                // 2. Categories
                if (backupObj.has("categories")) {
                    val catsArr = backupObj.getJSONArray("categories")
                    val importedCats = mutableListOf<ListCategory>()
                    for (i in 0 until catsArr.length()) {
                        val cObj = catsArr.getJSONObject(i)
                        importedCats.add(
                            ListCategory(
                                name = cObj.getString("name"),
                                isSystem = cObj.optBoolean("isSystem", false),
                                iconName = cObj.optString("iconName", "list")
                            )
                        )
                    }
                    if (importedCats.isNotEmpty()) {
                        repository.insertCategories(importedCats)
                    }
                }

                // 3. ListItems
                if (backupObj.has("items")) {
                    val itemsArr = backupObj.getJSONArray("items")
                    val importedItems = mutableListOf<ListItem>()
                    for (i in 0 until itemsArr.length()) {
                        val iObj = itemsArr.getJSONObject(i)
                        val completedTime = if (iObj.isNull("timestampCompleted")) null else iObj.getLong("timestampCompleted")
                        importedItems.add(
                            ListItem(
                                id = iObj.optInt("id", 0),
                                title = iObj.getString("title"),
                                category = iObj.getString("category"),
                                status = iObj.optString("status", "WANNA_DO"),
                                priority = iObj.optString("priority", "MEDIUM"),
                                timeframe = iObj.optString("timeframe", "DAY"),
                                notes = iObj.optString("notes", ""),
                                timestampCreated = iObj.optLong("timestampCreated", System.currentTimeMillis()),
                                timestampCompleted = completedTime,
                                lifeArea = iObj.optString("lifeArea", "Personal Growth")
                            )
                        )
                    }
                    if (importedItems.isNotEmpty()) {
                        repository.insertItems(importedItems)
                    }
                }

                // 4. Documents
                if (backupObj.has("documents")) {
                    val docsArr = backupObj.getJSONArray("documents")
                    val importedDocs = mutableListOf<Document>()
                    for (i in 0 until docsArr.length()) {
                        val dObj = docsArr.getJSONObject(i)
                        importedDocs.add(
                            Document(
                                id = dObj.optInt("id", 0),
                                title = dObj.getString("title"),
                                content = dObj.getString("content"),
                                type = dObj.optString("type", "DOCUMENT"),
                                timestampCreated = dObj.optLong("timestampCreated", System.currentTimeMillis()),
                                filePath = if (dObj.isNull("filePath")) null else dObj.getString("filePath"),
                                fileSize = if (dObj.isNull("fileSize")) null else dObj.getString("fileSize"),
                                fileMimeType = if (dObj.isNull("fileMimeType")) null else dObj.getString("fileMimeType")
                            )
                        )
                    }
                    if (importedDocs.isNotEmpty()) {
                        repository.insertDocuments(importedDocs)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun exportToUri(context: android.content.Context, uri: android.net.Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonStr = exportBackupToJson()
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(jsonStr.toByteArray(Charsets.UTF_8))
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Unknown error exporting backup")
            }
        }
    }

    fun importFromUri(context: android.content.Context, uri: android.net.Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val stringBuilder = StringBuilder()
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream, Charsets.UTF_8))
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append('\n')
                        line = reader.readLine()
                    }
                }
                importBackupFromJson(stringBuilder.toString())
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Unknown error importing backup")
            }
        }
    }

    // --- AI/LLM Category Suggestion feature ---
    fun suggestCategory(
        title: String,
        notes: String,
        modelSource: String = "OFFLINE_LOCAL", // Default to local engine / heuristics
        apiKey: String = "",
        modelName: String = "gemini-3.5-flash",
        ollamaUrl: String = "",
        onCompleted: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (title.isBlank() && notes.isBlank()) {
                onCompleted("General")
                return@launch
            }

            // Immediately calculate heuristic option
            val localResult = com.example.data.AiService.suggestAreaOfLife(title, notes)

            if (modelSource == "OFFLINE_LOCAL") {
                onCompleted(localResult)
                return@launch
            }

            val prompt = """
                Based on the following item or note, classify it into exactly one of these areas of life categories:
                "Health", "Career", "Personal Growth", "Leisure", "Finance", "General".
                
                Title: $title
                Notes: $notes
                
                Respond with only the single category name select from: "Health", "Career", "Personal Growth", "Leisure", "Finance", "General". Do not write anything else. No explanation, no punctuation, no formatting.
            """.trimIndent()

            val result = try {
                when (modelSource) {
                    "OLLAMA" -> {
                        val response = com.example.data.AiService.callOllamaApi(ollamaUrl, modelName, prompt)
                        val cleaned = response.trim().replace(Regex("[^a-zA-Z ]"), "")
                        parseCategoryResponse(cleaned, localResult)
                    }
                    else -> { // ONLINE_GEMINI
                        val finalKey = if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                            apiKey
                        } else {
                            com.example.BuildConfig.GEMINI_API_KEY
                        }
                        if (finalKey.isBlank() || finalKey == "MY_GEMINI_API_KEY") {
                            localResult
                        } else {
                            val response = com.example.data.AiService.callGeminiApi(finalKey, modelName, prompt)
                            val cleaned = response.trim().replace(Regex("[^a-zA-Z ]"), "")
                            parseCategoryResponse(cleaned, localResult)
                        }
                    }
                }
            } catch (e: Exception) {
                localResult
            }

            onCompleted(result)
        }
    }

    private fun parseCategoryResponse(response: String, fallback: String): String {
        val matches = listOf("Health", "Career", "Personal Growth", "Leisure", "Finance", "General")
        for (m in matches) {
            if (response.contains(m, ignoreCase = true)) {
                return m
            }
        }
        if (response.contains("Personal", ignoreCase = true) || response.contains("Growth", ignoreCase = true)) {
            return "Personal Growth"
        }
        return fallback
    }
}
