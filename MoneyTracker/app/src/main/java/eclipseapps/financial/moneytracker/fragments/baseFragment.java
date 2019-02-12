package eclipseapps.financial.moneytracker.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import eclipseapps.android.FragmentN;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;

/**
 * Created by usuario on 07/03/18.
 */

public class baseFragment extends FragmentN {
    DBSmartWallet db;
    public Tracker mTracker;

    public baseFragment setDb(DBSmartWallet db) {
        this.db = db;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null && savedInstanceState.containsKey("db")){
            db= (DBSmartWallet) savedInstanceState.getSerializable("db");
        }
        if(mTracker==null){
            AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
            mTracker = application.getDefaultTracker(getActivity());
            if(mTracker!=null){
                String version="";
                try {
                    PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                mTracker.setAppVersion(version);
            }

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(mTracker!=null){
            String className = this.getClass().getSimpleName();
            mTracker.setScreenName("Fragment~" + className);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

    }

    public boolean onBackPressed(){return false;}
}
