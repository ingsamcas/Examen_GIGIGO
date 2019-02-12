package eclipseapps.financial.moneytracker.interfaces;

import android.accounts.Account;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.activities.Movement;
import eclipseapps.financial.moneytracker.activities.trackedActivity;
import eclipseapps.financial.moneytracker.cloud.DBMessages;
import eclipseapps.financial.moneytracker.sync.Authenticator;
import eclipseapps.financial.moneytracker.sync.SyncService;

import static eclipseapps.financial.moneytracker.sync.SyncService.notificationCenterPreferences.NotificationId;
import static eclipseapps.financial.moneytracker.sync.SyncService.notificationCenterPreferences.lastMessage;

public class onAlarmReceiver extends BroadcastReceiver {
    public final static String Message_num="Message_num";
    public final static String lockAccessDBMessages="lockAccessDBMessages";

    public final static String BOOT_COMPLETED="android.intent.action.BOOT_COMPLETED";
    public final static String NOTIFY="android.intent.action.NOTIFY";

    private final static String SharedPrefsName="eclipseapps.financial.moneytracker";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if(intent.getAction()!=null && intent.getAction().matches(BOOT_COMPLETED)){
            Log.d("SyncA","Boot_Completed");
            ContentResolver mResolver = context.getContentResolver();
            Account account= Authenticator.CreateSyncAccount(context);
            String AUTHORITY=Authenticator.Constants.AUTHORITY;
            Log.d("SyncA","Account:"+account.name);
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync( Authenticator.CreateSyncAccount(context), Authenticator.Constants.AUTHORITY, settingsBundle);
            setAlarms(context);//Al reiniciarse el dispositivo se vuelven a setear las alarmas
        }else if(intent.getAction()!=null &&  intent.getAction().matches(NOTIFY)){
            showNotification(context);//envia la notificacion al centro de notificaciones de android
        }

    }
    public static void showNotification(Context context){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifyEnabled = preferences.getBoolean("notifications", true);
        if(!notifyEnabled)return;
        SharedPreferences sharedPreferences=context.getSharedPreferences(SharedPrefsName,context.MODE_PRIVATE);
        String Message="";
        String MessageN="Default";
        int lastMessageIndex=0;
        if(new Random().nextBoolean()){

            boolean lock=sharedPreferences.getBoolean(lockAccessDBMessages,false);
            if(lock){
                return;
            }
            setLock(sharedPreferences);
            DBMessages messages=DBMessages.getInstance(context);
            int version=messages.getVersion();
            if(version==0){
                removeLock(sharedPreferences);//Terminada la notificacion se remueve el candado
                return;
            }
            lastMessageIndex=sharedPreferences.getInt(lastMessage,0);
            do{
                int countComents=messages.getAllComentariosCount();
                lastMessageIndex++;
                if(lastMessageIndex>countComents){
                    lastMessageIndex=0;
                }
                Message=messages.getMessage(lastMessageIndex);
            }while ((Message==null || Message.matches("")));
            if((Message==null || Message.matches(""))){
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putInt(lastMessage,0);
                editor.apply();
                AnalyticsApplication.startDefaultTracker(context);
                AnalyticsApplication.sendLogAsError("NotificationFault","No Messages avialable");
                return;
            }
            MessageN=String.valueOf(version)+"_"+String.valueOf(lastMessageIndex);
        }

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(context, Movement.class);
        resultIntent.setAction(trackedActivity.FROM_NOTIFICATION);
        resultIntent.putExtra(Message_num,MessageN);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent MovementPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Create an explicit intent for an Activity in your app
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setAction(trackedActivity.FROM_NOTIFICATION);
        mainActivityIntent.putExtra(Message_num,MessageN);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        SyncService.notificationCenterPreferences.createNotificationChannel(context);
        // sp.edit().putLong(SyncAdapter.syncPreference.lastMessageUpdate,System.currentTimeMillis()).apply();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, SyncService.notificationCenterPreferences.CHANNEL_ID_API26)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.personalmoneyicon))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(Message.matches("")?context.getString(R.string.recuerdaregistar):context.getString(R.string.sabiasque))
                .setContentText(Message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[] { 100, 100, 200, 300, 500 })
                //.addAction(R.drawable.mas, getString(R.string.agregar_movimiento),
                //      pendingIntent)
                .addAction(R.drawable.baseline_thumb_up_black_24,context.getString(R.string.agregar_movimiento),
                        MovementPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int notificationId=sharedPreferences.getInt(NotificationId,(int) Math.random());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(NotificationId,notificationId);
        editor.putInt(lastMessage,lastMessageIndex);
        editor.commit();

        removeLock(sharedPreferences);//Terminada la notificacion se remueve el candado
    }
    public static void setAlarms(Context context){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,12);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        if(System.currentTimeMillis()>calendar.getTimeInMillis()){//Ya pasaron de las 12 de la tarde del dia de hoy
            calendar.add(Calendar.HOUR,24);//Se programa para el dia siguiente
        }

        Intent Alarmintent12pm = new Intent(context, onAlarmReceiver.class);
        Alarmintent12pm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Alarmintent12pm.setAction(NOTIFY);
        PendingIntent pendingIntent12pm = PendingIntent.getBroadcast(context, 0, Alarmintent12pm, PendingIntent.FLAG_UPDATE_CURRENT);

        long timeForalarm=calendar.getTimeInMillis();
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeForalarm,
                AlarmManager.INTERVAL_DAY, pendingIntent12pm);


        Intent Alarmintent4pm = new Intent(context, onAlarmReceiver.class);
        Alarmintent4pm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Alarmintent4pm.setAction(NOTIFY);
        PendingIntent pendingIntent4pm = PendingIntent.getBroadcast(context, 0, Alarmintent4pm, PendingIntent.FLAG_IMMUTABLE);
        calendar.set(Calendar.HOUR_OF_DAY,16);
        calendar.set(Calendar.MINUTE,35);
        calendar.set(Calendar.SECOND,0);
        if(System.currentTimeMillis()>calendar.getTimeInMillis()){//Ya pasaron las 4 de la tarde del dia de hoy
            calendar.add(Calendar.HOUR,24);//Se programa para el dia siguiente
        }
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent4pm);



    }
    public static void setLock(SharedPreferences sharedPreferences){
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(lockAccessDBMessages,true);
        editor.apply();//Coloca el candado para que no se pueda accesar a la base de datos(Sin notificaciones) mientras se actualiza
    }
    public static void removeLock(SharedPreferences sharedPreferences){
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(lockAccessDBMessages,false);//quita el candado
        editor.apply();
    }

}
