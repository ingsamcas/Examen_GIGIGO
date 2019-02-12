package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class GridViewEx extends GridView {

    //private int mRequestedNumColumns = 0;

    public GridViewEx(Context context) {
        super(context);
    }

    public GridViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int numcol=getNumColumns();
        int numitems=getAdapter().getCount();

        if(numitems<numcol){
            int width = (numitems * getColumnWidth())
                    + ((numitems -1) * getHorizontalSpacing())
                    + getListPaddingLeft() + getListPaddingRight();

            setMeasuredDimension(width, getMeasuredHeight());
        }

    }
}