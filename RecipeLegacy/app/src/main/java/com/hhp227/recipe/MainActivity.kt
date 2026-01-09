package com.hhp227.recipe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hhp227.recipe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    val binding get() = requireNotNull(_binding)

    private val viewModel: MainViewModel by viewModels()

    private val adapter = RecipeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        // activity_main.xml 레이아웃 설정
        setContentView(binding.root)

        // Adapter 및 LayoutManager 설정
        binding.recyclerViewRecipes.adapter = adapter.apply {
            setOnItemClickListener(object : RecipeAdapter.OnRecipeItemClickListener {
                override fun onDetailClick(recipe: Recipe) {
                    Intent(this@MainActivity, RecipeDetailActivity::class.java)
                        .putExtra("recipeId", recipe.id)
                        .also(::startActivity)
                }

                override fun onSaveClick(recipe: Recipe) {
                    Toast.makeText(this@MainActivity, "recipeid: ${recipe.id}", Toast.LENGTH_LONG).show()
                }
            })
        }

        subscribeUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun subscribeUi() {
        viewModel.recipe.observe(this) { recipes ->
            adapter.submitList(recipes)
        }
    }
}