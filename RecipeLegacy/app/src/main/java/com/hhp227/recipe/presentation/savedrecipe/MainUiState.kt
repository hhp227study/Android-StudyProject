package com.hhp227.recipe.presentation.savedrecipe

import com.hhp227.recipe.data.Recipe

data class MainUiState(
    val isLoading: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val message: String? = null
)