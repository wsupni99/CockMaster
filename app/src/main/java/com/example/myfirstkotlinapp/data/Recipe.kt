package com.example.myfirstkotlinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val rating: Float,
    val category: String,
    val description: String?,
    val ingredients: List<String>,
    val steps: List<String>,
    val isFavorite: Boolean = false
)