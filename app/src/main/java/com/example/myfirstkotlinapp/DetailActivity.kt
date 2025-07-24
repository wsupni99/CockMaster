package com.example.myfirstkotlinapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.myfirstkotlinapp.data.Recipe

class DetailActivity : AppCompatActivity() {

    private lateinit var detailViewModel: DetailViewModel
    private var currentRecipe: Recipe? = null
    private var favoriteMenuItem: MenuItem? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolbar: Toolbar = findViewById(R.id.detailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val detailRecipeImageView: ImageView = findViewById(R.id.detailRecipeImageView)
        val detailRecipeName: TextView = findViewById(R.id.detailRecipeName)
        val ingredientsList: TextView = findViewById(R.id.ingredientsList)
        val stepsList: TextView = findViewById(R.id.stepsList)
        val recipeCategory: TextView = findViewById(R.id.recipeCategory)
        val recipeRating: TextView = findViewById(R.id.recipeRating)
        val recipeDescription: TextView = findViewById(R.id.recipeDescription)


        val recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId == null) {
            Toast.makeText(this, "Ошибка: ID рецепта не передан.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val application = application as CookMasterApplication
        val viewModelFactory = DetailViewModelFactory(application.repository, recipeId)
        detailViewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)

        detailViewModel.recipe.observe(this, Observer { recipe ->
            if (recipe != null) {
                currentRecipe = recipe // Сохраняем текущий рецепт
                supportActionBar?.title = recipe.name
                detailRecipeName.text = recipe.name
                ingredientsList.text = "Ингредиенты:\n" + recipe.ingredients.joinToString("\n")
                stepsList.text = "Этапы приготовления:\n" + recipe.steps.joinToString("\n")
                recipeCategory.text = "Категория: ${recipe.category}"
                recipeRating.text = "Рейтинг: ${String.format("%.1f", recipe.rating)} / 5.0"
                recipeDescription.text = "Описание: ${recipe.description ?: "Нет описания."}"

                recipe.imageUrl?.let { url ->
                    detailRecipeImageView.load(url) {
                        placeholder(R.drawable.ic_image_placeholder)
                        error(R.drawable.ic_broken_image)
                    }
                }
                updateFavoriteIcon(recipe.isFavorite) // Обновляем иконку избранного
            } else {
                Toast.makeText(this, "Рецепт не найден.", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        favoriteMenuItem = menu?.findItem(R.id.action_favorite)
        currentRecipe?.let { updateFavoriteIcon(it.isFavorite) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                currentRecipe?.let {
                    detailViewModel.toggleFavoriteStatus(it)
                }
                true
            }
            R.id.action_edit -> {
                currentRecipe?.let { recipe ->
                    val intent = Intent(this, AddEditRecipeActivity::class.java).apply {
                        putExtra("RECIPE_ID", recipe.id)
                    }
                    startActivity(intent)
                }
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        favoriteMenuItem?.let {
            if (isFavorite) {
                it.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_filled)
            } else {
                it.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_border)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}