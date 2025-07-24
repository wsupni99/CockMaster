package com.example.myfirstkotlinapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.data.RecipeRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: RecipeRepository, private val recipeId: String) : ViewModel() {

    val recipe: LiveData<Recipe?> = repository.getRecipeById(recipeId).asLiveData()

    fun toggleFavoriteStatus(recipe: Recipe) {
        viewModelScope.launch {
            val updatedRecipe = recipe.copy(isFavorite = !recipe.isFavorite)
            repository.updateRecipe(updatedRecipe)
        }
    }
}