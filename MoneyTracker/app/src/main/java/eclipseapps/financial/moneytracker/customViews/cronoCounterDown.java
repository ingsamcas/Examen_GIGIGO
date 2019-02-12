package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import eclipseapps.financial.moneytracker.R;

public class cronoCounterDown extends LinearLayout {
    TextView cronometer;
    public cronoCounterDown(Context context) {
        super(context);
        init();
    }

    public cronoCounterDown(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public cronoCounterDown(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void init(){
        inflate(getContext(), R.layout.counter_back_message,this);
        cronometer=findViewById(R.id.counter_back_cronometer);
    }
    public void setCounterDownTimer(long millisInFuture,long countDownInterval){
        final Calendar cal = Calendar.getInstance();


        new CountDownTimer(millisInFuture, countDownInterval) {

            public void onTick(long millisUntilFinished) {

                if(cronometer!=null){
                    Date date = new Date(millisUntilFinished);
                    // formattter
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // Pass date object
                    String formatted = formatter.format(date);
                    cronometer.setText(formatted);
                }
            }

            public void onFinish() {
                if(cronometer!=null)cronometer.setText("Se acabo la espera\nya puedes volver a utilizar la función!");
            }
        }.start();
    }
}
