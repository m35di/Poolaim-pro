package com.onyx.poolaim;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 1001;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ساخت layout به صورت برنامه‌نویسی (بدون XML)
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0A0A0A);
        root.setPadding(60, 120, 60, 60);
        root.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("PoolAim Pro");
        title.setTextColor(0xFF00FF00);
        title.setTextSize(28);
        title.setGravity(android.view.Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Aim + Predictor Tool");
        sub.setTextColor(0xFF888888);
        sub.setTextSize(13);
        sub.setGravity(android.view.Gravity.CENTER);
        sub.setPadding(0, 10, 0, 40);
        root.addView(sub);

        status = new TextView(this);
        status.setTextSize(16);
        status.setGravity(android.view.Gravity.CENTER);
        status.setPadding(20, 20, 20, 20);
        status.setBackgroundColor(0xFF1A1A1A);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        sp.bottomMargin = 40;
        root.addView(status, sp);

        Button grant = new Button(this);
        grant.setText("1. دادن دسترسی Overlay");
        grant.setTextSize(15);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160);
        bp.bottomMargin = 20;
        root.addView(grant, bp);

        Button start = new Button(this);
        start.setText("2. فعال‌سازی چیت");
        start.setTextSize(15);
        root.addView(start, bp);

        Button stop = new Button(this);
        stop.setText("3. خاموش کردن");
        stop.setTextSize(15);
        root.addView(stop, bp);

        TextView hint = new TextView(this);
        hint.setText("راهنما:\n۱. Overlay رو مجاز کن\n۲. فعال‌سازی بزن\n۳. برو تو بازی\n۴. دکمه 🎯 رو بزن");
        hint.setTextColor(0xFF666666);
        hint.setTextSize(12);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hp.topMargin = 40;
        root.addView(hint, hp);

        setContentView(root);

        updateStatus();

        grant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOverlay();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServiceNow();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopService(new Intent(MainActivity.this, OverlayService.class));
                    Toast.makeText(MainActivity.this, "خاموش شد", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "خطا", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateStatus() {
        try {
            if (Settings.canDrawOverlays(this)) {
                status.setText("وضعیت: آماده ✅");
                status.setTextColor(0xFF00FF00);
            } else {
                status.setText("وضعیت: دسترسی Overlay لازمه ❌");
                status.setTextColor(0xFFFF4444);
            }
        } catch (Exception ignored) {}
    }

    private void requestOverlay() {
        try {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "قبلاً داده شده", Toast.LENGTH_SHORT).show();
                updateStatus();
                return;
            }
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, REQ_OVERLAY);
        } catch (Exception e) {
            Toast.makeText(this, "خطا در باز کردن تنظیمات", Toast.LENGTH_SHORT).show();
        }
    }

    private void startServiceNow() {
        try {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "اول Overlay رو مجاز کن", Toast.LENGTH_LONG).show();
                return;
            }
            Intent svc = new Intent(this, OverlayService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            Toast.makeText(this, "✅ فعال شد", Toast.LENGTH_SHORT).show();
            moveTaskToBack(true);
        } catch (Exception e) {
            Toast.makeText(this, "خطا: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
