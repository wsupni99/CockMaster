package com.example.myfirstkotlinapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
// import com.example.myfirstkotlinapp.databinding.ActivityMainBinding // Удаляем эту строку
import com.example.myfirstkotlinapp.data.Recipe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    // private lateinit var binding: ActivityMainBinding // Удаляем это объявление
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var drawerLayout: DrawerLayout

    // Объявляем переменные для Views, которые будут найдены через findViewById
    private lateinit var toolbarMain: Toolbar
    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var fabAddRecipe: FloatingActionButton
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Устанавливаем макет

        // Инициализируем Views через findViewById
        toolbarMain = findViewById(R.id.toolbarMain)
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes)
        fabAddRecipe = findViewById(R.id.fab_add_recipe)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        setSupportActionBar(toolbarMain)
        supportActionBar?.title = "Cook Master"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbarMain,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    mainViewModel.setFilter(RecipeFilter.ALL)
                }
                R.id.nav_favorites -> {
                    mainViewModel.setFilter(RecipeFilter.FAVORITES)
                }
                R.id.nav_categories -> {
                    // TODO: Implement category selection logic
                    Toast.makeText(this, "Выбраны Категории", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_share -> {
                    Toast.makeText(this, "Выбраны Поиск", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_send -> {
                    Toast.makeText(this, "Выбраны Новые рецепты", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val application = application as CookMasterApplication
        val factory = MainViewModelFactory(application.repository)
        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("RECIPE_ID", recipe.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { recipe ->
                mainViewModel.toggleFavoriteStatus(recipe)
                Toast.makeText(this, "Избранное обновлено", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerViewRecipes.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = recipeAdapter
        }

        mainViewModel.filteredRecipes.observe(this, Observer {
            it?.let {
                recipeAdapter.submitList(it)
                Log.d("MainActivity", "Recipes observed: ${it.size}")
            }
        })

        fabAddRecipe.setOnClickListener {
            val intent = Intent(this, AddEditRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mainViewModel.setSearchQuery(query.orEmpty())
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mainViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            android.R.id.home -> { // Обработка нажатия на кнопку "назад" в тулбаре (гамбургер)
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    drawerLayout.openDrawer(navView) // Открыть Navigation Drawer
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}