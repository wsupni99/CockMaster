package com.example.myfirstkotlinapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.data.RecipeRepository
import kotlinx.coroutines.launch

class AddEditRecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    fun getRecipe(recipeId: String): LiveData<Recipe?> {
        return repository.getRecipeById(recipeId).asLiveData()
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.insertRecipe(recipe) // insertRecipe будет работать как upsert благодаря OnConflictStrategy.REPLACE
        }
    }
}

class AddEditRecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditRecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}