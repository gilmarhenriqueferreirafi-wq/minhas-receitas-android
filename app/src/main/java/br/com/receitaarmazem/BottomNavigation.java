package br.com.receitaarmazem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.Button;

public final class BottomNavigation {
    private static final int ACCENT = Color.rgb(233, 75, 53);
    private static final int MUTED = Color.rgb(170, 165, 157);
    private BottomNavigation() { }

    public static void bind(Activity activity, int selected) {
        bindButton(activity, R.id.nav_home, selected == 0, () -> launch(activity, HomeActivity.class));
        bindButton(activity, R.id.nav_search, selected == 1, () -> openList(activity, RecipeListActivity.MODE_SEARCH));
        bindButton(activity, R.id.nav_favorites, selected == 2, () -> openList(activity, RecipeListActivity.MODE_FAVORITES));
        bindButton(activity, R.id.nav_categories, selected == 3, () -> launch(activity, CategoriesActivity.class));
        bindButton(activity, R.id.nav_settings, selected == 4, () -> launch(activity, SettingsActivity.class));
    }

    private static void bindButton(Activity activity, int id, boolean selected, Runnable action) {
        Button button = activity.findViewById(id);
        if (button == null) return;
        button.setTextColor(selected ? ACCENT : MUTED);
        button.setBackground(selected ? activeBackground() : activity.getResources().getDrawable(R.drawable.bg_surface));
        button.setOnClickListener(view -> { if (!selected) action.run(); });
    }

    private static GradientDrawable activeBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(45, 45, 45));
        background.setCornerRadius(8);
        return background;
    }

    private static void launch(Activity current, Class<?> target) {
        Intent intent = new Intent(current, target);
        // Mantém as abas já abertas no back stack para que a troca não recrie a tela.
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        current.startActivity(intent);
        current.overridePendingTransition(R.anim.app_slide_in_right, R.anim.app_slide_out_left);
    }

    private static void openList(Activity current, int mode) {
        Intent intent = new Intent(current, RecipeListActivity.class);
        intent.putExtra(RecipeListActivity.EXTRA_MODE, mode);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        current.startActivity(intent);
        current.overridePendingTransition(R.anim.app_slide_in_right, R.anim.app_slide_out_left);
    }

    public static void returnToHome(Activity current) {
        Intent intent = new Intent(current, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        current.startActivity(intent);
        current.overridePendingTransition(R.anim.app_slide_in_right, R.anim.app_slide_out_left);
    }
}
