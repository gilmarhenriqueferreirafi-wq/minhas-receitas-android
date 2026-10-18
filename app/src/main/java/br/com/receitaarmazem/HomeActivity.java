package br.com.receitaarmazem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends Activity {
    private static final int SURFACE = Color.rgb(32, 32, 32);
    private static final int TEXT = Color.rgb(245, 241, 235);
    private static final int MUTED = Color.rgb(170, 165, 157);
    private static final int ACCENT = Color.rgb(233, 75, 53);
    private RecipeStore store;
    private long lastBackPressed;
    private final List<FeaturedOption> featuredOptions = new ArrayList<>();
    private int featuredIndex;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new RecipeStore(this);
        showHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (store != null) showHome();
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBackPressed < 2000) {
            super.onBackPressed();
            return;
        }
        lastBackPressed = now;
        Toast.makeText(this, "Pressione voltar novamente para sair", Toast.LENGTH_SHORT).show();
    }

    private void showHome() {
        setContentView(R.layout.activity_home);
        Recipe featured = store.getMostAccessedOrCreateSample();
        List<Recipe> recipes = store.load();
        buildFeaturedOptions(recipes);

        TextView featuredTitle = findViewById(R.id.home_featured_title);
        TextView featuredDescription = findViewById(R.id.home_featured_description);
        ImageView featuredPhoto = findViewById(R.id.home_featured_photo);
        featuredPhoto.setBackgroundColor(Color.BLACK);
        String featuredUri = featured.photoUri == null || featured.photoUri.isEmpty()
                ? featured.bannerUri : featured.photoUri;
        if (featuredUri != null && !featuredUri.isEmpty()) {
            featuredPhoto.setImageURI(Uri.parse(featuredUri));
            featuredPhoto.setContentDescription("Foto de " + featured.title);
        }
        featuredTitle.setText(featured.title.isEmpty() ? "Sua próxima receita" : featured.title);
        featuredDescription.setText(featured.description.isEmpty()
                ? "Uma receita para preparar em casa."
                : featured.description);
        renderFeatured();
        View openFeatured = findViewById(R.id.home_featured_open);
        openFeatured.setOnClickListener(view -> openRecipe(featuredOptions.get(featuredIndex).recipe));
        findViewById(R.id.home_featured).setOnClickListener(view -> openRecipe(featuredOptions.get(featuredIndex).recipe));
        findViewById(R.id.home_featured_previous).setOnClickListener(view -> {
            featuredIndex = (featuredIndex - 1 + featuredOptions.size()) % featuredOptions.size();
            renderFeatured();
            animateFeaturedCard();
        });
        findViewById(R.id.home_featured_next).setOnClickListener(view -> {
            featuredIndex = (featuredIndex + 1) % featuredOptions.size();
            renderFeatured();
            animateFeaturedCard();
        });

        findViewById(R.id.home_add).setOnClickListener(view ->
                startActivity(new Intent(this, RecipeEditorActivity.class)));
        findViewById(R.id.home_all).setOnClickListener(view -> openAllRecipes());
        findViewById(R.id.home_settings).setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.nav_search).setOnClickListener(view -> openSearch());
        findViewById(R.id.nav_favorites).setOnClickListener(view -> openFavorites());
        findViewById(R.id.nav_categories).setOnClickListener(view ->
                startActivity(new Intent(this, CategoriesActivity.class)));
        findViewById(R.id.nav_settings).setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
        BottomNavigation.bind(this, 0);

        populateGrid(recipes);
    }

    private void buildFeaturedOptions(List<Recipe> recipes) {
        featuredOptions.clear();
        Recipe mostAccessed = highestAccess(recipes, false);
        if (mostAccessed != null) featuredOptions.add(new FeaturedOption(
                "🔥  MAIS ACESSADA", mostAccessed, "A receita que você mais visita."));
        Recipe favorite = highestAccess(recipes, true);
        if (favorite != null) featuredOptions.add(new FeaturedOption(
                "♥  FAVORITA MAIS ACESSADA", favorite, "Uma favorita que merece voltar ao menu."));
        String popularCategory = mostUsedCategory(recipes);
        if (popularCategory != null) {
            Recipe categoryRecipe = highestAccessInCategory(recipes, popularCategory);
            if (categoryRecipe != null) featuredOptions.add(new FeaturedOption(
                    "▦  CATEGORIA MAIS USADA", popularCategory, categoryRecipe,
                    "Sua categoria mais usada: " + popularCategory + "."));
        }
        Recipe recent = mostRecent(recipes);
        if (recent != null && (mostAccessed == null || !recent.id.equals(mostAccessed.id))) {
            featuredOptions.add(new FeaturedOption(
                    "✦  ADICIONADA RECENTEMENTE", recent, "Uma receita nova para experimentar."));
        }
        if (featuredOptions.isEmpty() && !recipes.isEmpty()) {
            featuredOptions.add(new FeaturedOption("✦  DESTAQUE", recipes.get(0), "Uma receita para preparar em casa."));
        }
        featuredIndex = 0;
    }

    private void renderFeatured() {
        if (featuredOptions.isEmpty()) return;
        FeaturedOption option = featuredOptions.get(featuredIndex);
        ((TextView) findViewById(R.id.home_featured_label)).setText(option.label);
        ((TextView) findViewById(R.id.home_featured_title)).setText(
                option.title.isEmpty() ? "Sua próxima receita" : option.title);
        ((TextView) findViewById(R.id.home_featured_description)).setText(
                option.recipe.description.isEmpty() ? option.description : option.recipe.description);
        ImageView photo = findViewById(R.id.home_featured_photo);
        photo.setImageDrawable(null);
        photo.setBackgroundColor(Color.BLACK);
        String uri = option.recipe.photoUri == null || option.recipe.photoUri.isEmpty()
                ? option.recipe.bannerUri : option.recipe.photoUri;
        if (uri != null && !uri.isEmpty()) photo.setImageURI(Uri.parse(uri));
        ((TextView) findViewById(R.id.home_featured_indicator)).setText(
                (featuredIndex + 1) + " / " + featuredOptions.size());
    }

    private void animateFeaturedCard() {
        View card = findViewById(R.id.home_featured);
        card.setAlpha(0.72f);
        card.setTranslationY(8f);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .start();
    }

    private Recipe highestAccess(List<Recipe> recipes, boolean favoriteOnly) {
        Recipe result = null;
        for (Recipe recipe : recipes) {
            if (favoriteOnly && !recipe.favorite) continue;
            if (result == null || recipe.accessCount > result.accessCount) result = recipe;
        }
        return result;
    }

    private Recipe mostRecent(List<Recipe> recipes) {
        Recipe result = null;
        for (Recipe recipe : recipes) if (result == null || recipe.updatedAt > result.updatedAt) result = recipe;
        return result;
    }

    private String mostUsedCategory(List<Recipe> recipes) {
        Map<String, Integer> counts = new HashMap<>();
        for (Recipe recipe : recipes) {
            if (!recipe.categories.isEmpty()) {
                for (String category : recipe.categories) addCategoryCount(counts, category);
            } else if (recipe.category != null && !recipe.category.trim().isEmpty()) {
                addCategoryCount(counts, recipe.category.trim());
            }
        }
        String result = null;
        int highest = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > highest) { highest = entry.getValue(); result = entry.getKey(); }
        }
        return result;
    }

    private void addCategoryCount(Map<String, Integer> counts, String category) {
        if (category == null || category.trim().isEmpty()) return;
        String value = category.trim();
        counts.put(value, counts.containsKey(value) ? counts.get(value) + 1 : 1);
    }

    private Recipe highestAccessInCategory(List<Recipe> recipes, String category) {
        Recipe result = null;
        for (Recipe recipe : recipes) {
            if (!recipe.hasCategory(category)) continue;
            if (result == null || recipe.accessCount > result.accessCount) result = recipe;
        }
        return result;
    }

    private static class FeaturedOption {
        final String label;
        final String title;
        final Recipe recipe;
        final String description;

        FeaturedOption(String label, Recipe recipe, String description) {
            this(label, recipe.title, recipe, description);
        }

        FeaturedOption(String label, String title, Recipe recipe, String description) {
            this.label = label;
            this.title = title;
            this.recipe = recipe;
            this.description = description;
        }
    }

    private void populateGrid(List<Recipe> recipes) {
        LinearLayout grid = findViewById(R.id.home_grid);
        grid.removeAllViews();
        for (int index = 0; index < recipes.size(); index += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            Recipe first = recipes.get(index);
            row.addView(recipeCard(first, index), new LinearLayout.LayoutParams(0, -2, 1));
            if (index + 1 < recipes.size()) {
                Recipe second = recipes.get(index + 1);
                LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(0, -2, 1);
                gap.setMargins(10, 0, 0, 0);
                row.addView(recipeCard(second, index + 1), gap);
            }
            grid.addView(row, params());
        }
    }

    private View recipeCard(Recipe recipe, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);
        card.setMinimumHeight(dp(128));
        GradientDrawable background = new GradientDrawable();
        background.setColor(SURFACE);
        background.setCornerRadius(dp(16));
        card.setBackground(background);
        ImageView photo = new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setBackgroundColor(Color.BLACK);
        if (recipe.photoUri != null && !recipe.photoUri.isEmpty()) {
            photo.setImageURI(Uri.parse(recipe.photoUri));
        }
        card.addView(photo, new LinearLayout.LayoutParams(-1, dp(72)));
        TextView title = textView(recipe.title.isEmpty() ? "Sem título" : recipe.title, 17, TEXT);
        title.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        title.setMaxLines(2);
        title.setPadding(0, 12, 0, 4);
        card.addView(title, params());
        String category = recipe.category.isEmpty() ? "Receita" : recipe.category;
        card.addView(textView("• " + category, 12, MUTED), params());
        card.setOnClickListener(view -> openRecipe(recipe));
        return card;
    }

    private TextView textView(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private void openRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(intent);
    }

    private void openAllRecipes() {
        startActivity(new Intent(this, RecipeListActivity.class));
    }

    private void openSearch() {
        Intent intent = new Intent(this, RecipeListActivity.class);
        intent.putExtra(RecipeListActivity.EXTRA_MODE, RecipeListActivity.MODE_SEARCH);
        startActivity(intent);
    }

    private void openFavorites() {
        Intent intent = new Intent(this, RecipeListActivity.class);
        intent.putExtra(RecipeListActivity.EXTRA_MODE, RecipeListActivity.MODE_FAVORITES);
        startActivity(intent);
    }

    private LinearLayout.LayoutParams params() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
