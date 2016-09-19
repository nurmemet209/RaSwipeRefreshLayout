package com.example.nurmemet.raswiperefreshlayout;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.animation.LinearInterpolator;

/**
 * Created by nurmemet on 9/17/2016.
 */
public class RaSwipeRefreshDrawable extends Drawable {

    public enum SwipeState {PULL_TO_REFRESH, RELEASE_TO_REFRESH, REFRESHING}

    ;

    private SwipeState mState = SwipeState.PULL_TO_REFRESH;

    private Drawable advertizeDrawable;
    private Drawable indicatorDrawable;
    private TextPaint textPaint;
    private String pull2Refresh = "下拉刷新";
    private String release2Refresh = "松开刷新";
    private String refreshing = "正在刷新...";
    private int textSize = 14;
    private Rect boundsRect = new Rect();
    private Rect advertizeBounds = new Rect();
    private Rect textBounds = new Rect();
    private Rect indicatorBounds = new Rect();
    private int rotateDegree = 0;
    /**
     * 以px为单位
     */
    private int textPadding = 20;

    public RaSwipeRefreshDrawable(Drawable indicatorDrawable, Drawable advertizeDrawable) {
        this.indicatorDrawable = indicatorDrawable;
        this.advertizeDrawable = advertizeDrawable;
        init();
    }

    private void init() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(45);
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);

        float indicatorWidth = indicatorDrawable.getIntrinsicWidth();
        float advertizeWidth = advertizeDrawable.getIntrinsicWidth();
        boundsRect.right = (int) Math.max(indicatorWidth + getMaxTextWidth(), advertizeWidth);
        advertizeBounds.top = 0;
        advertizeBounds.bottom = advertizeDrawable.getIntrinsicHeight();
        if (boundsRect.right > advertizeWidth) {
            advertizeBounds.left = (int) (boundsRect.right - advertizeWidth) / 2;
            advertizeBounds.right = advertizeDrawable.getIntrinsicWidth() + advertizeBounds.left;
            indicatorBounds.left = 0;
        } else {
            advertizeBounds.left = 0;
            advertizeBounds.right = (int) advertizeWidth;
            indicatorBounds.left = (int) (boundsRect.right - indicatorWidth - getMaxTextWidth()) / 2;
        }
        indicatorBounds.top = advertizeDrawable.getIntrinsicHeight();
        indicatorBounds.bottom = indicatorBounds.top + indicatorDrawable.getIntrinsicHeight();
        indicatorBounds.right = indicatorBounds.left + indicatorDrawable.getIntrinsicWidth();

        textBounds.left = indicatorBounds.right + textPadding;
        textBounds.right = (int) (textBounds.left + getMaxTextWidth());
        textBounds.top = indicatorBounds.top;
        textBounds.bottom = indicatorBounds.bottom;


        advertizeDrawable.setBounds(advertizeBounds);
        indicatorDrawable.setBounds(indicatorBounds);

        boundsRect.bottom = advertizeBounds.height() + indicatorBounds.height();


    }

    private String getDrawString() {
        String str = "iiiii";
        switch (mState) {
            case PULL_TO_REFRESH:
                str = pull2Refresh;
                break;
            case REFRESHING:
                str = refreshing;
                break;
            case RELEASE_TO_REFRESH:
                str = release2Refresh;
                break;

        }
        return str;
    }

    private float getMaxTextWidth() {
        float width = 0;
        width = Math.max(width, textPaint.measureText(pull2Refresh));
        width = Math.max(width, textPaint.measureText(release2Refresh));
        width = Math.max(width, textPaint.measureText(refreshing));
        return width;
    }

    @Override
    public void draw(Canvas canvas) {
        advertizeDrawable.draw(canvas);

        canvas.save();
        final int px = indicatorBounds.left + indicatorBounds.width() / 2;
        final int py = indicatorBounds.top + indicatorBounds.height() / 2;
        canvas.rotate(rotateDegree, px, py);
        indicatorDrawable.draw(canvas);
        canvas.restore();

        String str = getDrawString();
        resetTextBounds(str);
        canvas.drawText(str, textBounds.left, textBounds.bottom, textPaint);
    }

    private void resetTextBounds(String str) {
        Rect tBounds = new Rect();
        textPaint.getTextBounds(str, 0, str.length(), tBounds);
        int diff = (textBounds.height() - tBounds.height()) / 2;
        textBounds.top += diff;
        textBounds.bottom -= diff;

    }

    public void set2State(SwipeState state) {
        if (mState != null) {

            if (mState == SwipeState.PULL_TO_REFRESH && state == SwipeState.RELEASE_TO_REFRESH) {
                ValueAnimator animator = ValueAnimator.ofInt(0, 180);
                animator.setDuration(400);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (Integer) animation.getAnimatedValue();
                        rotateDegree = value;
                        invalidateSelf();
                    }
                });
                animator.start();
            } else if (mState == SwipeState.RELEASE_TO_REFRESH && state == SwipeState.PULL_TO_REFRESH) {
                ValueAnimator animator = ValueAnimator.ofInt(180, 0);
                animator.setDuration(400);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (Integer) animation.getAnimatedValue();
                        rotateDegree = value;
                        invalidateSelf();
                    }
                });
                animator.start();
            }
            mState = state;
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return boundsRect.height();
    }

    @Override
    public int getIntrinsicWidth() {
        return boundsRect.width();
    }

    @Override
    public void setAlpha(int alpha) {
        if (advertizeDrawable != null) {
            advertizeDrawable.setAlpha(alpha);
        }
        indicatorDrawable.setAlpha(alpha);
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (advertizeDrawable != null) {
            advertizeDrawable.setColorFilter(colorFilter);
        }
        indicatorDrawable.setColorFilter(colorFilter);
        textPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return indicatorDrawable != null ? indicatorDrawable.getOpacity() : PixelFormat.TRANSPARENT;
    }
}
