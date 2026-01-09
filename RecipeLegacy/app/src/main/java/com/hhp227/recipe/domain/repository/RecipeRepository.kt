package com.hhp227.recipe.domain.repository

import com.hhp227.recipe.data.Recipe

interface RecipeRepository {
    suspend fun getAllRecipes(): List<Recipe>
}