package com.example.myfirstkotlinapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.data.RecipeRepository
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>()
    private val _filter = MutableLiveData<RecipeFilter>()

    init {
        _searchQuery.value = "" // Изначально пустой запрос
        _filter.value = RecipeFilter.ALL // Изначально показываем все рецепты
        insertDummyRecipes() // Вставляем тестовые рецепты при первом запуске ViewModel
    }

    // LiveData, которая комбинирует поисковый запрос и фильтр
    val filteredRecipes: LiveData<List<Recipe>> = MediatorLiveData<List<Recipe>>().apply {
        var currentSearchRecipes: LiveData<List<Recipe>>? = null
        var currentAllRecipes: LiveData<List<Recipe>>? = null
        var currentFavoriteRecipes: LiveData<List<Recipe>>? = null

        // Функция для обновления LiveData на основе текущего состояния
        fun updateValue() {
            val query = _searchQuery.value.orEmpty()
            val filter = _filter.value ?: RecipeFilter.ALL

            // Удаляем старые источники, чтобы избежать утечек и дублирования
            currentSearchRecipes?.let { removeSource(it) }
            currentAllRecipes?.let { removeSource(it) }
            currentFavoriteRecipes?.let { removeSource(it) }

            if (query.isNotEmpty()) {
                currentSearchRecipes = repository.searchRecipes(query).asLiveData()
                addSource(currentSearchRecipes!!) { recipes ->
                    value = if (filter == RecipeFilter.FAVORITES) {
                        recipes?.filter { it.isFavorite }
                    } else {
                        recipes
                    }
                }
            } else {
                when (filter) {
                    RecipeFilter.ALL -> {
                        currentAllRecipes = repository.getAllRecipes().asLiveData()
                        addSource(currentAllRecipes!!) { value = it }
                    }
                    RecipeFilter.FAVORITES -> {
                        currentFavoriteRecipes = repository.getAllRecipes().asLiveData().map { list ->
                            list.filter { it.isFavorite }
                        }
                        addSource(currentFavoriteRecipes!!) { value = it }
                    }
                }
            }
        }

        // Наблюдаем за изменениями поискового запроса и фильтра
        addSource(_searchQuery) { updateValue() }
        addSource(_filter) { updateValue() }
    }


    // Переключение статуса избранного
    fun toggleFavoriteStatus(recipe: Recipe) {
        viewModelScope.launch {
            val updatedRecipe = recipe.copy(isFavorite = !recipe.isFavorite)
            repository.updateRecipe(updatedRecipe)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: RecipeFilter) {
        _filter.value = filter
    }

    // Определение возможных фильтров
    enum class RecipeFilter {
        ALL, FAVORITES
    }


    private fun insertDummyRecipes() = viewModelScope.launch {
        if (repository.getRecipeCount() == 0) {
            val dummyRecipes = listOf(
                Recipe(
                    id = UUID.randomUUID().toString(),
                    name = "Кофе",
                    category = "Напитки",
                    rating = 4.0f,
                    imageUrl = "https://cdn.pixabay.com/photo/2017/05/12/08/29/coffee-2299883_1280.jpg",
                    description = "Ароматный утренний кофе.",
                    ingredients = listOf("Кофе молотый", "Вода", "Сахар"),
                    steps = listOf("Заварить кофе", "Добавить сахар"),
                    isFavorite = false
                ),
                Recipe(
                    id = UUID.randomUUID().toString(),
                    name = "Горячий шоколад",
                    category = "Напитки",
                    rating = 4.5f,
                    imageUrl = "https://cdn.pixabay.com/photo/2017/01/17/10/50/hot-chocolate-1986427_1280.jpg",
                    description = "Насыщенный горячий шоколад для уютных вечеров.",
                    ingredients = listOf("Какао порошок", "Молоко", "Сахар", "Шоколад"),
                    steps = listOf("Нагреть молоко", "Растворить какао", "Добавить сахар и шоколад"),
                    isFavorite = false
                ),
                Recipe(
                    id = UUID.randomUUID().toString(),
                    name = "Рис",
                    category = "Основные блюда",
                    rating = 3.0f,
                    imageUrl = "https://cdn.pixabay.com/photo/2017/06/07/19/08/rice-2381283_1280.jpg",
                    description = "Простой вареный рис как гарнир.",
                    ingredients = listOf("Рис 300 г", "Вода 700 мл", "Соль 1/4 ч.л."),
                    steps = listOf(
                        "Вскипятить воду 700 мл",
                        "Добавить Рис 300 г и Соль 1/4 ч.л.",
                        "Варить до готовности 25 мин"
                    ),
                    isFavorite = false
                ),
                Recipe(
                    id = UUID.randomUUID().toString(),
                    name = "Тыквенный суп",
                    category = "Супы",
                    rating = 4.7f,
                    imageUrl = "https://cdn.pixabay.com/photo/2016/01/29/01/24/pumpkin-soup-1166417_1280.jpg",
                    description = "Кремовый и ароматный суп из тыквы.",
                    ingredients = listOf(
                        "Тыква 500 г",
                        "Морковь 1 шт",
                        "Лук 1 шт",
                        "Чеснок 2 зубчика",
                        "Имбирь 1 см",
                        "Овощной бульон 500 мл",
                        "Сливки (по желанию) 100 мл",
                        "Соль, перец по вкусу",
                        "Оливковое масло"
                    ),
                    steps = listOf(
                        "1. Подготовка овощей: Тыкву очистить от кожуры и семян, нарезать кубиками. Морковь, лук и чеснок также очистить и нарезать крупно. Имбирь натереть на мелкой терке.",
                        "2. Обжарка: В кастрюле с толстым дном разогреть оливковое масло. Обжарить лук до мягкости, затем добавить морковь, тыкву, чеснок и имбирь. Обжаривать, помешивая, 5-7 минут.",
                        "3. Варка: Влить овощной бульон, довести до кипения. Уменьшить огонь, накрыть крышкой и варить 15-20 минут, пока овощи не станут очень мягкими.",
                        "4. Пюрирование: Снять кастрюлю с огня. Измельчить суп погружным блендером до однородной консистенции. При необходимости, протереть через сито для большей гладкости.",
                        "5. Завершение: Вернуть суп на плиту. Добавить сливки (если используете), соль и перец по вкусу. Прогреть, не доводя до кипения. Если суп слишком густой, можно добавить еще немного бульона или воды.",
                        "6. Подача: Подавать горячим, украсив зеленью или тыквенными семечками."
                    ),
                    isFavorite = true // Сделаем один избранным по умолчанию
                )
            )
            dummyRecipes.forEach { recipe ->
                repository.insertRecipe(recipe)
            }
        }
    }
}

class MainViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}