package com.pratik.iiits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val categoryRepository = CategoryRepository()
    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            val categoryList = categoryRepository.getCategories()
            _categories.postValue(categoryList)
        }
    }

    fun addCategory(category: String) {
        viewModelScope.launch {
            categoryRepository.addCategory(category)
            fetchCategories()  // Refresh categories after adding
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
            fetchCategories()  // Refresh categories after deleting
        }
    }
}
