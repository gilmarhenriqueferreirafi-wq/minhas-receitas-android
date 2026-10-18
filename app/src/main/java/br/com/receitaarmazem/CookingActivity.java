package br.com.receitaarmazem;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class CookingActivity extends Activity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    private static final int BACKGROUND = Color.rgb(20, 20, 20);
    private static final int TEXT = Color.WHITE;
    private static final int MUTED = Color.LTGRAY;
    private static final int ACCENT = Color.rgb(255, 86, 66);
    private Recipe recipe;
    private int stepIndex;
    private long timerEndAt;
    private long timerRemaining;
    private boolean reachedLast;
    private final Set<Integer> completed = new HashSet<>();
    private TextView progress;
    private TextView stepText;
    private TextView timerText;
    private Button completeButton;
    private Button nextButton;
    private ProgressBar progressBar;
    private View stepContent;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        String id = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        recipe = new RecipeStore(this).findById(id);
        if (recipe == null || recipe.steps.isEmpty()) {
            finish();
            return;
        }
        restoreSession();
        setContentView(R.layout.activity_cooking);
        bindXmlScreen();
        renderStep();
    }

    private void bindXmlScreen() {
        ((TextView) findViewById(R.id.cooking_title)).setText(recipe.title);
        progress = findViewById(R.id.cooking_progress);
        stepText = findViewById(R.id.cooking_step);
        timerText = findViewById(R.id.cooking_timer);
        completeButton = findViewById(R.id.cooking_complete);
        nextButton = findViewById(R.id.cooking_next);
        progressBar = findViewById(R.id.cooking_progress_bar);
        stepContent = findViewById(R.id.cooking_step_content);

        findViewById(R.id.cooking_back).setOnClickListener(view -> finish());
        findViewById(R.id.cooking_start_timer).setOnClickListener(view -> startTimer(true));
        completeButton.setOnClickListener(view -> toggleComplete());
        findViewById(R.id.cooking_previous).setOnClickListener(view -> move(-1));
        findViewById(R.id.cooking_next).setOnClickListener(view -> move(1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerEndAt > 0) startTimer(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSession();
        if (timer != null) timer.cancel();
    }

    private ScrollView buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BACKGROUND);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(28, 24, 28, 40);
        scroll.addView(content);

        Button back = button("‹  Voltar", ACCENT);
        back.setOnClickListener(view -> finish());
        content.addView(back, params());
        content.addView(label(recipe.title, 28, TEXT), params());
        progress = label("", 16, MUTED);
        content.addView(progress, params());
        stepText = label("", 22, TEXT);
        stepText.setPadding(0, 32, 0, 32);
        content.addView(stepText, params());
        timerText = label("", 28, ACCENT);
        timerText.setGravity(Gravity.CENTER);
        content.addView(timerText, params());

        Button startTimer = button("Iniciar / retomar temporizador", TEXT);
        startTimer.setOnClickListener(view -> startTimer(true));
        content.addView(startTimer, params());
        completeButton = button("Marcar etapa como concluída", TEXT);
        completeButton.setOnClickListener(view -> toggleComplete());
        content.addView(completeButton, params());

        LinearLayout navigation = new LinearLayout(this);
        Button previous = button("← Anterior", TEXT);
        previous.setOnClickListener(view -> move(-1));
        Button next = button("Próxima →", TEXT);
        next.setOnClickListener(view -> move(1));
        navigation.addView(previous, weightParams());
        navigation.addView(next, weightParams());
        content.addView(navigation, params());
        return scroll;
    }

    private void renderStep() {
        renderStep(true);
    }

    private void renderStep(boolean animateContent) {
        RecipeStep step = recipe.steps.get(stepIndex);
        if (stepIndex >= recipe.steps.size() - 1) reachedLast = true;
        progress.setText("Etapa " + (stepIndex + 1) + " de " + recipe.steps.size());
        progressBar.setProgress((stepIndex + 1) * 100 / recipe.steps.size());
        stepText.setText(step.order + ". " + step.text);
        if (animateContent) MotionUtils.reveal(stepContent);
        nextButton.setText(stepIndex >= recipe.steps.size() - 1 ? "Encerrar" : "Próxima →");
        completeButton.setText(completed.contains(stepIndex)
                ? "✓ Etapa concluída (desmarcar)"
                : "Marcar etapa como concluída");
        if (timerEndAt > 0) updateTimerText(Math.max(0, timerEndAt - System.currentTimeMillis()));
        else if (timerRemaining > 0) updateTimerText(timerRemaining);
        else if (step.durationSeconds > 0) updateTimerText(step.durationSeconds * 1000L);
        else timerText.setText("Sem temporizador nesta etapa");
    }

    private void move(int amount) {
        if (amount > 0 && stepIndex >= recipe.steps.size() - 1) {
            reachedLast = true;
            saveSession();
            finish();
            return;
        }
        stepIndex = Math.max(0, Math.min(recipe.steps.size() - 1, stepIndex + amount));
        timerEndAt = 0;
        timerRemaining = 0;
        if (timer != null) timer.cancel();
        renderStep();
        saveSession();
    }

    private void toggleComplete() {
        if (!completed.add(stepIndex)) completed.remove(stepIndex);
        if (completed.size() >= recipe.steps.size()) reachedLast = true;
        renderStep(false);
        MotionUtils.pulse(completeButton);
        saveSession();
    }

    private void startTimer(boolean reset) {
        RecipeStep step = recipe.steps.get(stepIndex);
        long duration = reset || timerRemaining <= 0 ? step.durationSeconds * 1000L : timerRemaining;
        if (duration <= 0) {
            timerText.setText("Esta etapa não tem duração definida");
            return;
        }
        timerEndAt = System.currentTimeMillis() + duration;
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(duration, 1000) {
            public void onTick(long ignored) { updateTimerText(Math.max(0, timerEndAt - System.currentTimeMillis())); }
            public void onFinish() {
                timerEndAt = 0;
                timerRemaining = 0;
                completed.add(stepIndex);
                reachedLast = stepIndex >= recipe.steps.size() - 1;
                timerText.setText("Tempo concluído");
                saveSession();
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (stepIndex < recipe.steps.size() - 1) move(1);
                    else finish();
                }, 1000);
            }
        }.start();
        saveSession();
    }

    private void updateTimerText(long milliseconds) {
        long totalSeconds = Math.max(0, (milliseconds + 999) / 1000);
        timerText.setText(String.format(java.util.Locale.US, "⏱ %02d:%02d", totalSeconds / 60, totalSeconds % 60));
    }

    private void saveSession() {
        getSharedPreferences("cooking_sessions", MODE_PRIVATE).edit()
                .putInt(sessionKey("step"), stepIndex)
                .putBoolean(sessionKey("reachedLast"), reachedLast)
                .putStringSet(sessionKey("completed"), toStrings())
                .putLong(sessionKey("timerEndAt"), timerEndAt)
                .putLong(sessionKey("timerRemaining"), timerEndAt > 0 ? Math.max(0, timerEndAt - System.currentTimeMillis()) : timerRemaining)
                .apply();
    }

    private void restoreSession() {
        android.content.SharedPreferences preferences = getSharedPreferences("cooking_sessions", MODE_PRIVATE);
        int savedStep = preferences.getInt(sessionKey("step"), 0);
        reachedLast = preferences.getBoolean(sessionKey("reachedLast"), false)
                || savedStep >= recipe.steps.size() - 1;
        if (reachedLast) {
            clearSession(preferences);
            stepIndex = 0;
            timerEndAt = 0;
            timerRemaining = 0;
            completed.clear();
            reachedLast = false;
            return;
        }
        stepIndex = Math.max(0, Math.min(recipe.steps.size() - 1, savedStep));
        for (String value : preferences.getStringSet(sessionKey("completed"), new HashSet<>())) {
            try { completed.add(Integer.parseInt(value)); } catch (NumberFormatException ignored) { }
        }
        timerEndAt = preferences.getLong(sessionKey("timerEndAt"), 0);
        timerRemaining = preferences.getLong(sessionKey("timerRemaining"), 0);
        if (timerEndAt > 0 && timerEndAt <= System.currentTimeMillis()) {
            timerEndAt = 0;
            timerRemaining = 0;
        }
        if (completed.size() >= recipe.steps.size()) {
            clearSession(preferences);
            stepIndex = 0;
            timerEndAt = 0;
            timerRemaining = 0;
            completed.clear();
            reachedLast = false;
        }
    }

    private String sessionKey(String suffix) {
        return "recipe_" + recipe.id + "_" + suffix;
    }

    private void clearSession(android.content.SharedPreferences preferences) {
        preferences.edit()
                .remove(sessionKey("step"))
                .remove(sessionKey("reachedLast"))
                .remove(sessionKey("completed"))
                .remove(sessionKey("timerEndAt"))
                .remove(sessionKey("timerRemaining"))
                .apply();
    }

    private Set<String> toStrings() {
        Set<String> values = new HashSet<>();
        for (Integer value : completed) values.add(String.valueOf(value));
        return values;
    }

    private TextView label(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(5, 1.05f);
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

    private LinearLayout.LayoutParams params() { return new LinearLayout.LayoutParams(-1, -2); }
    private LinearLayout.LayoutParams weightParams() { return new LinearLayout.LayoutParams(0, -2, 1); }
}
