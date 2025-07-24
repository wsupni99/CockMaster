// RecipeAdapter.kt
package com.example.myfirstkotlinapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myfirstkotlinapp.data.Recipe
import com.example.myfirstkotlinapp.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit // Убедитесь, что этот лямбда-функция передается
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding, onClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe)
    }

    class RecipeViewHolder(
        private val binding: ItemRecipeBinding,
        private val onClick: (Recipe) -> Unit,
        private val onFavoriteClick: (Recipe) -> Unit // Принимаем лямбду для обработки избранного
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.apply {
                textViewRecipeName.text = recipe.name
                textViewRecipeCategory.text = recipe.category
                textViewRecipeRating.text = String.format("%.1f", recipe.rating)

                imageViewRecipe.load(recipe.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_broken_image)
                }

                imageViewFavorite.setImageResource(
                    if (recipe.isFavorite) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_border
                )

                root.setOnClickListener { onClick(recipe) }
                // Вот здесь мы используем onFavoriteClick
                imageViewFavorite.setOnClickListener { onFavoriteClick(recipe) }
            }
        }
    }
}

class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem == newItem
    }
}