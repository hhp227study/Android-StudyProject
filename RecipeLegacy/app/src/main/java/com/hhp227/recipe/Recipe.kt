package com.hhp227.recipe

data class Recipe(
    val id: Int,
    val title: String,
    val chefName: String,
    val cookTime: String,
    val rating: Double,
    val imageUrl: Int // 실제 프로젝트에서는 String(URL) 또는 Int(Drawable ID) 사용
)