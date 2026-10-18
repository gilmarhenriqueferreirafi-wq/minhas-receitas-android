package br.com.receitaarmazem;

import org.json.JSONException;
import org.json.JSONObject;

public class RecipeStep {
    public final int order;
    public final String text;
    public final int durationSeconds;

    public RecipeStep(int order, String text, int durationSeconds) {
        this.order = order;
        this.text = text == null ? "" : text;
        this.durationSeconds = Math.max(0, durationSeconds);
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("order", order)
                .put("text", text)
                .put("durationSeconds", durationSeconds);
    }

    static RecipeStep fromJson(JSONObject json) {
        return new RecipeStep(
                json.optInt("order"),
                json.optString("text"),
                json.optInt("durationSeconds")
        );
    }
}
