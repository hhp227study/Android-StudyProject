package com.hhp227.recipe.core.di

import com.hhp227.recipe.data.datasource.RecipeDataSource
import com.hhp227.recipe.data.datasource.local.RecipeDataSourceImpl
import org.koin.dsl.module

val dataModule = module {
    single<RecipeDataSource> {
        RecipeDataSourceImpl()
    }
}