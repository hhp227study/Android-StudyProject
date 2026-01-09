package com.hhp227.recipe.presentation.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class RecipeDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val recipeId: Int = savedStateHandle.get("recipeId") ?: 0
}