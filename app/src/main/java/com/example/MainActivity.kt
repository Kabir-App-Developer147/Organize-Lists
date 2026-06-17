package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Document
import com.example.data.ListItem
import com.example.data.Profile
import com.example.ui.FocusScreen
import com.example.ui.MainViewModel
import com.example.ui.OnboardingScreen
import com.example.ui.AiCopilotTabContent
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MainViewModel = viewModel()) {
    val profileState by viewModel.profile.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val activeFocusItem by viewModel.activeFocusItem.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    // Determine state
    if (profileState == null || profileState?.isOnboardingCompleted != true) {
        OnboardingScreen(
            onComplete = { name, priority, pace, type ->
                viewModel.completeOnboarding(name, priority, pace, type)
            }
        )
    } else {
        val currentProfile = profileState!!
        if (activeFocusItem != null) {
            FocusScreen(
                item = activeFocusItem!!,
                onCompleteToggle = { viewModel.toggleItemStatus(activeFocusItem!!) },
                onExit = { viewModel.endFocusSession() }
            )
        } else {
            MainTabsContainer(
                profile = currentProfile,
                filteredItems = filteredItems,
                categories = categories,
                documents = documents,
                selectedCategory = selectedCategory,
                selectedTimeframe = selectedTimeframe,
                searchQuery = searchQuery,
                sortBy = sortBy,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsContainer(
    profile: Profile,
    filteredItems: List<ListItem>,
    categories: List<com.example.data.ListCategory>,
    documents: List<Document>,
    selectedCategory: String?,
    selectedTimeframe: String,
    searchQuery: String,
    sortBy: String,
    viewModel: MainViewModel
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: Home/Lists, 1: Paragraphs Planner, 2: AI Copilot, 3: Storage Vault, 4: My Profile

    // Bottom sheets state for quick task creation
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Focus Lists",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Welcome back, ${profile.userName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { activeTab = 4 },
                        modifier = Modifier.testTag("avatar_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.userName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Home Lists") },
                    label = { Text("Lists", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_lists")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Description, contentDescription = "Docs Planner") },
                    label = { Text("Planner", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_planner")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Copilot") },
                    label = { Text("AI Copilot", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_ai")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.Folder, contentDescription = "Storage Vault") },
                    label = { Text("Vault", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_vault")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Profile", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("add_item_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add List Item")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                0 -> ListsTabContent(
                    profile = profile,
                    filteredItems = filteredItems,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    selectedTimeframe = selectedTimeframe,
                    searchQuery = searchQuery,
                    sortBy = sortBy,
                    viewModel = viewModel
                )
                1 -> DocumentPlannerTabContent(
                    documents = documents,
                    viewModel = viewModel
                )
                2 -> AiCopilotTabContent(
                    documents = documents,
                    viewModel = viewModel
                )
                3 -> StorageVaultTabContent(
                    documents = documents,
                    categories = categories,
                    filteredItems = filteredItems,
                    viewModel = viewModel
                )
                4 -> ProfileTabContent(
                    profile = profile,
                    itemsCount = filteredItems.size,
                    docsCount = documents.size,
                    viewModel = viewModel
                )
            }
        }

        if (showAddTaskDialog) {
            AddTaskBottomSheet(
                categories = categories.map { it.name },
                initialTimeframe = if (selectedTimeframe == "ALL") "DAY" else selectedTimeframe,
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, cat, priority, timeframe, notes ->
                    viewModel.addItem(title, cat, priority, timeframe, notes)
                    showAddTaskDialog = false
                }
            )
        }
    }
}

@Composable
fun ListsTabContent(
    profile: Profile,
    filteredItems: List<ListItem>,
    categories: List<com.example.data.ListCategory>,
    selectedCategory: String?,
    selectedTimeframe: String,
    searchQuery: String,
    sortBy: String,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personalized Insight Card (Theme Highlight)
        PersonalizedInsightBanner(profile = profile, items = filteredItems)

        // Timeframe Pill Selector: Today, Week, Month, Year
        TimeframeSelector(
            selectedTimeframe = selectedTimeframe,
            onSelect = { viewModel.setTimeframeFilter(it) }
        )

        // Category Filter list row
        CategoryFilterRow(
            categories = categories,
            selected = selectedCategory,
            onSelect = { viewModel.setCategoryFilter(it) },
            viewModel = viewModel
        )

        // Search & Sort bar
        SearchAndSortBar(
            searchQuery = searchQuery,
            onSearchChange = { viewModel.setSearchQuery(it) },
            sortBy = sortBy,
            onSortChange = { viewModel.setSortBy(it) }
        )

        // Header for priorities count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Priorities",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            )
            val todoCount = filteredItems.count { it.status == "WANNA_DO" }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$todoCount WANNA DO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // List rendering
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No items match those criteria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Try adding a distraction-free task or adjust filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("priorities_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    ListItemCard(
                        item = item,
                        onToggle = { viewModel.toggleItemStatus(item) },
                        onCyclePriority = {
                            val nextPriority = when (item.priority) {
                                "LOW" -> "MEDIUM"
                                "MEDIUM" -> "HIGH"
                                else -> "LOW"
                            }
                            viewModel.addItem(item.title, item.category, nextPriority, item.timeframe, item.notes)
                            viewModel.deleteItem(item)
                        },
                        onFocus = { viewModel.startFocusSession(item) },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalizedInsightBanner(profile: Profile, items: List<ListItem>) {
    val pendingTodayCount = items.count { it.timeframe == "DAY" && it.status == "WANNA_DO" }
    
    val dynamicInsight = when {
        profile.pace == "Busy" && pendingTodayCount > 3 -> {
            "Peak Clarity: Your pace is set to Busy with $pendingTodayCount today-items. De-clutter & focus on 1 main objective."
        }
        profile.pace == "Relaxed" -> {
            "Morning Calm: Smooth sailing today. Complete tasks with mindful intention and dive into leisure."
        }
        profile.priorityTopic == "Movies" -> {
            "Recreation Alert: Settle your responsibilities early, save room for distraction-free movie viewing tonight!"
        }
        profile.priorityTopic == "Health" -> {
            "Zen Notice: Remember to integrate small walking blocks or stretch breaks during today's tasks."
        }
        else -> {
            "Clarity Flow: Today is custom tailored to your '${profile.priorityTopic}' layout and balanced lifestyle pacing."
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("insight_banner"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Insight icon",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Personalized Flow Insight",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dynamicInsight,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun TimeframeSelector(
    selectedTimeframe: String,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        "DAY" to "Today",
        "WEEK" to "Week",
        "MONTH" to "Month",
        "YEAR" to "Year",
        "ANYTIME" to "Goals",
        "ALL" to "All Items"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (key, label) ->
                val isSelected = selectedTimeframe == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { onSelect(key) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(
    categories: List<com.example.data.ListCategory>,
    selected: String?,
    onSelect: (String?) -> Unit,
    viewModel: MainViewModel
) {
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selected == "All",
                    onClick = { onSelect("All") },
                    label = { Text("All Categories") }
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selected == category.name,
                    onClick = { onSelect(category.name) },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (category.iconName) {
                                    "movie" -> Icons.Default.Movie
                                    "book" -> Icons.Default.Book
                                    "travel" -> Icons.Default.CardTravel
                                    "task" -> Icons.Default.Lightbulb
                                    else -> Icons.Default.List
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(category.name)
                        }
                    },
                    trailingIcon = {
                        if (!category.isSystem) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Category",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.deleteCategory(category) }
                            )
                        }
                    }
                )
            }

            item {
                InputChip(
                    selected = false,
                    onClick = { showAddCategory = true },
                    label = { Text("+ Custom List") }
                )
            }
        }

        if (showAddCategory) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("Custom list name (e.g. Travel Bucket)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCustomCategory(newCategoryName.trim(), "list")
                            newCategoryName = ""
                            showAddCategory = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
                IconButton(onClick = { showAddCategory = false }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                }
            }
        }
    }
}

@Composable
fun SearchAndSortBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    onSortChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search priorities...", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Simple sorting spinner simulation
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = when (sortBy) {
                        "PRIORITY" -> Icons.Default.PriorityHigh
                        "DATE" -> Icons.Default.List
                        "NAME" -> Icons.Default.Home
                        else -> Icons.Default.Check
                    },
                    contentDescription = "Sort Icon",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (sortBy) {
                        "PRIORITY" -> "Priority"
                        "DATE" -> "Date"
                        "NAME" -> "A-Z"
                        else -> "Status"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Sort by Priority") },
                    onClick = {
                        onSortChange("PRIORITY")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Date Added") },
                    onClick = {
                        onSortChange("DATE")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Alphabetical") },
                    onClick = {
                        onSortChange("NAME")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Status") },
                    onClick = {
                        onSortChange("STATUS")
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ListItemCard(
    item: ListItem,
    onToggle: () -> Unit,
    onCyclePriority: () -> Unit,
    onFocus: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = item.status == "DID_IT"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("priority_item_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Checkbox Dot
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .background(if (isCompleted) MaterialTheme.colorScheme.secondary else Color.Transparent)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed mark",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Title + Notes info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Start distraction-free focus mode button!
                if (!isCompleted) {
                    IconButton(
                        onClick = onFocus,
                        modifier = Modifier
                            .testTag("focus_session_button_${item.id}")
                            .size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Focus Mode",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row: Priority chip (interactive) + Category + Timeframe
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Pill (Clicking cycles priority: Low -> Med -> High)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (item.priority) {
                                "HIGH" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                "MEDIUM" -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { onCyclePriority() }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Priority: ${item.priority}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (item.priority) {
                                "HIGH" -> Color(0xFFEF4444)
                                "MEDIUM" -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.outline
                            },
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Timeframe indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (item.timeframe) {
                            "DAY" -> "Today"
                            "WEEK" -> "This Week"
                            "MONTH" -> "This Month"
                            "YEAR" -> "This Year"
                            else -> "Anytime"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Category pill label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentPlannerTabContent(
    documents: List<Document>,
    viewModel: MainViewModel
) {
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("DOCUMENT") } // "DOCUMENT", "SNIPPET", "NOTE"
    var autoParseTasks by remember { mutableStateOf(true) }

    val docCount = documents.count { it.type == "DOCUMENT" }
    val snippetCount = documents.count { it.type == "SNIPPET" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Paragraph Planner Editor
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paragraph Planner & List Auto-Compiler",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = "Write snippets, document files, or paragraphs on what you plan to do. Our parser compiles it into priorities on the fly!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = { Text("Title (e.g. My study plan or movie ideas)") },
                    modifier = Modifier.fillMaxWidth().testTag("doc_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    placeholder = {
                        Text(
                            "Type your plans here...\n" +
                            "Use bullets starting with '-' or key words like:\n" +
                            " - Watch high priority movie tonight\n" +
                            " - Set up reading schedule this week"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("doc_content_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type selector
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("DOCUMENT", "SNIPPET").forEach { type ->
                            val isChosen = selectedType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = type,
                                    color = if (isChosen) Color.White else MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Auto extraction toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = autoParseTasks,
                            onCheckedChange = { autoParseTasks = it },
                            modifier = Modifier.testTag("auto_parse_checkbox")
                        )
                        Text(
                            text = "Auto-Extract Tasks",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Button(
                    onClick = {
                        if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                            viewModel.addDocument(
                                title = titleInput.trim(),
                                content = contentInput.trim(),
                                type = selectedType,
                                autoParse = autoParseTasks
                            )
                            titleInput = ""
                            contentInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_planner_button"),
                    enabled = titleInput.isNotBlank() && contentInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save planner")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Vault & Extract Tasks")
                }
            }
        }

        // Previous documents / clips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Documents & Snippets ($docCount docs, $snippetCount snippets)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (documents.filter { it.type != "FILE" }.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No written documents yet. Type something above!",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(documents.filter { it.type != "FILE" }) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (doc.type == "SNIPPET") Icons.Default.Description else Icons.Default.AutoAwesome,
                                        contentDescription = "type icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Row {
                                    // Trigger re-parsing manually
                                    IconButton(
                                        onClick = { viewModel.parseTextToTasks(doc.title, doc.content) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Recompile lists",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteDocument(doc) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete doc",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = doc.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = doc.type,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                    )
                                }

                                val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                Text(
                                    text = df.format(Date(doc.timestampCreated)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StorageVaultTabContent(
    documents: List<Document>,
    categories: List<com.example.data.ListCategory>,
    filteredItems: List<ListItem>,
    viewModel: MainViewModel
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var mockFileName by remember { mutableStateOf("") }
    var mockFileType by remember { mutableStateOf("PDF") } // "PDF", "JPG", "DOCX", "ZIP"

    val filesOnly = documents.filter { it.type == "FILE" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vault Welcome message
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Vault",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Document & File Vault",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "A designated space to keep lists, notes, files, blueprints, and links safe and organized.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Dynamic Virtual Folders
        Text(
            text = "Active Folders (Click to filter list priorities)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { category ->
                item {
                    val folderItemCount = filteredItems.count { it.category == category.name }
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { viewModel.setCategoryFilter(category.name) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "folder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                            Column {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$folderItemCount goals",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // File vault manager section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Personal Files & Attachments",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Button(
                onClick = { showUploadDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add File Item", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (filesOnly.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files or attachments stored yet. Add custom files above!",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filesOnly) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = "file icon",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text(
                                        text = file.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = file.fileMimeType ?: "FILE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = file.fileSize ?: "Unspecified Size",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.deleteDocument(file) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete file",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showUploadDialog) {
            AlertDialog(
                onDismissRequest = { showUploadDialog = false },
                title = { Text("Store File Item Reference") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Add dynamic files, photos, mock plans, or links to store in your private Vault directory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        OutlinedTextField(
                            value = mockFileName,
                            onValueChange = { mockFileName = it },
                            placeholder = { Text("File label (e.g. workspace_isometric.png)") },
                            modifier = Modifier.fillMaxWidth().testTag("vault_file_label"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Select Document File Type:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("PDF", "PNG", "ZIP", "DOCX").forEach { type ->
                                val selected = mockFileType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { mockFileType = type }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.outline,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (mockFileName.isNotBlank()) {
                                val ranSize = (2..24).random()
                                viewModel.addDocument(
                                    title = mockFileName.trim(),
                                    content = "Private mock document attachment references",
                                    type = "FILE",
                                    filePath = "/user/vault/${mockFileName.lowercase()}.${mockFileType.lowercase()}",
                                    fileSize = "${ranSize} MB",
                                    fileMimeType = mockFileType
                                )
                                mockFileName = ""
                                showUploadDialog = false
                            }
                        },
                        enabled = mockFileName.isNotBlank()
                    ) {
                        Text("Add to Vault", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileTabContent(
    profile: Profile,
    itemsCount: Int,
    docsCount: Int,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large user badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.userName.take(1).uppercase(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }

        Text(
            text = profile.userName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Metadata grid stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$itemsCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Total Active Goals", style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$docsCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Vault Entries", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Onboarding configurations
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Your Lifestyle Customizations",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Selected Main Category:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Text(profile.preferredType, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lifestyle Navigation Pace:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Text(profile.pace, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Primary Life Focus:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Text(profile.priorityTopic, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Reset Onboarding/Profile to start fresh!
        Button(
            onClick = { viewModel.resetProfile() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_profile_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Restart Onboarding Flow", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Custom simple Dialog for injecting a list item
@Composable
fun AddTaskBottomSheet(
    categories: List<String>,
    initialTimeframe: String,
    onDismiss: () -> Unit,
    onAdd: (title: String, cat: String, priority: String, timeframe: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(categories.firstOrNull() ?: "General Tasks") }
    var selectedPriority by remember { mutableStateOf("MEDIUM") } // "HIGH", "MEDIUM", "LOW"
    var selectedTimeframe by remember { mutableStateOf(initialTimeframe) } // "DAY", "WEEK", "MONTH", "YEAR"
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create New Goal",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("What do you wanna do?") },
                        modifier = Modifier.fillMaxWidth().testTag("add_item_title"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    // Category choice buttons
                    Text("Choose List Category:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            item {
                                val isSelected = selectedCat == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedCat = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.outline,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Timeframe pills
                    Text("Target Timeframe:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "DAY" to "Today",
                            "WEEK" to "Week",
                            "MONTH" to "Month",
                            "YEAR" to "Year",
                            "ANYTIME" to "Goals"
                        ).forEach { (key, label) ->
                            val isSelected = selectedTimeframe == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedTimeframe = key }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    // Priority choice
                    Text("Set Intent Priority:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            val isSelected = selectedPriority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            when (p) {
                                                "HIGH" -> Color(0xFFEF4444)
                                                "MEDIUM" -> MaterialTheme.colorScheme.primary
                                                else -> Color(0xFF64748B)
                                            }
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clickable { selectedPriority = p }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Extra planning notes or descriptions...") },
                        modifier = Modifier.fillMaxWidth().testTag("add_item_notes"),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), selectedCat, selectedPriority, selectedTimeframe, notes.trim())
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_item_button")
            ) {
                Text("Insert Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
