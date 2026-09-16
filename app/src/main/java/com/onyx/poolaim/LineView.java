package com.onyx.poolaim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineView extends View {

    private Paint mainPaint, predictPaint, cushionPaint, circlePaint, cuePaint;
    private int W = 0, H = 0;
    private PointF cue = new PointF();
    private PointF ball = new PointF();
    private List<PointF> path = new ArrayList<>();

    public LineView(Context ctx) {
        super(ctx);
        try {
            mainPaint = new Paint();
            mainPaint.setColor(0xFF00FF00);
            mainPaint.setStrokeWidth(5f);
            mainPaint.setAntiAlias(true);
            mainPaint.setStyle(Paint.Style.STROKE);

            predictPaint = new Paint();
            predictPaint.setColor(0xFFFFEB3B);
            predictPaint.setStrokeWidth(4f);
            predictPaint.setAntiAlias(true);
            predictPaint.setStyle(Paint.Style.STROKE);

            cushionPaint = new Paint();
            cushionPaint.setColor(0xFF00BCD4);
            cushionPaint.setStrokeWidth(3f);
            cushionPaint.setAntiAlias(true);
            cushionPaint.setStyle(Paint.Style.STROKE);
            cushionPaint.setPathEffect(new DashPathEffect(new float[]{25f, 15f}, 0));

            circlePaint = new Paint();
            circlePaint.setColor(0xFFFF1744);
            circlePaint.setAntiAlias(true);
            circlePaint.setStyle(Paint.Style.FILL);

            cuePaint = new Paint();
            cuePaint.setColor(Color.WHITE);
            cuePaint.setAntiAlias(true);
            cuePaint.setStyle(Paint.Style.FILL);

            setLayerType(LAYER_TYPE_HARDWARE, null);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        W = w; H = h;
        cue.set(w * 0.10f, h * 0.80f);
        ball.set(w * 0.60f, h * 0.50f);
        recalc();
    }

    private void recalc() {
        try {
            path.clear();
            if (W <= 0 || H <= 0) return;

            float dx = ball.x - cue.x;
            float dy = ball.y - cue.y;
            float len = (float) Math.sqrt(dx*dx + dy*dy);
            if (len < 5f) return;

            float dirX = dx / len;
            float dirY = dy / len;

            float px = ball.x, py = ball.y;
            float cxMin = W * 0.05f, cyMin = H * 0.05f;
            float cxMax = W * 0.95f, cyMax = H * 0.95f;

            path.add(new PointF(px, py));

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

                float hx = px + dirX * tMin;
                float hy = py + dirY * tMin;
                path.add(new PointF(hx, hy));

                if (hit == 0 || hit == 1) dirX = -dirX;
                if (hit == 2 || hit == 3) dirY = -dirY;

                px = hx + dirX * 2f;
                py = hy + dirY * 2f;
            }
            path.add(new PointF(px, py));
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        try {
            if (W <= 0 || H <= 0 || c == null) return;

            c.drawLine(cue.x, cue.y, ball.x, ball.y, mainPaint);
            c.drawCircle(cue.x, cue.y, 16f, cuePaint);
            c.drawCircle(ball.x, ball.y, 20f, circlePaint);

            if (path.size() >= 2) {
                for (int i = 0; i < path.size() - 1; i++) {
                    PointF p1 = path.get(i);
                    PointF p2 = path.get(i + 1);
                    if (p1 == null || p2 == null) continue;
                    Paint p = (i == 0) ? predictPaint : cushionPaint;
                    c.drawLine(p1.x, p1.y, p2.x, p2.y, p);
                    if (i > 0 && i < path.size() - 1) {
                        c.drawCircle(p1.x, p1.y, 7f, predictPaint);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
