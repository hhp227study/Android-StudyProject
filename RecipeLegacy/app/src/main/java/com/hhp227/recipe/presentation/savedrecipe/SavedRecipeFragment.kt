package com.hhp227.recipe.presentation.savedrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hhp227.recipe.data.Recipe
import com.hhp227.recipe.data.datasource.local.RecipeDataSourceImpl
import com.hhp227.recipe.data.repository.RecipeRepositoryImpl
import com.hhp227.recipe.databinding.FragmentSavedRecipeBinding
import com.hhp227.recipe.domain.usecase.GetRecipesUseCase

class SavedRecipeFragment : Fragment() {
    private var _binding: FragmentSavedRecipeBinding? = null
    val binding get() = requireNotNull(_binding)

    private val viewModel: SavedRecipeViewModel by viewModels {
        object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SavedRecipeViewModel(GetRecipesUseCase(RecipeRepositoryImpl(RecipeDataSourceImpl()))) as T
            }
        }
    }

    private val adapter = RecipeAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Adapter 및 LayoutManager 설정
        binding.recyclerViewRecipes.adapter = adapter.apply {
            setOnItemClickListener(object : RecipeAdapter.OnRecipeItemClickListener {
                override fun onDetailClick(recipe: Recipe) {
                    val direction = SavedRecipeFragmentDirections.actionSavedRecipeFragmentToRecipeDetailFragment(recipe.id)

                    findNavController().navigate(direction)
                }

                override fun onSaveClick(recipe: Recipe) {
                    Toast.makeText(requireContext(), "recipeid: ${recipe.id}", Toast.LENGTH_LONG).show()
                }
            })
        }

        subscribeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeUi() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.recipes)
        }
    }
}