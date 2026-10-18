package br.com.receitaarmazem;

import org.json.JSONException;
import org.json.JSONObject;

public class Ingredient {
    public final String quantity;
    public final String unit;
    public final String name;
    public final String notes;
    public final boolean scalable;

    public Ingredient(String quantity, String unit, String name, String notes, boolean scalable) {
        this.quantity = quantity == null ? "" : quantity;
        this.unit = unit == null ? "" : unit;
        this.name = name == null ? "" : name;
        this.notes = notes == null ? "" : notes;
        this.scalable = scalable;
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("quantity", quantity)
                .put("unit", unit)
                .put("name", name)
                .put("notes", notes)
                .put("scalable", scalable);
    }

    static Ingredient fromJson(JSONObject json) {
        return new Ingredient(
                json.optString("quantity"),
                json.optString("unit"),
                json.optString("name"),
                json.optString("notes"),
                json.optBoolean("scalable", true)
        );
    }
}
