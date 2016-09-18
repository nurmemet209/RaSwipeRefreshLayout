package com.example.nurmemet.raswiperefreshlayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DrawableUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by nurmemet on 9/16/2016.
 */
public class RaSwipeRefreshTopView extends TextView {
    private enum SwipeState {PULL_TO_REFRESH, RELEASE_TO_REFRESH, REFRESHING}

    ;
    private SwipeState mState = SwipeState.PULL_TO_REFRESH;
    private Drawable drawable;

    public RaSwipeRefreshTopView(Context context) {
        super(context);
    }

    public RaSwipeRefreshTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private void init() {

        drawable = ContextCompat.getDrawable(getContext(), R.mipmap.pull2refres);
        RotateDrawable rotateDrawable=new RotateDrawable();
        rotateDrawable.setDrawable(drawable);
        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    public void setState(SwipeState state) {
        if (state == state) {
            return;
        }
        switch (state) {
            case PULL_TO_REFRESH:
                setPull2RefreshState();
                break;
            case RELEASE_TO_REFRESH:
                setRelease2RefreshState();
                break;
            case REFRESHING:
                setRefreshingState();
                break;
        }
    }

    private void setPull2RefreshState() {

    }

    private void setRefreshingState() {

    }

    private void setRelease2RefreshState() {

    }

}
