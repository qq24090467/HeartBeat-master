package com.maxiee.heartbeat.common.cloudview;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.maxiee.heartbeat.R;

import java.util.List;

/**
 * Created by maxiee on 15-6-24.
 */

@RemoteViews.RemoteView
public class CloudView extends ViewGroup {

    private static final int MAX_SIZE = 70;
    private OnLabelClickListener mCallback;

    public interface OnLabelClickListener {
        void onClick(String label);
    }

    public CloudView(Context context) {
        super(context);
    }

    public CloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CloudView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnLabelClickListener(OnLabelClickListener callback) {
        mCallback = callback;
    }

    public void addLabels(List<Pair<String, Integer>> labels) {

        removeAllViews();

        if (labels == null) {
            return;
        }

        float maxFreq = computeMaxFreq(labels);

        final TypedValue accentValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorAccent, accentValue, true);
        int  accentColor = accentValue.data;

        final TypedValue grayValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, grayValue, true);
        int grayColor = grayValue.data;

        float redStep = (Color.red(accentColor) - Color.red(grayColor)) / maxFreq;
        float greenStep = (Color.green(accentColor) - Color.green(grayColor)) / maxFreq;
        float blueStep = (Color.blue(accentColor) - Color.blue(grayColor)) / maxFreq;

        for (final Pair<String, Integer> label: labels) {
            TextView tagView = new TextView(getContext());
            tagView.setText(label.first);
            tagView.setTextSize(label.second / maxFreq * MAX_SIZE);
            tagView.setTextColor(
                    Color.rgb(
                            (int) (Color.red(grayColor) + redStep * label.second),
                            (int) (Color.green(grayColor) + greenStep * label.second),
                            (int) (Color.blue(grayColor) + blueStep * label.second)
                    )
            );
            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(20, 5, 20, 5);
            tagView.setLayoutParams(layoutParams);
            tagView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onClick(label.first);
                    }
                }
            });
            addView(tagView);
        }

    }

    private float computeMaxFreq(List<Pair<String, Integer>> labels) {
        float maxFreq = 1;

        for (Pair<String, Integer> label: labels) {
            if (label.second > maxFreq) {
                maxFreq = label.second;
            }
        }

        return maxFreq;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int mViewGroupWidth = getMeasuredWidth();
        int mViewGroupHeight = getMeasuredHeight();

        int heightMax = 0;
        int mPainterPosX = l;
        int mPainterPosY = t;

        int childCount = getChildCount();
        for (int i=0; i<childCount; i++) {
            View childView = getChildAt(i);
            int width = childView.getMeasuredWidth();
            int height = childView.getMeasuredHeight();

            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();

            if (mPainterPosX + layoutParams.leftMargin + width + layoutParams.rightMargin > mViewGroupWidth) {
                mPainterPosX = l;
                mPainterPosY += heightMax;
                heightMax = 0;
            }

            childView.layout(
                    mPainterPosX + layoutParams.leftMargin,
                    mPainterPosY + layoutParams.topMargin,
                    mPainterPosX + width + layoutParams.rightMargin,
                    mPainterPosY + height + layoutParams.bottomMargin
            );

            mPainterPosX += width + layoutParams.leftMargin + layoutParams.rightMargin;

            if (height > heightMax) {
                heightMax = height;
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int start = 0;
        int currentLineWidth = 0;
        int currentLineHeight = 0;

        int totalHeight = 0;
        int totalWeight = 0;

        for (int i=0; i<count; i++) {
            final View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (i==0) { // first one
                start = child.getLeft();
                currentLineWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                currentLineHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            } else if (child.getLeft() != start) { // not the last
                currentLineHeight = Math.max(
                        currentLineHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                currentLineWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            } else { // the last one
                totalHeight += currentLineHeight;
                totalWeight= Math.max(currentLineWidth, totalWeight);
                currentLineWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                currentLineHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }

        }
        setMeasuredDimension(resolveSize(totalWeight, widthMeasureSpec), resolveSize(totalHeight, heightMeasureSpec));

    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }
}
