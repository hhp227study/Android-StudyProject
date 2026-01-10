package com.hhp227.recipe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.recipe.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil.setContentView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }
}