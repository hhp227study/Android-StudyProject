package com.hhp227.recipe.data.datasource.local

import com.hhp227.recipe.data.Recipe
import com.hhp227.recipe.data.datasource.RecipeDataSource

class RecipeDataSourceImpl : RecipeDataSource {
    override fun getRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = 0,
                title = "Traditional spare ribs baked",
                chefName = "Chef John",
                cookTime = "20 min",
                rating = 4.0,
                imageUrl = 0
            ),
            Recipe(
                id = 1,
                title = "Spice roasted chicken with flavored rice",
                chefName = "Mark Kelvin",
                cookTime = "20 min",
                rating = 4.0,
                imageUrl = 0
            ),
            Recipe(
                id = 2,
                title = "Spicy fried rice mix chicken bali",
                chefName = "Spicy Nelly",
                cookTime = "20 min",
                rating = 4.0,
                imageUrl = 0
            ),
            Recipe(
                id = 3,
                title = "Lamb chops with fruity",
                chefName = "Chef Smith",
                cookTime = "20 min",
                rating = 3.0,
                imageUrl = 0
            )
        )
    }
}