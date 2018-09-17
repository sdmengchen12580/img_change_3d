package com.bihucj.mcandroid.utils;

import android.content.Context;

/**
 * Created by User on 2017/2/21.
 */

public final class DensityUtil {

    public static int dip2px(final Context context, final float n) {
        return (int) (n * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int px2dip(final Context context, final float n) {
        return (int) (n / context.getResources().getDisplayMetrics().density);
    }

    public static float px2sp(final Context context, final float n) {
        return (n / context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static float sp2px(final Context context, final float n) {
        return (n * context.getResources().getDisplayMetrics().scaledDensity);
    }
}

