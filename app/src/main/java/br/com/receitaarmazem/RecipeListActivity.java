package br.com.receitaarmazem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.net.Uri;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;

public class RecipeListActivity extends Activity {
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_CATEGORY = "category";
    public static final int MODE_SEARCH = 1;
    public static final int MODE_FAVORITES = 2;
    private LinearLayout listContainer;
    private EditText search;
    private TextView summary;
    private View searchBox;
    private View clearSearch;
    private View filterPanel;
    private Button filterToggle;
    private Button favoritesFilter;
    private LinearLayout categoryFilters;
    private View categoryAddButton;
    private boolean favoritesOnly;
    private int mode;
    private String category;

    @Override
    public void onBackPressed() {
        BottomNavigation.returnToHome(this);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_SEARCH);
        category = getIntent().getStringExtra(EXTRA_CATEGORY);
        setContentView(R.layout.activity_recipe_list);
        listContainer = findViewById(R.id.list_container);
        search = findViewById(R.id.list_search);
        summary = findViewById(R.id.list_summary);
        searchBox = findViewById(R.id.search_box);
        clearSearch = findViewById(R.id.list_clear_search);
        filterPanel = findViewById(R.id.list_filter_panel);
        filterToggle = findViewById(R.id.list_filter_toggle);
        favoritesFilter = findViewById(R.id.list_filter_favorites);
        categoryFilters = findViewById(R.id.list_filter_categories);
        categoryAddButton = findViewById(R.id.list_category_add);
        categoryAddButton.setOnClickListener(view -> addRecipesToCurrentCategory());
        updateHeader();
        findViewById(R.id.list_back).setVisibility(android.view.View.GONE);
        BottomNavigation.bind(this, mode == MODE_FAVORITES ? 2 : 1);
        clearSearch.setOnClickListener(view -> search.setText(""));
        filterToggle.setOnClickListener(view -> {
            boolean open = filterPanel.getVisibility() != View.VISIBLE;
            filterPanel.setVisibility(open ? View.VISIBLE : View.GONE);
            updateFilterControls();
        });
        favoritesFilter.setOnClickListener(view -> {
            favoritesOnly = !favoritesOnly;
            updateFilterControls();
            refresh();
        });
        findViewById(R.id.list_filter_clear).setOnClickListener(view -> {
            category = null;
            favoritesOnly = false;
            updateHeader();
            updateFilterControls();
            refresh();
        });
        buildCategoryFilters();
        if (mode == MODE_FAVORITES) {
            searchBox.setVisibility(View.GONE);
            filterToggle.setVisibility(View.GONE);
            filterPanel.setVisibility(View.GONE);
        } else {
            search.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    clearSearch.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
                    refresh();
                }
                public void afterTextChanged(Editable s) { }
            });
        }
        refresh();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mode = intent.getIntExtra(EXTRA_MODE, MODE_SEARCH);
        category = intent.getStringExtra(EXTRA_CATEGORY);
        favoritesOnly = false;
        updateHeader();
        searchBox.setVisibility(mode == MODE_FAVORITES ? View.GONE : View.VISIBLE);
        filterToggle.setVisibility(mode == MODE_FAVORITES ? View.GONE : View.VISIBLE);
        filterPanel.setVisibility(View.GONE);
        buildCategoryFilters();
        BottomNavigation.bind(this, mode == MODE_FAVORITES ? 2 : 1);
        refresh();
    }

    private void updateHeader() {
        ((TextView) findViewById(R.id.list_title)).setText(mode == MODE_FAVORITES ? "Favoritos"
                : category != null ? category : "Buscar receitas");
        ((TextView) findViewById(R.id.list_subtitle)).setText(mode == MODE_FAVORITES
                ? "Suas receitas guardadas para depois"
                : category != null ? "Receitas desta categoria" : "Encontre algo gostoso para preparar");
        if (categoryAddButton != null) categoryAddButton.setVisibility(mode != MODE_FAVORITES && category != null ? View.VISIBLE : View.GONE);
    }

    private void addRecipesToCurrentCategory() {
        if (category == null) return;
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
                    if (store.save(recipes)) refresh();
                }).show();
    }

    private void buildCategoryFilters() {
        if (categoryFilters == null) return;
        categoryFilters.removeAllViews();
        Set<String> values = new LinkedHashSet<>();
        for (Recipe recipe : new RecipeStore(this).load()) {
            if (recipe.category != null && !recipe.category.trim().isEmpty()) values.add(recipe.category.trim());
        }
        addCategoryChip("Todas", null);
        for (String value : values) addCategoryChip(value, value);
        updateFilterControls();
    }

    private void addCategoryChip(String title, String value) {
        Button chip = new Button(this);
        chip.setText(title);
        chip.setAllCaps(false);
        chip.setTextSize(13);
        chip.setMinHeight(dp(38));
        chip.setMinimumHeight(dp(38));
        chip.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(-2, dp(38));
        chipParams.setMargins(0, 0, dp(8), 0);
        categoryFilters.addView(chip, chipParams);
        chip.setTag(value);
        chip.setOnClickListener(view -> {
            category = value;
            updateHeader();
            updateFilterControls();
            refresh();
        });
    }

    private void updateFilterControls() {
        if (categoryFilters != null) {
            for (int i = 0; i < categoryFilters.getChildCount(); i++) {
                Button chip = (Button) categoryFilters.getChildAt(i);
                Object tag = chip.getTag();
                boolean selected = category == null ? tag == null : category.equals(tag);
                chip.setBackgroundResource(selected ? R.drawable.bg_accent : R.drawable.bg_dark_button);
                chip.setTextColor(selected ? Color.WHITE : Color.rgb(170, 165, 157));
            }
        }
        if (favoritesFilter != null) {
            favoritesFilter.setText(favoritesOnly ? "♥ Só favoritas · ativo" : "♡ Só favoritas");
            favoritesFilter.setTextColor(favoritesOnly ? Color.WHITE : Color.rgb(170, 165, 157));
            favoritesFilter.setBackgroundResource(favoritesOnly ? R.drawable.bg_accent : R.drawable.bg_dark_button);
        }
        if (filterToggle != null && mode != MODE_FAVORITES) {
            filterToggle.setText(category != null || favoritesOnly ? "Filtros · ativo"
                    : filterPanel != null && filterPanel.getVisibility() == View.VISIBLE ? "Fechar" : "Filtros");
        }
    }

    private ScrollView buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(20, 20, 20));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(28, 24, 28, 40);
        scroll.addView(content);
        TextView title = label(mode == MODE_FAVORITES ? "Favoritos" : "Buscar receitas");
        content.addView(title, params());
        if (mode == MODE_SEARCH) {
            search = new EditText(this);
            search.setHint("Buscar por título ou ingrediente");
            search.setTextColor(Color.WHITE);
            search.setHintTextColor(Color.LTGRAY);
            search.setTextSize(17);
            search.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
                public void afterTextChanged(Editable s) { }
            });
            content.addView(search, params());
        }
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(listContainer, params());
        return scroll;
    }

    private void refresh() {
        if (listContainer == null) return;
        listContainer.removeAllViews();
        String query = search == null ? "" : normalize(search.getText().toString());
        List<Recipe> recipes = new RecipeStore(this).load();
        int count = 0;
        for (Recipe recipe : recipes) {
            if (mode == MODE_FAVORITES && !recipe.favorite) continue;
            if (category != null && !recipe.hasCategory(category)) continue;
            if (favoritesOnly && !recipe.favorite) continue;
            StringBuilder searchable = new StringBuilder(recipe.title).append(' ')
                    .append(recipe.description).append(' ').append(recipe.category);
            for (String recipeCategory : recipe.categories) searchable.append(' ').append(recipeCategory);
            for (Ingredient ingredient : recipe.ingredients) {
                searchable.append(' ').append(ingredient.name).append(' ').append(ingredient.notes);
            }
            String haystack = normalize(searchable.toString());
            if (!query.isEmpty() && !haystack.contains(query)) continue;
            View item = recipeCard(recipe);
            LinearLayout.LayoutParams itemParams = params();
            itemParams.setMargins(0, 0, 0, 10);
            listContainer.addView(item, itemParams);
            count++;
        }
        summary.setText(count == 1 ? "1 receita" : count + " receitas");
        if (count == 0) {
            String message;
            if (mode == MODE_FAVORITES) message = "Você ainda não favoritou nenhuma receita. Toque no coração dentro de uma receita para guardá-la aqui.";
            else if (recipes.isEmpty()) message = "Sua coleção ainda está vazia. Adicione uma receita para começar.";
            else message = "Não encontrei receitas com esse termo. Tente outro nome ou ingrediente.";
            TextView empty = label(message);
            empty.setTextSize(16);
            empty.setTextColor(Color.rgb(170, 165, 157));
            empty.setPadding(18, 22, 18, 22);
            empty.setBackgroundResource(R.drawable.bg_surface);
            listContainer.addView(empty, params());
        }
    }

    private View recipeCard(Recipe recipe) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(14), dp(12));
        card.setBackgroundResource(R.drawable.bg_surface);
        ImageView photo = new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setBackgroundColor(Color.BLACK);
        photo.setContentDescription("Foto de " + recipe.title);
        String imageUri = recipe.photoUri == null || recipe.photoUri.isEmpty() ? recipe.bannerUri : recipe.photoUri;
        if (imageUri != null && !imageUri.isEmpty()) photo.setImageURI(Uri.parse(imageUri));
        card.addView(photo, new LinearLayout.LayoutParams(dp(78), dp(78)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(14), 0, 0, 0);
        TextView title = label(recipe.title.isEmpty() ? "Receita sem título" : recipe.title);
        title.setTextSize(17);
        title.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        title.setMaxLines(2);
        text.addView(title, params());
        String detail = recipe.category == null || recipe.category.isEmpty() ? "Receita" : recipe.category;
        if (recipe.description != null && !recipe.description.trim().isEmpty()) detail += "  ·  " + recipe.description.trim();
        TextView subtitle = label(detail);
        subtitle.setTextSize(13);
        subtitle.setTextColor(Color.rgb(170, 165, 157));
        subtitle.setMaxLines(2);
        LinearLayout.LayoutParams subtitleParams = params();
        subtitleParams.topMargin = dp(5);
        text.addView(subtitle, subtitleParams);
        card.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        card.setOnClickListener(view -> openRecipe(recipe));
        return card;
    }

    private void openRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(intent);
    }

    private String normalize(String value) {
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private TextView label(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(28);
        return view;
    }

    private LinearLayout.LayoutParams params() { return new LinearLayout.LayoutParams(-1, -2); }
}
