package com.priveat.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.priveat.app.R;

public class CalorieRingView extends View {
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private float progress = 0f;

    public CalorieRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(28f);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.card_border));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(28f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(ContextCompat.getColor(context, R.color.purple_primary));
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(progress, 1f));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = 32f;
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawArc(rectF, 135f, 270f, false, backgroundPaint);
        canvas.drawArc(rectF, 135f, 270f * progress, false, progressPaint);
    }
}
