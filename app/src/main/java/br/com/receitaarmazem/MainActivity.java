package br.com.receitaarmazem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showHome();
    }

    private void showHome() {
        RecipeStore store = new RecipeStore(this);
        Recipe recipe = store.getMostAccessedOrCreateSample();
        List<Recipe> recipes = store.load();
        setContentView(new RecipeHomeView(this, recipe, recipes));
    }

    private static class RecipeHomeView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int background = Color.rgb(14, 14, 14);
        private final int surface = Color.rgb(25, 25, 25);
        private final int muted = Color.rgb(175, 175, 175);
        private final int accent = Color.rgb(255, 86, 66);
        private final Recipe featuredRecipe;
        private final List<Recipe> recipes;
        private final float density;
        private float downX;
        private float downY;

        RecipeHomeView(Context context, Recipe featuredRecipe, List<Recipe> recipes) {
            super(context);
            this.featuredRecipe = featuredRecipe;
            this.recipes = recipes;
            density = getResources().getDisplayMetrics().density;
            setBackgroundColor(background);
            setFocusable(true);
            setContentDescription("Tela inicial do aplicativo Minhas Receitas");
        }

        private float dp(float value) { return value * density; }

        private void text(Canvas canvas, String value, float x, float y, float size,
                          int color, Typeface typeface) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(dp(size));
            paint.setTypeface(typeface);
            canvas.drawText(value, dp(x), dp(y), paint);
        }

        private void rounded(Canvas canvas, float left, float top, float right, float bottom,
                             float radius, int color) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawRoundRect(dp(left), dp(top), dp(right), dp(bottom), dp(radius), dp(radius), paint);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth() / density;
            float height = getHeight() / density;
            float side = 20;
            float contentWidth = width - side * 2;

            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(dp(2.4f));
            paint.setColor(Color.WHITE);
            for (int i = 0; i < 3; i++) {
                canvas.drawLine(dp(side), dp(28 + i * 7), dp(side + 21), dp(28 + i * 7), paint);
            }
            text(canvas, "Minhas", width / 2 - 51, 35, 24, Color.WHITE, Typeface.create("serif", Typeface.BOLD));
            text(canvas, "Receitas", width / 2 + 8, 35, 24, accent, Typeface.create("serif", Typeface.BOLD));
            text(canvas, "⚙", width - 47, 36, 24, Color.WHITE, Typeface.DEFAULT);

            float heroTop = 65;
            float heroBottom = 245;
            rounded(canvas, side, heroTop, width - side, heroBottom, 20, Color.rgb(73, 27, 20));
            paint.setShader(new LinearGradient(dp(side), dp(heroTop), dp(width - side), dp(heroBottom),
                    Color.rgb(95, 24, 16), Color.rgb(24, 17, 15), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(dp(side), dp(heroTop), dp(width - side), dp(heroBottom), dp(20), dp(20), paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(accent);
            canvas.drawRoundRect(dp(side), dp(heroTop), dp(width - side), dp(heroBottom), dp(20), dp(20), paint);
            paint.setStyle(Paint.Style.FILL);
            text(canvas, "🔥  MAIS ACESSADA", side + 18, heroTop + 30, 11, Color.rgb(255, 192, 120), Typeface.DEFAULT_BOLD);
            String title = featuredRecipe.title.isEmpty() ? "Lasanha à Bolonhesa" : featuredRecipe.title;
            String[] titleLines = splitTitle(title);
            text(canvas, titleLines[0], side + 18, heroTop + 77, 27, Color.WHITE, Typeface.DEFAULT_BOLD);
            text(canvas, titleLines[1], side + 18, heroTop + 108, 27, Color.WHITE, Typeface.DEFAULT_BOLD);
            text(canvas, shortDescription(), side + 18, heroTop + 139, 14, Color.WHITE, Typeface.DEFAULT);
            rounded(canvas, side + 18, heroTop + 169, side + 137, heroTop + 207, 22, accent);
            text(canvas, "Ver receita  →", side + 34, heroTop + 194, 14, Color.WHITE, Typeface.DEFAULT_BOLD);
            paint.setColor(Color.rgb(185, 102, 45));
            canvas.drawCircle(dp(width - 78), dp(heroTop + 102), dp(50), paint);
            paint.setColor(Color.rgb(243, 181, 92));
            canvas.drawOval(dp(width - 119), dp(heroTop + 75), dp(width - 37), dp(heroTop + 130), paint);
            paint.setColor(Color.rgb(255, 221, 137));
            canvas.drawOval(dp(width - 105), dp(heroTop + 86), dp(width - 49), dp(heroTop + 116), paint);
            text(canvas, "♡", width - 52, heroTop + 44, 31, Color.WHITE, Typeface.DEFAULT);

            text(canvas, "Minhas receitas", side, 286, 23, Color.WHITE, Typeface.DEFAULT_BOLD);
            text(canvas, "Ver todas  →", width - 103, 286, 13, accent, Typeface.DEFAULT_BOLD);

            float cardGap = 12;
            float cardWidth = (contentWidth - cardGap) / 2;
            int[] colors = {Color.rgb(132, 82, 39), Color.rgb(206, 151, 57), Color.rgb(151, 62, 25), Color.rgb(82, 35, 24)};
            float[] lefts = {side, side + cardWidth + cardGap, side, side + cardWidth + cardGap};
            float[] tops = {305, 305, 432, 432};
            for (int i = 0; i < Math.min(4, recipes.size()); i++) {
                drawRecipeCard(canvas, lefts[i], tops[i], cardWidth, recipes.get(i), colors[i]);
            }

            float navTop = height - 72;
            paint.setColor(Color.rgb(23, 23, 23));
            canvas.drawRect(0, dp(navTop), getWidth(), getHeight(), paint);
            String[] labels = {"⌂", "⌕", "♡", "▦", "⚙"};
            String[] names = {"Início", "Buscar", "Favoritos", "Categorias", "Configurações"};
            float itemWidth = width / 5;
            for (int i = 0; i < labels.length; i++) {
                float x = itemWidth * i + itemWidth / 2;
                int color = i == 0 ? accent : muted;
                text(canvas, labels[i], x - (i == 4 ? 9 : 10), navTop + 28, 24, color, Typeface.DEFAULT);
                text(canvas, names[i], x - (names[i].length() * 3.2f), navTop + 53, 10, color, Typeface.DEFAULT);
            }
        }

        private String[] splitTitle(String title) {
            String[] words = title.trim().split("\\s+");
            if (words.length <= 1) return new String[]{title, ""};
            int middle = Math.max(1, words.length / 2);
            StringBuilder first = new StringBuilder();
            StringBuilder second = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                StringBuilder target = i < middle ? first : second;
                if (target.length() > 0) target.append(' ');
                target.append(words[i]);
            }
            return new String[]{first.toString(), second.toString()};
        }

        private String shortDescription() {
            if (featuredRecipe.description == null || featuredRecipe.description.trim().isEmpty()) {
                return "Uma receita para preparar em casa.";
            }
            String value = featuredRecipe.description.trim();
            return value.length() > 28 ? value.substring(0, 28) + "…" : value;
        }

        private void drawRecipeCard(Canvas canvas, float left, float top, float width,
                                    Recipe recipe, int imageColor) {
            rounded(canvas, left, top, left + width, top + 115, 16, surface);
            paint.setColor(imageColor);
            canvas.drawRoundRect(dp(left + 1), dp(top + 1), dp(left + width - 1), dp(top + 56), dp(15), dp(15), paint);
            paint.setColor(Color.argb(45, 255, 255, 255));
            canvas.drawCircle(dp(left + width - 28), dp(top + 27), dp(22), paint);
            text(canvas, "♡", left + width - 39, top + 35, 23, Color.WHITE, Typeface.DEFAULT);
            String[] title = splitCardTitle(recipe.title);
            text(canvas, title[0], left + 12, top + 78, 15, Color.WHITE, Typeface.DEFAULT_BOLD);
            text(canvas, title[1], left + 12, top + 97, 15, Color.WHITE, Typeface.DEFAULT_BOLD);
            String category = recipe.category.isEmpty() ? "Receita" : recipe.category;
            text(canvas, "•  " + category, left + 12, top + 110, 11, muted, Typeface.DEFAULT);
        }

        private String[] splitCardTitle(String title) {
            if (title == null || title.trim().isEmpty()) return new String[]{"Sem título", ""};
            String[] words = title.trim().split("\\s+");
            StringBuilder first = new StringBuilder();
            StringBuilder second = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                StringBuilder target = i < Math.max(1, words.length / 2) ? first : second;
                if (target.length() > 0) target.append(' ');
                target.append(words[i]);
            }
            return new String[]{first.toString(), second.toString()};
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getX() / density;
                downY = event.getY() / density;
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float x = event.getX() / density;
                float y = event.getY() / density;
                if (Math.abs(x - downX) < 24 && Math.abs(y - downY) < 24) {
                    float height = getHeight() / density;
                    if (x <= 80 && y <= 70) {
                        getContext().startActivity(new Intent(getContext(), RecipeEditorActivity.class));
                    } else if (y >= height - 80) {
                        float width = getWidth() / density;
                        int item = (int) ((x / width) * 5);
                        Intent intent;
                        if (item == 1) {
                            intent = new Intent(getContext(), RecipeListActivity.class);
                            intent.putExtra(RecipeListActivity.EXTRA_MODE, RecipeListActivity.MODE_SEARCH);
                            getContext().startActivity(intent);
                        } else if (item == 2) {
                            intent = new Intent(getContext(), RecipeListActivity.class);
                            intent.putExtra(RecipeListActivity.EXTRA_MODE, RecipeListActivity.MODE_FAVORITES);
                            getContext().startActivity(intent);
                        } else if (item == 3) {
                            getContext().startActivity(new Intent(getContext(), CategoriesActivity.class));
                        }
                    } else if (y >= 65 && y <= 245) {
                        Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, featuredRecipe.id);
                        getContext().startActivity(intent);
                    } else if (y >= 305 && y <= 560 && !recipes.isEmpty()) {
                        int index = y < 432 ? (x < getWidth() / density / 2 ? 0 : 1) : (x < getWidth() / density / 2 ? 2 : 3);
                        if (index < recipes.size()) {
                            Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
                            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipes.get(index).id);
                            getContext().startActivity(intent);
                        }
                    }
                }
                return true;
            }
            return true;
        }
    }
}
