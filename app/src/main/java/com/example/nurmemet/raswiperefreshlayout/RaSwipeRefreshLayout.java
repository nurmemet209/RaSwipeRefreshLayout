package com.example.nurmemet.raswiperefreshlayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
    private static String CLASS_NAME = RaSwipeRefreshLayout.class.getSimpleName();
    private static final String LOG_TAG = RaSwipeRefreshLayout.class.getSimpleName();
    private ImageView mTopImageView;
    private RaSwipeRefreshDrawable mTopDrawable;
    private ViewGroup mContainer;
    private int mTouchSlop;
    private int mActivePointerId;
    private boolean mIsBeingDragged = false;
    private float mInitialDownY;
    private float mInitialMotionY;
    private int mTopOffset = -1;
    private int mReleaseSlop = 120;

    private static final int STATE_PULL_TO_REFRESH = 1;
    private static final int STATE_RELEASE_TO_REFRESH = 2;
    private static final int STATE_REFRESHING = 3;
    private int mState = STATE_PULL_TO_REFRESH;
    private static final float DRAG_RATE = .3f;

    public RaSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public RaSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int childNum = getChildCount();
        if (childNum > 1) {
            throw new IllegalStateException(CLASS_NAME + "只能有一个孩子");
        }
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();


    }


    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        if (mTopImageView == null) {
            mContainer = (ViewGroup) getChildAt(0);
            mTopImageView = new ImageView(getContext());
            Drawable indicator = ContextCompat.getDrawable(getContext(), R.mipmap.pull2refres);
            Drawable advertize = ContextCompat.getDrawable(getContext(), R.mipmap.advertize_view);
            Drawable refreshingDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.refreshing);
            mTopDrawable = new RaSwipeRefreshDrawable(indicator, advertize, refreshingDrawable, getContext());
            mTopImageView.setImageDrawable(mTopDrawable);
            mTopOffset = mTopDrawable.getIntrinsicHeight();
            mReleaseSlop = mTopDrawable.getMainItemHeight() - mTopOffset;
            mContainer.addView(mTopImageView, 0);
        } else {
            final int width = getMeasuredWidth();
            final int height = getMeasuredHeight();
            if (getChildCount() == 0) {
                return;
            }
            final View child = mContainer;
            final int childLeft = getPaddingLeft();
            final int childTop = getPaddingTop();
            final int childWidth = width - getPaddingLeft() - getPaddingRight();
            final int childHeight = height - getPaddingTop() - getPaddingBottom();
            child.layout(childLeft, childTop - mTopOffset, childLeft + childWidth, childTop + childHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (canChildScrollUp(mContainer)) {
            return false;
        }
        int pointerIndex = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE:
                mIsBeingDragged = true;
                pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                final float y = MotionEventCompat.getY(event, pointerIndex);
                final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;

                if (mContainer.getTop() > mReleaseSlop && mState == STATE_PULL_TO_REFRESH) {
                    mTopDrawable.set2State(RaSwipeRefreshDrawable.SwipeState.RELEASE_TO_REFRESH);
                    mState = STATE_RELEASE_TO_REFRESH;
                    ViewCompat.offsetTopAndBottom(mContainer, (int) overscrollTop);
                } else if (mContainer.getTop() < mReleaseSlop && mState == STATE_RELEASE_TO_REFRESH) {
                    mTopDrawable.set2State(RaSwipeRefreshDrawable.SwipeState.PULL_TO_REFRESH);
                    mState = STATE_PULL_TO_REFRESH;
                    ViewCompat.offsetTopAndBottom(mContainer, (int) overscrollTop);
                } else if (mContainer.getTop() < mReleaseSlop && mState == STATE_REFRESHING) {
                    mTopDrawable.set2State(RaSwipeRefreshDrawable.SwipeState.PULL_TO_REFRESH);
                    mState = STATE_PULL_TO_REFRESH;
                    ViewCompat.offsetTopAndBottom(mContainer, (int) overscrollTop);
                } else if (mContainer.getTop() + overscrollTop <= -mTopOffset && overscrollTop < 0) {
                    //System.out.println("top="+mContainer.getTop());
                    // final float scroll= mContainer.getTop()+overscrollTop+ mTopOffset;
                    //ViewCompat.offsetTopAndBottom(mContainer, (int) scroll);
                } else {
                    ViewCompat.offsetTopAndBottom(mContainer, (int) overscrollTop);
                }
                mInitialMotionY = y;
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(event);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                if (mActivePointerId == INVALID_POINTER) {
                    mActivePointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                }

            }
            break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
                //finishSpinner(overscrollTop);
                mActivePointerId = INVALID_POINTER;
                int top = mContainer.getTop();
                ViewCompat.offsetTopAndBottom(mContainer, -top - mTopOffset + getPaddingTop());
            }
            return false;
            case MotionEvent.ACTION_UP: {
                pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                mIsBeingDragged = false;
                //finishSpinner(overscrollTop);
                mHandler.removeCallbacksAndMessages(null);
                mActivePointerId = INVALID_POINTER;
                final int top = mContainer.getTop();
                if (mState == STATE_RELEASE_TO_REFRESH) {
                    int d = -(top - mReleaseSlop);
                    ViewCompat.offsetTopAndBottom(mContainer, d);
                    mTopDrawable.set2State(RaSwipeRefreshDrawable.SwipeState.REFRESHING);
                    mState = STATE_REFRESHING;
                    delayHide();
                } else if (mState == STATE_REFRESHING) {
                    int d = -(top - mReleaseSlop);
                    ViewCompat.offsetTopAndBottom(mContainer, d);
                    delayHide();
                } else {
                    mTopDrawable.set2State(RaSwipeRefreshDrawable.SwipeState.PULL_TO_REFRESH);
                    ViewCompat.offsetTopAndBottom(mContainer, -top - mTopOffset + getPaddingTop());
                }
                return false;
            }

        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private void delayHide() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int top = mContainer.getTop();
                mTopDrawable.set2State(RaSwipeRefreshDrawable.SwipeState.PULL_TO_REFRESH);
                ViewCompat.offsetTopAndBottom(mContainer, -top - mTopOffset + getPaddingTop());
            }
        }, 2000);
    }

    private Handler mHandler = new Handler();


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量孩子节点
        if (mContainer != null) {
            mContainer.measure(MeasureSpec.makeMeasureSpec(
                    getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                    MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                    getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        }


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (canChildScrollUp(mContainer)) {
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
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
                if (Math.abs(yDiff) > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                    mInitialMotionY = y;
                }
                System.out.println(mIsBeingDragged + "");

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;

        }
        return mIsBeingDragged;

    }

    private float getMotionEventY(MotionEvent ev, int pointerId) {
        int index = MotionEventCompat.getPointerId(ev, pointerId);
        if (index < 0) {
            Log.e(LOG_TAG, "invalid poinerId");
            return -1;
        }
        return MotionEventCompat.getY(ev, pointerId);
    }

    public boolean canChildScrollUp(View view) {
        return ViewCompat.canScrollVertically(view, -1);
    }
}
