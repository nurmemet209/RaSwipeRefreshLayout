package com.example.nurmemet.raswiperefreshlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by nurmemet on 9/16/2016.
 */
public class RaSwipeRefreshLayout extends ViewGroup {
    private static String className = RaSwipeRefreshLayout.class.getSimpleName();
    private ImageView imageView;
    private RaSwipeRefreshDrawable drawable;
    private int height = 350;
    private ViewGroup container;

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


    }


    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        if (imageView == null) {
            container = (ViewGroup) getChildAt(0);
            imageView = new ImageView(getContext());
            Drawable indicator = ContextCompat.getDrawable(getContext(), R.mipmap.pull2refres);
            //drawable = new RaSwipeRefreshDrawable(indicator, null);
            //imageView.setImageDrawable(drawable);
            imageView.setImageResource(R.mipmap.pull2refres);
            container.addView(imageView, 0);
        }else{
            container.layout(i, i1-150, i2, i3);
        }
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
}
