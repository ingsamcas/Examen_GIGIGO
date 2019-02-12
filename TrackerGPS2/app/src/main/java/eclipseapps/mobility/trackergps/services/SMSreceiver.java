package eclipseapps.mobility.trackergps.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.os.ResultReceiver;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.mobility.trackergps.MainActivity;
import eclipseapps.mobility.trackergps.R;

/**
 * Created by usuario on 01/04/18.
 */

public class SMSreceiver extends BroadcastReceiver {
    public static int Init=1001;
    public static int Auth=1002;
    public OrderMap<Integer,ResultReceiver> listeners=new OrderMap<>();
    public SmsMessage messages[] = null;
    static SMSreceiver instance;

    public static SMSreceiver getInstace(){
        instance=new SMSreceiver();
        return  instance;
    }
    public SMSreceiver addListener(int orderId,ResultReceiver listener){
        listeners.put(orderId,listener);
        return this;
    }

    private void showNotification(Context context, String sms) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText(sms);
        mBuilder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        try {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                messages = null;
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    String sms = "";
                    String mobile = "5534255939";
                    abortBroadcast();
                    for (int i = 0; i < pdus.length; i++) {
                        SmsMessage tmp = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        String senderMobile = tmp.getOriginatingAddress();

                        if (senderMobile.equals(mobile)) {
                            sms = tmp.getMessageBody();
                            Bundle resultData=new Bundle();
                            resultData.putString("sms",sms);
                            for (int a=0;i<listeners.size();a++) {
                               if(a<listeners.size()) ((ResultReceiver)listeners.get(a)).send((Integer) listeners.keyAt(a),resultData);
                            }
                            showNotification(context, sms);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("Exception caught", e.getMessage());
        }
    }
}

