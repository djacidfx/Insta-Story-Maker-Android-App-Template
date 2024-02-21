package com.example.storymaker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;

public class DensityUtil {
    @SuppressLint("WrongConstant")
    public static int dp2px(Context context, float f) {
        return context == null ? (int) f : (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, f, context.getResources().getDisplayMetrics());
    }

    @SuppressLint("WrongConstant")
    public static int sp2px(Context context, float f) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, f, context.getResources().getDisplayMetrics());
    }

    public static float px2dp(Context context, float f) {
        return f / context.getResources().getDisplayMetrics().density;
    }

    public static float px2sp(Context context, float f) {
        return f / context.getResources().getDisplayMetrics().scaledDensity;
    }
}
