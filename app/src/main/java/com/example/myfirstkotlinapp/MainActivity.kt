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
import com.google.android.material.dialog.MaterialAlertDialogBuilder // Импортируем для AlertDialog

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
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView: NavigationView = binding.navView
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    mainViewModel.setFilter(RecipeFilter.ALL)
                    mainViewModel.setSearchQuery("") // Очищаем поиск при переходе на главную
                    Toast.makeText(this, "Показаны все рецепты", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_favorites -> {
                    mainViewModel.setFilter(RecipeFilter.FAVORITES)
                    mainViewModel.setSearchQuery("") // Очищаем поиск при переходе в избранное
                    Toast.makeText(this, "Показаны Избранные рецепты", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_categories -> {
                    showCategorySelectionDialog() // Вызываем метод для показа диалога
                }
                R.id.nav_share -> {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Оцените приложение Cook Master для рецептов! [Ссылка на ваше приложение, если есть]")
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
                    Toast.makeText(this, "Поделиться", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_send -> {
                    val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:") // Только почтовые клиенты
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("daprosvirnin@kpfu.ru")) // Замените на свой email
                        putExtra(Intent.EXTRA_SUBJECT, "Отзыв о приложении Cook Master")
                        putExtra(Intent.EXTRA_TEXT, "Здравствуйте,\n\nПишу по поводу приложения Cook Master...")
                    }
                    if (sendIntent.resolveActivity(packageManager) != null) {
                        startActivity(sendIntent)
                    } else {
                        Toast.makeText(this, "Не найдено почтового приложения", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this, "Отправить", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers() // Закрываем выдвижное меню после выбора пункта
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
                Toast.makeText(this, "Статус избранного изменен", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewRecipes.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 1)
            adapter = recipeAdapter
        }

        mainViewModel.filteredRecipes.observe(this, Observer {
            it?.let {
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
            // Убедитесь, что `android.R.id.home` правильно обрабатывается,
            // если вы используете свою иконку или поведение гамбургер-меню
            android.R.id.home -> {
                if (drawerLayout.isDrawerOpen(binding.navView)) {
                    drawerLayout.closeDrawer(binding.navView)
                } else {
                    drawerLayout.openDrawer(binding.navView)
                }
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

    private fun showCategorySelectionDialog() {
        mainViewModel.allCategories.observe(this, Observer { categories ->
            // Убедимся, что обсервер удаляется после использования, чтобы избежать многократных вызовов
            // Это простой способ, для более сложных сценариев можно использовать removeObserver
            mainViewModel.allCategories.removeObservers(this)

            if (categories.isNullOrEmpty()) {
                Toast.makeText(this, "Категории не найдены.", Toast.LENGTH_SHORT).show()
                return@Observer
            }

            val categoryOptions = mutableListOf("Все категории").apply {
                addAll(categories.sorted()) // Сортируем категории для лучшего отображения
            }.toTypedArray()

            // Определяем текущую выбранную категорию для установки в диалоге
            // _filter является приватным, поэтому нужно добавить геттер или сделать его public/internal
            // Или использовать LiveData.value, чтобы получить текущее значение
            val currentFilter = mainViewModel.filteredRecipes.value?.firstOrNull()?.let {
                when {
                    it.isFavorite && mainViewModel.filteredRecipes.value?.all { r -> r.isFavorite } == true -> RecipeFilter.FAVORITES
                    it.category == it.category && mainViewModel.filteredRecipes.value?.all { r -> r.category == it.category } == true -> RecipeFilter.CATEGORY(it.category)
                    else -> RecipeFilter.ALL
                }
            } ?: RecipeFilter.ALL


            val currentCategoryName = if (currentFilter is RecipeFilter.CATEGORY) {
                currentFilter.category
            } else {
                "Все категории" // Если фильтр не категория, или ALL/FAVORITES, по умолчанию выбираем "Все категории"
            }

            val selectedCategoryIndex = categoryOptions.indexOf(currentCategoryName)

            MaterialAlertDialogBuilder(this)
                .setTitle("Выберите категорию")
                .setSingleChoiceItems(categoryOptions, selectedCategoryIndex) { dialog, which ->
                    val selectedCategory = if (which == 0) null else categoryOptions[which]
                    if (selectedCategory != null) {
                        mainViewModel.setFilter(RecipeFilter.CATEGORY(selectedCategory))
                        Toast.makeText(this, "Показаны рецепты категории: $selectedCategory", Toast.LENGTH_SHORT).show()
                    } else {
                        mainViewModel.setFilter(RecipeFilter.ALL) // Или RecipeFilter.ALL, если выбрано "Все категории"
                        Toast.makeText(this, "Показаны все рецепты", Toast.LENGTH_SHORT).show()
                    }
                    mainViewModel.setSearchQuery("") // Очищаем поиск при выборе категории
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        })
    }
}