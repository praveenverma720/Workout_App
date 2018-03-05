

package com.praveen.pilani.workout.util.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable divider;
    private final boolean drawAtEnd;

    public DividerItemDecoration(Context context, boolean drawAtEnd) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.listDivider});
        divider = a.getDrawable(0);
        a.recycle();
        this.drawAtEnd = drawAtEnd;
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        RecyclerView.LayoutManager manager = parent.getLayoutManager();
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int lastDecoratedChild = getLastDecoratedChild(parent);
        for (int i = 0; i < lastDecoratedChild; i++) {
            final View child = parent.getChildAt(i);
            final int ty = (int) (child.getTranslationY() + 0.5f);
            final int tx = (int) (child.getTranslationX() + 0.5f);
            final int bottom = manager.getDecoratedBottom(child) + ty;
            final int top = bottom - divider.getIntrinsicHeight();
            divider.setBounds(left + tx, top, right + tx, bottom);
            divider.draw(c);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        RecyclerView.LayoutManager manager = parent.getLayoutManager();
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int lastDecoratedChild = getLastDecoratedChild(parent);
        for (int i = 0; i < lastDecoratedChild; i++) {
            final View child = parent.getChildAt(i);
            final int ty = (int) (child.getTranslationY() + 0.5f);
            final int tx = (int) (child.getTranslationX() + 0.5f);
            final int right = manager.getDecoratedRight(child) + tx;
            final int left = right - divider.getIntrinsicWidth();
            divider.setBounds(left, top + ty, right, bottom + ty);
            divider.draw(c);
        }
    }

    private int getLastDecoratedChild(RecyclerView parent) {
        return parent.getChildCount() - (drawAtEnd ? 0 : 1);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) <= getLastDecoratedChild(parent)) {
            if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                outRect.set(0, 0, 0, divider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, divider.getIntrinsicWidth(), 0);
            }
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }

    private int getOrientation(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalArgumentException("DividerItemDecoration can only be added to RecyclerView with LinearLayoutManager");
        }
        return ((LinearLayoutManager) layoutManager).getOrientation();
    }
}
