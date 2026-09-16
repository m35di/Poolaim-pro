package com.onyx.poolaim;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_OVERLAY = 1001;
    private static final int REQ_NOTIF = 1002;
    private TextView status;
    private Button grantBtn;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            status = findViewById(R.id.status);
            grantBtn = findViewById(R.id.grant_btn);
            startBtn = findViewById(R.id.start_btn);
            Button stopBtn = findViewById(R.id.stop_btn);

            updateStatus();

            grantBtn.setOnClickListener(v -> requestOverlay());
            startBtn.setOnClickListener(v -> startOverlayService());
            stopBtn.setOnClickListener(v -> {
                try {
                    stopService(new Intent(this, OverlayService.class));
                    Toast.makeText(this, "خاموش شد", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "خطا: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "خطای شروع: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateStatus() {
        try {
            boolean hasOverlay = Settings.canDrawOverlays(this);
            if (hasOverlay) {
                status.setText("✅ آماده — Overlay مجاز");
                status.setTextColor(0xFF00FF00);
                if (startBtn != null) startBtn.setEnabled(true);
            } else {
                status.setText("❌ دسترسی Overlay لازمه");
                status.setTextColor(0xFFFF4444);
                if (startBtn != null) startBtn.setEnabled(false);
            }
        } catch (Exception e) {
            if (status != null) status.setText("وضعیت نامشخص");
        }
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

    private void startOverlayService() {
        try {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "اول Overlay رو مجاز کن", Toast.LENGTH_LONG).show();
                return;
            }

            // درخواست نوتیفیکیشن برای اندروید ۱۳+
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
                    // ادامه می‌دیم — اگه رد کرد هم سرویس کار می‌کنه
                }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQ_OVERLAY) {
                updateStatus();
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
                                                      }
