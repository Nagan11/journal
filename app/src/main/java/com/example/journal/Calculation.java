package com.example.journal;

import android.content.Context;

public class Calculation {
    public static int dpToPx(float dp, Context context) {
        return (int)(dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}