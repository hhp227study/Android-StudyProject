package com.hhp227.recipe.core.di

import com.hhp227.recipe.presentation.recipedetail.RecipeDetailViewModel
import com.hhp227.recipe.presentation.savedrecipe.SavedRecipeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SavedRecipeViewModel(get())
    }
    viewModel {
        RecipeDetailViewModel(get())
    }
}