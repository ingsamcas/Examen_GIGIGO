package eclipseapps.mobility.parkeame.dialogs;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import eclipseapps.mobility.parkeame.R;

/**
 * Created by usuario on 26/08/17.
 */
public class NoCorrectTime extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.b_nocorrect_time);
        this.setFinishOnTouchOutside(false);
        Button aceptar= (Button) findViewById(R.id.b_aceptarButton);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int isAutoTime=android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0);
                if (isAutoTime!=0){
                    NoCorrectTime.this.finish();
                }
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            int isAutoTime=android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0);
            if (isAutoTime==0){
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
