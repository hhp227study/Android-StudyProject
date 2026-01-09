package com.hhp227.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _recipes = MutableLiveData(generateDummyRecipes())
    val recipe: LiveData<List<Recipe>> = _recipes

    private fun generateDummyRecipes(): List<Recipe> {
        // 실제 프로젝트에서는 R.drawable.image_name 형태의 리소스 ID를 사용합니다.
        // 여기서는 임시 값 0을 사용했습니다.
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