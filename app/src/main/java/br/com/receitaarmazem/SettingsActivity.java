package br.com.receitaarmazem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SettingsActivity extends Activity {
    private static final int REQUEST_EXPORT = 40;
    private static final int REQUEST_IMPORT = 41;
    private RecipeStore store;

    @Override
    public void onBackPressed() {
        BottomNavigation.returnToHome(this);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new RecipeStore(this);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.settings_back).setVisibility(android.view.View.GONE);
        BottomNavigation.bind(this, 4);
        findViewById(R.id.settings_export).setOnClickListener(view -> exportRecipes());
        findViewById(R.id.settings_import).setOnClickListener(view -> importRecipes());
        findViewById(R.id.settings_reset).setOnClickListener(view -> resetRecipes());
    }

    private void exportRecipes() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "minhas-receitas.json");
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private void importRecipes() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    private void resetRecipes() {
        new AlertDialog.Builder(this).setTitle("Restaurar exemplo?")
                .setMessage("Isso substitui suas receitas atuais pela receita de exemplo.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar", (dialog, which) -> {
                    Recipe sample = store.getOrCreateSample();
                    store.save(java.util.Collections.singletonList(sample));
                    Toast.makeText(this, "Dados restaurados.", Toast.LENGTH_SHORT).show();
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == REQUEST_EXPORT) writeExport(data.getData());
        if (requestCode == REQUEST_IMPORT) readImport(data.getData());
    }

    private void writeExport(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new Exception("arquivo indisponível");
            output.write(store.exportJson().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Receitas exportadas.", Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Toast.makeText(this, "Não foi possível exportar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void readImport(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) content.append(line);
            Toast.makeText(this, store.importJson(content.toString()) ? "Receitas importadas." : "Arquivo de receitas inválido.", Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Toast.makeText(this, "Não foi possível importar.", Toast.LENGTH_SHORT).show();
        }
    }
}
