package com.hhp227.recipe.domain.usecase

import com.hhp227.recipe.data.Recipe
import com.hhp227.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.runBlocking

class GetRecipesUseCase(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(): Result<List<Recipe>> {
        return runBlocking { runCatching { recipeRepository.getAllRecipes() } }
    }
}