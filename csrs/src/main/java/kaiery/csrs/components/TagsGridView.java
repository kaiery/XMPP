package kaiery.csrs.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 一个没有滚动条的GridView
 */
public class TagsGridView extends GridView {


    public TagsGridView(Context context) {
        super(context);
    }

    public TagsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagsGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec=MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }


}