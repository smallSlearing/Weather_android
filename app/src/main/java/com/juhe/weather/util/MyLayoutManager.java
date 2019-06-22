package com.juhe.weather.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * 继承LinearLayoutManager,这样就得到一个可以水平排向或者竖向排向的布局策略
 */

public class MyLayoutManager extends LinearLayoutManager
    implements RecyclerView.OnChildAttachStateChangeListener {
    private int mDrift;//位移，用来判断移动方向
    //用来辅助帮助RecyclerView的元素对齐
    private PagerSnapHelper mPagerSnapHelper;
    private OnViewPagerListener mOnViewPagerListener;

    public MyLayoutManager(Context context) {
        super(context);
    }

    public MyLayoutManager(Context context,
       int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mPagerSnapHelper = new PagerSnapHelper();
    }

    /**
     * 页面加载RecyclerView触发的事件
     * @param view
     */
    @Override
    public void onAttachedToWindow(RecyclerView view) {
        view.addOnChildAttachStateChangeListener(this);
//        把RecyclerView交给管理PagerSnapHelper
        mPagerSnapHelper.attachToRecyclerView(view);
        super.onAttachedToWindow(view);
    }

    //加载RecyclerView的子元素的时候会触发
    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
    //  播放视频操作 即将要播放的是上一个视频 还是下一个视频
        int position = getPosition(view);
        if (0 == position) {
            if (mOnViewPagerListener != null) {
                //选中的监听
                mOnViewPagerListener.onPageSelected(getPosition(view), false);
            }

        }
    }


    public void setOnViewPagerListener(OnViewPagerListener mOnViewPagerListener) {
        this.mOnViewPagerListener = mOnViewPagerListener;
    }


    /**
     * 停止滑动屏幕的处理函数
     * @param state
     */
    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                /*查找当前的view，当前视频所在的view*/
                View view = mPagerSnapHelper.findSnapView(this);
                /*获得当前view的下标*/
                int position = getPosition(view);
                if (mOnViewPagerListener != null) {
                    //选中的监听
                    mOnViewPagerListener.onPageSelected(position,
                        position == getItemCount() - 1);
                }
                break;
        }
        super.onScrollStateChanged(state);
    }

    /**
     * 当RecyclerView的子元素被移除处理函数
     * @param view
     */
    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
    //暂停播放操作
        if (mDrift >= 0) {
            if (mOnViewPagerListener != null)
                mOnViewPagerListener.onPageRelease(true, getPosition(view));
        } else {
            if (mOnViewPagerListener != null)
                mOnViewPagerListener.onPageRelease(false, getPosition(view));
        }
    }

    /**
     * 滑动屏幕（未释放）处理函数
     * @param dy    滑动的距离
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
        RecyclerView.State state) {
        Log.e("MyLayoutManager","dy:" + dy);
        this.mDrift = dy;
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }
}
