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
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.util.Random;

/**
 * Yassine & Hedi's Magic Adventure
 * A native, offline Android TV celebration. No browser, WebView, network or permissions.
 */
public final class MainActivity extends Activity {
    private MagicView magic;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        magic = new MagicView();
        setContentView(magic);
    }

    @Override protected void onPause() {
        super.onPause();
        if (magic != null) magic.setActive(false);
    }
    @Override protected void onResume() {
        super.onResume();
        if (magic != null) magic.setActive(true);
    }

    @Override public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_UP) {
            magic.changeWorld(-1);
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_DPAD_DOWN) {
            magic.changeWorld(1);
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER ||
            code == KeyEvent.KEYCODE_SPACE || code == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            magic.celebrate();
            return true;
        }
        return super.onKeyDown(code, event);
    }

    private final class MagicView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Random rng = new Random(12536);
        private final float[] starsX = new float[130];
        private final float[] starsY = new float[130];
        private final float[] starsR = new float[130];
        private final float[] starsPhase = new float[130];
        private final float[] confettiX = new float[105];
        private final float[] confettiY = new float[105];
        private final float[] confettiAngle = new float[105];
        private final float[] confettiSpeed = new float[105];

        private final String[] worlds = {"GALAXIE", "CHÂTEAU MAGIQUE", "OCÉAN MYSTÉRIEUX"};
        private final String[] winMessages = {
            "MISSION ACCOMPLIE !",
            "BRAVO LES CHAMPIONS !",
            "VOUS ÊTES LES MEILLEURS !",
            "YASSINE + HEDI = SUPER ÉQUIPE !"
        };
        private int world = 0;
        private int partyCount = 0;
        private long partyStart = -100000;
        private long lastInput = SystemClock.uptimeMillis();
        private boolean active = true;
        private final Typeface ultra = Typeface.create("sans-serif-black", Typeface.BOLD);
        private final Typeface strong = Typeface.create("sans-serif", Typeface.BOLD);
        private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        private final Typeface regular = Typeface.create("sans-serif", Typeface.NORMAL);

        MagicView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            for (int i=0;i<starsX.length;i++) {
                starsX[i] = rng.nextInt(1920);
                starsY[i] = rng.nextInt(970);
                starsR[i] = 1 + rng.nextFloat()*3;
                starsPhase[i] = rng.nextFloat()*6.283f;
            }
            for (int i=0;i<confettiX.length;i++) {
                confettiX[i] = 150 + rng.nextFloat()*1640;
                confettiY[i] = -120 + rng.nextFloat()*850;
                confettiAngle[i] = rng.nextFloat()*6.283f;
                confettiSpeed[i] = 1 + rng.nextFloat()*2.5f;
            }
            setFocusable(true);
            requestFocus();
        }

        void setActive(boolean value) {
            active=value;
            if (value) invalidate();
        }
        void changeWorld(int delta) {
            lastInput=SystemClock.uptimeMillis();
            world=(world+delta+worlds.length)%worlds.length;
            invalidate();
        }
        void celebrate() {
            lastInput=SystemClock.uptimeMillis();
            partyStart=lastInput;
            partyCount++;
            invalidate();
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction()==MotionEvent.ACTION_UP) {
                float x=e.getX()*1920f/Math.max(1,getWidth());
                if (x<540) changeWorld(-1);
                else if (x>1380) changeWorld(1);
                else celebrate();
                performClick();
                return true;
            }
            return true;
        }
        @Override public boolean performClick() {
            super.performClick();
            return true;
        }

        private int rgba(int color, float alpha) {
            return (Math.max(0,Math.min(255,(int)(alpha*255)))<<24) | (color&0x00ffffff);
        }
        private void base() {
            p.reset();
            p.setAntiAlias(true);
            p.setDither(true);
        }
        private void rounded(Canvas c,float l,float t,float r,float b,float radius,int color) {
            base();p.setColor(color);rect.set(l,t,r,b);c.drawRoundRect(rect,radius,radius,p);
        }
        private void outline(Canvas c,float l,float t,float r,float b,float rad,int color,float sw) {
            base();p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);
            rect.set(l,t,r,b);c.drawRoundRect(rect,rad,rad,p);p.setStyle(Paint.Style.FILL);
        }
        private void ball(Canvas c,float x,float y,float rad,int color) {
            base();p.setColor(color);c.drawCircle(x,y,rad,p);
        }
        private void ellipse(Canvas c,float l,float t,float r,float b,int color) {
            base();p.setColor(color);c.drawOval(l,t,r,b,p);
        }
        private void line(Canvas c,float x1,float y1,float x2,float y2,int col,float width) {
            base();p.setColor(col);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(x1,y1,x2,y2,p);
        }
        private void path(Canvas c,int col,float... xy) {
            if(xy.length<6)return;
            Path shape=new Path();shape.moveTo(xy[0],xy[1]);
            for(int i=2;i<xy.length;i+=2)shape.lineTo(xy[i],xy[i+1]);
            shape.close();base();p.setColor(col);c.drawPath(shape,p);
        }
        private void label(Canvas c,String value,float x,float y,float size,int col,Typeface face,Paint.Align align) {
            base();p.setColor(col);p.setTypeface(face);p.setTextSize(size);p.setTextAlign(align);
            c.drawText(value,x,y,p);
        }
        private void glowLabel(Canvas c,String value,float x,float y,float size,int top,int bottom) {
            base();p.setTypeface(ultra);p.setTextSize(size);p.setTextAlign(Paint.Align.CENTER);
            p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(17);p.setColor(0xff24265e);p.setShadowLayer(16,0,7,0xe33418b1);
            c.drawText(value,x,y,p);
            p.clearShadowLayer();p.setStrokeWidth(7);p.setColor(0xffe9f9ff);c.drawText(value,x,y,p);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(x,y-size,x,y+12,top,bottom,Shader.TileMode.CLAMP));
            p.setShadowLayer(24,0,5,0x9b14beff);
            c.drawText(value,x,y,p);
            p.clearShadowLayer();p.setShader(null);
        }
        private void glowingBall(Canvas c,float x,float y,float rad,int col) {
            base();p.setShader(new RadialGradient(x,y,rad,
                new int[]{col,rgba(col,0.25f),0x00000000},
                new float[]{0f,.3f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(x,y,rad,p);p.setShader(null);
        }
        private void background(Canvas c,float t) {
            int top= world==1 ? 0xff170b42 : world==2 ? 0xff031e4a : 0xff060b34;
            int bottom=world==1 ? 0xff422069 : world==2 ? 0xff075e83 : 0xff29135c;
            base();p.setShader(new LinearGradient(0,0,1920,1080,top,bottom,Shader.TileMode.CLAMP));
            c.drawRect(0,0,1920,1080,p);p.setShader(null);
            glowingBall(c,320,380,650,world==2?0x5920d5ff:0x73318dff);
            glowingBall(c,1590,620,730,world==1?0x654ae0f4:0x5baf40e8);
            glowingBall(c,990,890,480,0x418c37ed);
            for(int i=0;i<starsX.length;i++) {
                float twinkle=(float)(.6+.4*Math.sin(t*.0014+starsPhase[i]));
                ball(c,starsX[i],starsY[i],starsR[i]*(0.8f+0.3f*twinkle),
                     rgba(0xffd4efff,0.32f+0.68f*twinkle));
            }
            for(int i=0;i<18;i++) {
                float sx=(i*421+131)%1900;
                float sy=(i*263+81)%850;
                float tw=(float)Math.sin(t*.002+i*1.81);
                drawSpark(c,sx,sy,5+3*tw,rgba(0xfff7edff,.42f),1);
            }
        }
        private void drawSpark(Canvas c,float x,float y,float rad,int col,float weight) {
            if(rad<1)return;
            line(c,x-rad,y,x+rad,y,col,weight*2);
            line(c,x,y-rad,x,y+rad,col,weight*2);
            ball(c,x,y,weight*1.6f,col);
        }
        private void planets(Canvas c,float t) {
            // Saturn-like planet, intentionally in the corner away from the children's names.
            c.save();c.rotate(-23,1702,190);
            base();p.setColor(0x995d4eff);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(19);
            c.drawOval(1564,135,1840,245,p);p.setStyle(Paint.Style.FILL);
            glowingBall(c,1702,189,110,0x887e79ff);
            base();p.setShader(new LinearGradient(1640,120,1770,285,0xff8cddff,0xff8248eb,Shader.TileMode.CLAMP));
            c.drawCircle(1702,190,81,p);p.setShader(null);
            c.restore();
            glowingBall(c,298,139,110,0x888a52fa);
            ball(c,298,139,46,0xff8558d6);
            ball(c,277,122,12,0x6ceac7ff);
            ball(c,479,86,15,0xfff5cc65);
        }
        private void clouds(Canvas c,float x,float y,float scale) {
            c.save();c.translate(x,y);c.scale(scale,scale);
            glowingBall(c,0,0,210,0x3f9c50ef);
            ellipse(c,-135,-19,110,70,0xb66140af);
            ellipse(c,-70,-72,119,62,0xc36c55cc);
            ellipse(c,5,-33,216,76,0xa65043bf);
            c.restore();
        }
        private void island(Canvas c,float x,float y,float sc,int tone) {
            c.save();c.translate(x,y);c.scale(sc,sc);
            ellipse(c,-210,-3,225,45,0x9d8b58f7);
            path(c,0xff26356b,-207,9,215,9,100,95,15,185,-48,155,-145,89);
            path(c,0xff4e38a0,32,28,210,13,105,96,15,177);
            rounded(c,-210,-9,214,27,19,tone);
            for(int i=0;i<12;i++) {
                float px=-174+i*31;
                path(c, i%3==0?0xffa1fcbb:0xff4cdd8e,px,-8,px+10,-20,px+17,-8);
            }
            c.restore();
        }
        private void castle(Canvas c,float x,float y,float sc) {
            c.save();c.translate(x,y);c.scale(sc,sc);
            island(c,0,154,.9f,0xff30bfa7);
            rounded(c,-106,-37,103,139,9,0xff7553cc);
            rounded(c,-156,-119,-82,146,5,0xff8963dc);
            rounded(c,85,-130,158,147,5,0xff8963dc);
            rounded(c,-32,-162,43,143,5,0xff9c6ce3);
            path(c,0xffe284ef,-167,-117,-119,-184,-73,-117);
            path(c,0xffb56fff,76,-128,122,-200,171,-128);
            path(c,0xffd2a8ff,-42,-161,6,-239,55,-161);
            rounded(c,-22,71,30,139,26,0xff28335e);
            for(int i=0;i<4;i++){
                rounded(c,-144,(-84+i*51),-116,(-58+i*51),5,0xffffd482);
                rounded(c,109,(-90+i*53),136,(-65+i*53),5,0xffffd482);
            }
            rounded(c,-22,-85,16,-50,6,0xffffd482);
            c.restore();
        }
        private void robot(Canvas c,float x,float y,float sc,float t) {
            c.save();c.translate(x,y+(float)Math.sin(t*.0017)*10);c.rotate(-9);c.scale(sc,sc);
            line(c,-40,-97,-45,-147,0xffb4ffff,12);
            ball(c,-45,-150,17,0xffffd86c);
            // arms
            line(c,-110,25,-156,-13,0xffedfaff,34);
            line(c,109,23,154,-16,0xffedfaff,34);
            ball(c,-157,-15,25,0xff78e8ff);ball(c,155,-19,25,0xff78e8ff);
            // body
            rounded(c,-105,-18,105,145,60,0xffe9f7ff);
            rounded(c,-54,65,54,121,19,0xff69dcff);
            ball(c,0,93,20,0xff149dcc);
            // feet
            rounded(c,-84,120,-28,173,24,0xffc3eafe);
            rounded(c,28,120,84,173,24,0xffc3eafe);
            // ears and head
            rounded(c,-149,-98,-106,15,19,0xffb3d9ff);
            rounded(c,106,-98,149,15,19,0xffb3d9ff);
            rounded(c,-124,-142,124,37,55,0xffebf7ff);
            rounded(c,-107,-124,107,20,45,0xff13234f);
            // smiling eyes
            base();p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(10);p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(0xff62ecff);
            RectF left=new RectF(-70,-67,-24,-24), right=new RectF(24,-67,70,-24);
            c.drawArc(left,195,160,false,p);c.drawArc(right,195,160,false,p);
            RectF smile=new RectF(-28,-37,28,-3);
            p.setStrokeWidth(7);c.drawArc(smile,8,164,false,p);p.setStyle(Paint.Style.FILL);
            c.restore();
        }
        private void rocket(Canvas c,float x,float y,float sc,float t) {
            c.save();
            c.translate(x+(float)Math.sin(t*.001)*14,y+(float)Math.sin(t*.0017)*21);
            c.rotate(34);c.scale(sc,sc);
            // flame
            path(c,0xffff53a4,-32,122,0,235,33,122);
            path(c,0xffffd269,-20,118,0,196,20,118);
            // fins
            path(c,0xffff4a7d,-55,74,-101,151,-34,123);
            path(c,0xffff4a7d,55,74,101,151,34,123);
            rounded(c,-53,-68,53,130,52,0xffedfaff);
            path(c,0xfff54b78,-50,-55,0,-154,50,-55);
            rounded(c,-53,85,53,123,8,0xff478dff);
            ball(c,0,-13,35,0xff286ccd);
            ball(c,0,-13,23,0xff97faff);
            ellipse(c,-28,-27,-5,-4,0x99ffffff);
            c.restore();
        }
        private void chest(Canvas c,float x,float y,float scale) {
            c.save();c.translate(x,y);c.scale(scale,scale);
            glowingBall(c,0,10,180,0x77ffb838);
            rounded(c,-106,-60,106,73,23,0xff7f3994);
            rounded(c,-106,-71,106,-6,22,0xffffc142);
            rounded(c,-92,-58,92,-17,14,0xff954b82);
            rounded(c,-102,2,102,24,3,0xffffcd4c);
            rounded(c,-18,-70,18,65,9,0xffffe58b);
            drawSpark(c,0,-10,31,0xffffffff,4);
            c.restore();
        }
        private void crown(Canvas c,float x,float y,float scale) {
            c.save();c.translate(x,y);c.scale(scale,scale);
            path(c,0xffffda54,-101,13,-116,-60,-50,-21,0,-88,51,-21,118,-60,101,13,94,48,-95,48);
            rounded(c,-98,39,98,57,8,0xfffff29c);
            ball(c,-116,-65,9,0xffffffff);ball(c,0,-92,11,0xffffffff);ball(c,118,-65,9,0xffffffff);
            c.restore();
        }
        private void firework(Canvas c,float cx,float cy,float age,float scale,int col) {
            float r=age*175*scale;
            if(age<0||age>1) return;
            int color=rgba(col,(1-age)*.9f);
            for(int i=0;i<12;i++) {
                double a=i*Math.PI/6;
                float x=cx+(float)Math.cos(a)*r;
                float y=cy+(float)Math.sin(a)*r;
                drawSpark(c,x,y,Math.max(1,8*(1-age)),color,2.6f);
            }
            glowingBall(c,cx,cy,30*(1-age),rgba(col,.26f*(1-age)));
        }
        private void confetti(Canvas c,float t,long since) {
            if(since<0 || since>8500)return;
            float age=since/1000f;
            float fade= Math.min(1,Math.min(age*2.5f,(8.5f-age)*1.3f));
            int[] cols={0xffffd866,0xff53e8ff,0xfffa64be,0xff76ffc3,0xffe4b5ff};
            for(int i=0;i<confettiX.length;i++) {
                float x=(confettiX[i]+(float)Math.sin(age*1.3+confettiAngle[i])*50);
                float y=(confettiY[i]+age*65*confettiSpeed[i]+1100)%1200-50;
                c.save();c.rotate((age*110+i*57)%360,x,y);
                rounded(c,x-5,y-9,x+5,y+9,3,rgba(cols[i%cols.length],fade));
                c.restore();
            }
            for(int j=0;j<7;j++) {
                float elapsed=(since-j*620)/1150f;
                if(elapsed>=0&&elapsed<1)
                    firework(c,230+(j*303)%1480,135+(j*193)%560,elapsed,1.0f,cols[j%cols.length]);
            }
        }

        @Override protected void onDraw(Canvas actual) {
            super.onDraw(actual);
            int saved=actual.save();
            actual.scale(getWidth()/1920f,getHeight()/1080f);
            long now=SystemClock.uptimeMillis();
            float t=now;
            boolean sleep = now-lastInput>360000; // automatic OLED protection after six idle minutes
            if(sleep) {
                actual.drawColor(0xff02040b);
                float movingX=700+(float)Math.sin(t*.0002)*350;
                label(actual,"Appuie sur OK pour réveiller la magie ✨",movingX,560,32,0xff607496,
                    medium,Paint.Align.CENTER);
                actual.restoreToCount(saved);
                if(active) postInvalidateDelayed(2500);
                return;
            }

            background(actual,t);
            planets(actual,t);
            // Floating islands and clouds frame the center without obscuring the names.
            clouds(actual,90,910,1.8f);
            clouds(actual,1630,940,1.7f);
            clouds(actual,840,1040,1.45f);
            island(actual,1610,806,1.0f,0xff38c69f);
            island(actual,275,872,.62f,0xff37b2a8);
            castle(actual,1640,664,.69f);
            chest(actual,281,814,.70f);
            robot(actual,295,467,.86f,t);
            rocket(actual,1650,335,.78f,t);
            crown(actual,970,141,.72f);
            drawSpark(actual,535,230,21,0xffffda78,4);
            drawSpark(actual,1407,318,21,0xffffd26b,4);
            drawSpark(actual,1450,648,14,0xffb8f8ff,3);

            // Dynamic positioning gently shifts text to protect the OLED panel.
            float x=960+(float)Math.sin(t*.00023)*7;
            float shift=(float)Math.sin(t*.00017)*5;
            glowLabel(actual,"YASSINE",x,401+shift,205,0xffe4ffff,0xff20b7ff);
            label(actual,"ET",x-340,547+shift,70,0xfff5d2ff,strong,Paint.Align.CENTER);
            glowLabel(actual,"HEDI",x+48,582+shift,212,0xfffff1a0,0xffff8b26);

            // Welcome banner
            rounded(actual,470,640,1455,731,39,0xc51c205f);
            outline(actual,470,640,1455,731,39,0xfffe6bc7,5);
            label(actual,"Bienvenue les champions !",963,705,68,0xffffe68e,strong,Paint.Align.CENTER);
            label(actual,"Prêts pour l'aventure ?",960,793,53,0xffd8faff,medium,Paint.Align.CENTER);

            float pulse=(float)(.5+.5*Math.sin(t*.004));
            rounded(actual,631,839,1289,930,44,0xf22c4aa6);
            outline(actual,631,839,1289,930,44,0xff80e9ff,4+pulse*2);
            label(actual,"✦   APPUIE SUR OK POUR LA MAGIE   ✦",
                    960,896,38,0xffffffff,strong,Paint.Align.CENTER);

            label(actual,"◀  ▶    CHANGER DE MONDE",210,1004,28,0xffb8d6f9,medium,Paint.Align.LEFT);
            label(actual,worlds[world] + "   •   TV LAB  0.3",1690,1004,27,0xffb8d6f9,medium,Paint.Align.RIGHT);

            long elapsed=now-partyStart;
            confetti(actual,t,elapsed);
            if(elapsed>=0 && elapsed<7500) {
                rounded(actual,340,445,1580,606,46,0xe9141648);
                outline(actual,340,445,1580,606,46,0xffffd960,6);
                label(actual,winMessages[(partyCount-1)%winMessages.length],960,552,
                    partyCount%4==0?60:76,0xfffff2ae,strong,Paint.Align.CENTER);
            }
            actual.restoreToCount(saved);
            if(active) postInvalidateDelayed(48);
        }
    }
}
