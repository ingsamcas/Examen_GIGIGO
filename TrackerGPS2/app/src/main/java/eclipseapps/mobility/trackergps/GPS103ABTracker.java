package eclipseapps.mobility.trackergps;

import android.telephony.SmsManager;

import eclipseapps.mobility.trackergps.services.SMSreceiver;

/**
 * Created by usuario on 02/04/18.
 */

public class GPS103ABTracker {
    String password="123456";
    String trackerNumber="5534255939";
    SMSreceiver sreceiver;
    public SMSreceiver Init(){ //Mi tracker:5534255939
        sendSMS(trackerNumber,"begin"+password);
        return SMSreceiver.getInstace();
    }
    public SMSreceiver setAuth(String cellPhoneNumber){
        sendSMS(trackerNumber,"admin"+password+" "+cellPhoneNumber);
        return SMSreceiver.getInstace();
    }
    public void deleteAuth(String cellPhoneNumber){
        sendSMS(trackerNumber,"noadmin"+password+" "+cellPhoneNumber);
    }
    public void autoTrack(int secondsInterval,int times){
        secondsInterval=secondsInterval<15?15:secondsInterval;
        secondsInterval=secondsInterval>300?300:secondsInterval;
        times=times>300?300:times;
        sendSMS(trackerNumber,"fix"+String.format("%03d", secondsInterval)+"s"+String.format("%03d", times)+"n"+password);
    }
    void sendSMS(String phoneNumber,String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
