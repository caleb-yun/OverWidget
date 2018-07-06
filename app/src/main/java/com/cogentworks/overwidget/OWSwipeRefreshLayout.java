package com.cogentworks.overwidget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

public class OWSwipeRefreshLayout extends SwipeRefreshLayout {
    private View mScrollingView;

    public OWSwipeRefreshLayout(Context context) {
        super(context);
    }

    public OWSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return mScrollingView != null && ViewCompat.canScrollVertically(mScrollingView, -1);
    }

    public void setScrollingView(View scrollingView) {
        mScrollingView = scrollingView;
    }
}