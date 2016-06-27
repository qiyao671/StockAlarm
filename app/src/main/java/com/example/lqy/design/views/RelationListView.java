package com.example.lqy.design.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by lqy on 16-6-18.
 */
public class RelationListView extends ListView {
    private RelationListView mListView;
    public RelationListView(Context context) {
        super(context);
    }

    public RelationListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    public void onTouch(MotionEvent ev) {
//        super.onTouchEvent(ev);
//    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //设置控件滚动监听，得到滚动的距离，然后让传进来的view也设置相同的滚动具体
        if(mListView!=null) {
            mListView.scrollTo(l, t);
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return true;
//    }

    public void setRelatedListView(RelationListView listView) {
        mListView = listView;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if(null != mListView) {
//            mListView.onTouch(ev);
////            mListView.dispatchTouchEvent(ev);
//        }
//
//        return super.onTouchEvent(ev);
//    }


}
