package com.hhp227.recipe

import android.app.Application
import com.hhp227.recipe.core.di.dataModule
import com.hhp227.recipe.core.di.repositoryModule
import com.hhp227.recipe.core.di.useCaseModule
import com.hhp227.recipe.core.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RecipeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@RecipeApplication)
            modules(
                viewModelModule,
                useCaseModule,
                repositoryModule,
                dataModule
            )
        }
    }
}