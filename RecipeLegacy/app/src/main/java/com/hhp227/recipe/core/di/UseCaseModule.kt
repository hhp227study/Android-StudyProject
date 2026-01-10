package com.hhp227.recipe.core.di

import com.hhp227.recipe.domain.usecase.GetRecipesUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        GetRecipesUseCase(get())
    }
}