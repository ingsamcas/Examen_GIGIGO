package eclipseapps.financial.moneytracker.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import eclipseapps.android.ActivityN;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.BuildConfig;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.interfaces.onAlarmReceiver;

/**
 * Created by usuario on 02/08/18.
 */

public class trackedActivity extends ActivityN {
    public final static String FROM_NOTIFICATION="android.intent.action.FROM_NOTIFICATION";
    String countryCodeValue;
    public static SharedPreferences sp;
    DBSmartWallet db;
    private static Tracker mTracker;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        //PreferenceManager.setDefaultValues(getApplicationContext(),"eclipseapps.financial.moneytracker",MODE_PRIVATE,);
        sp= getApplicationContext().getSharedPreferences("eclipseapps.financial.moneytracker",MODE_PRIVATE);
        db=DBSmartWallet.getInstance(this);
        if(mTracker==null){
            AnalyticsApplication application = (AnalyticsApplication) getApplication();
            mTracker = application.getDefaultTracker(this);
            if(mTracker!=null){
                String version="";
                try {
                    PackageInfo pInfo =getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                mTracker.setAppVersion(version);
            }
        }
        countryCodeValue=sp.getString("countryCodeValue","");
        if (!countryCodeValue.matches("mx")){
            TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
            countryCodeValue = tm.getNetworkCountryIso();
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("countryCodeValue",countryCodeValue);
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String className = this.getClass().getSimpleName();
        if(mTracker!=null){
            mTracker.setScreenName("Activity~" + className);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        if(getIntent().getAction()!=null && getIntent().getAction().matches(FROM_NOTIFICATION)){
            usabilityAppTracking(AnalyticsApplication.Usability.NotificationRetention,className,getIntent().getStringExtra(onAlarmReceiver.Message_num));
        }
    }
    public static void sendLogAsError(String event,String trace){
        AnalyticsApplication.sendLogAsError(event,trace);
    }


    public void usabilityAppTracking(AnalyticsApplication.Usability Category, AnalyticsApplication.Gestures Action, String Tag){
        sendTrack(Category.getValue(),Action.getValue(),Tag);
    }

    private void sendTrack(String category, String action, String Tag) {
        AnalyticsApplication.sendTrack(category,action,Tag);
    }

    public void usabilityAppTracking(AnalyticsApplication.Usability Category, String action, String Tag){
        sendTrack(Category.getValue(),action,Tag);
    }
    public void usabilityAppTracking(AnalyticsApplication.Gestures Action, String Tag){
        usabilityAppTracking(AnalyticsApplication.Usability.Interaction,Action,Tag);
    }

    public void ReadMovementsTracking(String Action, String Tag){
        sendTrack("ReadMovements",Action,Tag);
    }


    public void writeMovementsTracking(AnalyticsApplication.Write Category, AnalyticsApplication.Action Action){
        AnalyticsApplication.writeMovementsTracking(Category,Action,"");
    }
    public void MarketingTracking(String Action, long value){

        if (!BuildConfig.DEBUG && mTracker!=null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Marketing")
                    .setAction(Action)
                    .setValue(value)
                    .build());
        }
    }
    public void SellsTracking(String Action, long value){
        if (!BuildConfig.DEBUG && mTracker!=null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Sells")
                    .setAction(Action)
                    .setValue(value)
                    .build());
        }
    }
    public void SellsTracking(String Action, String tag){
        if (!BuildConfig.DEBUG && mTracker!=null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Sells")
                    .setAction(Action)
                    .setLabel(tag)
                    .build());
        }
    }
    public void AcountTracking(String Action, String tag){
        if (!BuildConfig.DEBUG && mTracker!=null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Accounts")
                    .setAction(Action)
                    .setLabel(tag)
                    .build());
        }
    }

}
