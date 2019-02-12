package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import eclipseapps.financial.moneytracker.R;

public class FilterSelector extends RelativeLayout {
    public FilterSelector(Context context) {
        super(context);
        init();
    }

    public FilterSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FilterSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        inflate(getContext(), R.layout.customviews_filterselector_b,this);
    }
}
