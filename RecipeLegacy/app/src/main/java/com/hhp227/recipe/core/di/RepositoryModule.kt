package com.hhp227.recipe.core.di

import com.hhp227.recipe.data.repository.RecipeRepositoryImpl
import com.hhp227.recipe.domain.repository.RecipeRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<RecipeRepository> {
        RecipeRepositoryImpl(get())
    }
}