package eclipseapps.financial.moneytracker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import eclipseapps.android.FragmentN;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.baseActivity;

import static android.view.View.GONE;

public class bannerFragment extends FragmentN {

    protected AdView mAdView;
    private boolean mIsPremium;


    public boolean ismIsPremium() {
        return mIsPremium;
    }

    public bannerFragment setmIsPremium(boolean mIsPremium) {
        this.mIsPremium = mIsPremium;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       RelativeLayout LL= (RelativeLayout) inflater.inflate(R.layout.activity_banner,container,false);
        mAdView= (AdView) LL.findViewById(R.id.adView);
        return LL;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Log.d("Premium","Ads Thread");

                Log.d("Premium","Checking for Ads");                //sp.getBoolean("FirstMovAdded",false)
                if(!mIsPremium && isAditionalLockOpen()){//Solo Si ya ha hecho por lo menos un movmiento se carga el anuncio, antes no
                    // Create an ad request.
                    final AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
                    if (baseActivity.isTest(getActivity())){
                        // Optionally populate the ad request builder.
                        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                        adRequestBuilder.addTestDevice("7133CAA9A2071DB223C73B033D80B7A7");
                        adRequestBuilder.addTestDevice("D24C7C2BC9BD67B1FBDB54360AF7BD27");
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mAdView!=null){
                                mAdView.loadAd(adRequestBuilder.build());
                            }
                        }
                    });
                    Log.d("Premium","No Premium.Need Ads");


                }else if(mIsPremium && mAdView!=null){
                    removeAds();
                }
            }
        }, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPause() {

        if(!mIsPremium && mAdView!=null){//Solo Si ya ha hecho por lo menos un movmiento se carga el anuncio, antes no
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(!mIsPremium && mAdView!=null){//Solo Si ya ha hecho por lo menos un movmiento se carga el anuncio, antes no
            // Destroy the AdView.
            mAdView.destroy();
        }
        super.onDestroy();
    }



    public void showAdView(){
        if(mIsPremium && mAdView!=null){
            mAdView.setVisibility(GONE);
        }else if(mAdView!=null){
            mAdView.setVisibility(View.VISIBLE);
        }
    }
    public void hideAdView(){
        if(mIsPremium && mAdView!=null){
            mAdView.setVisibility(GONE);
        }else if(mAdView!=null){
            mAdView.setVisibility(View.GONE);
        }
    }

    public void removeAds(){
        if(mAdView!=null){
            if(mAdView.isLoading())mAdView.pause();
            mAdView.setVisibility(GONE);
        }
    }
    protected boolean isAditionalLockOpen(){
        return true;
    }
}
