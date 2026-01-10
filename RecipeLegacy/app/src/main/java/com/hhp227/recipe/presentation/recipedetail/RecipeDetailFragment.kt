package com.hhp227.recipe.presentation.recipedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.hhp227.recipe.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private val viewModel: RecipeDetailViewModel by viewModel()

    private var _binding: FragmentRecipeDetailBinding? = null
    val binding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}