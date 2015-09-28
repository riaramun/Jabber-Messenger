package ru.rian.riamessenger.utils;

import android.graphics.drawable.Drawable;

/**
 * Created by hasegawa on 6/17/15.
 */
class DrawableUtils {
     static final int[] EMPTY_STATE = new int[] {};

    public static void clearState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(EMPTY_STATE);
        }
    }
}
