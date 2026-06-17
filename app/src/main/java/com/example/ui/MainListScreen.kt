package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ListCategory
import com.example.data.ListItem
import com.example.data.Profile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainListScreen(
    viewModel: MainViewModel,
    profile: Profile,
    onStartFocus: (ListItem) -> Unit,
    onResetOnboarding: () -> Unit
) {
    val items by viewModel.filteredItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Onboarding Tailored Insight Generation
    val aiInsightTitle = when (profile.pace) {
        "Busy" -> "Focused Efficiency"
        "Relaxed" -> "Mindful Harmony"
        else -> "Daily Clarity"
    }

    val aiInsightDescription = when (profile.priorityTopic) {
        "Work" -> "Based on your ${profile.pace.lowercase()} work rhythm, optimize today's high-priority items so you can fully disconnect."
        "Health" -> "Health is wealth. Dedicating a focused gap for body-mind goals fits your current ${profile.pace.lowercase()} plan."
        "Leisure" -> "Unwind intentionally! Enjoy high focus during active tasks, then dive into your watchlists guilt-free."
        "Learning" -> "Growth requires space. Block 20 minutes for deliberate learning milestones amidst your day."
        else -> "Based on your focus pacing, today is perfect for resolving core tasks and finding calmness inside lists."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Sleek Application Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Perspective",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Hello, ${profile.userName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Profile Bubble
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { showProfileDialog = true }
                        .testTag("profile_bubble"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Details",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Personalized AI Insight Dashboard Banner (matches design bg-[#E7F0FF] rounded-3xl p-4 ect.)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Insights Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = aiInsightTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = aiInsightDescription,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear searching")
                        }
                    }
                },
                placeholder = {
                    Text(
                        "Search items & notes...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("search_field"),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Dynamic Timeframe Navigation Bar (Day, Week, Month, Year, Anytime, All)
            val timeframes = listOf(
                "DAY" to "Today",
                "WEEK" to "Week",
                "MONTH" to "Month",
                "YEAR" to "Year",
                "ANYTIME" to "Goals",
                "ALL" to "All List"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timeframes) { (key, display) ->
                    val isSelected = selectedTimeframe == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { viewModel.setTimeframeFilter(key) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("time_chip_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Categories Filter List Row (Tasks, Movies, Books, custom)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        // Add Custom Category label link
                        Text(
                            text = "+ New Category",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable { showAddCategoryDialog = true }
                                .testTag("add_category_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isSelected = selectedCategory == "All"
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCategoryFilter("All") },
                                label = { Text("All Categories") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AllInclusive,
                                        contentDescription = "All"
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        items(categories) { category ->
                            val isSelected = selectedCategory == category.name
                            val icon = when (category.iconName) {
                                "movie" -> Icons.Default.Movie
                                "book" -> Icons.Default.Book
                                "travel" -> Icons.Default.Luggage
                                "task" -> Icons.Default.Check
                                else -> Icons.Default.List
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCategoryFilter(category.name) },
                                label = { Text(category.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = category.name,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (!category.isSystem) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete custom category",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.deleteCategory(category) }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Main List scroll area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (items.isEmpty()) {
                    // Peaceful minimal empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "Inner Zen",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Distraction-Free Space",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No plans registered for this view yet.\nPress the Floating button to record a plan or target.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 16.sp
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val activeCount = items.count { it.status == "WANNA_DO" }
                                Text(
                                    text = "Top Priorities",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$activeCount ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        items(items, key = { it.id }) { item ->
                            val isCompleted = item.status == "DID_IT"

                            // Custom Card reflecting the requested Sleek design (with borders, styling, custom priority indicators)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement()
                                    .border(
                                        width = 1.dp,
                                        color = if (isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("item_card_${item.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Checkbox
                                    IconButton(
                                        onClick = { viewModel.toggleItemStatus(item) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("check_button_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isCompleted) Icons.Default.CheckCircle
                                            else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Toggle Complete",
                                            tint = if (isCompleted) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Item Title/Details Block
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                else MaterialTheme.colorScheme.onSurface,
                                                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Labels (Area of Life, Category, Timeframe, Priority)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val badgeColor = when (item.lifeArea) {
                                                "Health" -> Color(0xFF10B981)
                                                "Career" -> Color(0xFF3B82F6)
                                                "Personal Growth" -> Color(0xFF8B5CF6)
                                                "Leisure" -> Color(0xFFF59E0B)
                                                "Finance" -> Color(0xFFEC4899)
                                                else -> Color(0xFF64748B)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = badgeColor.copy(alpha = 0.12f),
                                            ) {
                                                Text(
                                                    text = item.lifeArea.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = badgeColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                                            )
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                                            )
                                            Text(
                                                text = item.timeframe,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                                            )

                                            val priorityColor = when (item.priority) {
                                                "HIGH" -> Color(0xFFEF4444)
                                                "MEDIUM" -> Color(0xFFF59E0B)
                                                else -> Color(0xFF10B981)
                                            }
                                            Text(
                                                text = "${item.priority} priority",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = priorityColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }

                                        if (item.notes.isNotBlank()) {
                                            Text(
                                                text = item.notes,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }

                                    // Action tools (Start Focus or Delete)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (!isCompleted) {
                                            IconButton(
                                                onClick = { onStartFocus(item) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("focus_button_${item.id}"),
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Engage Focus Timer",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteItem(item) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("delete_button_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete item",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                modifier = Modifier.size(18.dp)
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

        // Floating Action Button to Add Items (Styled as rounded rounded-2xl to match Sleek custom look)
        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_item"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Item",
                modifier = Modifier.size(28.dp)
            )
        }

        // Add ListItem Dialog
        if (showAddDialog) {
            var itemTitle by remember { mutableStateOf("") }
            var itemCategory by remember { mutableStateOf(if (categories.isNotEmpty()) categories.first().name else "General Tasks") }
            var itemPriority by remember { mutableStateOf("MEDIUM") }
            var itemTimeframe by remember { mutableStateOf("DAY") }
            var itemNotes by remember { mutableStateOf("") }
            var itemLifeArea by remember { mutableStateOf("Personal Growth") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Record New Plan") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                             value = itemTitle,
                             onValueChange = { itemTitle = it },
                             label = { Text("Title of task/movie etc.") },
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .testTag("add_item_title"),
                             singleLine = true
                        )

                        // Area of Life selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Area of Life", style = MaterialTheme.typography.titleSmall)
                            
                            var isSuggesting by remember { mutableStateOf(false) }
                            
                            TextButton(
                                onClick = {
                                    if (itemTitle.isNotBlank()) {
                                        isSuggesting = true
                                        viewModel.suggestCategory(
                                            title = itemTitle,
                                            notes = itemNotes,
                                            onCompleted = { suggested ->
                                                itemLifeArea = suggested
                                                isSuggesting = false
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("suggest_area_button"),
                                enabled = itemTitle.isNotBlank() && !isSuggesting,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Suggest Area",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSuggesting) "Analyzing..." else "AI Suggest",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val lifeAreas = listOf("Health", "Career", "Personal Growth", "Leisure", "Finance", "General")
                            items(lifeAreas) { area ->
                                val active = itemLifeArea == area
                                val activeColor = when (area) {
                                    "Health" -> Color(0xFF10B981)
                                    "Career" -> Color(0xFF3B82F6)
                                    "Personal Growth" -> Color(0xFF8B5CF6)
                                    "Leisure" -> Color(0xFFF59E0B)
                                    "Finance" -> Color(0xFFEC4899)
                                    else -> Color(0xFF64748B)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) activeColor else Color.LightGray.copy(alpha = 0.3f))
                                        .clickable { itemLifeArea = area }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = area,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        // Category Picker Selection (Quick list)
                        Text("Category", style = MaterialTheme.typography.titleSmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val active = itemCategory == cat.name
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f))
                                        .clickable { itemCategory = cat.name }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        // Timeframe selection (Day, Week, Month, Year, Anytime)
                        Text("Target Interval", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val frames = listOf(
                                "DAY" to "Today",
                                "WEEK" to "Week",
                                "MONTH" to "Month",
                                "YEAR" to "Year",
                                "ANYTIME" to "Goals"
                            )
                            frames.forEach { (key, label) ->
                                val active = itemTimeframe == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) MaterialTheme.colorScheme.secondary else Color.LightGray.copy(alpha = 0.3f))
                                        .clickable { itemTimeframe = key }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (active) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        // Priority rating (High, Medium, Low)
                        Text("Priority Rank", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val rates = listOf("HIGH", "MEDIUM", "LOW")
                            rates.forEach { rate ->
                                val active = itemPriority == rate
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) Color(0xFFEF4444) else Color.LightGray.copy(alpha = 0.3f))
                                        .clickable { itemPriority = rate }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = itemNotes,
                            onValueChange = { itemNotes = it },
                            label = { Text("Private notes/context details") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (itemTitle.isNotBlank()) {
                                viewModel.addItem(
                                    title = itemTitle.trim(),
                                    category = itemCategory,
                                    priority = itemPriority,
                                    timeframe = itemTimeframe,
                                    notes = itemNotes.trim(),
                                    lifeArea = itemLifeArea
                                )
                                showAddDialog = false
                            }
                        },
                        enabled = itemTitle.isNotBlank(),
                        modifier = Modifier.testTag("confirm_add_item_button")
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Dismiss")
                    }
                }
            )
        }

        // Add Category Dialog
        if (showAddCategoryDialog) {
            var catName by remember { mutableStateOf("") }
            var catIcon by remember { mutableStateOf("task") } // "task", "movie", "book", "travel"

            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("Create Private Category") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { catName = it },
                            label = { Text("Category Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_name_input"),
                            singleLine = true
                        )

                        Text("Select Motif Icon:")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val icons = listOf(
                                "task" to Icons.Default.Check,
                                "movie" to Icons.Default.Movie,
                                "book" to Icons.Default.Book,
                                "travel" to Icons.Default.Luggage,
                                "list" to Icons.Default.List
                            )

                            icons.forEach { (name, vector) ->
                                val isChosen = catIcon == name
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.2f))
                                        .clickable { catIcon = name }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = name,
                                        tint = if (isChosen) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (catName.isNotBlank()) {
                                viewModel.addCustomCategory(catName.trim(), catIcon)
                                showAddCategoryDialog = false
                            }
                        },
                        enabled = catName.isNotBlank(),
                        modifier = Modifier.testTag("confirm_add_category_button")
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Dismiss")
                    }
                }
            )
        }

        // Profile details modal
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text("Personal Space Settings") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("User: ${profile.userName}", fontWeight = FontWeight.Bold)
                        Text("Immediate Focus: ${profile.priorityTopic}")
                        Text("Pace: ${profile.pace} mode")
                        Text("Primary Layout: ${profile.preferredType}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To alter these preferences or redefine your lifestyle options, reset onboarding below. Your current lists will be refreshed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProfileDialog = false
                            onResetOnboarding()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("reset_profile_button")
                    ) {
                        Text("Reset & Onboard Fresh")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
