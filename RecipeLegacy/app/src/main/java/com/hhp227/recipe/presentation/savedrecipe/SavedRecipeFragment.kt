package com.hhp227.recipe.presentation.savedrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hhp227.recipe.data.Recipe
import com.hhp227.recipe.databinding.FragmentSavedRecipeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedRecipeFragment : Fragment() {
    private var _binding: FragmentSavedRecipeBinding? = null
    val binding get() = requireNotNull(_binding)

    private val viewModel: SavedRecipeViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // 데이터 세팅
        _binding = FragmentSavedRecipeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.recyclerViewRecipes.adapter = RecipeAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Adapter onItemClickListener 설정
        (binding.recyclerViewRecipes.adapter as RecipeAdapter)
            .setOnItemClickListener(object : RecipeAdapter.OnRecipeItemClickListener {
                override fun onDetailClick(recipe: Recipe) {
                    val direction = SavedRecipeFragmentDirections.actionSavedRecipeFragmentToRecipeDetailFragment(recipe.id)

                    findNavController().navigate(direction)
                }

                override fun onSaveClick(recipe: Recipe) {
                    Toast.makeText(requireContext(), "recipeid: ${recipe.id}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}