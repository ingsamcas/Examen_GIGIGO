package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import eclipseapps.android.FragmentN;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.activities.baseActivity;
import eclipseapps.financial.moneytracker.cloud.ventas;

/**
 * Created by usuario on 30/07/18.
 */

public class rewardedFragment extends FragmentN implements RewardedVideoAdListener {
    public static  String Name="rewardedFragment";
    //Id app=ca-app-pub-7245032634198204~4288416777;
    //Id Ads=ca-app-pub-7245032634198204/8906675434;
    //"Visualiza este anuncio de vídeo y recibirás 100 monedas de oro"
    final static String testAdUnitID="ca-app-pub-7245032634198204/8906675434";//"ca-app-pub-7245032634198204/8906675434";//"ca-app-pub-7245032634198204~4288416777";//"ca-app-pub-7245032634198204~4288416777";//ca-app-pub-3940256099942544/5224354917: test ad unit ID for Android rewarded video// "ca-app-pub-7245032634198204~4288416777"
    final static String reward="reward";
    final static String quantityReward="quantityReward";
    public final static String user_Rewarded="user_Rewarded";//action
    public final static String Ad_failedToLoad="Ad_failedToLoad";//action

    RewardedVideoAd mRewardedVideoAd;
    private FragmentN.Action action;
    Intent intent;
    private boolean playNow=false;
    private boolean onVideoAdClosed=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!((baseActivity)getActivity()).mIsPremium){
            MobileAds.initialize(getActivity(), testAdUnitID);
            // Use an activity context to get the rewarded video instance.
            loadRewardedVideoAd();
        }



    }
    @Override
    public void onResume() {
        super.onResume();
        if(mRewardedVideoAd!=null){
            try{
                mRewardedVideoAd.resume(getActivity());
            }catch (ClassCastException e){

            }
        }
        if(onVideoAdClosed){
            playNow=false;
            onVideoAdClosed=false;//Se restaura la bandera
            if(intent==null){//Salio antes de la recompensa
                showOkDialog("Para ser recompensado debes ver el video completo","OK",false,null);
            }else{
                if(action!=null)action.execute(intent);//Este es el intent que en teoria viene del metodo onReward
            }
            // Load the next rewarded video ad.
            loadRewardedVideoAd();
        }
    }

    @Override
    public void onPause() {
        if(mRewardedVideoAd!=null){
        try{
            mRewardedVideoAd.pause(getActivity());
        }catch (ClassCastException e){

        }

        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(mRewardedVideoAd!=null){
            try {
                mRewardedVideoAd.destroy(getActivity());
            }catch (ClassCastException e){

            }
        }
        super.onDestroy();
    }
    private void loadRewardedVideoAd() {
        if(mRewardedVideoAd==null){
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
            mRewardedVideoAd.setRewardedVideoAdListener(this);
        }
        mRewardedVideoAd.loadAd(testAdUnitID,
                new AdRequest.Builder().build());
    }
    public void displayAd(FragmentN.Action action){
        //      leftapp=false;
        this.action=action;
        intent=null;
        if (mRewardedVideoAd.isLoaded()) {
            dismissWait();
            mRewardedVideoAd.show();
        }else{
            wait("Cargando...",true);
            playNow=true;
            loadRewardedVideoAd();
        }
    }
    @Override
    public void onRewardedVideoAdLoaded() {
        if(playNow){
            displayAd(action);
        }
        AnalyticsApplication.logD("RewardedVideAd","VideoAdLoaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        AnalyticsApplication.logD("RewardedVideAd","VideoAdOpened");
    }

    @Override
    public void onRewardedVideoStarted() {
        AnalyticsApplication.logD("RewardedVideAd","VideoStarted");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        AnalyticsApplication.logD("RewardedVideAd","VideoAdClosed");
        onVideoAdClosed=true;

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        AnalyticsApplication.logD("RewardedVideAd","Rewarded");
        intent=new Intent();
        intent.setAction(user_Rewarded);
        intent.putExtra(reward,rewardItem.getType());
        intent.putExtra(quantityReward,rewardItem.getAmount());

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        ((baseActivity)getActivity()).sp.edit().putBoolean(ventas.sellsPreferences.hasCredit,true).apply();//Se le da un credito para para que cuanto regrese a la app pueda relizar la accion sin tener que volver a
        //a ver el video(debido a que ya lo vio y de hecho interactuo con el)
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d("RewardedVideAd","failed To load");
        playNow=false;
        dismissWait();
        if(action!=null){
            Intent intent=new Intent();
            intent.setAction(Ad_failedToLoad);
            action.execute(intent);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}
