package fr.tvlab.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.RectF;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A tiny offline, native Android TV dashboard.
 * No WebView, network, analytics, root or special permissions.
 */
public final class MainActivity extends Activity {
    private DashboardView dashboard;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        dashboard = new DashboardView();
        setContentView(dashboard);
    }

    @Override public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_UP) {
            dashboard.move(-1);
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_DPAD_DOWN) {
            dashboard.move(1);
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER
                || code == KeyEvent.KEYCODE_NUMPAD_ENTER || code == KeyEvent.KEYCODE_SPACE) {
            dashboard.select();
            return true;
        }
        return super.onKeyDown(code, event);
    }

    private final class DashboardView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final RectF r = new RectF();
        private final String[] themes = {"AURORA", "NÉON VIOLET", "OLED MINIMAL"};
        private int theme = 0;
        private int focused = 0;

        DashboardView() {
            super(MainActivity.this);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }

        void move(int delta) {
            focused = (focused + delta + themes.length) % themes.length;
            invalidate();
        }

        void select() {
            theme = focused;
            invalidate();
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float sx = event.getX() * 1920f / Math.max(1, getWidth());
                float sy = event.getY() * 1080f / Math.max(1, getHeight());
                if (sy >= 823 && sy <= 927 && sx >= 145 && sx <= 1450) {
                    int i = Math.min(2, Math.max(0, (int) ((sx - 145) / 395)));
                    focused = i;
                    select();
                }
                performClick();
                return true;
            }
            return true;
        }

        @Override public boolean performClick() {
            super.performClick();
            return true;
        }

        private int accent() {
            return theme == 1 ? 0xffd9adff : (theme == 2 ? 0xffe9f4ff : 0xff58efe9);
        }

        private void text(Canvas c, String s, float x, float y, float size, int color, Typeface font) {
            p.reset();
            p.setAntiAlias(true);
            p.setColor(color);
            p.setTypeface(font);
            p.setTextSize(size);
            p.setTextAlign(Paint.Align.LEFT);
            c.drawText(s, x, y, p);
        }

        private void fillRound(Canvas c, float l, float t, float rr, float b, float rad, int color) {
            p.reset(); p.setAntiAlias(true); p.setColor(color);
            r.set(l, t, rr, b); c.drawRoundRect(r, rad, rad, p);
        }

        private void strokeRound(Canvas c, float l, float t, float rr, float b, float rad, int color, float width) {
            p.reset(); p.setAntiAlias(true); p.setColor(color);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(width);
            r.set(l, t, rr, b); c.drawRoundRect(r, rad, rad, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void circleGlow(Canvas c, float cx, float cy, float radius, int inner) {
            p.reset(); p.setAntiAlias(true);
            p.setShader(new RadialGradient(cx, cy, radius,
                new int[]{inner, 0x0e132537, 0x00000000},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, radius, p);
            p.setShader(null);
        }

        @Override protected void onDraw(Canvas actualCanvas) {
            super.onDraw(actualCanvas);
            Canvas c = actualCanvas;
            int save = c.save();
            c.scale(getWidth() / 1920f, getHeight() / 1080f);
            c.drawColor(theme == 2 ? 0xff000000 : 0xff050916);

            if (theme != 2) {
                circleGlow(c, 560, 330, 1050, theme == 1 ? 0x513b206a : 0x53254b71);
                circleGlow(c, 1640, 820, 870, theme == 1 ? 0x3c612763 : 0x2d292b71);
                p.reset(); p.setAntiAlias(true); p.setColor(0x0affffff); p.setStrokeWidth(1);
                for (int x = 0; x <= 1920; x += 90) c.drawLine(x, 0, x, 1080, p);
                for (int y = 0; y <= 1080; y += 90) c.drawLine(0, y, 1920, y, p);
            }

            int a = accent();
            // A one-pixel shift every few minutes avoids permanent static placement on an OLED.
            float shift = (SystemClock.uptimeMillis() / 180000L % 5L) - 2f;
            c.translate(shift * 2, shift);

            p.reset(); p.setAntiAlias(true); p.setColor(a);
            c.drawCircle(150, 108, 10, p);
            text(c, "TV LAB", 180, 123, 42, 0xfff2f6ff, Typeface.create("sans-serif-medium", Typeface.NORMAL));
            text(c, "/ EXPÉRIENCE 02", 391, 122, 24, 0xff7f97b6, Typeface.DEFAULT);
            strokeRound(c, 1455, 71, 1784, 140, 34, 0x485f7892, 2);
            text(c, "PHILIPS OLED848", 1490, 116, 26, 0xffb6cde4, Typeface.DEFAULT);

            text(c, "BIENVENUE À LA MAISON", 150, 330, 31, a,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
            String time = new SimpleDateFormat("HH:mm", Locale.FRANCE).format(new Date());
            text(c, time, 120, 645, 319, 0xfff0f7ff,
                Typeface.create("sans-serif-thin", Typeface.NORMAL));

            String day = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE).format(new Date());
            day = day.substring(0,1).toUpperCase(Locale.FRANCE) + day.substring(1);
            text(c, day, 154, 725, 49, 0xffc7d9ea,
                Typeface.create("sans-serif-light", Typeface.NORMAL));
            text(c, "Ton tableau de bord, sans navigateur et sans connexion Internet.",
                154, 781, 25, 0xff91aac7, Typeface.DEFAULT);

            for (int i = 0; i < 3; i++) {
                float left = 150 + i * 395f;
                fillRound(c, left, 837, left + 369, 932, 22,
                    i == theme ? (theme == 2 ? 0xff1b2730 : 0xff19354a) : 0x81334558);
                strokeRound(c, left, 837, left + 369, 932, 22,
                    i == focused ? a : 0x445b7490, i == focused ? 4 : 2);
                text(c, (i == focused ? "◉   " : "○   ") + themes[i], left + 31, 896, 28,
                    i == theme ? 0xffe2ffff : 0xffbccfe6,
                    Typeface.create("sans-serif-medium", Typeface.NORMAL));
            }

            text(c, "◀  ▶  naviguer    •    OK  sélectionner    •    Retour  quitter",
                151, 1024, 23, 0xff819ab7, Typeface.DEFAULT);
            text(c, "NATIVE ANDROID TV  ·  OFFLINE  ·  V0.1", 1397, 1024, 20, 0xff819ab7, Typeface.DEFAULT);

            c.restoreToCount(save);
            // One refresh every 15 seconds is enough for a minute-based clock.
            postInvalidateDelayed(15000);
        }
    }
}
