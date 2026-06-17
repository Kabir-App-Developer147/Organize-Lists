package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0")
    fun getProfile(): Flow<Profile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)
}

@Dao
interface ListCategoryDao {
    @Query("SELECT * FROM list_categories")
    fun getAllCategories(): Flow<List<ListCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ListCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ListCategory>)

    @Delete
    suspend fun deleteCategory(category: ListCategory)
}

@Dao
interface ListItemDao {
    @Query("SELECT * FROM list_items ORDER BY timestampCreated DESC")
    fun getAllItems(): Flow<List<ListItem>>

    @Query("SELECT * FROM list_items WHERE category = :category ORDER BY timestampCreated DESC")
    fun getItemsByCategory(category: String): Flow<List<ListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ListItem>)

    @Update
    suspend fun updateItem(item: ListItem)

    @Delete
    suspend fun deleteItem(item: ListItem)

    @Query("DELETE FROM list_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestampCreated DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<Document>)

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)
}
