package br.com.receitaarmazem;

import android.view.View;

final class MotionUtils {
    private MotionUtils() {
    }

    static void pulse(View view) {
        view.animate().cancel();
        view.postDelayed(() -> {
            view.setScaleX(0.92f);
            view.setScaleY(0.92f);
            view.animate()
                    .scaleX(1.18f)
                    .scaleY(1.18f)
                    .setDuration(210)
                    .withEndAction(() -> view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(260)
                            .start())
                    .start();
        }, 100);
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
