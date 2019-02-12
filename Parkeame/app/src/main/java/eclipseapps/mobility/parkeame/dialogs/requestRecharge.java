package eclipseapps.mobility.parkeame.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import eclipseapps.mobility.parkeame.R;

/**
 * Created by usuario on 17/10/17.
 */

public class requestRecharge extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //builder.setView()
        return super.onCreateDialog(savedInstanceState);
    }
/*
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
    }*/


}
