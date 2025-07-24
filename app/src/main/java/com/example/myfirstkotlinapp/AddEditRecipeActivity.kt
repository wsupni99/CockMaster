package com.example.myfirstkotlinapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.myfirstkotlinapp.data.Recipe
// import com.example.myfirstkotlinapp.databinding.ActivityAddEditRecipeBinding // Удаляем эту строку
import java.util.UUID

class AddEditRecipeActivity : AppCompatActivity() {

    // private lateinit var binding: ActivityAddEditRecipeBinding // Удаляем это объявление
    private lateinit var viewModel: AddEditRecipeViewModel
    private var recipeId: String? = null
    private var currentRecipe: Recipe? = null

    // Объявляем переменные для Views, которые будут найдены через findViewById
    private lateinit var editTextRecipeName: EditText
    private lateinit var editTextImageUrl: EditText
    private lateinit var editTextRating: EditText
    private lateinit var editTextCategory: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextIngredients: EditText
    private lateinit var editTextSteps: EditText
    private lateinit var buttonSaveRecipe: Button
    private lateinit var imageRecipePreview: ImageView
    private lateinit var addEditToolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_recipe) // Устанавливаем макет

        // Инициализируем Views через findViewById
        addEditToolbar = findViewById(R.id.addEditToolbar)
        editTextRecipeName = findViewById(R.id.editTextRecipeName)
        editTextImageUrl = findViewById(R.id.editTextImageUrl)
        editTextRating = findViewById(R.id.editTextRating)
        editTextCategory = findViewById(R.id.editTextCategory)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextIngredients = findViewById(R.id.editTextIngredients)
        editTextSteps = findViewById(R.id.editTextSteps)
        buttonSaveRecipe = findViewById(R.id.buttonSaveRecipe)
        imageRecipePreview = findViewById(R.id.imageRecipePreview)


        setSupportActionBar(addEditToolbar)
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
                    currentRecipe = it
                    editTextRecipeName.setText(it.name)
                    editTextImageUrl.setText(it.imageUrl)
                    editTextRating.setText(it.rating.toString())
                    editTextCategory.setText(it.category)
                    editTextDescription.setText(it.description)
                    editTextIngredients.setText(it.ingredients.joinToString("\n"))
                    editTextSteps.setText(it.steps.joinToString("\n"))
                    if (!it.imageUrl.isNullOrEmpty()) {
                        imageRecipePreview.load(it.imageUrl)
                    }
                }
            }
        } else {
            supportActionBar?.title = "Добавить новый рецепт"
        }

        buttonSaveRecipe.setOnClickListener {
            saveRecipe()
        }
    }

    private fun saveRecipe() {
        val name = editTextRecipeName.text.toString().trim()
        val imageUrl = editTextImageUrl.text.toString().trim()
        val ratingString = editTextRating.text.toString().trim()
        val category = editTextCategory.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val ingredientsText = editTextIngredients.text.toString().trim()
        val stepsText = editTextSteps.text.toString().trim()

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
            isFavorite = currentRecipe?.isFavorite ?: false
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