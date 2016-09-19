package com.example.nurmemet.raswiperefreshlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by nurmemet on 9/16/2016.
 */
public class RaSwipeRefreshLayout extends ViewGroup {
    private static final int INVALID_POINTER = -1;
    private static String className = RaSwipeRefreshLayout.class.getSimpleName();
    private static final String LOG_TAG = RaSwipeRefreshLayout.class.getSimpleName();
    private ImageView imageView;
    private RaSwipeRefreshDrawable drawable;
    private ViewGroup container;
    private int mTouchSlop;
    private int mActivePointerId;
    private boolean mIsDraging = false;
    private float mInitialDownY;
    private float mInitialMotionY;
    private int topOffset = 100;

    public RaSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public RaSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void ensureTarget() {

    }

    private void init() {
        int childNum = getChildCount();
        if (childNum > 1) {
            throw new IllegalStateException(className + "只能有一个孩子");
        }
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();


    }


    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        if (imageView == null) {
            container = (ViewGroup) getChildAt(0);
            imageView = new ImageView(getContext());
            Drawable indicator = ContextCompat.getDrawable(getContext(), R.mipmap.pull2refres);
            Drawable advertize=ContextCompat.getDrawable(getContext(),R.mipmap.advertize_view);
            drawable = new RaSwipeRefreshDrawable(indicator, advertize);
            imageView.setImageDrawable(drawable);
            topOffset=drawable.getIntrinsicHeight();
            //imageView.setImageResource(R.mipmap.pull2refres);
            container.addView(imageView, 0);
        } else {
            final int width = getMeasuredWidth();
            final int height = getMeasuredHeight();
            if (getChildCount() == 0) {
                return;
            }
            final View child = container;
            final int childLeft = getPaddingLeft();
            final int childTop = getPaddingTop();
            final int childWidth = width - getPaddingLeft() - getPaddingRight();
            final int childHeight = height - getPaddingTop() - getPaddingBottom();
            child.layout(childLeft, childTop-topOffset, childLeft + childWidth, childTop + childHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (canChildScrollUp(container)) {
            return false;
        }
        int pointerIndex = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mIsDraging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                pointerIndex = MotionEventCompat.getPointerId(event, 0);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "invalid pointerId");
                    return false;
                }
                final float y = MotionEventCompat.getY(event, mActivePointerId);
                final float yDiff = y - mInitialMotionY;
                ViewCompat.offsetTopAndBottom(container, (int) yDiff);
                mInitialMotionY = y;
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(event);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = MotionEventCompat.getPointerId(event, pointerIndex);
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                mIsDraging = false;
                //finishSpinner(overscrollTop);
                mActivePointerId = INVALID_POINTER;
                int top = container.getTop();
                ViewCompat.offsetTopAndBottom(container, -top - topOffset + getPaddingTop());

                return false;
            }


        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量孩子节点
        if (container != null) {
            // container.measure(widthMeasureSpec, heightMeasureSpec);

            container.measure(MeasureSpec.makeMeasureSpec(
                    getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                    MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                    getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        }


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (canChildScrollUp(container)) {
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsDraging = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                mInitialMotionY = mInitialDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "invalid active pointerId");
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialDownY;
                if (yDiff > mTouchSlop && !mIsDraging) {
                    mIsDraging = true;
                    mInitialMotionY = y;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDraging = false;
                mActivePointerId = INVALID_POINTER;
                break;

        }
        return mIsDraging;

    }

    private float getMotionEventY(MotionEvent ev, int pointerId) {
        int index = MotionEventCompat.getPointerId(ev, pointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, pointerId);
    }

    public boolean canChildScrollUp(View view) {
        return ViewCompat.canScrollVertically(view, -1);
    }
}
