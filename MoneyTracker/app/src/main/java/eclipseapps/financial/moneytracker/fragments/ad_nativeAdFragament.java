package eclipseapps.financial.moneytracker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import eclipseapps.financial.moneytracker.R;

public class ad_nativeAdFragament extends baseFragment {
    private boolean hasAd=false;
    private NativeContentAdView adView;
    ViewGroup conitainer;
    //ImageLoader imageLoader;
    private NativeContentAd contentAd;
    private NativeAppInstallAd appInstallAd;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);/*
        if(!imageLoader.isInited()){
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
        }*/
        AdLoader adLoader = new AdLoader.Builder(getActivity(), "ca-app-pub-3940256099942544/2247696110")
                .forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                    @Override
                    public void onAppInstallAdLoaded(NativeAppInstallAd appInstallAd) {
                        // Show the app install ad.
                        ad_nativeAdFragament.this.appInstallAd=appInstallAd;
                        adView= (NativeContentAdView) getLayoutInflater().inflate(R.layout.nativeads_contentadview_1,conitainer,false);
                        TextView headlineView = adView.findViewById(R.id.nativeads_title);
                        ImageView imageAd=adView.findViewById(R.id.nativeads_canvas);
                        TextView descripAd=adView.findViewById(R.id.nativeads_descripcion);

                        imageAd.setImageDrawable(appInstallAd.getImages().get(0).getDrawable());
                        adView.setImageView(imageAd);

                        headlineView.setText(appInstallAd.getHeadline());
                        adView.setHeadlineView(headlineView);

                        descripAd.setText(appInstallAd.getBody());
                        adView.setBodyView(descripAd);

                        adView.setNativeAd(appInstallAd);

                        hasAd=true;
                    }
                })
                .forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(NativeContentAd contentAd) {
                        ad_nativeAdFragament.this.contentAd=contentAd;
                        adView= (NativeContentAdView) getLayoutInflater().inflate(R.layout.nativeads_contentadview_1,conitainer,false);
                        TextView headlineView = adView.findViewById(R.id.nativeads_title);
                        ImageView imageAd=adView.findViewById(R.id.nativeads_canvas);
                        TextView descripAd=adView.findViewById(R.id.nativeads_descripcion);

                        imageAd.setImageDrawable(contentAd.getImages().get(0).getDrawable());
                        adView.setImageView(imageAd);

                        headlineView.setText(contentAd.getHeadline());
                        adView.setHeadlineView(headlineView);

                        descripAd.setText(contentAd.getBody());
                        adView.setBodyView(descripAd);

                        adView.setNativeAd(contentAd);

                        hasAd=true;
                        // Show the content ad.
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
    public boolean hasAd(){
        return hasAd;
    }
    public NativeContentAdView getNativeAdView(){
        return adView;
    }
}
