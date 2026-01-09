package com.hhp227.recipe.data.repository

import com.hhp227.recipe.data.Recipe
import com.hhp227.recipe.data.datasource.RecipeDataSource
import com.hhp227.recipe.domain.repository.RecipeRepository

class RecipeRepositoryImpl(
    private val recipeDataSource: RecipeDataSource
) : RecipeRepository
{
    override suspend fun getAllRecipes(): List<Recipe> {
        return recipeDataSource.getRecipes()
    }
}