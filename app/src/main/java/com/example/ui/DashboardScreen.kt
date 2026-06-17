package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ListItem
import com.example.data.Profile

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    allItems: List<ListItem>,
    profile: Profile?,
    viewModel: MainViewModel
) {
    // -------------------------------------------------------------
    // Personalization States
    // -------------------------------------------------------------
    // 1. Dashboard Color Theme Accent Option
    var selectedThemeTone by remember { mutableStateOf("Ocean Blue") } // "Ocean Blue", "Emerald", "Royal Lavender", "Warm Amber"
    
    // 2. Filter Priority Mode
    var priorityFilter by remember { mutableStateOf("ALL") } // "ALL", "HIGH", "MEDIUM", "LOW"
    
    // 2b. Filter Area of Life Mode
    var areaOfLifeFilter by remember { mutableStateOf("ALL") } // "ALL", "Health", "Career", "Personal Growth", "Leisure", "Finance", "General"
    
    // 3. User Customizable Target Completion Rate Threshold
    var targetCompletionThreshold by remember { mutableStateOf(0.75f) } // default 75%
    
    // 4. View Style Options
    var viewStyle by remember { mutableStateOf("Rings") } // "Rings", "Bars", "Grid"

    // 5. Selected Detail Timeframe for Actionable Recommendations
    var activeRecommendationTimeframe by remember { mutableStateOf("DAY") }

    val primaryColor = when (selectedThemeTone) {
        "Emerald" -> Color(0xFF10B981)
        "Royal Lavender" -> Color(0xFF8B5CF6)
        "Warm Amber" -> Color(0xFFF59E0B)
        else -> Color(0xFF3B82F6) // Ocean Blue
    }

    val secondaryColor = when (selectedThemeTone) {
        "Emerald" -> Color(0xFF34D399)
        "Royal Lavender" -> Color(0xFFA78BFA)
        "Warm Amber" -> Color(0xFFFBBF24)
        else -> Color(0xFF60A5FA)
    }

    val tertiaryColor = when (selectedThemeTone) {
        "Emerald" -> Color(0xFF047857)
        "Royal Lavender" -> Color(0xFF6D28D9)
        "Warm Amber" -> Color(0xFFB45309)
        else -> Color(0xFF1D4ED8)
    }

    // Filter items based on priority and area of life selection
    val filteredItems = remember(allItems, priorityFilter, areaOfLifeFilter) {
        var result = allItems
        if (priorityFilter != "ALL") {
            result = result.filter { it.priority.equals(priorityFilter, ignoreCase = true) }
        }
        if (areaOfLifeFilter != "ALL") {
            result = result.filter { it.lifeArea.equals(areaOfLifeFilter, ignoreCase = true) }
        }
        result
    }

    // Calculate rates for DAY, WEEK, MONTH
    val (dayDone, dayTotal) = remember(filteredItems) {
        val dayList = filteredItems.filter { it.timeframe.equals("DAY", ignoreCase = true) }
        dayList.count { it.status == "DID_IT" } to dayList.size
    }

    val (weekDone, weekTotal) = remember(filteredItems) {
        val weekList = filteredItems.filter { it.timeframe.equals("WEEK", ignoreCase = true) }
        weekList.count { it.status == "DID_IT" } to weekList.size
    }

    val (monthDone, monthTotal) = remember(filteredItems) {
        val monthList = filteredItems.filter { it.timeframe.equals("MONTH", ignoreCase = true) }
        monthList.count { it.status == "DID_IT" } to monthList.size
    }

    // General anytime/someday
    val (otherDone, otherTotal) = remember(filteredItems) {
        val otherList = filteredItems.filter { 
            !it.timeframe.equals("DAY", ignoreCase = true) && 
            !it.timeframe.equals("WEEK", ignoreCase = true) && 
            !it.timeframe.equals("MONTH", ignoreCase = true)
        }
        otherList.count { it.status == "DID_IT" } to otherList.size
    }

    val dayRate = if (dayTotal > 0) dayDone.toFloat() / dayTotal else 0f
    val weekRate = if (weekTotal > 0) weekDone.toFloat() / weekTotal else 0f
    val monthRate = if (monthTotal > 0) monthDone.toFloat() / monthTotal else 0f
    val otherRate = if (otherTotal > 0) otherDone.toFloat() / otherTotal else 0f

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Row inside Dashboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Dashboard Statistics",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Personalized Analytics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Visualizing completion rates & milestones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Simple Info Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (allItems.isNotEmpty()) "${allItems.count { it.status == "DID_IT" }}/${allItems.size} Done" else "No Goals",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // VISUAL COMPLETION RATES DISPLAY
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header style selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completion Rates",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Rings vs Bars Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        listOf("Rings", "Bars", "Grid").forEach { style ->
                            val active = viewStyle == style
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { viewStyle = style }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = style,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                        color = if (active) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // Main visualization container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (viewStyle) {
                        "Rings" -> {
                            // High-quality Concentric Rings resembling premium smartwatch trackers
                            ConcentricRingsVisualizer(
                                dayRate = dayRate,
                                weekRate = weekRate,
                                monthRate = monthRate,
                                primary = primaryColor,
                                secondary = secondaryColor,
                                tertiary = tertiaryColor,
                                targetThreshold = targetCompletionThreshold
                            )
                        }
                        "Bars" -> {
                            // Premium horizontal progress bar graph visualization
                            ProgressBarVisualizer(
                                dayRate = dayRate,
                                dayDone = dayDone,
                                dayTotal = dayTotal,
                                weekRate = weekRate,
                                weekDone = weekDone,
                                weekTotal = weekTotal,
                                monthRate = monthRate,
                                monthDone = monthDone,
                                monthTotal = monthTotal,
                                otherRate = otherRate,
                                otherDone = otherDone,
                                otherTotal = otherTotal,
                                accentColor = primaryColor,
                                targetRate = targetCompletionThreshold
                            )
                        }
                        else -> {
                            // Circular Ring Grid system
                            RingsGridVisualizer(
                                dayRate = dayRate,
                                dayLabel = "Daily",
                                weekRate = weekRate,
                                weekLabel = "Weekly",
                                monthRate = monthRate,
                                monthLabel = "Monthly",
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                tertiaryColor = tertiaryColor
                            )
                        }
                    }
                }

                // Custom Legend Section matching values
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(color = primaryColor, label = "Daily Tasks", rate = dayRate)
                    LegendItem(color = secondaryColor, label = "Weekly", rate = weekRate)
                    LegendItem(color = tertiaryColor, label = "Monthly", rate = monthRate)
                }
            }
        }

        // -------------------------------------------------------------
        // PERSISTENT PERSONALIZATION CONTROLS PANEL
        // -------------------------------------------------------------
        Text(
            text = "Personalize Dashboard View",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Selector Theme Accents
                Column {
                    Text(
                        text = "🎨 Dashboard Focus Color:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Ocean Blue", "Emerald", "Royal Lavender", "Warm Amber").forEach { tone ->
                            val active = selectedThemeTone == tone
                            val toneBg = when (tone) {
                                "Emerald" -> Color(0xFF10B981)
                                "Royal Lavender" -> Color(0xFF8B5CF6)
                                "Warm Amber" -> Color(0xFFF59E0B)
                                else -> Color(0xFF3B82F6)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) toneBg.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (active) 2.dp else 1.dp,
                                        color = if (active) toneBg else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedThemeTone = tone }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(toneBg)
                                    )
                                    Text(
                                        text = tone,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = if (active) toneBg else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Priority Filter Selection
                Column {
                    Text(
                        text = "⚡ Filter Analytics by Priority Level:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "HIGH", "MEDIUM", "LOW").forEach { level ->
                            val active = priorityFilter == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { priorityFilter = level }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // 2b. Area of Life Filter Selection
                Column {
                    Text(
                        text = "🌱 Filter Analytics by Area of Life:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "Health", "Career", "Personal Growth", "Leisure", "Finance", "General").forEach { area ->
                            val active = areaOfLifeFilter == area
                            val activeColor = when (area) {
                                "Health" -> Color(0xFF10B981)
                                "Career" -> Color(0xFF3B82F6)
                                "Personal Growth" -> Color(0xFF8B5CF6)
                                "Leisure" -> Color(0xFFF59E0B)
                                "Finance" -> Color(0xFFEC4899)
                                "ALL" -> primaryColor
                                else -> Color(0xFF64748B)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { areaOfLifeFilter = area }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = area,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Slider Threshold metric customization
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Target Completion Threshold:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${(targetCompletionThreshold * 100).toInt()}% Done",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                    }
                    Slider(
                        value = targetCompletionThreshold,
                        onValueChange = { targetCompletionThreshold = it },
                        valueRange = 0.5f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = primaryColor.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // TACTICAL RECOMMENDATIONS FOR TARGET ACHIEVEMENT
        // -------------------------------------------------------------
        Text(
            text = "Target Optimization & Ideas",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        val targetMetDay = dayRate >= targetCompletionThreshold
        val targetMetWeek = weekRate >= targetCompletionThreshold
        val targetMetMonth = monthRate >= targetCompletionThreshold

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Focus area select chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "DAY" to "Daily Goals",
                        "WEEK" to "Weekly Goals",
                        "MONTH" to "Monthly Goals"
                    ).forEach { (code, label) ->
                        val active = activeRecommendationTimeframe == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) primaryColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (active) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { activeRecommendationTimeframe = code }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = if (active) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Render detail block matching chosen status
                val isMet = when (activeRecommendationTimeframe) {
                    "DAY" -> targetMetDay
                    "WEEK" -> targetMetWeek
                    else -> targetMetMonth
                }

                val ratePercent = when (activeRecommendationTimeframe) {
                    "DAY" -> (dayRate * 100).toInt()
                    "WEEK" -> (weekRate * 100).toInt()
                    else -> (monthRate * 100).toInt()
                }

                val currentTotal = when (activeRecommendationTimeframe) {
                    "DAY" -> dayTotal
                    "WEEK" -> weekTotal
                    else -> monthTotal
                }

                val outstandingCount = when (activeRecommendationTimeframe) {
                    "DAY" -> dayTotal - dayDone
                    "WEEK" -> weekTotal - weekDone
                    else -> monthTotal - monthDone
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.background(
                        color = if (isMet) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFF59E0B).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ).padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Status icon",
                        tint = if (isMet) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isMet) {
                                "Awesome! Target Achieved"
                            } else {
                                "Focus Increase Needed"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isMet) Color(0xFF047857) else Color(0xFFB45309)
                            )
                        )
                        Text(
                            text = if (isMet) {
                                "Your $activeRecommendationTimeframe rate of $ratePercent% is currently meeting or exceeding your customized $targetCompletionThreshold target. Keep up this magnificent focus level!"
                            } else {
                                "Your $activeRecommendationTimeframe rate is $ratePercent%. You are $outstandingCount items short of your target goal. Resolve outstanding items to exceed your target."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action suggestions List
                val actionableItems = remember(filteredItems, activeRecommendationTimeframe) {
                    filteredItems.filter { 
                        it.timeframe.equals(activeRecommendationTimeframe, ignoreCase = true) && 
                        it.status == "WANNA_DO" 
                    }.take(2)
                }

                if (actionableItems.isNotEmpty()) {
                    Text(
                        text = "🚀 Suggested Next Goals to Tackle:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    actionableItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.toggleItemStatus(item)
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Priority: ${item.priority} • Category: ${item.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Complete task",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else if (currentTotal == 0) {
                    Text(
                        text = "💡 Quick Tip: You have no tasks registered for timeframe '$activeRecommendationTimeframe'. Create list items in of this timeframe to start visualizing progress statistics in real-time!",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Cup", tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All goals in this timeframe are completed! Absolute excellence!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// HIGH QUALITY HAND-DRAWN COMPOSABLES
// -----------------------------------------------------------------

@Composable
fun ConcentricRingsVisualizer(
    dayRate: Float,
    weekRate: Float,
    monthRate: Float,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    targetThreshold: Float
) {
    val dayAnimated by animateFloatAsState(targetValue = dayRate, animationSpec = tween(1200))
    val weekAnimated by animateFloatAsState(targetValue = weekRate, animationSpec = tween(1200))
    val monthAnimated by animateFloatAsState(targetValue = monthRate, animationSpec = tween(1200))

    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    androidx.compose.foundation.Canvas(modifier = Modifier.size(190.dp)) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val strokeWidth = 14.dp.toPx()

        // Background reference grid lines matching recharts radar radial lines
        val radialSteps = listOf(0.35f, 0.6f, 0.85f)
        radialSteps.forEach { step ->
            drawCircle(
                color = gridLineColor,
                radius = (width / 2f) * step,
                style = Stroke(width = 1f)
            )
        }

        // Draw radial grid ticks (8 cross bars)
        for (angle in 0..315 step 45) {
            val radians = Math.toRadians(angle.toDouble())
            val endX = center.x + (width / 2f) * Math.cos(radians).toFloat() * 0.9f
            val endY = center.y + (height / 2f) * Math.sin(radians).toFloat() * 0.9f
            drawLine(
                color = gridLineColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1f
            )
        }

        // concentric radius calculation
        val maxRadius = (width / 2f) - (strokeWidth / 2f)
        val radiusSpacing = 20.dp.toPx()

        val ringDefinitions = listOf(
            Triple(dayAnimated, primary, maxRadius),
            Triple(weekAnimated, secondary, maxRadius - radiusSpacing),
            Triple(monthAnimated, tertiary, maxRadius - (radiusSpacing * 2))
        )

        ringDefinitions.forEach { (rate, color, radius) ->
            // Background Track Gray
            drawCircle(
                color = color.copy(alpha = 0.1f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Active completion arc (starts at -90deg/top to mirror smartwatches)
            val sweep = rate * 360f
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Inner stats percentage glow
        val avgComplete = ((dayRate + weekRate + monthRate) / 3f)
        drawCircle(
            color = primary.copy(alpha = 0.05f),
            radius = maxRadius - (radiusSpacing * 2.8f)
        )
    }
}

@Composable
fun ProgressBarVisualizer(
    dayRate: Float,
    dayDone: Int,
    dayTotal: Int,
    weekRate: Float,
    weekDone: Int,
    weekTotal: Int,
    monthRate: Float,
    monthDone: Int,
    monthTotal: Int,
    otherRate: Float,
    otherDone: Int,
    otherTotal: Int,
    accentColor: Color,
    targetRate: Float
) {
    val dayAnim by animateFloatAsState(targetValue = dayRate, animationSpec = tween(1000))
    val weekAnim by animateFloatAsState(targetValue = weekRate, animationSpec = tween(1000))
    val monthAnim by animateFloatAsState(targetValue = monthRate, animationSpec = tween(1000))
    val otherAnim by animateFloatAsState(targetValue = otherRate, animationSpec = tween(1000))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            RowData("Daily", dayAnim, dayDone, dayTotal, Color(0xFF3B82F6)),
            RowData("Weekly", weekAnim, weekDone, weekTotal, Color(0xFF10B981)),
            RowData("Monthly", monthAnim, monthDone, monthTotal, Color(0xFF8B5CF6)),
            RowData("Someday/Anytime", otherAnim, otherDone, otherTotal, Color(0xFFF59E0B))
        ).forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${item.done}/${item.total} (${(item.rate * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(item.color.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (item.rate > 0f) item.rate else 0.001f)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(item.color, item.color.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
            }
        }
    }
}

data class RowData(val label: String, val rate: Float, val done: Int, val total: Int, val color: Color)

@Composable
fun RingsGridVisualizer(
    dayRate: Float,
    dayLabel: String,
    weekRate: Float,
    weekLabel: String,
    monthRate: Float,
    monthLabel: String,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Triple(dayRate, dayLabel, primaryColor),
            Triple(weekRate, weekLabel, secondaryColor),
            Triple(monthRate, monthLabel, tertiaryColor)
        ).forEach { (rate, label, color) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val rateAnim by animateFloatAsState(targetValue = rate, animationSpec = tween(1100))

                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 8.dp.toPx()
                        val radius = (size.width / 2f) - (stroke / 2f)
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Base track
                        drawCircle(
                            color = color.copy(alpha = 0.1f),
                            radius = radius,
                            style = Stroke(width = stroke)
                        )

                        // Arc
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = rateAnim * 360f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = "${(rate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, rate: Float) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label (${(rate * 100).toInt()}%)",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
