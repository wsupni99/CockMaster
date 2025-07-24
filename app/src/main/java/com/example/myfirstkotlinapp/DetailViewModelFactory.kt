package com.example.myfirstkotlinapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myfirstkotlinapp.data.RecipeRepository

class DetailViewModelFactory(
    private val repository: RecipeRepository,
    private val recipeId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(repository, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}