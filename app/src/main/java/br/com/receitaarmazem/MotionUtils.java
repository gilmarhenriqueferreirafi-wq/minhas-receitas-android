package br.com.receitaarmazem;

import android.view.View;

final class MotionUtils {
    private MotionUtils() {
    }

    static void pulse(View view) {
        view.animate().cancel();
        view.setScaleX(0.96f);
        view.setScaleY(0.96f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start();
    }

    static void reveal(View view) {
        view.animate().cancel();
        view.setAlpha(0.15f);
        view.setTranslationX(24f);
        view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(260)
                .start();
    }
}
