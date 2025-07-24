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
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbarMain
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Cook Master"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    mainViewModel.setSearchQuery("")
                    mainViewModel.setFilter(MainViewModel.RecipeFilter.ALL)
                    Toast.makeText(this, "Главная", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_favorites -> {
                    mainViewModel.setFilter(MainViewModel.RecipeFilter.FAVORITES)
                    Toast.makeText(this, "Избранное", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_categories -> {
                    Toast.makeText(this, "Категории (в разработке)", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_share -> {
                    Toast.makeText(this, "Поиск (используйте иконку вверху)", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_send -> {
                    Toast.makeText(this, "Новые рецепты (в разработке)", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }

        val application = application as CookMasterApplication
        val viewModelFactory = MainViewModelFactory(application.repository)
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("RECIPE_ID", recipe.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { recipe ->
                mainViewModel.toggleFavoriteStatus(recipe)
            }
        )

        binding.recyclerViewRecipes.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = recipeAdapter
        }

        mainViewModel.filteredRecipes.observe(this, Observer { recipes ->
            recipes?.let {
                recipeAdapter.submitList(it)
                Log.d("MainActivity", "Recipes observed: ${it.size}")
            }
        })

        binding.fabAddRecipe.setOnClickListener {
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(binding.navView)) {
            drawerLayout.closeDrawer(binding.navView)
        } else {
            super.onBackPressed()
        }
    }
}