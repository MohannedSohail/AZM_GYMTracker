package com.example.azm_gymtracker.ui.util

import androidx.annotation.DrawableRes
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.MuscleCategory

/**
 * Maps each muscle category to its designated real-gym athlete illustration.
 */
@DrawableRes
fun MuscleCategory.toDrawableRes(): Int = when (this) {
    MuscleCategory.CHEST -> R.drawable.img_category_chest
    MuscleCategory.BACK -> R.drawable.img_category_back
    MuscleCategory.LEGS -> R.drawable.img_category_legs
    MuscleCategory.SHOULDERS -> R.drawable.img_category_shoulders
    MuscleCategory.ARMS -> R.drawable.img_category_arms
    MuscleCategory.CORE -> R.drawable.img_category_core
    MuscleCategory.CARDIO -> R.drawable.img_category_cardio
    MuscleCategory.OTHER -> R.drawable.img_category_other
}

/**
 * Safely resolves any string or enum category name to a drawable resource.
 */
@DrawableRes
fun getExerciseCategoryDrawable(categoryString: String?): Int {
    if (categoryString == null) return R.drawable.img_category_other
    return try {
        MuscleCategory.valueOf(categoryString.uppercase().trim()).toDrawableRes()
    } catch (_: Exception) {
        when {
            categoryString.contains("صدر", ignoreCase = true) || categoryString.contains("chest", ignoreCase = true) -> R.drawable.img_category_chest
            categoryString.contains("ظهر", ignoreCase = true) || categoryString.contains("back", ignoreCase = true) -> R.drawable.img_category_back
            categoryString.contains("رجل", ignoreCase = true) || categoryString.contains("ارجل", ignoreCase = true) || categoryString.contains("أرجل", ignoreCase = true) || categoryString.contains("leg", ignoreCase = true) -> R.drawable.img_category_legs
            categoryString.contains("كتف", ignoreCase = true) || categoryString.contains("أكتاف", ignoreCase = true) || categoryString.contains("shoulder", ignoreCase = true) -> R.drawable.img_category_shoulders
            categoryString.contains("ذراع", ignoreCase = true) || categoryString.contains("باي", ignoreCase = true) || categoryString.contains("تراي", ignoreCase = true) || categoryString.contains("arm", ignoreCase = true) -> R.drawable.img_category_arms
            categoryString.contains("بطن", ignoreCase = true) || categoryString.contains("وسط", ignoreCase = true) || categoryString.contains("core", ignoreCase = true) || categoryString.contains("abs", ignoreCase = true) -> R.drawable.img_category_core
            categoryString.contains("كارديو", ignoreCase = true) || categoryString.contains("cardio", ignoreCase = true) -> R.drawable.img_category_cardio
            else -> R.drawable.img_category_other
        }
    }
}

