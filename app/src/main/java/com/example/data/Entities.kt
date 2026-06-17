package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 0,
    val userName: String = "",
    val priorityTopic: String = "Focus", // Focus, Work, Health, Leisure, Learning
    val pace: String = "Balanced", // Busy, Balanced, Relaxed
    val preferredType: String = "Tasks", // Tasks, Movies, Books, Travels, General
    val isOnboardingCompleted: Boolean = false
)

@Entity(tableName = "list_categories")
data class ListCategory(
    @PrimaryKey val name: String,
    val isSystem: Boolean = false,
    val iconName: String = "list" // "list", "movie", "book", "travel", "task"
)

@Entity(tableName = "list_items")
data class ListItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g., "Tasks To Do", "Movies/Series To Watch"
    val status: String = "WANNA_DO", // "WANNA_DO", "DID_IT"
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val timeframe: String = "DAY", // "DAY", "WEEK", "MONTH", "YEAR", "ANYTIME"
    val notes: String = "",
    val timestampCreated: Long = System.currentTimeMillis(),
    val timestampCompleted: Long? = null,
    val lifeArea: String = "Personal Growth" // "Health", "Career", "Personal Growth", "Leisure", "Finance", "General"
)

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String = "DOCUMENT", // "DOCUMENT", "SNIPPET", "NOTE", "FILE"
    val timestampCreated: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val fileSize: String? = null,
    val fileMimeType: String? = null
)
