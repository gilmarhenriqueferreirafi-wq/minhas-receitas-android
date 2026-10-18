package br.com.receitaarmazem;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeStore {
    private static final String PREFS = "recipe_store";
    private static final String RECIPES = "recipes";
    private static final String CATEGORIES = "categories";
    private final SharedPreferences preferences;

    public RecipeStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Recipe> load() {
        List<Recipe> recipes = new ArrayList<>();
        String raw = preferences.getString(RECIPES, "[]");
        try {
            JSONArray json = new JSONArray(raw);
            for (int i = 0; i < json.length(); i++) recipes.add(Recipe.fromJson(json.getJSONObject(i)));
        } catch (JSONException ignored) {
            // Dados corrompidos nao devem derrubar o app; o usuario pode recriar as receitas.
        }
        return recipes;
    }

    public List<String> loadCategories() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray saved = new JSONArray(preferences.getString(CATEGORIES, "[]"));
            for (int i = 0; i < saved.length(); i++) addUnique(result, saved.optString(i));
        } catch (JSONException ignored) { }
        for (Recipe recipe : load()) {
            for (String category : recipe.categories) addUnique(result, category);
            addUnique(result, recipe.category);
        }
        return result;
    }

    public boolean addCategory(String category) {
        List<String> categories = loadCategories();
        String value = category == null ? "" : category.trim();
        if (value.isEmpty()) return false;
        if (!categories.contains(value)) categories.add(value);
        return saveCategories(categories);
    }

    public boolean renameCategory(String oldValue, String newValue) {
        String replacement = newValue == null ? "" : newValue.trim();
        if (oldValue == null || oldValue.trim().isEmpty() || replacement.isEmpty()) return false;
        List<String> categories = loadCategories();
        for (int i = 0; i < categories.size(); i++) {
            if (oldValue.equals(categories.get(i))) categories.set(i, replacement);
        }
        addUnique(categories, replacement);
        List<Recipe> recipes = load();
        for (Recipe recipe : recipes) {
            if (recipe.hasCategory(oldValue)) {
                recipe.removeCategory(oldValue);
                recipe.addCategory(replacement);
            }
        }
        return save(recipes) && saveCategories(categories);
    }

    public boolean deleteCategory(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        List<String> categories = loadCategories();
        categories.remove(value);
        List<Recipe> recipes = load();
        for (Recipe recipe : recipes) recipe.removeCategory(value);
        return save(recipes) && saveCategories(categories);
    }

    private boolean saveCategories(List<String> categories) {
        JSONArray json = new JSONArray();
        for (String category : categories) json.put(category);
        return preferences.edit().putString(CATEGORIES, json.toString()).commit();
    }

    private void addUnique(List<String> target, String value) {
        if (value != null && !value.trim().isEmpty() && !target.contains(value.trim())) target.add(value.trim());
    }

    public boolean save(List<Recipe> recipes) {
        JSONArray json = new JSONArray();
        try {
            for (Recipe recipe : recipes) json.put(recipe.toJson());
        } catch (JSONException error) {
            return false;
        }
        return preferences.edit().putString(RECIPES, json.toString()).commit();
    }

    public String exportJson() {
        return preferences.getString(RECIPES, "[]");
    }

    public boolean importJson(String raw) {
        if (raw == null || raw.trim().isEmpty()) return false;
        try {
            JSONArray json = new JSONArray(raw);
            for (int i = 0; i < json.length(); i++) {
                if (json.optJSONObject(i) == null) return false;
                Recipe.fromJson(json.getJSONObject(i));
            }
            return preferences.edit().putString(RECIPES, json.toString()).commit();
        } catch (JSONException error) {
            return false;
        }
    }

    public Recipe create(String title, int servings, String category) {
        return new Recipe(UUID.randomUUID().toString(), title, "", servings, category);
    }

    public Recipe findById(String id) {
        for (Recipe recipe : load()) {
            if (recipe.id.equals(id)) return recipe;
        }
        return null;
    }

    public Recipe getOrCreateSample() {
        List<Recipe> recipes = load();
        if (!recipes.isEmpty()) return recipes.get(0);

        Recipe sample = create("Lasanha à Bolonhesa", 4, "Massas");
        sample.description = "Massa, molho de carne e muito queijo. Um clássico para compartilhar.";
        sample.ingredients.add(new Ingredient("500", "g", "carne moída", "", true));
        sample.ingredients.add(new Ingredient("500", "g", "massa de lasanha", "", true));
        sample.ingredients.add(new Ingredient("2", "xícaras", "molho de tomate", "", true));
        sample.ingredients.add(new Ingredient("300", "g", "muçarela", "ralada", true));
        sample.ingredients.add(new Ingredient("", "", "sal", "a gosto", false));
        sample.steps.add(new RecipeStep(1, "Prepare o molho de carne e deixe cozinhar até encorpar.", 30 * 60));
        sample.steps.add(new RecipeStep(2, "Monte camadas de massa, molho e queijo em uma assadeira.", 0));
        sample.steps.add(new RecipeStep(3, "Leve ao forno preaquecido a 180 °C até gratinar.", 25 * 60));
        recipes.add(sample);
        save(recipes);
        return sample;
    }

    public Recipe getLatestOrCreateSample() {
        List<Recipe> recipes = load();
        if (recipes.isEmpty()) return getOrCreateSample();
        Recipe latest = recipes.get(0);
        for (Recipe recipe : recipes) {
            if (recipe.updatedAt > latest.updatedAt) latest = recipe;
        }
        return latest;
    }

    public Recipe getMostAccessedOrCreateSample() {
        List<Recipe> recipes = load();
        if (recipes.isEmpty()) return getOrCreateSample();
        Recipe mostAccessed = recipes.get(0);
        for (Recipe recipe : recipes) {
            if (recipe.accessCount > mostAccessed.accessCount
                    || (recipe.accessCount == mostAccessed.accessCount
                    && recipe.updatedAt > mostAccessed.updatedAt)) {
                mostAccessed = recipe;
            }
        }
        return mostAccessed;
    }

    public Recipe recordAccess(String id) {
        if (id == null || id.isEmpty()) return null;
        List<Recipe> recipes = load();
        for (Recipe recipe : recipes) {
            if (id.equals(recipe.id)) {
                recipe.accessCount++;
                save(recipes);
                return recipe;
            }
        }
        return null;
    }
}
