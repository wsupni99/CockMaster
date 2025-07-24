package com.example.myfirstkotlinapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.databinding.ActivityAddEditRecipeBinding
import java.util.UUID

class AddEditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditRecipeBinding
    private lateinit var viewModel: AddEditRecipeViewModel
    private var recipeId: String? = null
    private var currentRecipe: Recipe? = null // Объявление переменной currentRecipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.addEditToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val application = application as CookMasterApplication
        val factory = AddEditRecipeViewModelFactory(application.repository)
        viewModel = ViewModelProvider(this, factory).get(AddEditRecipeViewModel::class.java)

        recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId != null) {
            supportActionBar?.title = "Редактировать рецепт"
            viewModel.getRecipe(recipeId!!).observe(this) { recipe ->
                recipe?.let {
                    currentRecipe = it // Присвоение значения currentRecipe
                    fillFormWithRecipe(it)
                }
            }
        } else {
            supportActionBar?.title = "Добавить новый рецепт"
        }

        binding.buttonSaveRecipe.setOnClickListener {
            saveRecipe()
        }
    }

    private fun fillFormWithRecipe(recipe: Recipe) {
        binding.editTextRecipeName.setText(recipe.name)
        binding.editTextImageUrl.setText(recipe.imageUrl)
        binding.editTextRating.setText(recipe.rating.toString())
        binding.editTextCategory.setText(recipe.category)
        binding.editTextDescription.setText(recipe.description)
        binding.editTextIngredients.setText(recipe.ingredients.joinToString("\n"))
        binding.editTextSteps.setText(recipe.steps.joinToString("\n"))
    }

    private fun saveRecipe() {
        val name = binding.editTextRecipeName.text.toString().trim()
        val imageUrl = binding.editTextImageUrl.text.toString().trim()
        val ratingString = binding.editTextRating.text.toString().trim()
        val category = binding.editTextCategory.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val ingredientsText = binding.editTextIngredients.text.toString().trim()
        val stepsText = binding.editTextSteps.text.toString().trim()

        if (name.isEmpty() || ratingString.isEmpty() || category.isEmpty() || ingredientsText.isEmpty() || stepsText.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все обязательные поля.", Toast.LENGTH_SHORT).show()
            return
        }

        val rating = ratingString.toFloatOrNull()
        if (rating == null || rating < 0 || rating > 5) {
            Toast.makeText(this, "Рейтинг должен быть числом от 0.0 до 5.0", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredients = ingredientsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val steps = stepsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val newRecipe = Recipe(
            id = recipeId ?: UUID.randomUUID().toString(),
            name = name,
            imageUrl = if (imageUrl.isEmpty()) null else imageUrl,
            rating = rating,
            category = category,
            description = if (description.isEmpty()) null else description,
            ingredients = ingredients,
            steps = steps,
            isFavorite = currentRecipe?.isFavorite ?: false // Используем currentRecipe
        )

        viewModel.saveRecipe(newRecipe)
        Toast.makeText(this, "Рецепт сохранен!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}