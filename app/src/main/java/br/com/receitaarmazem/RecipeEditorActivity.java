package br.com.receitaarmazem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;

public class RecipeEditorActivity extends Activity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    private static final int PICK_BANNER = 1001;
    private static final int TAKE_BANNER = 1002;
    private static final int PICK_PHOTO = 1003;
    private static final int TAKE_PHOTO = 1004;
    private static final int BACKGROUND = Color.rgb(20, 20, 20);
    private static final int TEXT = Color.WHITE;
    private static final int ACCENT = Color.rgb(255, 86, 66);
    private RecipeStore store;
    private Recipe recipe;
    private boolean editingExistingRecipe;
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText categoryInput;
    private EditText servingsInput;
    private ImageView bannerPreview;
    private ImageView photoPreview;
    private LinearLayout ingredientsContainer;
    private LinearLayout stepsContainer;
    private final List<IngredientRow> ingredientRows = new ArrayList<>();
    private final List<StepRow> stepRows = new ArrayList<>();
    private final List<String> selectedCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new RecipeStore(this);
        String id = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        editingExistingRecipe = id != null;
        recipe = id == null ? store.create("", 4, "") : store.findById(id);
        if (recipe == null) {
            recipe = store.create("", 4, "");
            editingExistingRecipe = false;
        }
        setContentView(R.layout.activity_recipe_editor);
        bindXmlScreen();
    }

    private void bindXmlScreen() {
        titleInput = findViewById(R.id.editor_title_input);
        descriptionInput = findViewById(R.id.editor_description_input);
        categoryInput = findViewById(R.id.editor_category_input);
        servingsInput = findViewById(R.id.editor_servings_input);
        bannerPreview = findViewById(R.id.editor_banner_preview);
        photoPreview = findViewById(R.id.editor_photo_preview);
        ingredientsContainer = findViewById(R.id.editor_ingredients_container);
        stepsContainer = findViewById(R.id.editor_steps_container);
        titleInput.setText(recipe.title);
        descriptionInput.setText(recipe.description);
        selectedCategories.clear();
        selectedCategories.addAll(recipe.categories);
        if (selectedCategories.isEmpty() && recipe.category != null && !recipe.category.trim().isEmpty()) {
            selectedCategories.add(recipe.category.trim());
        }
        categoryInput.setText(TextUtils.join(", ", selectedCategories));
        servingsInput.setText(String.valueOf(recipe.originalServings));
        ((TextView) findViewById(R.id.editor_title)).setText(editingExistingRecipe ? "Editar receita" : "Nova receita");
        findViewById(R.id.editor_back).setOnClickListener(view -> finish());
        findViewById(R.id.editor_choose_banner).setOnClickListener(view -> chooseBanner());
        findViewById(R.id.editor_take_banner).setOnClickListener(view -> takeBanner());
        findViewById(R.id.editor_choose_photo).setOnClickListener(view -> chooseImage(PICK_PHOTO));
        findViewById(R.id.editor_take_photo).setOnClickListener(view -> takeImage(TAKE_PHOTO));
        findViewById(R.id.editor_add_ingredient).setOnClickListener(view -> addIngredientRow("", "", "", ""));
        findViewById(R.id.editor_add_step).setOnClickListener(view -> addStepRow("", 0));
        categoryInput.setOnClickListener(view -> showCategoryPicker());
        findViewById(R.id.editor_save).setOnClickListener(view -> saveRecipe());
        View delete = findViewById(R.id.editor_delete);
        findViewById(R.id.editor_risk_zone).setVisibility(editingExistingRecipe ? View.VISIBLE : View.GONE);
        delete.setVisibility(editingExistingRecipe ? View.VISIBLE : View.GONE);
        delete.setOnClickListener(view -> confirmDelete());
        if (recipe.photoUri != null && !recipe.photoUri.isEmpty()) showImage(photoPreview, Uri.parse(recipe.photoUri));
        if (recipe.bannerUri != null && !recipe.bannerUri.isEmpty()) showImage(bannerPreview, Uri.parse(recipe.bannerUri));
        for (Ingredient ingredient : recipe.ingredients) {
            addIngredientRow(ingredient.quantity, ingredient.unit, ingredient.name, ingredient.notes);
        }
        if (ingredientRows.isEmpty()) addIngredientRow("", "", "", "");
        for (RecipeStep step : recipe.steps) addStepRow(step.text, step.durationSeconds / 60);
        if (stepRows.isEmpty()) addStepRow("", 0);
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BACKGROUND);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(28, 24, 28, 40);
        scroll.addView(content);

        Button back = button("‹  Cancelar", ACCENT);
        back.setOnClickListener(view -> finish());
        content.addView(back, params());
        content.addView(label("Nova receita"), params());

        Button chooseBanner = button("Escolher imagem do banner", ACCENT);
        chooseBanner.setOnClickListener(view -> chooseBanner());
        content.addView(chooseBanner, params());
        Button takeBanner = button("Tirar foto do banner", ACCENT);
        takeBanner.setOnClickListener(view -> takeBanner());
        content.addView(takeBanner, params());
        bannerPreview = new ImageView(this);
        bannerPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (recipe.photoUri != null && !recipe.photoUri.isEmpty()) {
            showBanner(Uri.parse(recipe.photoUri));
        }
        content.addView(bannerPreview, new LinearLayout.LayoutParams(-1, 220));

        titleInput = field("Título da receita", recipe.title, false);
        descriptionInput = field("Descrição (opcional)", recipe.description, true);
        categoryInput = field("Categoria", recipe.category, false);
        servingsInput = field("Porções originais", String.valueOf(recipe.originalServings), false);
        servingsInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        content.addView(titleInput, params());
        content.addView(descriptionInput, params());
        content.addView(categoryInput, params());
        content.addView(servingsInput, params());
        content.addView(label("Ingredientes"), params());
        ingredientsContainer = new LinearLayout(this);
        ingredientsContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(ingredientsContainer, params());
        for (Ingredient ingredient : recipe.ingredients) {
            addIngredientRow(ingredient.quantity, ingredient.unit, ingredient.name, ingredient.notes);
        }
        if (ingredientRows.isEmpty()) addIngredientRow("", "", "", "");
        Button addIngredient = button("+ Adicionar ingrediente", ACCENT);
        addIngredient.setOnClickListener(view -> addIngredientRow("", "", "", ""));
        content.addView(addIngredient, params());
        content.addView(label("Etapas do preparo"), params());
        stepsContainer = new LinearLayout(this);
        stepsContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(stepsContainer, params());
        for (RecipeStep step : recipe.steps) addStepRow(step.text, step.durationSeconds / 60);
        if (stepRows.isEmpty()) addStepRow("", 0);

        Button addStep = button("+ Adicionar etapa", ACCENT);
        addStep.setOnClickListener(view -> addStepRow("", 0));
        content.addView(addStep, params());

        Button save = button("Salvar receita", Color.WHITE);
        save.setTextColor(Color.BLACK);
        save.setOnClickListener(view -> saveRecipe());
        content.addView(save, params());
        if (editingExistingRecipe) {
            Button delete = button("Excluir receita", Color.rgb(220, 70, 60));
            delete.setOnClickListener(view -> confirmDelete());
            content.addView(delete, params());
        }
        return scroll;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir receita?")
                .setMessage("Essa ação remove a receita do aplicativo e não pode ser desfeita.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> deleteRecipe())
                .show();
    }

    private void deleteRecipe() {
        List<Recipe> recipes = store.load();
        for (int i = recipes.size() - 1; i >= 0; i--) {
            if (recipe.id.equals(recipes.get(i).id)) recipes.remove(i);
        }
        if (store.save(recipes)) {
            Toast.makeText(this, "Receita excluída", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Não foi possível excluir a receita", Toast.LENGTH_LONG).show();
        }
    }

    private void chooseBanner() {
        chooseImage(PICK_BANNER);
    }

    private void chooseImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, requestCode);
    }

    private void takeBanner() {
        takeImage(TAKE_BANNER);
    }

    private void takeImage(int requestCode) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(this, "Nenhuma câmera disponível", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == TAKE_BANNER || requestCode == TAKE_PHOTO) {
            Bitmap bitmap = data.getParcelableExtra("data");
            if (bitmap == null) return;
            try {
                File directory = new File(getFilesDir(), "recipe-banners");
                if (!directory.exists() && !directory.mkdirs()) throw new java.io.IOException("Falha ao criar pasta");
                boolean isPhoto = requestCode == TAKE_PHOTO;
                File file = new File(directory, recipe.id + (isPhoto ? "-photo.jpg" : "-banner.jpg"));
                try (FileOutputStream output = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);
                }
                Uri savedUri = Uri.fromFile(file);
                if (isPhoto) {
                    recipe.photoUri = savedUri.toString();
                    photoPreview.setImageBitmap(bitmap);
                } else {
                    recipe.bannerUri = savedUri.toString();
                    bannerPreview.setImageBitmap(bitmap);
                }
            } catch (java.io.IOException error) {
                Toast.makeText(this, "Não foi possível salvar a foto", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if ((requestCode == PICK_BANNER || requestCode == PICK_PHOTO) && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Alguns provedores não oferecem permissão persistente.
            }
            if (requestCode == PICK_PHOTO) {
                recipe.photoUri = uri.toString();
                showImage(photoPreview, uri);
            } else {
                recipe.bannerUri = uri.toString();
                showImage(bannerPreview, uri);
            }
        }
    }

    private void showBanner(Uri uri) {
        showImage(bannerPreview, uri);
    }

    private void showImage(ImageView target, Uri uri) {
        if ("file".equals(uri.getScheme())) {
            Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
            if (bitmap != null) target.setImageBitmap(bitmap);
        } else {
            target.setImageURI(uri);
        }
    }

    private void saveRecipe() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            titleInput.setError("Informe um título");
            return;
        }
        int servings;
        try {
            servings = Math.max(1, Integer.parseInt(servingsInput.getText().toString().trim()));
        } catch (NumberFormatException error) {
            servingsInput.setError("Informe um número de porções");
            return;
        }

        recipe.title = title;
        recipe.description = descriptionInput.getText().toString().trim();
        recipe.categories.clear();
        for (String category : selectedCategories) recipe.addCategory(category);
        recipe.category = selectedCategories.isEmpty() ? "" : selectedCategories.get(0);
        recipe.originalServings = servings;
        recipe.updatedAt = System.currentTimeMillis();
        recipe.ingredients.clear();
        recipe.steps.clear();

        for (IngredientRow row : ingredientRows) {
            String name = row.name.getText().toString().trim();
            if (name.isEmpty()) continue;
            recipe.ingredients.add(new Ingredient(
                    row.quantity.getText().toString().trim(),
                    row.unit.getText().toString().trim(),
                    name,
                    row.notes.getText().toString().trim(),
                    isNumeric(row.quantity.getText().toString().trim())));
        }

        int order = 1;
        for (StepRow row : stepRows) {
            String value = row.text.getText().toString().trim();
            if (value.isEmpty()) continue;
            int minutes = 0;
            String minutesText = row.minutes.getText().toString().trim();
            if (!minutesText.isEmpty()) {
                try {
                    minutes = Integer.parseInt(minutesText);
                    if (minutes < 0) throw new NumberFormatException();
                } catch (NumberFormatException error) {
                    row.minutes.setError("Use minutos inteiros");
                    return;
                }
            }
            recipe.steps.add(new RecipeStep(order++, value, minutes * 60));
        }

        List<Recipe> recipes = store.load();
        boolean replaced = false;
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).id.equals(recipe.id)) {
                recipes.set(i, recipe);
                replaced = true;
                break;
            }
        }
        if (!replaced) recipes.add(recipe);
        if (store.save(recipes)) {
            Toast.makeText(this, "Receita salva", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Não foi possível salvar a receita", Toast.LENGTH_LONG).show();
        }
    }

    private void showCategoryPicker() {
        List<String> categories = store.loadCategories();
        if (categories.isEmpty()) {
            Toast.makeText(this, "Crie uma categoria primeiro na aba Categorias.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean[] checked = new boolean[categories.size()];
        for (int i = 0; i < categories.size(); i++) checked[i] = selectedCategories.contains(categories.get(i));
        LinearLayout popupContent = new LinearLayout(this);
        popupContent.setOrientation(LinearLayout.VERTICAL);
        popupContent.setPadding(8, 8, 8, 8);
        popupContent.setBackgroundResource(R.drawable.bg_dialog);

        ScrollView list = new ScrollView(this);
        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < categories.size(); i++) {
            CheckBox option = new CheckBox(this);
            option.setText(categories.get(i));
            option.setTextColor(TEXT);
            option.setTextSize(16);
            option.setPadding(8, 4, 8, 4);
            option.setChecked(checked[i]);
            option.setButtonTintList(android.content.res.ColorStateList.valueOf(ACCENT));
            final int index = i;
            option.setOnCheckedChangeListener((button, isChecked) -> checked[index] = isChecked);
            options.addView(option, new LinearLayout.LayoutParams(-1, -2));
        }
        list.addView(options, new ScrollView.LayoutParams(-1, -2));
        popupContent.addView(list, new LinearLayout.LayoutParams(-1, dp(260)));

        Button apply = button("Aplicar", ACCENT);
        apply.setOnClickListener(view -> {
            selectedCategories.clear();
            for (int i = 0; i < categories.size(); i++) if (checked[i]) selectedCategories.add(categories.get(i));
            categoryInput.setText(TextUtils.join(", ", selectedCategories));
            ((PopupWindow) popupContent.getTag()).dismiss();
        });
        popupContent.addView(apply, new LinearLayout.LayoutParams(-1, dp(48)));

        PopupWindow popup = new PopupWindow(popupContent, categoryInput.getWidth(), -2, true);
        popupContent.setTag(popup);
        popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_dialog));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(8));
        popup.showAsDropDown(categoryInput, 0, dp(4));
    }

    private void showUnitPicker(EditText unitInput) {
        String[] units = {"g", "kg", "mg", "ml", "l", "xícara", "colher de sopa",
                "colher de chá", "unidade", "pitada", "a gosto"};
        LinearLayout popupContent = new LinearLayout(this);
        popupContent.setOrientation(LinearLayout.VERTICAL);
        popupContent.setPadding(8, 8, 8, 8);
        popupContent.setBackgroundResource(R.drawable.bg_dialog);
        ScrollView list = new ScrollView(this);
        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.VERTICAL);
        for (String unit : units) {
            TextView option = new TextView(this);
            option.setText(unit);
            option.setTextColor(TEXT);
            option.setTextSize(16);
            option.setGravity(android.view.Gravity.CENTER_VERTICAL);
            option.setPadding(14, 14, 14, 14);
            option.setBackgroundResource(R.drawable.bg_surface);
            option.setOnClickListener(view -> {
                unitInput.setText(unit);
                ((PopupWindow) popupContent.getTag()).dismiss();
            });
            LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(-1, -2);
            optionParams.setMargins(0, 0, 0, 4);
            options.addView(option, optionParams);
        }
        list.addView(options, new ScrollView.LayoutParams(-1, -2));
        popupContent.addView(list, new LinearLayout.LayoutParams(-1, dp(280)));
        PopupWindow popup = new PopupWindow(popupContent, unitInput.getWidth(), -2, true);
        popupContent.setTag(popup);
        popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_dialog));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(8));
        popup.showAsDropDown(unitInput, 0, dp(4));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void addStepRow(String text, int minutes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(12, 12, 12, 12);
        row.setBackgroundResource(R.drawable.bg_surface);

        EditText stepText = field("Descreva esta etapa", text, true);
        EditText stepMinutes = field("Minutos (opcional)", minutes > 0 ? String.valueOf(minutes) : "", false);
        stepMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        Button remove = button("Remover etapa", ACCENT);
        StepRow stepRow = new StepRow(stepText, stepMinutes, row);
        remove.setOnClickListener(view -> {
            stepRows.remove(stepRow);
            stepsContainer.removeView(row);
            if (stepRows.isEmpty()) addStepRow("", 0);
        });

        row.addView(stepText, params());
        row.addView(stepMinutes, params());
        row.addView(remove, params());
        stepRows.add(stepRow);
        LinearLayout.LayoutParams stepParams = params();
        stepParams.setMargins(0, 0, 0, 10);
        stepsContainer.addView(row, stepParams);
    }

    private void addIngredientRow(String quantity, String unit, String name, String notes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(14, 14, 14, 14);
        row.setBackgroundResource(R.drawable.bg_surface);

        EditText quantityInput = field("Quantidade (opcional)", quantity, false);
        EditText unitInput = field("Unidade (g, ml, xícara...)", unit, false);
        EditText nameInput = field("Nome do ingrediente", name, false);
        EditText notesInput = field("Observação (opcional)", notes, false);
        unitInput.setFocusable(false);
        unitInput.setClickable(true);
        unitInput.setOnClickListener(view -> showUnitPicker(unitInput));
        Button remove = button("Remover ingrediente", ACCENT);
        IngredientRow ingredientRow = new IngredientRow(quantityInput, unitInput, nameInput, notesInput, row);
        remove.setOnClickListener(view -> {
            ingredientRows.remove(ingredientRow);
            ingredientsContainer.removeView(row);
            if (ingredientRows.isEmpty()) addIngredientRow("", "", "", "");
        });

        LinearLayout ingredientMain = new LinearLayout(this);
        ingredientMain.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams quantityParams = new LinearLayout.LayoutParams(0, -2, 0.9f);
        LinearLayout.LayoutParams unitParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, -2, 2.2f);
        unitParams.setMargins(6, 0, 0, 0);
        nameParams.setMargins(6, 0, 0, 0);
        ingredientMain.addView(quantityInput, quantityParams);
        ingredientMain.addView(unitInput, unitParams);
        ingredientMain.addView(nameInput, nameParams);
        row.addView(ingredientMain, params());
        LinearLayout.LayoutParams notesParams = params();
        notesParams.topMargin = 6;
        row.addView(notesInput, notesParams);
        row.addView(remove, params());
        ingredientRows.add(ingredientRow);
        LinearLayout.LayoutParams ingredientParams = params();
        ingredientParams.setMargins(0, 0, 0, 10);
        ingredientsContainer.addView(row, ingredientParams);
    }

    private boolean isNumeric(String value) {
        if (value.isEmpty()) return false;
        try {
            Double.parseDouble(value.replace(',', '.'));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String ingredientText() {
        StringBuilder value = new StringBuilder();
        for (Ingredient ingredient : recipe.ingredients) {
            if (value.length() > 0) value.append('\n');
            value.append(ingredient.quantity).append('|').append(ingredient.unit).append('|')
                    .append(ingredient.name).append('|').append(ingredient.notes);
        }
        return value.toString();
    }

    private String stepText() {
        StringBuilder value = new StringBuilder();
        for (RecipeStep step : recipe.steps) {
            if (value.length() > 0) value.append('\n');
            value.append(step.text);
            if (step.durationSeconds > 0) {
                value.append(" | ").append(Math.max(1, step.durationSeconds / 60));
            }
        }
        return value.toString();
    }

    private static class StepRow {
        final EditText text;
        final EditText minutes;
        final LinearLayout container;

        StepRow(EditText text, EditText minutes, LinearLayout container) {
            this.text = text;
            this.minutes = minutes;
            this.container = container;
        }
    }

    private static class IngredientRow {
        final EditText quantity;
        final EditText unit;
        final EditText name;
        final EditText notes;
        final LinearLayout container;

        IngredientRow(EditText quantity, EditText unit, EditText name, EditText notes, LinearLayout container) {
            this.quantity = quantity;
            this.unit = unit;
            this.name = name;
            this.notes = notes;
            this.container = container;
        }
    }

    private EditText field(String hint, String value, boolean multiline) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value);
        field.setTextColor(TEXT);
        field.setHintTextColor(Color.LTGRAY);
        field.setTextSize(17);
        field.setPadding(12, 12, 12, 12);
        field.setBackgroundResource(R.drawable.bg_editor_field);
        field.setSingleLine(!multiline);
        if (multiline) {
            field.setSingleLine(false);
            field.setMinLines(3);
            field.setGravity(android.view.Gravity.TOP);
        }
        return field;
    }

    private TextView label(String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextColor(TEXT);
        label.setTextSize(28);
        label.setPadding(0, 28, 0, 20);
        return label;
    }

    private Button button(String value, int color) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(color);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_dark_button);
        return button;
    }

    private LinearLayout.LayoutParams params() {
        return new LinearLayout.LayoutParams(-1, -2);
    }
}
