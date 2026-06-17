package com.example.data

import kotlinx.coroutines.flow.Flow

class ListRepository(
    private val profileDao: ProfileDao,
    private val categoryDao: ListCategoryDao,
    private val itemDao: ListItemDao,
    private val documentDao: DocumentDao
) {
    val profile: Flow<Profile?> = profileDao.getProfile()
    val allCategories: Flow<List<ListCategory>> = categoryDao.getAllCategories()
    val allItems: Flow<List<ListItem>> = itemDao.getAllItems()
    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()

    suspend fun insertProfile(profile: Profile) = profileDao.insertProfile(profile)

    suspend fun insertCategory(category: ListCategory) = categoryDao.insertCategory(category)
    suspend fun insertCategories(categories: List<ListCategory>) = categoryDao.insertCategories(categories)
    suspend fun deleteCategory(category: ListCategory) = categoryDao.deleteCategory(category)

    fun getItemsByCategory(category: String): Flow<List<ListItem>> = itemDao.getItemsByCategory(category)
    suspend fun insertItem(item: ListItem) = itemDao.insertItem(item)
    suspend fun updateItem(item: ListItem) = itemDao.updateItem(item)
    suspend fun deleteItem(item: ListItem) = itemDao.deleteItem(item)
    suspend fun deleteItemById(id: Int) = itemDao.deleteItemById(id)

    // Document operations
    suspend fun insertDocument(document: Document) = documentDao.insertDocument(document)
    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)
    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)
    suspend fun deleteDocumentById(id: Int) = documentDao.deleteDocumentById(id)
}
