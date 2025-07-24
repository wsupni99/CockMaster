package com.example.myfirstkotlinapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.data.RecipeRepository
import kotlinx.coroutines.flow.map // Добавьте этот импорт
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
        // Объявите переменную для отслеживания текущего источника LiveData
        var currentSource: LiveData<List<Recipe>>? = null

        // Функция для обновления LiveData на основе текущего состояния
        fun updateSource() {
            // Удалите предыдущий источник, чтобы избежать утечек памяти и дублирования обновлений
            currentSource?.let { removeSource(it) }

            // Определите новый источник LiveData в зависимости от текущего фильтра и поискового запроса
            val newSource = when (_filter.value) {
                RecipeFilter.ALL -> {
                    if (_searchQuery.value.isNullOrEmpty()) {
                        repository.getAllRecipes().asLiveData()
                    } else {
                        repository.searchRecipes(_searchQuery.value!!).asLiveData()
                    }
                }
                RecipeFilter.FAVORITES -> {
                    if (_searchQuery.value.isNullOrEmpty()) {
                        // Используем новую функцию для получения избранных рецептов
                        repository.getFavoriteRecipes().asLiveData()
                    } else {
                        // Если поиск внутри избранных, сначала ищем, затем фильтруем
                        repository.searchRecipes(_searchQuery.value!!)
                            .map { list -> list.filter { it.isFavorite } }
                            .asLiveData()
                    }
                }
                is RecipeFilter.CATEGORY -> {
                    val category = (_filter.value as RecipeFilter.CATEGORY).category
                    if (_searchQuery.value.isNullOrEmpty()) {
                        repository.getAllRecipes()
                            .map { list -> list.filter { it.category == category } }
                            .asLiveData()
                    } else {
                        repository.searchRecipes(_searchQuery.value!!)
                            .map { list -> list.filter { it.category == category } }
                            .asLiveData()
                    }
                }
                // Добавьте else-ветку, если есть другие возможные состояния фильтра,
                // или используйте exhaustive when для предотвращения этой необходимости
                else -> repository.getAllRecipes().asLiveData()
            }
            // Установите новый источник и добавьте его
            currentSource = newSource
            addSource(newSource) { value = it }
        }

        // Добавляем источники для _searchQuery и _filter, чтобы вызывать updateSource при их изменении
        addSource(_searchQuery) { updateSource() }
        addSource(_filter) { updateSource() }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: RecipeFilter) {
        _filter.value = filter
    }

    // Вспомогательная функция для вставки тестовых данных
    private fun insertDummyRecipes() {
        viewModelScope.launch {
            // Вставляем только если база данных пуста
            if (repository.getRecipeCount() == 0) {
                val dummyRecipes = listOf(
                    Recipe(
                        id = UUID.randomUUID().toString(),
                        name = "Тыквенный суп-пюре",
                        imageUrl = "https://example.com/pumpkin_soup.jpg",
                        rating = 4.8f,
                        category = "Супы",
                        description = "Нежный и ароматный тыквенный суп-пюре, идеален для осени.",
                        ingredients = listOf(
                            "Тыква",
                            "Морковь",
                            "Лук",
                            "Чеснок",
                            "Имбирь",
                            "Овощной бульон",
                            "Сливки (по желанию)",
                            "Соль",
                            "Перец",
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

    fun toggleFavoriteStatus(recipe: Recipe) {
        viewModelScope.launch {
            val updatedRecipe = recipe.copy(isFavorite = !recipe.isFavorite)
            repository.updateRecipe(updatedRecipe)
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