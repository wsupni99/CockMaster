package com.example.myfirstkotlinapp.data

import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
    }

    fun getRecipeById(recipeId: String): Flow<Recipe?> {
        return recipeDao.getRecipeById(recipeId)
    }

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query)
    }

    suspend fun deleteAllRecipes() {
        recipeDao.deleteAllRecipes()
    }

    suspend fun getRecipeCount(): Int {
        return recipeDao.getRecipeCount()
    }
}