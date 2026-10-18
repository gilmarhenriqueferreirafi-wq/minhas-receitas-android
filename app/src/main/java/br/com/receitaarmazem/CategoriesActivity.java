package br.com.receitaarmazem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.view.View;
import android.view.Gravity;
import android.graphics.Typeface;
import android.net.Uri;
import android.widget.ImageView;

import java.util.List;

public class CategoriesActivity extends Activity {
    private LinearLayout categoryContainer;
    private LinearLayout recipeContainer;
    private LinearLayout pickerContainer;
    private View listScreen;
    private View detailScreen;
    private View pickerScreen;
    private View mainHeader;
    private String selectedCategory;
    private List<Recipe> pickerRecipes;
    private final List<CheckBox> pickerChecks = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_categories);
        categoryContainer = findViewById(R.id.category_container);
        recipeContainer = findViewById(R.id.category_recipe_container);
        pickerContainer = findViewById(R.id.category_picker_container);
        listScreen = findViewById(R.id.category_list_screen);
        detailScreen = findViewById(R.id.category_detail_screen);
        pickerScreen = findViewById(R.id.category_picker_screen);
        mainHeader = findViewById(R.id.categories_main_header);
        findViewById(R.id.categories_add).setOnClickListener(view -> createCategory());
        findViewById(R.id.category_detail_back).setOnClickListener(view -> showCategoryList());
        findViewById(R.id.category_detail_add).setOnClickListener(view -> showRecipePicker());
        findViewById(R.id.category_picker_back).setOnClickListener(view -> showCategoryDetail());
        findViewById(R.id.category_picker_save).setOnClickListener(view -> saveRecipeSelection());
        BottomNavigation.bind(this, 3);
        populateCategories();
    }

    private void populateCategories() {
        categoryContainer.removeAllViews();
        RecipeStore store = new RecipeStore(this);
        List<String> categories = store.loadCategories();
        List<Recipe> allRecipes = store.load();
        for (String category : categories) {
            String categoryValue = category;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(10), dp(10), dp(10));
            row.setBackgroundResource(R.drawable.bg_category_row);
            ImageView cover = new ImageView(this);
            cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            cover.setBackgroundColor(Color.BLACK);
            Recipe representative = firstRecipeInCategory(allRecipes, categoryValue);
            if (representative != null) loadRecipeImage(cover, representative);
            row.addView(cover, new LinearLayout.LayoutParams(dp(58), dp(58)));
            LinearLayout textBlock = new LinearLayout(this);
            textBlock.setOrientation(LinearLayout.VERTICAL);
            textBlock.setGravity(Gravity.CENTER_VERTICAL);
            textBlock.setPadding(dp(14), 0, dp(8), 0);
            TextView title = new TextView(this);
            title.setText(category);
            title.setTextColor(Color.WHITE);
            title.setTextSize(17);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            title.setMaxLines(1);
            TextView summary = new TextView(this);
            int recipeCount = countRecipesInCategory(allRecipes, categoryValue);
            summary.setText(recipeCount == 1 ? "1 receita" : recipeCount + " receitas");
            summary.setTextColor(Color.rgb(170, 165, 157));
            summary.setTextSize(13);
            LinearLayout.LayoutParams summaryParams = params();
            summaryParams.topMargin = dp(3);
            textBlock.addView(title, params());
            textBlock.addView(summary, summaryParams);
            textBlock.setOnClickListener(view -> openCategory(categoryValue));
            row.addView(textBlock, new LinearLayout.LayoutParams(0, -1, 1));
            TextView arrow = new TextView(this);
            arrow.setText(">");
            arrow.setTextColor(Color.rgb(233, 75, 53));
            arrow.setTextSize(22);
            arrow.setGravity(Gravity.CENTER);
            arrow.setOnClickListener(view -> openCategory(categoryValue));
            row.addView(arrow, new LinearLayout.LayoutParams(dp(28), dp(58)));
            Button manage = new Button(this);
            manage.setBackgroundResource(R.drawable.bg_category_action);
            manage.setTextColor(Color.rgb(245, 241, 235));
            manage.setText("⋮");
            manage.setAllCaps(false);
            manage.setTextSize(16);
            manage.setPadding(0, 0, 0, dp(4));
            manage.setOnClickListener(view -> manageCategory(categoryValue, category));
            row.addView(manage, new LinearLayout.LayoutParams(dp(42), dp(42)));
            LinearLayout.LayoutParams rowParams = params();
            rowParams.setMargins(0, 0, 0, dp(10));
            categoryContainer.addView(row, rowParams);
        }
        if (categories.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Ainda não existem categorias.");
            empty.setTextColor(Color.LTGRAY);
            empty.setTextSize(18);
            categoryContainer.addView(empty, params());
        }
    }

    private void createCategory() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Nome da categoria");
        new AlertDialog.Builder(this).setTitle("Nova categoria").setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty() && new RecipeStore(this).addCategory(name)) populateCategories();
                }).show();
    }

    private void addRecipesToCategory(String category) {
        RecipeStore store = new RecipeStore(this);
        List<Recipe> recipes = store.load();
        if (recipes.isEmpty()) {
            Toast.makeText(this, "Cadastre uma receita primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] titles = new String[recipes.size()];
        boolean[] checked = new boolean[recipes.size()];
        for (int i = 0; i < recipes.size(); i++) {
            titles[i] = recipes.get(i).title.isEmpty() ? "Receita sem título" : recipes.get(i).title;
            checked[i] = recipes.get(i).hasCategory(category);
        }
        new AlertDialog.Builder(this).setTitle("Receitas em " + category)
                .setMultiChoiceItems(titles, checked, (dialog, which, selected) -> checked[which] = selected)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    for (int i = 0; i < recipes.size(); i++) {
                        if (checked[i]) recipes.get(i).addCategory(category);
                        else recipes.get(i).removeCategory(category);
                    }
                    if (store.save(recipes)) {
                        Toast.makeText(this, "Receitas da categoria atualizadas.", Toast.LENGTH_SHORT).show();
                        populateCategories();
                    }
                }).show();
    }

    private void openCategory(String category) {
        selectedCategory = category;
        showCategoryDetail();
    }

    private void showCategoryList() {
        selectedCategory = null;
        mainHeader.setVisibility(View.VISIBLE);
        listScreen.setVisibility(View.VISIBLE);
        detailScreen.setVisibility(View.GONE);
        pickerScreen.setVisibility(View.GONE);
        populateCategories();
    }

    private void showCategoryDetail() {
        if (selectedCategory == null) return;
        mainHeader.setVisibility(View.GONE);
        listScreen.setVisibility(View.GONE);
        detailScreen.setVisibility(View.VISIBLE);
        pickerScreen.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.category_detail_title)).setText(selectedCategory);
        recipeContainer.removeAllViews();
        int count = 0;
        for (Recipe recipe : new RecipeStore(this).load()) {
            if (!recipe.hasCategory(selectedCategory)) continue;
            LinearLayout.LayoutParams cardParams = params();
            cardParams.setMargins(0, 0, 0, dp(10));
            recipeContainer.addView(categoryRecipeCard(recipe), cardParams);
            count++;
        }
        ((TextView) findViewById(R.id.category_detail_summary)).setText(
                count == 1 ? "1 receita nesta categoria" : count + " receitas nesta categoria");
        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("Esta categoria ainda está vazia. Toque no + para adicionar receitas.");
            empty.setTextColor(Color.LTGRAY);
            empty.setTextSize(16);
            empty.setPadding(dp(16), dp(20), dp(16), dp(20));
            empty.setBackgroundResource(R.drawable.bg_surface);
            recipeContainer.addView(empty, params());
        }
    }

    private View categoryRecipeCard(Recipe recipe) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(10), dp(14), dp(10));
        card.setBackgroundResource(R.drawable.bg_surface);
        ImageView photo = new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setBackgroundColor(Color.BLACK);
        loadRecipeImage(photo, recipe);
        card.addView(photo, new LinearLayout.LayoutParams(dp(76), dp(76)));
        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);
        textBlock.setPadding(dp(14), 0, 0, 0);
        TextView title = new TextView(this);
        title.setText(recipe.title.isEmpty() ? "Receita sem título" : recipe.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textBlock.addView(title, params());
        if (recipe.description != null && !recipe.description.trim().isEmpty()) {
            TextView description = new TextView(this);
            description.setText(recipe.description);
            description.setTextColor(Color.LTGRAY);
            description.setTextSize(14);
            description.setMaxLines(2);
            LinearLayout.LayoutParams descriptionParams = params();
            descriptionParams.topMargin = dp(5);
            textBlock.addView(description, descriptionParams);
        }
        card.addView(textBlock, new LinearLayout.LayoutParams(0, -2, 1));
        card.setOnClickListener(view -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });
        return card;
    }

    private void showRecipePicker() {
        if (selectedCategory == null) return;
        pickerRecipes = new RecipeStore(this).load();
        if (pickerRecipes.isEmpty()) {
            Toast.makeText(this, "Cadastre uma receita primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }
        pickerContainer.removeAllViews();
        pickerChecks.clear();
        ((TextView) findViewById(R.id.category_picker_subtitle)).setText(
                "Escolha as receitas de “" + selectedCategory + "”. Uma receita pode estar em várias categorias.");
        for (Recipe recipe : pickerRecipes) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(10), dp(14), dp(10));
            row.setBackgroundResource(R.drawable.bg_surface);
            ImageView photo = new ImageView(this);
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photo.setBackgroundColor(Color.BLACK);
            loadRecipeImage(photo, recipe);
            row.addView(photo, new LinearLayout.LayoutParams(dp(58), dp(58)));
            LinearLayout textBlock = new LinearLayout(this);
            textBlock.setOrientation(LinearLayout.VERTICAL);
            textBlock.setPadding(dp(12), 0, dp(4), 0);
            TextView title = new TextView(this);
            title.setText(recipe.title.isEmpty() ? "Receita sem título" : recipe.title);
            title.setTextColor(Color.WHITE);
            title.setTextSize(16);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            textBlock.addView(title, params());
            TextView categoryText = new TextView(this);
            categoryText.setText(recipe.categories.isEmpty() ? "Sem categoria" : android.text.TextUtils.join(" · ", recipe.categories));
            categoryText.setTextColor(Color.LTGRAY);
            categoryText.setTextSize(13);
            textBlock.addView(categoryText, params());
            row.addView(textBlock, new LinearLayout.LayoutParams(0, -2, 1));
            CheckBox check = new CheckBox(this);
            check.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(233, 75, 53)));
            check.setChecked(recipe.hasCategory(selectedCategory));
            row.addView(check);
            row.setOnClickListener(view -> check.setChecked(!check.isChecked()));
            LinearLayout.LayoutParams rowParams = params();
            rowParams.setMargins(0, 0, 0, dp(8));
            pickerContainer.addView(row, rowParams);
            pickerChecks.add(check);
        }
        listScreen.setVisibility(View.GONE);
        detailScreen.setVisibility(View.GONE);
        pickerScreen.setVisibility(View.VISIBLE);
        mainHeader.setVisibility(View.GONE);
    }

    private void saveRecipeSelection() {
        if (pickerRecipes == null || selectedCategory == null) return;
        for (int i = 0; i < pickerRecipes.size(); i++) {
            if (pickerChecks.get(i).isChecked()) pickerRecipes.get(i).addCategory(selectedCategory);
            else pickerRecipes.get(i).removeCategory(selectedCategory);
        }
        if (new RecipeStore(this).save(pickerRecipes)) {
            Toast.makeText(this, "Receitas atualizadas.", Toast.LENGTH_SHORT).show();
            showCategoryDetail();
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private Recipe firstRecipeInCategory(List<Recipe> recipes, String category) {
        Recipe first = null;
        for (Recipe recipe : recipes) {
            if (!recipe.hasCategory(category)) continue;
            if (first == null) first = recipe;
            if ((recipe.photoUri != null && !recipe.photoUri.isEmpty())
                    || (recipe.bannerUri != null && !recipe.bannerUri.isEmpty())) return recipe;
        }
        return first;
    }

    private int countRecipesInCategory(List<Recipe> recipes, String category) {
        int count = 0;
        for (Recipe recipe : recipes) if (recipe.hasCategory(category)) count++;
        return count;
    }

    private void loadRecipeImage(ImageView target, Recipe recipe) {
        String uri = recipe.photoUri == null || recipe.photoUri.isEmpty() ? recipe.bannerUri : recipe.photoUri;
        if (uri != null && !uri.isEmpty()) {
            target.setImageURI(Uri.parse(uri));
            target.setContentDescription("Foto de " + recipe.title);
        }
    }

    @Override
    public void onBackPressed() {
        if (pickerScreen.getVisibility() == View.VISIBLE) showCategoryDetail();
        else if (detailScreen.getVisibility() == View.VISIBLE) showCategoryList();
        else BottomNavigation.returnToHome(this);
    }

    private void manageCategory(String value, String displayName) {
        String[] actions = {"Renomear", "Excluir"};
        new AlertDialog.Builder(this)
                .setTitle(displayName)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) renameCategory(value, displayName);
                    else deleteCategory(value, displayName);
                })
                .show();
    }

    private void renameCategory(String oldValue, String displayName) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(oldValue);
        input.setHint("Nome da categoria");
        new AlertDialog.Builder(this)
                .setTitle("Renomear categoria")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.isEmpty() && new RecipeStore(this).renameCategory(oldValue, newValue)) populateCategories();
                })
                .show();
    }

    private void deleteCategory(String value, String displayName) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir categoria?")
                .setMessage("As receitas ficarão sem categoria, mas não serão apagadas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    if (new RecipeStore(this).deleteCategory(value)) populateCategories();
                })
                .show();
    }

    private LinearLayout.LayoutParams params() { return new LinearLayout.LayoutParams(-1, -2); }
}
