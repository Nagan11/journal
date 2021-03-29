package com.example.journal

import android.content.Context

class Calculation {
    companion object {
        fun dpToPx(dp: Float, context: Context): Int {
            return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
        }
    }
}