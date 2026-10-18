package br.com.receitaarmazem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RecipeDetailActivity extends Activity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    private static final int BACKGROUND = Color.rgb(20, 20, 20);
    private static final int TEXT = Color.rgb(245, 245, 245);
    private static final int MUTED = Color.rgb(180, 180, 180);
    private static final int ACCENT = Color.rgb(255, 86, 66);
    private Recipe recipe;
    private RecipeStore store;
    private int servings;
    private LinearLayout ingredientsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String id = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        store = new RecipeStore(this);
        recipe = id == null ? store.getOrCreateSample() : store.findById(id);
        if (recipe == null) recipe = store.getOrCreateSample();
        Recipe accessed = store.recordAccess(recipe.id);
        if (accessed != null) recipe = accessed;
        servings = recipe.originalServings;
        setContentView(R.layout.activity_recipe_detail);
        bindXmlScreen();
    }

    private void bindXmlScreen() {
        Button back = findViewById(R.id.detail_back);
        Button favorite = findViewById(R.id.detail_favorite_top);
        Button minus = findViewById(R.id.detail_minus);
        Button plus = findViewById(R.id.detail_plus);
        Button cook = findViewById(R.id.detail_cook);
        Button edit = findViewById(R.id.detail_edit);
        TextView servingsView = findViewById(R.id.detail_servings);
        ImageView banner = findViewById(R.id.detail_banner);

        ((TextView) findViewById(R.id.detail_header_title)).setText(recipe.title);
        ((TextView) findViewById(R.id.detail_title)).setText(recipe.title);
        ((TextView) findViewById(R.id.detail_description)).setText(recipe.description);
        String category = recipe.category == null || recipe.category.trim().isEmpty()
                ? "Receita" : recipe.category.trim();
        ((TextView) findViewById(R.id.detail_meta)).setText(
                category + "  •  " + recipe.ingredients.size() + " ingredientes  •  "
                        + recipe.steps.size() + " etapas");
        servingsView.setText(String.valueOf(servings));
        favorite.setText(recipe.favorite ? "♥" : "♡");
        String bannerUri = recipe.bannerUri == null || recipe.bannerUri.isEmpty()
                ? recipe.photoUri : recipe.bannerUri;
        if (bannerUri != null && !bannerUri.isEmpty()) {
            Uri imageUri = Uri.parse(bannerUri);
            if ("file".equals(imageUri.getScheme())) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
                if (bitmap != null) banner.setImageBitmap(bitmap);
            } else {
                banner.setImageURI(imageUri);
            }
            banner.setVisibility(View.VISIBLE);
        }

        back.setOnClickListener(view -> finish());
        minus.setOnClickListener(view -> {
            servings = Math.max(1, servings - 1);
            servingsView.setText(String.valueOf(servings));
            fillIngredients((LinearLayout) findViewById(R.id.detail_ingredients));
        });
        plus.setOnClickListener(view -> {
            servings++;
            servingsView.setText(String.valueOf(servings));
            fillIngredients((LinearLayout) findViewById(R.id.detail_ingredients));
        });
        favorite.setOnClickListener(view -> {
            MotionUtils.pulse(favorite);
            recipe.favorite = !recipe.favorite;
            favorite.setText(recipe.favorite ? "♥" : "♡");
            saveCurrentRecipe();
        });
        cook.setOnClickListener(view -> {
            MotionUtils.pulse(cook);
            Intent intent = new Intent(this, CookingActivity.class);
            intent.putExtra(CookingActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });
        edit.setOnClickListener(view -> {
            Intent intent = new Intent(this, RecipeEditorActivity.class);
            intent.putExtra(RecipeEditorActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });

        fillIngredients((LinearLayout) findViewById(R.id.detail_ingredients));
        fillSteps((LinearLayout) findViewById(R.id.detail_steps));
    }

    private void fillIngredients(LinearLayout container) {
        container.removeAllViews();
        for (Ingredient ingredient : recipe.ingredients) {
            String quantity = recipe.scaledQuantity(ingredient.quantity, servings);
            String line = quantity.isEmpty() ? ingredient.name : quantity + " " + ingredient.unit + " " + ingredient.name;
            if (!ingredient.notes.isEmpty()) line += " (" + ingredient.notes + ")";
            container.addView(text("• " + line.trim(), 16, TEXT, false), matchWrap());
        }
    }

    private void fillSteps(LinearLayout container) {
        container.removeAllViews();
        for (RecipeStep step : recipe.steps) {
            String duration = step.durationSeconds > 0 ? "\n⏱ " + formatDuration(step.durationSeconds) : "";
            TextView item = text(step.order + "   " + step.text + duration, 16, TEXT, false);
            item.setPadding(0, 0, 0, 16);
            container.addView(item, matchWrap());
        }
    }

    private void saveCurrentRecipe() {
        List<Recipe> recipes = store.load();
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).id.equals(recipe.id)) recipes.set(i, recipe);
        }
        store.save(recipes);
    }

    private void confirmDeleteFromDetail() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir receita?")
                .setMessage("A receita será removida do aplicativo.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    List<Recipe> recipes = store.load();
                    for (int i = recipes.size() - 1; i >= 0; i--) {
                        if (recipes.get(i).id.equals(recipe.id)) recipes.remove(i);
                    }
                    store.save(recipes);
                    finish();
                })
                .show();
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(28, 24, 28, 40);
        scroll.addView(content);

        Button back = button("‹  Voltar", ACCENT);
        back.setOnClickListener(view -> finish());
        content.addView(back, matchWrap());

        String bannerUri = recipe.bannerUri == null || recipe.bannerUri.isEmpty()
                ? recipe.photoUri : recipe.bannerUri;
        if (bannerUri != null && !bannerUri.isEmpty()) {
            ImageView banner = new ImageView(this);
            Uri imageUri = Uri.parse(bannerUri);
            if ("file".equals(imageUri.getScheme())) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
                if (bitmap != null) banner.setImageBitmap(bitmap);
            } else {
                banner.setImageURI(imageUri);
            }
            banner.setScaleType(ImageView.ScaleType.CENTER_CROP);
            content.addView(banner, new LinearLayout.LayoutParams(-1, 260));
        }

        TextView title = text(recipe.title, 30, TEXT, true);
        title.setPadding(0, 28, 0, 8);
        content.addView(title, matchWrap());
        content.addView(text(recipe.description, 18, MUTED, false), matchWrap());

        LinearLayout servingsRow = new LinearLayout(this);
        servingsRow.setGravity(Gravity.CENTER_VERTICAL);
        servingsRow.setPadding(0, 24, 0, 16);
        servingsRow.addView(text("Porções", 18, TEXT, false), weightParams());
        Button minus = button("−", TEXT);
        Button count = button(String.valueOf(servings), TEXT);
        Button plus = button("+", TEXT);
        minus.setOnClickListener(view -> changeServings(-1, count));
        plus.setOnClickListener(view -> changeServings(1, count));
        servingsRow.addView(minus, buttonParams());
        servingsRow.addView(count, buttonParams());
        servingsRow.addView(plus, buttonParams());
        content.addView(servingsRow, matchWrap());

        Button cook = button("Começar a cozinhar", Color.WHITE);
        cook.setTextColor(Color.BLACK);
        cook.setOnClickListener(view -> {
            Intent intent = new Intent(this, CookingActivity.class);
            intent.putExtra(CookingActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });
        content.addView(cook, matchWrap());

        Button edit = button("Editar receita", ACCENT);
        edit.setOnClickListener(view -> {
            Intent intent = new Intent(this, RecipeEditorActivity.class);
            intent.putExtra(RecipeEditorActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });
        content.addView(edit, matchWrap());

        Button favorite = button(recipe.favorite ? "★ Remover dos favoritos" : "☆ Adicionar aos favoritos", ACCENT);
        favorite.setOnClickListener(view -> {
            MotionUtils.pulse(favorite);
            recipe.favorite = !recipe.favorite;
            List<Recipe> recipes = store.load();
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).id.equals(recipe.id)) recipes.set(i, recipe);
            }
            store.save(recipes);
            favorite.setText(recipe.favorite ? "★ Remover dos favoritos" : "☆ Adicionar aos favoritos");
        });
        content.addView(favorite, matchWrap());

        content.addView(sectionTitle("INGREDIENTES"), matchWrap());
        ingredientsContainer = new LinearLayout(this);
        ingredientsContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(ingredientsContainer, matchWrap());
        refreshIngredients();

        content.addView(sectionTitle("MODO DE PREPARO"), matchWrap());
        for (RecipeStep step : recipe.steps) {
            String duration = step.durationSeconds > 0
                    ? "\n⏱ " + formatDuration(step.durationSeconds)
                    : "";
            content.addView(text(step.order + "   " + step.text + duration, 17, TEXT, false), matchWrap());
        }
        return scroll;
    }

    private void changeServings(int amount, Button count) {
        servings = Math.max(1, servings + amount);
        count.setText(String.valueOf(servings));
        refreshIngredients();
    }

    private void refreshIngredients() {
        ingredientsContainer.removeAllViews();
        for (Ingredient ingredient : recipe.ingredients) {
            String quantity = recipe.scaledQuantity(ingredient.quantity, servings);
            String line = quantity.isEmpty() ? ingredient.name : quantity + " " + ingredient.unit + " " + ingredient.name;
            if (!ingredient.notes.isEmpty()) line += " (" + ingredient.notes + ")";
            ingredientsContainer.addView(text("• " + line.trim(), 17, TEXT, false), matchWrap());
        }
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        return minutes >= 60 ? (minutes / 60) + "h " + (minutes % 60) + "min" : minutes + " min";
    }

    private TextView sectionTitle(String value) {
        TextView view = text(value, 15, MUTED, true);
        view.setPadding(0, 34, 0, 14);
        return view;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setLineSpacing(4, 1.05f);
        if (bold) view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return view;
    }

    private Button button(String value, int color) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(color);
        button.setTextSize(16);
        button.setAllCaps(false);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams buttonParams() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    private LinearLayout.LayoutParams weightParams() {
        return new LinearLayout.LayoutParams(0, -2, 1);
    }
}
