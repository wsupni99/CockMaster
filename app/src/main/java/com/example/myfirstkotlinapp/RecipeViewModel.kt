package com.example.myfirstkotlinapp

import androidx.lifecycle.ViewModel
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.data.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    val allRecipes = repository.getAllRecipes().flowOn(Dispatchers.IO)

    fun searchRecipes(query: String) =
        repository.searchRecipes(query).flowOn(Dispatchers.IO)

    suspend fun updateRecipe(recipe: Recipe) =
        withContext(Dispatchers.IO) { repository.updateRecipe(recipe) }
}