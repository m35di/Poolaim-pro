package com.onyx.poolaim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineView extends View {

    private Paint mainPaint;
    private Paint predictPaint;
    private Paint cushionPaint;
    private Paint circlePaint;
    private Paint cuePaint;

    private int screenW = 0, screenH = 0;
    private int mode = 0;
    private int colorIndex = 0;
    private int thicknessIndex = 1;

    private PointF cuePoint = new PointF();
    private PointF ballPoint = new PointF();
    private PointF cueDefault = new PointF();
    private boolean ballPointSet = false;

    private List<PointF> predictedPath = new ArrayList<>();

    private static final int[] COLORS = {
            0xFF00FF00, 0xFFFF00FF, 0xFF00FFFF, 0xFFFF6600
    };
    private static final float[] THICKNESS = { 3f, 5f, 8f };

    public LineView(Context ctx) {
        super(ctx);
        try {
            mainPaint = new Paint();
            mainPaint.setAntiAlias(true);
            mainPaint.setStyle(Paint.Style.STROKE);
            mainPaint.setStrokeCap(Paint.Cap.ROUND);

            predictPaint = new Paint();
            predictPaint.setAntiAlias(true);
            predictPaint.setStyle(Paint.Style.STROKE);
            predictPaint.setStrokeCap(Paint.Cap.ROUND);

            cushionPaint = new Paint();
            cushionPaint.setAntiAlias(true);
            cushionPaint.setStyle(Paint.Style.STROKE);
            cushionPaint.setStrokeCap(Paint.Cap.ROUND);
            cushionPaint.setPathEffect(new DashPathEffect(new float[]{25f, 15f}, 0));

            circlePaint = new Paint();
            circlePaint.setAntiAlias(true);
            circlePaint.setStyle(Paint.Style.FILL);

            cuePaint = new Paint();
            cuePaint.setAntiAlias(true);
            cuePaint.setStyle(Paint.Style.FILL);
            cuePaint.setColor(Color.WHITE);

            applyColors();
            setLayerType(LAYER_TYPE_HARDWARE, null);
        } catch (Exception ignored) {}
    }

    private void applyColors() {
        try {
            int c = COLORS[colorIndex % COLORS.length];
            float t = THICKNESS[thicknessIndex % THICKNESS.length];

            mainPaint.setColor(c);
            mainPaint.setStrokeWidth(t);

            predictPaint.setColor(0xFFFFEB3B);
            predictPaint.setStrokeWidth(t - 1);

            cushionPaint.setColor(0xFF00BCD4);
            cushionPaint.setStrokeWidth(Math.max(2f, t - 2));

            circlePaint.setColor(0xFFFF1744);
        } catch (Exception ignored) {}
    }

    public void setMode(int m) {
        try {
            mode = m;
            invalidate();
        } catch (Exception ignored) {}
    }

    public void cycleColor() {
        try {
            colorIndex = (colorIndex + 1) % COLORS.length;
            applyColors();
            invalidate();
        } catch (Exception ignored) {}
    }

    public void cycleThickness() {
        try {
            thicknessIndex = (thicknessIndex + 1) % THICKNESS.length;
            applyColors();
            invalidate();
        } catch (Exception ignored) {}
    }

    public void moveCue(float dx, float dy) {
        try {
            cueDefault.x += dx;
            cueDefault.y += dy;
            cuePoint.set(cueDefault);
            recalc();
            invalidate();
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                if (x >= 0 && y >= 0 && x <= screenW && y <= screenH) {
                    ballPoint.set(x, y);
                    ballPointSet = true;
                    recalc();
                    invalidate();
                }
                return true;
            }
        } catch (Exception ignored) {}
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        try {
            screenW = w;
            screenH = h;
            if (!ballPointSet) {
                cueDefault.set(w * 0.10f, h * 0.80f);
                cuePoint.set(cueDefault);
                ballPoint.set(w * 0.60f, h * 0.50f);
            }
            recalc();
        } catch (Exception ignored) {}
    }

    private void recalc() {
        try {
            predictedPath.clear();
            if (screenW <= 0 || screenH <= 0) return;
            if (cuePoint == null || ballPoint == null) return;

            float dx = ballPoint.x - cuePoint.x;
            float dy = ballPoint.y - cuePoint.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 5f) return;

            float dirX = dx / len;
            float dirY = dy / len;

            float px = ballPoint.x;
            float py = ballPoint.y;

            float cxMin = screenW * 0.05f;
            float cyMin = screenH * 0.05f;
            float cxMax = screenW * 0.95f;
            float cyMax = screenH * 0.95f;

            predictedPath.add(new PointF(px, py));

            for (int i = 0; i < 4; i++) {
                float tMin = Float.MAX_VALUE;
                int hit = -1;

                if (dirX < -0.0001f) {
                    float t = (cxMin - px) / dirX;
                    if (t > 0 && t < tMin) { tMin = t; hit = 0; }
                } else if (dirX > 0.0001f) {
                    float t = (cxMax - px) / dirX;
                    if (t > 0 && t < tMin) { tMin = t; hit = 1; }
                }

                if (dirY < -0.0001f) {
                    float t = (cyMin - py) / dirY;
                    if (t > 0 && t < tMin) { tMin = t; hit = 2; }
                } else if (dirY > 0.0001f) {
                    float t = (cyMax - py) / dirY;
                    if (t > 0 && t < tMin) { tMin = t; hit = 3; }
                }

                if (hit == -1 || tMin == Float.MAX_VALUE || tMin > 5000f) break;

                float hitX = px + dirX * tMin;
                float hitY = py + dirY * tMin;

                predictedPath.add(new PointF(hitX, hitY));

                if (hit == 0 || hit == 1) dirX = -dirX;
                if (hit == 2 || hit == 3) dirY = -dirY;

                px = hitX + dirX * 2f;
                py = hitY + dirY * 2f;
            }

            predictedPath.add(new PointF(px, py));
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (screenW <= 0 || screenH <= 0) return;
            if (canvas == null) return;

            // خط اصلی
            canvas.drawLine(cuePoint.x, cuePoint.y, ballPoint.x, ballPoint.y, mainPaint);

            // نقطه cue
            canvas.drawCircle(cuePoint.x, cuePoint.y, 16f, cuePaint);

            // توپ هدف
            canvas.drawCircle(ballPoint.x, ballPoint.y, 20f, circlePaint);

            // مسیر پیش‌بینی
            if (predictedPath.size() >= 2) {
                for (int i = 0; i < predictedPath.size() - 1; i++) {
                    PointF p1 = predictedPath.get(i);
                    PointF p2 = predictedPath.get(i + 1);
                    if (p1 == null || p2 == null) continue;

                    Paint p = (i == 0) ? predictPaint : cushionPaint;
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, p);

                    if (i > 0 && i < predictedPath.size() - 1) {
                        canvas.drawCircle(p1.x, p1.y, 7f, predictPaint);
                    }
                }
            }

            // حالت Box
            if (mode == 2) {
                for (int i = 1; i <= 3; i++) {
                    float off = i * 50f;
                    canvas.drawLine(cuePoint.x, cuePoint.y - off, ballPoint.x, ballPoint.y - off, cushionPaint);
                    canvas.drawLine(cuePoint.x, cuePoint.y + off, ballPoint.x, ballPoint.y + off, cushionPaint);
                }
            }
        } catch (Exception ignored) {}
    }
}
