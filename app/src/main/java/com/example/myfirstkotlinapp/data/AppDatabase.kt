package com.example.myfirstkotlinapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_database"
                )
                    .addCallback(RecipeDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class RecipeDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        // Эта функция insertDummyRecipes() теперь вызывается из MainViewModel
                        // Так что здесь ее можно не дублировать, если MainViewModel уже это делает.
                        // Если вы хотите, чтобы база данных была заполнена при создании,
                        // даже если MainViewModel не инициализируется сразу (например, для тестов),
                        // то можно оставить, но убедитесь, что нет дублирования.
                        // Для этого проекта, где MainViewModel управляет начальным заполнением,
                        // этот блок можно закомментировать или удалить,
                        // чтобы избежать повторной вставки при каждом создании базы данных
                        // после переустановки приложения.
                        // database.recipeDao().insertRecipe(
                        //    Recipe(
                        //        id = UUID.randomUUID().toString(),
                        //        name = "Тыквенный суп",
                        //        category = "Супы",
                        //        rating = 4.7f,
                        //        imageUrl = "https://cdn.pixabay.com/photo/2016/01/29/01/24/pumpkin-soup-1166417_1280.jpg",
                        //        description = "Кремовый и ароматный суп из тыквы.",
                        //        ingredients = listOf(
                        //            "Тыква 500 г",
                        //            "Морковь 1 шт",
                        //            "Лук 1 шт",
                        //            "Чеснок 2 зубчика",
                        //            "Имбирь 1 см",
                        //            "Овощной бульон 500 мл",
                        //            "Сливки (по желанию) 100 мл",
                        //            "Соль, перец по вкусу",
                        //            "Оливковое масло"
                        //        ),
                        //        steps = listOf(
                        //            "1. Подготовка овощей: Тыкву очистить от кожуры и семян, нарезать кубиками. Морковь, лук и чеснок также очистить и нарезать крупно. Имбирь натереть на мелкой терке.",
                        //            "2. Обжарка: В кастрюле с толстым дном разогреть оливковое масло. Обжарить лук до мягкости, затем добавить морковь, тыкву, чеснок и имбирь. Обжаривать, помешивая, 5-7 минут.",
                        //            "3. Варка: Влить овощной бульон, довести до кипения. Уменьшить огонь, накрыть крышкой и варить 15-20 минут, пока овощи не станут очень мягкими.",
                        //            "4. Пюрирование: Снять кастрюлю с огня. Измельчить суп погружным блендером до однородной консистенции. При необходимости, протереть через сито для большей гладкости.",
                        //            "5. Завершение: Вернуть суп на плиту. Добавить сливки (если используете), соль и перец по вкусу. Прогреть, не доводя до кипения. Если суп слишком густой, можно добавить еще немного бульона или воды.",
                        //            "6. Подача: Подавать горячим, украсив зеленью или тыквенными семечками."
                        //        )
                        //    )
                        // )
                    }
                }
            }
        }
    }
}