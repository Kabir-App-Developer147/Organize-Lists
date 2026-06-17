package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Document

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiCopilotTabContent(
    documents: List<Document>,
    viewModel: MainViewModel
) {
    val aiResultText by viewModel.aiResultText.collectAsState()
    val isGenerating by viewModel.isAiGenerating.collectAsState()

    // 1. Source State Configuration
    // OFFLINE_LOCAL (Heuristic parsing - always offline), OLLAMA (Local Ollama server), ONLINE_GEMINI (Online backend API)
    var modelSource by remember { mutableStateOf("ONLINE_GEMINI") }
    var modelName by remember { mutableStateOf("gemini-3.5-flash") }
    var ollamaUrl by remember { mutableStateOf("http://10.0.2.2:11434") }
    var customApiKey by remember { mutableStateOf("") }

    // 2. Multiselect Document State
    var selectedDocIds by remember { mutableStateOf(setOf<Int>()) }

    // 3. Preset prompt state
    // PRIORITY_LIST (Goals report), EXECUTIVE_REPORT (Performance Analysis), SOMEDAY_GOALS (Indefinite Goals list), CUSTOM (User text box)
    var presetType by remember { mutableStateOf("PRIORITY_LIST") }
    var customPromptInput by remember { mutableStateOf("") }
    var reportSaveTitle by remember { mutableStateOf("Personalized Goals Report") }

    var showConfigExpand by remember { mutableStateOf(false) }

    var showQuickAddNote by remember { mutableStateOf(false) }
    var quickAddTitle by remember { mutableStateOf("") }
    var quickAddContent by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Heading Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "AI Personalized Copilot",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Synthesize file contents and documents using online models or offline localhost servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Advanced Configuration Dropdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showConfigExpand = !showConfigExpand }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Config", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "AI Server & Model Config",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Icon(
                        imageVector = if (showConfigExpand) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }

                if (showConfigExpand) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Processor Environment:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Mode selections Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("ONLINE_GEMINI", "Online Gemini", Icons.Outlined.CloudQueue),
                            Triple("OLLAMA", "Ollama Local", Icons.Outlined.Computer),
                            Triple("OFFLINE_LOCAL", "Offline Fallback", Icons.Outlined.SettingsBackupRestore)
                        ).forEach { (mode, label, icon) ->
                            val active = modelSource == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        modelSource = mode
                                        // Update default model name matching mode
                                        if (mode == "ONLINE_GEMINI") {
                                            modelName = "gemini-3.5-flash"
                                        } else if (mode == "OLLAMA") {
                                            modelName = "gemma:2b"
                                        }
                                    },
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (active) Color.White else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (modelSource == "ONLINE_GEMINI") {
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("Model Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customApiKey,
                            onValueChange = { customApiKey = it },
                            label = { Text("Custom Gemini API Key (Optional Override)") },
                            placeholder = { Text("Default key loaded automatically") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (modelSource == "OLLAMA") {
                        OutlinedTextField(
                            value = ollamaUrl,
                            onValueChange = { ollamaUrl = it },
                            label = { Text("Local Ollama Server Web End") },
                            placeholder = { Text("http://10.0.2.2:11434") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("Local model name") },
                            placeholder = { Text("gemma:2b or mistral") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Local IP '10.0.2.2' maps to your computer's localhost from the Android emulator. Keep Ollama running on your desktop with OLLAMA_ORIGINS=\"*\"!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (modelSource == "OFFLINE_LOCAL") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🔒 Offline Fallback Mode generates fully detailed productivity summaries & checklists locally under our customized semantic parsing engine, without requiring standard internet connections or servers.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Context File Selection
        Text(
            text = "Step 1: Select Context Files & Documents (${documents.size} available)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        if (documents.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No files found in Vault. Create documents or files in 'Vault' or 'Planner' tabs first to provide context!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Documents:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { selectedDocIds = documents.map { it.id }.toSet() }) {
                                Text("Select All", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { selectedDocIds = emptySet() }) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        documents.forEach { doc ->
                            val isSelected = selectedDocIds.contains(doc.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                                    .clickable {
                                        selectedDocIds = if (isSelected) {
                                            selectedDocIds - doc.id
                                        } else {
                                            selectedDocIds + doc.id
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedDocIds = if (checked == true) {
                                            selectedDocIds + doc.id
                                        } else {
                                            selectedDocIds - doc.id
                                        }
                                    },
                                    modifier = Modifier.testTag("checkbox_doc_${doc.id}")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (doc.type == "FILE") Icons.Default.InsertDriveFile else Icons.Default.Description,
                                    contentDescription = doc.type,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${doc.type} • ${doc.content.length} chars",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick add context section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showQuickAddNote = !showQuickAddNote }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Quick Add", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Quick Paste Text Note (New Context)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Icon(
                        imageVector = if (showQuickAddNote) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand note adder"
                    )
                }

                if (showQuickAddNote) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = quickAddTitle,
                        onValueChange = { quickAddTitle = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g. My Study Notes or Project Blueprint") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quickAddContent,
                        onValueChange = { quickAddContent = it },
                        label = { Text("Note Content Text") },
                        placeholder = { Text("Paste any ideas, lists or content to analyze here...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (quickAddContent.isNotBlank()) {
                                val finalTitle = quickAddTitle.trim().ifBlank { "Quick Note" }
                                viewModel.addDocument(
                                    title = finalTitle,
                                    content = quickAddContent,
                                    type = "NOTE",
                                    autoParse = false
                                )
                                quickAddTitle = ""
                                quickAddContent = ""
                                showQuickAddNote = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = quickAddContent.isNotBlank()
                    ) {
                        Text("Add to Context Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Step 2: Choose Prompt Task Presets
        Text(
            text = "Step 2: Selection Action Preset",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("PRIORITY_LIST", "Compile Priority List", Icons.Outlined.Checklist),
                Triple("EXECUTIVE_REPORT", "Executive Performance Report", Icons.Outlined.Assessment),
                Triple("SOMEDAY_GOALS", "Indefinite & Someday Goals", Icons.Default.AutoAwesome),
                Triple("CUSTOM", "Ask Custom Query...", Icons.Default.Edit)
            ).forEach { (preset, label, icon) ->
                val active = presetType == preset
                ElevatedFilterChip(
                    selected = active,
                    onClick = { presetType = preset },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(14.dp)) }
                )
            }
        }

        if (presetType == "CUSTOM") {
            OutlinedTextField(
                value = customPromptInput,
                onValueChange = { customPromptInput = it },
                label = { Text("What specific report/list question do you have?") },
                placeholder = { Text("e.g., Extract all learning topics mentioned and design a syllabus.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Step 3: Run Generation
        Button(
            onClick = {
                val selectedDocsList = documents.filter { selectedDocIds.contains(it.id) }
                viewModel.generateAiReport(
                    apiKey = customApiKey,
                    modelSource = modelSource,
                    modelName = modelName,
                    ollamaUrl = ollamaUrl,
                    selectedDocs = selectedDocsList,
                    presetType = presetType,
                    customPrompt = customPromptInput
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("ai_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            enabled = !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.Cyclone, contentDescription = "Run")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Personalized Copilot Report", fontWeight = FontWeight.Bold)
            }
        }

        // Result panel
        if (aiResultText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Report Output",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )

                        Surface(
                            shape = CircleShape,
                            color = if (isGenerating) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = if (modelSource == "OFFLINE_LOCAL") "OFFLINE" else if (modelSource == "OLLAMA") "OLLAMA" else "GEMINI",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Result Body
                    SelectionContainer {
                        Text(
                            text = aiResultText,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isGenerating) {
                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = reportSaveTitle,
                                onValueChange = { reportSaveTitle = it },
                                label = { Text("Report Document Title") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.saveResultAsDocument(reportSaveTitle, aiResultText)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Note", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Add Extra Action to Instantiate list items automatically!
                        Button(
                            onClick = {
                                viewModel.parseTextToTasks("AI Copilot Report", aiResultText)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlaylistAddCheck, contentDescription = "Taskify")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Extract & Save Items to Goals List", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
