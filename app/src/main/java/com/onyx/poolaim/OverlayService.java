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
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {

    private WindowManager wm;
    private FrameLayout guidelineLayer;
    private Button mainBtn;
    private LineView lineView;

    private boolean overlayOn = false;

    private WindowManager.LayoutParams lineParams;
    private WindowManager.LayoutParams mainBtnParams;

    private static final int TYPE_OVERLAY =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (wm == null) init();
        } catch (Exception e) {
            Toast.makeText(this, "خطا: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void init() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "دسترسی Overlay نداری", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) { stopSelf(); return; }

        startNotif();

        // لایه خطوط
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
                TYPE_OVERLAY, lineFlags, PixelFormat.TRANSLUCENT);
        lineParams.gravity = Gravity.TOP | Gravity.START;

        wm.addView(guidelineLayer, lineParams);
        guidelineLayer.setVisibility(View.GONE);

        // دکمه شناور
        mainBtn = new Button(this);
        mainBtn.setText("AIM");
        mainBtn.setTextSize(16);
        mainBtn.setBackgroundColor(0xCC00FF00);
        mainBtn.setAlpha(0.92f);

        mainBtnParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                TYPE_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mainBtnParams.gravity = Gravity.TOP | Gravity.START;
        mainBtnParams.x = 40;
        mainBtnParams.y = 400;

        mainBtn.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy;
            float tx, ty;
            boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                try {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            sx = mainBtnParams.x;
                            sy = mainBtnParams.y;
                            tx = e.getRawX();
                            ty = e.getRawY();
                            moved = false;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int)(e.getRawX() - tx);
                            int dy = (int)(e.getRawY() - ty);
                            if (Math.abs(dx) > 15 || Math.abs(dy) > 15) moved = true;
                            mainBtnParams.x = sx + dx;
                            mainBtnParams.y = sy + dy;
                            wm.updateViewLayout(mainBtn, mainBtnParams);
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (!moved) toggle();
                            return true;
                    }
                } catch (Exception ignored) {}
                return false;
            }
        });

        wm.addView(mainBtn, mainBtnParams);
    }

    private void toggle() {
        try {
            overlayOn = !overlayOn;
            if (overlayOn) {
                guidelineLayer.setVisibility(View.VISIBLE);
                mainBtn.setText("X");
                mainBtn.setBackgroundColor(0xCCFF0000);
                toast("خط فعال");
            } else {
                guidelineLayer.setVisibility(View.GONE);
                mainBtn.setText("AIM");
                mainBtn.setBackgroundColor(0xCC00FF00);
                toast("خاموش");
            }
        } catch (Exception ignored) {}
    }

    private void startNotif() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(
                        "onyx_ch", "PoolAim", NotificationManager.IMPORTANCE_LOW);
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) nm.createNotificationChannel(ch);
            }
            Notification n;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                n = new Notification.Builder(this, "onyx_ch")
                        .setContentTitle("PoolAim فعاله")
                        .setSmallIcon(android.R.drawable.ic_menu_view)
                        .build();
            } else {
                n = new Notification.Builder(this)
                        .setContentTitle("PoolAim فعاله")
                        .setSmallIcon(android.R.drawable.ic_menu_view)
                        .build();
            }
            startForeground(1, n);
        } catch (Exception ignored) {}
    }

    private void toast(String s) {
        try { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if (guidelineLayer != null && wm != null) wm.removeView(guidelineLayer); } catch (Exception ignored) {}
        try { if (mainBtn != null && wm != null) wm.removeView(mainBtn); } catch (Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
