package com.onyx.poolaim;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class OverlayService extends Service {

    private WindowManager wm;
    private FrameLayout guidelineLayer;
    private Button mainBtn;
    private Button modeBtn;
    private Button aimBtn;
    private Button colorBtn;
    private Button thickBtn;
    private Button cueUpBtn;
    private Button cueDownBtn;

    private LineView lineView;
    private boolean overlayOn = false;
    private boolean aimMode = false;
    private int currentMode = 0;

    private WindowManager.LayoutParams lineParams;
    private WindowManager.LayoutParams mainBtnParams;
    private WindowManager.LayoutParams modeBtnParams;
    private WindowManager.LayoutParams aimBtnParams;
    private WindowManager.LayoutParams colorBtnParams;
    private WindowManager.LayoutParams thickBtnParams;
    private WindowManager.LayoutParams cueUpParams;
    private WindowManager.LayoutParams cueDownParams;

    private static final int TYPE_OVERLAY =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (wm == null) {
                initOverlay();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطا: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void initOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "دسترسی Overlay نداری", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) {
            stopSelf();
            return;
        }

        // نوتیفیکیشن سرویس
        startNotification();

        // ============ لایه خطوط ============
        guidelineLayer = new FrameLayout(this);
        guidelineLayer.setBackgroundColor(Color.TRANSPARENT);

        lineView = new LineView(this);
        guidelineLayer.addView(lineView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        int lineFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        lineParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                TYPE_OVERLAY,
                lineFlags,
                PixelFormat.TRANSLUCENT);
        lineParams.gravity = Gravity.TOP | Gravity.START;

        wm.addView(guidelineLayer, lineParams);
        guidelineLayer.setVisibility(View.GONE);

        // ============ ساخت دکمه‌ها ============
        mainBtn = makeButton("🎯", "#CC00FF00");
        mainBtnParams = makeParams(40, 400);
        attachDrag(mainBtn, mainBtnParams, "main");
        wm.addView(mainBtn, mainBtnParams);

        modeBtn = makeButton("L", "#CC0066FF");
        modeBtnParams = makeParams(40, 550);
        attachDrag(modeBtn, modeBtnParams, "mode");
        wm.addView(modeBtn, modeBtnParams);
        modeBtn.setVisibility(View.GONE);

        aimBtn = makeButton("👆", "#CCFF6600");
        aimBtnParams = makeParams(40, 700);
        attachDrag(aimBtn, aimBtnParams, "aim");
        wm.addView(aimBtn, aimBtnParams);
        aimBtn.setVisibility(View.GONE);

        colorBtn = makeButton("🎨", "#CC6600CC");
        colorBtnParams = makeParams(40, 850);
        attachDrag(colorBtn, colorBtnParams, "color");
        wm.addView(colorBtn, colorBtnParams);
        colorBtn.setVisibility(View.GONE);

        thickBtn = makeButton("➖", "#CC444444");
        thickBtnParams = makeParams(40, 1000);
        attachDrag(thickBtn, thickBtnParams, "thick");
        wm.addView(thickBtn, thickBtnParams);
        thickBtn.setVisibility(View.GONE);

        cueUpBtn = makeButton("⬆", "#CC0099AA");
        cueUpParams = makeParams(40, 1150);
        attachDrag(cueUpBtn, cueUpParams, "cueup");
        wm.addView(cueUpBtn, cueUpParams);
        cueUpBtn.setVisibility(View.GONE);

        cueDownBtn = makeButton("⬇", "#CC0099AA");
        cueDownParams = makeParams(40, 1300);
        attachDrag(cueDownBtn, cueDownParams, "cuedown");
        wm.addView(cueDownBtn, cueDownParams);
        cueDownBtn.setVisibility(View.GONE);
    }

    private Button makeButton(String text, String colorHex) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(18);
        try {
            b.setBackgroundColor(Color.parseColor(colorHex));
        } catch (Exception ignored) {}
        b.setAlpha(0.92f);
        b.setPadding(20, 10, 20, 10);
        return b;
    }

    private WindowManager.LayoutParams makeParams(int x, int y) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                TYPE_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = x;
        p.y = y;
        return p;
    }

    private void attachDrag(final Button btn, final WindowManager.LayoutParams params, final String type) {
        btn.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;
            float touchX, touchY;
            boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                try {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = params.x;
                            startY = params.y;
                            touchX = e.getRawX();
                            touchY = e.getRawY();
                            moved = false;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int) (e.getRawX() - touchX);
                            int dy = (int) (e.getRawY() - touchY);
                            if (Math.abs(dx) > 15 || Math.abs(dy) > 15) moved = true;
                            params.x = startX + dx;
                            params.y = startY + dy;
                            if (wm != null) wm.updateViewLayout(btn, params);
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (!moved) handleClick(type);
                            return true;
                    }
                } catch (Exception ignored) {}
                return false;
            }
        });
    }

    private void handleClick(String type) {
        try {
            switch (type) {
                case "main": toggleOverlay(); break;
                case "mode": switchMode(); break;
                case "aim": toggleAimMode(); break;
                case "color":
                    if (lineView != null) lineView.cycleColor();
                    toast("رنگ عوض شد");
                    break;
                case "thick":
                    if (lineView != null) lineView.cycleThickness();
                    toast("ضخامت عوض شد");
                    break;
                case "cueup":
                    if (lineView != null) lineView.moveCue(0, -15);
                    break;
                case "cuedown":
                    if (lineView != null) lineView.moveCue(0, 15);
                    break;
            }
        } catch (Exception ignored) {}
    }

    private void toggleOverlay() {
        try {
            overlayOn = !overlayOn;
            int[] hideable = {View.VISIBLE, View.GONE};
            int v = overlayOn ? View.VISIBLE : View.GONE;
            int g = overlayOn ? View.GONE : View.VISIBLE;

            guidelineLayer.setVisibility(v);
            modeBtn.setVisibility(v);
            aimBtn.setVisibility(v);
            colorBtn.setVisibility(v);
            thickBtn.setVisibility(v);
            cueUpBtn.setVisibility(v);
            cueDownBtn.setVisibility(v);

            if (overlayOn) {
                mainBtn.setText("✖");
                mainBtn.setBackgroundColor(Color.parseColor("#CCFF0000"));
                toast("خط فعال");
            } else {
                mainBtn.setText("🎯");
                mainBtn.setBackgroundColor(Color.parseColor("#CC00FF00"));
                aimMode = false;
                updateLineTouchability();
                toast("خاموش");
            }
        } catch (Exception ignored) {}
    }

    private void switchMode() {
        try {
            currentMode = (currentMode + 1) % 3;
            switch (currentMode) {
                case 0:
                    modeBtn.setText("L");
                    modeBtn.setBackgroundColor(Color.parseColor("#CC0066FF"));
                    toast("Long");
                    break;
                case 1:
                    modeBtn.setText("T");
                    modeBtn.setBackgroundColor(Color.parseColor("#CCFF6600"));
                    toast("Triple + Predictor");
                    break;
                case 2:
                    modeBtn.setText("B");
                    modeBtn.setBackgroundColor(Color.parseColor("#CC9900FF"));
                    toast("Box");
                    break;
            }
            if (lineView != null) lineView.setMode(currentMode);
        } catch (Exception ignored) {}
    }

    private void toggleAimMode() {
        try {
            aimMode = !aimMode;
            if (aimMode) {
                aimBtn.setText("🔒");
                aimBtn.setBackgroundColor(Color.parseColor("#CC00AA00"));
            } else {
                aimBtn.setText("👆");
                aimBtn.setBackgroundColor(Color.parseColor("#CCFF6600"));
            }
            updateLineTouchability();
        } catch (Exception ignored) {}
    }

    private void updateLineTouchability() {
        try {
            if (lineParams == null || wm == null || guidelineLayer == null) return;

            int baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

            if (aimMode) {
                lineParams.flags = baseFlags;
            } else {
                lineParams.flags = baseFlags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            }
            wm.updateViewLayout(guidelineLayer, lineParams);
        } catch (Exception ignored) {}
    }

    private void startNotification() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(
                        "poolaim_ch",
                        "PoolAim",
                        NotificationManager.IMPORTANCE_LOW);
                ch.setShowBadge(false);
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) nm.createNotificationChannel(ch);
            }

            Notification notif;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notif = new Notification.Builder(this, "poolaim_ch")
                        .setContentTitle("PoolAim Pro")
                        .setContentText("فعال — دکمه 🎯 رو بزن")
                        .setSmallIcon(android.R.drawable.ic_menu_view)
                        .setOngoing(true)
                        .build();
            } else {
                notif = new Notification.Builder(this)
                        .setContentTitle("PoolAim Pro")
                        .setContentText("فعال")
                        .setSmallIcon(android.R.drawable.ic_menu_view)
                        .build();
            }

            startForeground(1, notif);
        } catch (Exception ignored) {}
    }

    private void toast(String s) {
        try {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if (guidelineLayer != null && wm != null) wm.removeView(guidelineLayer); } catch (Exception ignored) {}
        try { if (mainBtn != null && wm != null) wm.removeView(mainBtn); } catch (Exception ignored) {}
        try { if (modeBtn != null && wm != null) wm.removeView(modeBtn); } catch (Exception ignored) {}
        try { if (aimBtn != null && wm != null) wm.removeView(aimBtn); } catch (Exception ignored) {}
        try { if (colorBtn != null && wm != null) wm.removeView(colorBtn); } catch (Exception ignored) {}
        try { if (thickBtn != null && wm != null) wm.removeView(thickBtn); } catch (Exception ignored) {}
        try { if (cueUpBtn != null && wm != null) wm.removeView(cueUpBtn); } catch (Exception ignored) {}
        try { if (cueDownBtn != null && wm != null) wm.removeView(cueDownBtn); } catch (Exception ignored) {}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
