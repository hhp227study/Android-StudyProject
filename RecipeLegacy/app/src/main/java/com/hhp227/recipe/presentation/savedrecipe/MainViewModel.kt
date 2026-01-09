package com.hhp227.recipe.presentation.savedrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hhp227.recipe.domain.usecase.GetRecipesUseCase

class MainViewModel(
    private val getRecipesUseCase: GetRecipesUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData(MainUiState())
    val uiState: LiveData<MainUiState> = _uiState

    private fun loadRecipes() {
        _uiState.postValue(uiState.value?.copy(isLoading = true))
        getRecipesUseCase()
            .onSuccess { recipes ->
                _uiState.postValue(
                    uiState.value?.copy(
                        isLoading = false,
                        recipes = recipes
                    )
                )
            }
            .onFailure {
                _uiState.postValue(
                    uiState.value?.copy(
                        isLoading = false,
                        message = it.message
                    )
                )
            }
    }
    init {
        loadRecipes()
    }
}