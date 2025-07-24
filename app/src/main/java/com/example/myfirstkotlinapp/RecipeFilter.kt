package com.example.myfirstkotlinapp

sealed class RecipeFilter {
    object ALL : RecipeFilter()
    object FAVORITES : RecipeFilter()
    data class CATEGORY(val category: String) : RecipeFilter()
}