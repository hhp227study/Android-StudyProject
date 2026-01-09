package com.hhp227.recipe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.recipe.databinding.ActivityRecipeDetailBinding

class RecipeDetailActivity : AppCompatActivity() {
    private var _binding: ActivityRecipeDetailBinding? = null
    val binding get() = requireNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvId.text = intent.getIntExtra("recipeId", 0).toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}