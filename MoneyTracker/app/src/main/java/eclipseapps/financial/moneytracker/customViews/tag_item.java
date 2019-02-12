package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import eclipseapps.financial.moneytracker.R;

public class tag_item extends LinearLayout {
    private TextView Text;
    private ImageView remove;

    public tag_item(Context context) {
        super(context);
        init();
    }

    public tag_item(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public tag_item(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        inflate(getContext(), R.layout.tag_item, this);
        this.Text = (TextView)findViewById(R.id.tag_item_text);
        this.remove = (ImageView)findViewById(R.id.tag_item_remove);
    }
    public void setText(String text){
        Text.setText(text);
    }
    public String getText(){
        return (String) Text.getText();
    }
    public void addOnRemoveListener(OnClickListener listener){
        remove.setOnClickListener(listener);
    }
}
