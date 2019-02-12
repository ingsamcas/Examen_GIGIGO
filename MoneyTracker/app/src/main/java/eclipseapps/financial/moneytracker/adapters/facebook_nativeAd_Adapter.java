package eclipseapps.financial.moneytracker.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import eclipseapps.android.ActivityN;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.libraries.library.general.functions.Timers;
import eclipseapps.libraries.library.general.functions.general;

public class facebook_nativeAd_Adapter extends movementsListAdpter  {
    Timers timer;
    static int impressCounted=0;
    private NativeBannerAd nativeBannerAd;

    //private final List<InMobiNative> mNativeAds = new ArrayList<>();
    private int AD_POSITION_ON_CHILD=2;
    private int AD_POSITION_ON_PARENT=0;
    public static NativeAdListener nativeAdEventListener;

    public facebook_nativeAd_Adapter(Context context) {
        super(context);
    }

    @Override
    public movementsListAdpter setData(List<RowData> _rows, Bundle _sum) {
        movementsListAdpter result=super.setData(_rows, _sum);
        if(_listDataChild.size()>=1){
            AD_POSITION_ON_PARENT= general.randoms(0,_listDataChild.size()-1,1,true)[0];
            if(((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).size()>1){
                AD_POSITION_ON_CHILD=general.randoms(1,((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).size()-1,1,true)[0];
            }else if(((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).size()==1){
                AD_POSITION_ON_CHILD=1;
            }
            if(nativeBannerAd==null || impressCounted>5){//Si el usuario ya vie el anuncio 5 veces y no le dio click se solicita otro anuncio
                impressCounted=0;
                nativeAdEventListener=new NativeAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        if(AnalyticsApplication.debug)Log.d("facebook_nativead","failed:"+adError.getErrorMessage());
                        if(adError!=null){
                            switch (adError.getErrorCode()){
                                case 1000://“Network Error"
                                    timer=new Timers(30000, new TimerTask() {
                                        @Override
                                        public void run() {
                                            if(general.isOnline()){
                                                refreshAd();
                                            }else{
                                                timer.Start();
                                            }

                                        }
                                    });
                                    timer.Start();
                                    break;
                                case 1001://No Fill
                                    timer=new Timers(30000, new TimerTask() {
                                        @Override
                                        public void run() {
                                            refreshAd();
                                        }
                                    });
                                    timer.Start();
                                    break;
                                case 1002://“Load Too Frequently”
                                    timer=new Timers(1800000, new TimerTask() {
                                        @Override
                                        public void run() {
                                            refreshAd();
                                        }
                                    });
                                    timer.Start();
                                    break;
                                case 2000://“Server Error"
                                    timer=new Timers(30000, new TimerTask() {
                                        @Override
                                        public void run() {
                                            if(general.isOnline()){
                                                refreshAd();
                                            }else{
                                                timer.Start();
                                            }

                                        }
                                    });
                                    timer.Start();
                                    break;
                                case 2001://“Internal Error”
                                    timer=new Timers(30000, new TimerTask() {
                                        @Override
                                        public void run() {
                                            if(general.isOnline()){
                                                refreshAd();
                                            }else{
                                                timer.Start();
                                            }

                                        }
                                    });
                                    timer.Start();
                                    break;
                            }
                        }

                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        // super.onAdLoadSucceeded(inMobiNative);
                        if (nativeBannerAd == null || nativeBannerAd != ad) {
                            return;
                        }
                        // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
                        if(nativeBannerAd.isAdInvalidated()) {
                            refreshAd();
                            return;
                        }
                        if(nativeBannerAd.getAdvertiserName()==null || nativeBannerAd.getAdvertiserName().matches("")){
                           refreshAd();
                           return;
                        }
                        AD_POSITION_ON_PARENT= general.randoms(0,_listDataChild.size()<3?_listDataChild.size()-1:3,1,true)[0];

                        if(_listDataChild.size()>0){
                            if(AnalyticsApplication.debug)Log.d("facebook_nativead","onAdLoaded");
                            if(nativeBannerAd!=null){//Si llega un nuevo anuncio se reemplaza con el anterior
                                _listDataChild.remove(AdGroup);
                            }
                            if(AD_POSITION_ON_PARENT==0)AD_POSITION_ON_PARENT++;//El anuncio no puede aparecer en la primera posicion
                            _listDataChild.put(AD_POSITION_ON_PARENT,AdGroup,_listDataChild.get(AD_POSITION_ON_PARENT-1));// Se crea una posicion para el anuncio en la vista por días
                            notifyDataSetChanged();
                            if(AnalyticsApplication.debug)Log.d("facebook_nativead","Ad setted in Datalist");
                        }
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                        Log.d("inmobi_nativead/Succes","onAdClicked");
                        //Si el usuario le dio click al anuncio, entonces se limpia el anuncio y se solicita otro
                        impressCounted=0;
                        _listDataChild.remove(AdGroup);
                        notifyDataSetChanged();

                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {
                        if(AnalyticsApplication.debug)Log.d("facebook_nativead","onLoggingImpression");
                    }

                    @Override
                    public void onMediaDownloaded(Ad ad) {
                        if(AnalyticsApplication.debug)Log.d("facebook_nativead","onMediaDownloaded");
                    }
                };
                refreshAd();
            }else if(nativeBannerAd!=null){
                if(nativeAdEventListener!=null)nativeAdEventListener.onAdLoaded(nativeBannerAd);
            }
        }
        return result;
    }
    public void refreshAd(){
        if(nativeBannerAd!=null){
            if(AnalyticsApplication.debug)Log.d("facebook_nativead","Unregister and destroy last Ad");
            nativeBannerAd.unregisterView();
            nativeBannerAd.destroy();
        }
        //AdSettings.addTestDevice("137fad33-e3f7-4cc3-8dc6-773a8fc595d7");
        ((ActivityN)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    nativeBannerAd = new NativeBannerAd(context, "725639624466531_725639897799837");
                    //AdSettings.addTestDevice("38b48bf0-f34f-4aae-9ad2-821aed183c7c");
                    nativeBannerAd.setAdListener(nativeAdEventListener);
                    nativeBannerAd.loadAd(NativeAdBase.MediaCacheFlag.NONE);
                    //nativeBannerAd.loadAd(NativeAdBase.MediaCacheFlag.ALL);
                    if(AnalyticsApplication.debug)Log.d("facebook_nativead","Requesting ad");
                }catch (Exception exception){
                    if(AnalyticsApplication.debug)Log.d("facebook_nativead","exception:"+exception.getMessage());
                }
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(nativeBannerAd!=null)nativeBannerAd.destroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(timer!=null)timer.Stop();
    }

    @Override
    public boolean hasAd() {
        return super.hasAd();
    }

    /*
    @Override
    public View getChildNativeAdView(final Context context, View convertView, ViewGroup parent, long parentWitdth) {
        if(nativeAd!=null){

            if(convertView==null || !(convertView instanceof RelativeLayout)){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nativeads_contentadview_1, parent, false);
            }
            final View convertedView=convertView;

            final RelativeLayout canvas=((RelativeLayout)convertView.findViewById(R.id.nativeads_canvas));
            canvas.post(new Runnable() {
                @Override
                public void run() {
                    int width=canvas.getWidth();

                    View primaryView = nativeAd.getPrimaryViewOfWidth(context, convertedView, canvas, width);
                    if(primaryView!=null){
                        canvas.addView(primaryView);

                        ImageView icon= (ImageView) convertedView.findViewById(R.id.nativeads_icon);
                        String url=;
                        if(url!=null && !url.matches("")){
                            ImageLoader imageLoader = ImageLoader.getInstance();
                            imageLoader.displayImage(nativeAd.getAdCoverImage(), icon);

                        }
                        ((TextView)convertedView.findViewById(R.id.nativeads_title)).setText(nativeAd.getAdTitle());
                        ((TextView)convertedView.findViewById(R.id.nativeads_descripcion)).setText(nativeAd.getAdDescription());
                        ((TextView)convertedView.findViewById(R.id.nativeads_cta)).setText(nativeAd.getAdLandingPageUrl());
                        convertedView.setVisibility(View.VISIBLE);
                    }else{
                        convertedView.setVisibility(View.GONE);
                    }
                }
            });


        }else {
            convertView=super.getChildNativeAdView(context,convertView,parent,parentWitdth);
        }
        return convertView;
    }
*/
    @Override
    protected View getGroupNativeAdView(final Context context, View convertView, ViewGroup parent, int parentWidth) {
        if(nativeBannerAd!=null){
            nativeBannerAd.unregisterView();//Se libera el ultimo anuncio

            if(convertView==null || !(convertView instanceof RelativeLayout)){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nativeads_contentadview_facebook_banner, parent, false);
            }
            final View convertedView=convertView;
            final AdIconView canvas= (AdIconView) convertView.findViewById(R.id.nativeadsfacebookbanner_canvas);
            canvas.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout icon_adchoices= (LinearLayout) convertedView.findViewById(R.id.nativeadsfacebookbanner_adchoices);
                    AdChoicesView adChoicesView = new AdChoicesView(context, nativeBannerAd, true);
                    icon_adchoices.addView(adChoicesView, 0);
                    ((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_sponsorlabel)).setText(nativeBannerAd.getSponsoredTranslation());

                    ((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_title)).setText(nativeBannerAd.getAdvertiserName());
                    ((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_descripcion)).setText(nativeBannerAd.getAdSocialContext());
                    ((Button)convertedView.findViewById(R.id.nativeadsfacebookbanner_ctabutton)).setText(nativeBannerAd.getAdCallToAction());
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(((Button)convertedView.findViewById(R.id.nativeadsfacebookbanner_ctabutton)));
                    clickableViews.add((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_title));
                    clickableViews.add((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_descripcion));

                    ((Button)convertedView.findViewById(R.id.nativeadsfacebookbanner_ctabutton)).setVisibility(
                            nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                    nativeBannerAd.registerViewForInteraction(convertedView, canvas, clickableViews);
                }
            });


        }else {
            convertView=super.getChildNativeAdView(context,convertView,parent,parentWidth);
        }
        return convertView;

    }
}
