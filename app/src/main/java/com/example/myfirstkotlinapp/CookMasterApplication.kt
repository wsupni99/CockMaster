package com.example.myfirstkotlinapp

import android.app.Application
import com.example.myfirstkotlinapp.data.AppDatabase
import com.example.myfirstkotlinapp.data.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

class CookMasterApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository: RecipeRepository by lazy { RecipeRepository(database.recipeDao()) }
}