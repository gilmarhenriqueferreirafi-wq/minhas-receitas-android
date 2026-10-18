package br.com.receitaarmazem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public final String id;
    public String title;
    public String description;
    public String photoUri;
    public String bannerUri;
    public final List<String> categories = new ArrayList<>();
    public int originalServings;
    public String category;
    public boolean favorite;
    public int accessCount;
    public final long createdAt;
    public long updatedAt;
    public final List<Ingredient> ingredients = new ArrayList<>();
    public final List<RecipeStep> steps = new ArrayList<>();

    public Recipe(String id, String title, String description, int originalServings, String category) {
        this(id, title, description, "", originalServings, category, false, 0,
                System.currentTimeMillis(), System.currentTimeMillis());
    }

    private Recipe(String id, String title, String description, String photoUri,
                   int originalServings, String category, boolean favorite,
                   int accessCount, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.description = description == null ? "" : description;
        this.photoUri = photoUri == null ? "" : photoUri;
        this.bannerUri = "";
        this.originalServings = Math.max(1, originalServings);
        this.category = category == null ? "" : category;
        if (!this.category.trim().isEmpty()) this.categories.add(this.category.trim());
        this.favorite = favorite;
        this.accessCount = Math.max(0, accessCount);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String scaledQuantity(String quantity, int desiredServings) {
        if (quantity == null || quantity.trim().isEmpty()) return "";
        try {
            double original = Double.parseDouble(quantity.replace(',', '.'));
            double scaled = original * Math.max(1, desiredServings) / originalServings;
            if (Math.abs(scaled - Math.rint(scaled)) < 0.0001) return String.valueOf((int) Math.rint(scaled));
            return String.format(java.util.Locale.US, "%.2f", scaled).replace('.', ',');
        } catch (NumberFormatException ignored) {
            return quantity;
        }
    }

    public boolean hasCategory(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        if (categories.contains(value)) return true;
        return value.equals(category);
    }

    public void addCategory(String value) {
        if (value == null) return;
        String normalized = value.trim();
        if (normalized.isEmpty()) return;
        if (!categories.contains(normalized)) categories.add(normalized);
        if (category == null || category.trim().isEmpty()) category = normalized;
    }

    public void removeCategory(String value) {
        categories.remove(value);
        if (value != null && value.equals(category)) category = categories.isEmpty() ? "" : categories.get(0);
    }

    JSONObject toJson() throws JSONException {
        JSONArray ingredientJson = new JSONArray();
        for (Ingredient ingredient : ingredients) ingredientJson.put(ingredient.toJson());
        JSONArray stepJson = new JSONArray();
        for (RecipeStep step : steps) stepJson.put(step.toJson());
        JSONArray categoryJson = new JSONArray();
        for (String value : categories) categoryJson.put(value);
        return new JSONObject()
                .put("id", id)
                .put("title", title)
                .put("description", description)
                .put("photoUri", photoUri)
                .put("bannerUri", bannerUri)
                .put("categories", categoryJson)
                .put("originalServings", originalServings)
                .put("category", category)
                .put("favorite", favorite)
                .put("accessCount", accessCount)
                .put("createdAt", createdAt)
                .put("updatedAt", updatedAt)
                .put("ingredients", ingredientJson)
                .put("steps", stepJson);
    }

    static Recipe fromJson(JSONObject json) {
        Recipe recipe = new Recipe(
                json.optString("id"),
                json.optString("title"),
                json.optString("description"),
                json.optString("photoUri"),
                json.optInt("originalServings", 1),
                json.optString("category"),
                json.optBoolean("favorite"),
                json.optInt("accessCount"),
                json.optLong("createdAt", System.currentTimeMillis()),
                json.optLong("updatedAt", System.currentTimeMillis())
        );
        recipe.bannerUri = json.optString("bannerUri", "");
        recipe.categories.clear();
        JSONArray categoryJson = json.optJSONArray("categories");
        if (categoryJson != null) {
            for (int i = 0; i < categoryJson.length(); i++) recipe.addCategory(categoryJson.optString(i));
        }
        if (!recipe.category.trim().isEmpty()) recipe.addCategory(recipe.category);
        JSONArray ingredientJson = json.optJSONArray("ingredients");
        if (ingredientJson != null) {
            for (int i = 0; i < ingredientJson.length(); i++) {
                recipe.ingredients.add(Ingredient.fromJson(ingredientJson.optJSONObject(i)));
            }
        }
        JSONArray stepJson = json.optJSONArray("steps");
        if (stepJson != null) {
            for (int i = 0; i < stepJson.length(); i++) {
                recipe.steps.add(RecipeStep.fromJson(stepJson.optJSONObject(i)));
            }
        }
        return recipe;
    }
}
