package com.hhp227.recipe.presentation.savedrecipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.recipe.data.Recipe
import com.hhp227.recipe.databinding.ItemRecipeBinding

class RecipeAdapter : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(ItemDiffCallback()) {
    private lateinit var onItemClickListener: OnRecipeItemClickListener


    // XML 레이아웃에서 뷰 요소들을 연결하는 ViewHolder
    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipe = recipe

            binding.executePendingBindings()
        }

        init {
            binding.onItemClickListener = onItemClickListener
        }
    }

    // 1. ViewHolder 생성 (item_recipe.xml 인플레이션)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        return RecipeViewHolder(ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // 2. 데이터 바인딩
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)

        holder.bind(recipe)
    }

    fun setOnItemClickListener(listener: OnRecipeItemClickListener) {
        onItemClickListener = listener
    }

    interface OnRecipeItemClickListener {
        fun onDetailClick(recipe: Recipe)

        fun onSaveClick(recipe: Recipe)
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }
    }
}