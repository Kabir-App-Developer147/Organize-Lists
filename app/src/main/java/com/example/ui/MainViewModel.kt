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
                    notes = "Clear clutter to maintain peaceful focus."
                ),
                ListItem(
                    title = "Set up reading priorities for this month",
                    category = "General Tasks",
                    priority = "MEDIUM",
                    timeframe = "WEEK",
                    notes = "Take 10 minutes to plan books/articles."
                ),
                ListItem(
                    title = "Watch a thoughtful movie tonight",
                    category = if (type == "Movies") "Movies to Watch" else "General Tasks",
                    priority = "LOW",
                    timeframe = "DAY",
                    notes = "Enjoy distraction-free entertainment."
                )
            )
            for (item in starterItems) {
                repository.insertItem(item)
            }
        }
    }

    // Core list item modifications
    fun addItem(title: String, category: String, priority: String, timeframe: String, notes: String) {
        viewModelScope.launch {
            val item = ListItem(
                title = title,
                category = category,
                priority = priority,
                timeframe = timeframe,
                notes = notes
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
}
