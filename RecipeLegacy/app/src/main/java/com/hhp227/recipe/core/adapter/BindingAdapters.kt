package com.hhp227.recipe.core.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("submitList")
fun submitList(v: RecyclerView, list: List<Nothing>) {
    (v.adapter as? ListAdapter<*, *>)?.submitList(list)
}