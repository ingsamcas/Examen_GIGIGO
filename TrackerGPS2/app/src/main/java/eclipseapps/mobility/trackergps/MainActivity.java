package eclipseapps.mobility.trackergps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import eclipseapps.android.ActivityN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.mobility.trackergps.activities.DefaultSMSActivity;
import eclipseapps.mobility.trackergps.fragments.fragment_map;
import eclipseapps.mobility.trackergps.services.SMSreceiver;

public class MainActivity extends ActivityN {
    GPS103ABTracker gps=new GPS103ABTracker();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSMSListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if(!askingForPermission)askForSendPermission();
                return;
            } else {
                dismissOkDialog();
            }
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?APIKey  AIzaSyAyyXY63iWtWH2gWOlbrcztFLmdNVdl3R0
                askForReceivePermission();
                return;
            } else {
                dismissOkDialog();
            }
            setSMSListener();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(!Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
                 askForDefaultSMSappPermission();
            }
        }
        final fragment_map map=new fragment_map();
        Bundle args=new Bundle();
        args.putParcelable("receiver", new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if(resultCode==fragment_map.onMapReady){
                    map.onSearch();
                    requestCarLocation();
                }
            }
        });
        map.setArguments(args);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, map, "ActualFragment")
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults == null || grantResults.length == 0) return;
        askingForPermission = false;
        if (requestCode == getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_SEND_SMS) && grantResults[0] == 0) {
            dismissOkDialog();
        }
    }

    static boolean askingForPermission = false;

    void askForSendPermission() {
        // Explain to the user why we need to read the contacts
        if (!askingForPermission) {
            askingForPermission = true;
            showOkDialog("Necesitamos tu permiso para enviar instrucciones al GPS a traves de SMS", "ENTIENDO", false, new Dialogs.DialogsInterface() {
                @Override
                public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        MainActivity.this.requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                                getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_SEND_SMS));
                    }
                }
            });
        }
    }
    void askForReceivePermission() {
        // Explain to the user why we need to read the contacts
        if (!askingForPermission) {
            askingForPermission = true;
            showOkDialog("Necesitamos tu siguiente permiso para poder recibir las instrucciones desde el GPS", "ENTIENDO", false, new Dialogs.DialogsInterface() {
                @Override
                public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        MainActivity.this.requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},
                                getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_RECEIVE_SMS));
                    }
                }
            });
        }
    }
    void askForDefaultSMSappPermission(){
        if (!askingForPermission) {
            askingForPermission = true;
            showOkDialog("Necesitamos que establescas esta app como default para recibir las intrucciones por SMS", "ENTIENDO", false, new Dialogs.DialogsInterface() {
                @Override
                public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                    //Store default sms package name
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            getApplicationContext().getPackageName());
                    startActivity(intent);
                }
            });
        }
    }
    public void requestCarLocation(){
      // gps.setAuth();
    }
    public void setSMSListener(){
        //SMS event receiver
        SMSreceiver mSMSreceiver = new SMSreceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSreceiver, mIntentFilter);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,DefaultSMSActivity.class));
//        gps.Init();
    }
    public static void InitCloud(Context context){
        String AppId="A3F0EE40-1940-139D-FFC4-9A4D69FDAF00";
        String Secretkey="3927D2EA-CCF2-7B72-FF69-B17070587F00";
        String appVersion;
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            appVersion=info.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            appVersion="v1";
        }

        Backendless.initApp( context, AppId,//"1A4FB86F-A85F-CAB0-FFAB-C920CB871500",
                Secretkey);//"1A4B87D2-D755-D5CF-FFF7-597FE7AFD500"

        final SharedPreferences preferences=context.getSharedPreferences("Insulin",MODE_PRIVATE);
        boolean registered=preferences.getBoolean("registeredForMessaging",false);
        if(!registered){
            Backendless.Messaging.registerDevice("305748971595", "default", new AsyncCallback<Void>() {
                @Override
                public void handleResponse(Void response) {
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putBoolean("registeredForMessaging",true);
                    editor.commit();
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        }


    }
}
