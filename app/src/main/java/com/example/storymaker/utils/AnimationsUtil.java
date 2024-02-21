package com.example.storymaker.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.storymaker.R;

public class AnimationsUtil {

    public static Animation SlideUpIn;
    public static Animation SlideUpOut;

    public static void initAnimationsValue(Context context) {
        SlideUpIn = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.slide_in_top);
        SlideUpOut = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.slide_out_top);
    }
}
