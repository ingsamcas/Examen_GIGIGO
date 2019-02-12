package eclipseapps.mobility.parkeame.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.util.Timer;
import java.util.TimerTask;

import eclipseapps.libraries.library.general.functions.Timers;

/**
 * Created by usuario on 03/09/17.
 */

public class Cronometro extends IntentService {
    public static final String Tiempo="Tiempo";

    static int tiempo=0;//Tiempo en minutos


    public static final String ACTION_PROGRESO =
            "mobility.parkeame.intent.action.PROGRESO";
    public static final String ACTION_FIN =
            "mobility.parkeame.intent.action.FIN";
    public static String stateService=ACTION_FIN;
    public Cronometro() {
        super("Cronometro");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        int timeUpdate;
        tiempo=intent.getIntExtra(Tiempo,0);
        SharedPreferences prefs = getSharedPreferences("eclipseapps.mobility.parkeame", MODE_PRIVATE);
        for(int i=1; i<=(tiempo+1); tiempo--) {
            timeUpdate=prefs.getInt("UpdateTime",0);
            if (timeUpdate!=0){
                tiempo=tiempo+timeUpdate;
                prefs.edit().putInt("UpdateTime", 0).commit();
            }
            //Comunicamos el progreso
            stateService=ACTION_PROGRESO;
            Intent bcIntent = new Intent();
            bcIntent.setAction(ACTION_PROGRESO);
            bcIntent.putExtra("progreso", tiempo);//Tiempo restante
            sendBroadcast(bcIntent);
            Un_min();
        }
        stateService=ACTION_FIN;
        Intent bcIntent = new Intent();
        bcIntent.setAction(ACTION_FIN);
        sendBroadcast(bcIntent);
    }

    private void Un_min()
    {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}
    }
}


