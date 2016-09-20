package com.example.nurmemet.raswiperefreshlayout;

import android.animation.ValueAnimator;
import android.content.Context;
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
    private Drawable mAdvertizeDrawable;
    private Drawable mIndicatorDrawable;
    private TextPaint mTextPaint;
    private final static String PULL2REFRESH = "下拉刷新";
    private final static String RELEASE2REFRESH = "松开刷新";
    private final static String REFRESHING = "正在刷新...";
    private int mTextSize = 45;
    private Rect mBoundsRect = new Rect();
    private Rect mAdvertizeBounds = new Rect();
    private Rect mTextBounds = new Rect();
    private Rect mIndicatorBounds = new Rect();
    private int mRotateDegree = 0;
    private Rect mRefreshingRect = null;
    private Drawable mRefreshingDrawable;
    private ValueAnimator mAnimator;
    /**
     * 以px为单位
     */
    private int mTextPadding = 8;
    private int mIndicatorPadding = 8;
    private Context mContext;

    public RaSwipeRefreshDrawable(Drawable indicatorDrawable, Drawable advertizeDrawable, Drawable refreshingDrawable, Context context) {
        this.mIndicatorDrawable = indicatorDrawable;
        this.mAdvertizeDrawable = advertizeDrawable;
        this.mRefreshingDrawable = refreshingDrawable;
        this.mContext = context;
        init();
    }

    private void init() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL);

        final float indicatorWidth = mIndicatorDrawable.getIntrinsicWidth();
        final float advertizeWidth = mAdvertizeDrawable.getIntrinsicWidth();
        mBoundsRect.right = (int) Math.max(indicatorWidth + getMaxTextWidth(), advertizeWidth);
        mAdvertizeBounds.top = 0;
        mAdvertizeBounds.bottom = mAdvertizeDrawable.getIntrinsicHeight();
        if (mBoundsRect.right > advertizeWidth) {
            mAdvertizeBounds.left = (int) (mBoundsRect.right - advertizeWidth) / 2;
            mAdvertizeBounds.right = mAdvertizeDrawable.getIntrinsicWidth() + mAdvertizeBounds.left;
            mIndicatorBounds.left = 0;
        } else {
            mAdvertizeBounds.left = 0;
            mAdvertizeBounds.right = (int) advertizeWidth;
            mIndicatorBounds.left = (int) (mBoundsRect.right - indicatorWidth - getMaxTextWidth()) / 2;
        }
        mIndicatorBounds.top = mAdvertizeDrawable.getIntrinsicHeight() + dp2px(mContext, mIndicatorPadding);
        mIndicatorBounds.bottom = mIndicatorBounds.top + mIndicatorDrawable.getIntrinsicHeight();
        mIndicatorBounds.right = mIndicatorBounds.left + mIndicatorDrawable.getIntrinsicWidth();

        mTextBounds.left = mIndicatorBounds.right + dp2px(mContext, mTextPadding);
        mTextBounds.right = (int) (mTextBounds.left + getMaxTextWidth());
        mTextBounds.top = mIndicatorBounds.top;
        mTextBounds.bottom = mIndicatorBounds.bottom;


        mAdvertizeDrawable.setBounds(mAdvertizeBounds);
        mIndicatorDrawable.setBounds(mIndicatorBounds);

        mBoundsRect.bottom = mAdvertizeBounds.height() + mIndicatorBounds.height() + dp2px(mContext, mIndicatorPadding) * 2;


    }

    public int getMainItemHeight() {

        final int height = dp2px(mContext, mIndicatorPadding) * 2 + mIndicatorBounds.height();
        return height;
    }

    private String getDrawString() {
        String str = "";
        switch (mState) {
            case PULL_TO_REFRESH:
                str = PULL2REFRESH;
                break;
            case REFRESHING:
                str = REFRESHING;
                break;
            case RELEASE_TO_REFRESH:
                str = RELEASE2REFRESH;
                break;

        }
        return str;
    }

    private float getMaxTextWidth() {
        float width = 0;
        width = Math.max(width, mTextPaint.measureText(PULL2REFRESH));
        width = Math.max(width, mTextPaint.measureText(RELEASE2REFRESH));
        width = Math.max(width, mTextPaint.measureText(REFRESHING));
        return width;
    }

    @Override
    public void draw(Canvas canvas) {
        mAdvertizeDrawable.draw(canvas);
        canvas.save();
        final int px = mIndicatorBounds.left + mIndicatorBounds.width() / 2;
        final int py = mIndicatorBounds.top + mIndicatorBounds.height() / 2;
        canvas.rotate(mRotateDegree, px, py);
        if (mState != SwipeState.REFRESHING) {
            mIndicatorDrawable.draw(canvas);
        } else {
            if (mRefreshingRect == null) {
                mRefreshingRect = new Rect(mIndicatorBounds);
                mRefreshingRect.top += (mRefreshingRect.height() - mRefreshingDrawable.getIntrinsicHeight()) / 2;
                mRefreshingRect.bottom = mRefreshingRect.top + mRefreshingDrawable.getIntrinsicHeight();
                mRefreshingRect.left += (mRefreshingRect.width() - mRefreshingDrawable.getIntrinsicWidth()) / 2;
                mRefreshingRect.right = mRefreshingRect.left + mRefreshingDrawable.getIntrinsicWidth();
                mRefreshingDrawable.setBounds(mRefreshingRect);
            }
            mRefreshingDrawable.draw(canvas);
        }
        canvas.restore();
        String str = getDrawString();
        resetTextBounds(str);
        canvas.drawText(str, mTextBounds.left, mTextBounds.bottom, mTextPaint);
    }

    private void resetTextBounds(String str) {
        Rect tBounds = new Rect();
        mTextPaint.getTextBounds(str, 0, str.length(), tBounds);
        final int diff = (mTextBounds.height() - tBounds.height()) / 2;
        mTextBounds.top += diff;
        mTextBounds.bottom -= diff;

    }

    public void set2State(SwipeState state) {
        if (mState != null) {
            if (mAnimator != null) {
                mAnimator.cancel();
            }
            if (mState == SwipeState.PULL_TO_REFRESH && state == SwipeState.RELEASE_TO_REFRESH) {
                mAnimator = ValueAnimator.ofInt(0, 180);
                mAnimator.setDuration(400);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final int value = (Integer) animation.getAnimatedValue();
                        mRotateDegree = value;
                        invalidateSelf();
                    }
                });
                mAnimator.start();
            } else if (mState == SwipeState.RELEASE_TO_REFRESH && state == SwipeState.PULL_TO_REFRESH) {
                mAnimator = ValueAnimator.ofInt(180, 0);
                mAnimator.setDuration(400);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final int value = (Integer) animation.getAnimatedValue();
                        mRotateDegree = value;
                        invalidateSelf();
                    }
                });
                mAnimator.start();
            } else if (mState == SwipeState.RELEASE_TO_REFRESH && state == SwipeState.REFRESHING) {
                mAnimator = ValueAnimator.ofInt(360, 0);
                mAnimator.setDuration(1000);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final int value = (Integer) animation.getAnimatedValue();
                        mRotateDegree = value;
                        invalidateSelf();
                    }
                });
                mAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mAnimator.start();
            } else if (state == SwipeState.PULL_TO_REFRESH && mState == SwipeState.REFRESHING) {
                invalidateSelf();
                mRotateDegree = 0;
            }
            mState = state;
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return mBoundsRect.height();
    }

    @Override
    public int getIntrinsicWidth() {
        return mBoundsRect.width();
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAdvertizeDrawable != null) {
            mAdvertizeDrawable.setAlpha(alpha);
        }
        mIndicatorDrawable.setAlpha(alpha);
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mAdvertizeDrawable != null) {
            mAdvertizeDrawable.setColorFilter(colorFilter);
        }
        mIndicatorDrawable.setColorFilter(colorFilter);
        mTextPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mIndicatorDrawable != null ? mIndicatorDrawable.getOpacity() : PixelFormat.TRANSPARENT;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
