package com.hhp227.recipe.data.datasource

import com.hhp227.recipe.data.Recipe

interface RecipeDataSource {
    fun getRecipes(): List<Recipe>
}