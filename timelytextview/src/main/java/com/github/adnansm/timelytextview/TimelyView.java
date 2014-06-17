package com.github.adnansm.timelytextview;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.github.adnansm.timelytextview.animation.TimelyEvaluator;
import com.github.adnansm.timelytextview.model.NumberUtils;
import com.github.adnansm.timelytextview.model.number.Nothing;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.util.Property;

public class TimelyView extends View {
    private static       int                             ascent                  = 1536;
    private static       int                             descent                 = -128;
    private static final float                           RATIO                   = 1164f / (float) (ascent - descent);
    private static final Property<TimelyView, float[][]> CONTROL_POINTS_PROPERTY = new Property<TimelyView, float[][]>(float[][].class, "controlPoints") {
        @Override
        public float[][] get(TimelyView object) {
            return object.getControlPoints();
        }

        @Override
        public void set(TimelyView object, float[][] value) {
            object.setControlPoints(value);
        }
    };
    private static final boolean                         DEBUG                   = false;
    private              Paint                           mPaint                  = null;
    private              Paint                           mDebugPaint             = null;
    private              Path                            mPath                   = null;
    private              float[][]                       controlPoints           = null;

    public TimelyView(Context context) {
        super(context);
        init();
    }

    public TimelyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimelyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public float[][] getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(float[][] controlPoints) {
        this.controlPoints = controlPoints;
        invalidate();
    }

    public ObjectAnimator animate(int start, int end) {
        float[][] startPoints = NumberUtils.getControlPointsFor(start);
        float[][] endPoints = NumberUtils.getControlPointsFor(end);

        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(), startPoints, endPoints);
    }

    public ObjectAnimator animate(int to, boolean appearing) {
        float[][] nothingPoints = Nothing.getInstance().getControlPoints();
        float[][] numberPoints = NumberUtils.getControlPointsFor(to);

        float[][] startPoints = appearing ? nothingPoints : numberPoints;
        float[][] endPoints = appearing ? numberPoints : nothingPoints;

        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(), startPoints, endPoints);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (controlPoints == null) return;

        int length = controlPoints.length;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        Matrix scaleMatrix = new Matrix();
        float scaleFactor = (float) height / (ascent - descent);
        scaleMatrix.setTranslate(0, ascent * scaleFactor);
        scaleMatrix.preScale(scaleFactor, -scaleFactor);

        mPath.reset();
        mPath.moveTo(controlPoints[0][0], controlPoints[0][1]);
        for (int i = 1; i < length; i += 2) {
            mPath.quadTo(controlPoints[i][0], controlPoints[i][1],
                         controlPoints[i + 1][0], controlPoints[i + 1][1]);
        }

        RectF rectF = new RectF();
        mPath.computeBounds(rectF, true);

        mPath.transform(scaleMatrix);

        canvas.drawPath(mPath, mPaint);
        if (DEBUG) {
            canvas.drawPath(mPath, mDebugPaint);
            drawControlPoint(controlPoints[0], "0", -descent, scaleFactor, canvas);
            for (int i = 1; i < length; i += 2) {
                drawControlPoint(controlPoints[i + 1], String.valueOf((i + 1) / 2), -descent, scaleFactor, canvas);
            }
        }
    }

    private void drawControlPoint(float[] controlPoint, String text, int yTranslation, float scaleFactor, Canvas canvas) {
        canvas.save();
        canvas.scale(scaleFactor, -scaleFactor, 0, yTranslation);
        canvas.drawCircle(controlPoint[0], controlPoint[1], 16, mDebugPaint);
        canvas.restore();
        canvas.drawText(text, controlPoint[0] * scaleFactor, canvas.getHeight() - (controlPoint[1] + yTranslation) * scaleFactor, mDebugPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        int maxWidth = (int) (heigthWithoutPadding * RATIO);
        int maxHeight = (int) (widthWithoutPadding / RATIO);

        if (widthWithoutPadding > maxWidth) {
            width = maxWidth + getPaddingLeft() + getPaddingRight();
        } else {
            height = maxHeight + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(width, height);
    }

    private void init() {
        // A new paint with the style as stroke.
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPath = new Path();

        if (DEBUG) {
            mDebugPaint = new Paint();
            mDebugPaint.setAntiAlias(true);
            mDebugPaint.setColor(Color.BLUE);
            mDebugPaint.setStrokeWidth(4.0f);
            mDebugPaint.setTextSize(42);
            mDebugPaint.setStyle(Paint.Style.STROKE);
        }
    }
}
