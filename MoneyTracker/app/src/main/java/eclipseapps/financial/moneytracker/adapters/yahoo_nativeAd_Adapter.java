package eclipseapps.financial.moneytracker.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeListener;

import java.util.List;
import java.util.TimerTask;

import eclipseapps.android.ActivityN;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.libraries.library.general.functions.Timers;
import eclipseapps.libraries.library.general.functions.general;

public class yahoo_nativeAd_Adapter extends movementsListAdpter  {
    Timers timer;
    static int impressCounted=0;
    private FlurryAdNative nativeBannerAd;

    //private final List<InMobiNative> mNativeAds = new ArrayList<>();
    private int AD_POSITION_ON_CHILD=2;
    private int AD_POSITION_ON_PARENT=0;
    public static FlurryAdNativeListener nativeAdEventListener;

    public yahoo_nativeAd_Adapter(Context context) {
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
                nativeAdEventListener=new FlurryAdNativeListener() {
                    @Override
                    public void onFetched(FlurryAdNative flurryAdNative) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onFetched");
                        // super.onAdLoadSucceeded(inMobiNative);
                        if (nativeBannerAd == null || nativeBannerAd != flurryAdNative) {
                            return;
                        }
                        // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
                        if(nativeBannerAd.isExpired()&& !nativeBannerAd.isReady()) {
                            refreshAd();
                            return;
                        }
                        List assets=nativeBannerAd.getAssetList();
                        if(assets==null || assets.size()==0){
                            nativeBannerAd.fetchAd();
                            return;
                        }

                        AD_POSITION_ON_PARENT= general.randoms(0,_listDataChild.size()<3?_listDataChild.size()-1:3,1,true)[0];

                        if(_listDataChild.size()>0){
                            if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onAdLoaded");
                            if(nativeBannerAd!=null){//Si llega un nuevo anuncio se reemplaza con el anterior
                                _listDataChild.remove(AdGroup);
                            }
                            if(AD_POSITION_ON_PARENT==0)AD_POSITION_ON_PARENT++;//El anuncio no puede aparecer en la primera posicion
                            _listDataChild.put(AD_POSITION_ON_PARENT,AdGroup,_listDataChild.get(AD_POSITION_ON_PARENT-1));// Se crea una posicion para el anuncio en la vista por días
                            notifyDataSetChanged();
                            if(AnalyticsApplication.debug)Log.d("yahoo_nativead","Ad setted in Datalist");
                        }
                    }

                    @Override
                    public void onShowFullscreen(FlurryAdNative flurryAdNative) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onShowFullscreen");
                    }

                    @Override
                    public void onCloseFullscreen(FlurryAdNative flurryAdNative) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onCloseFullscreen");
                    }

                    @Override
                    public void onAppExit(FlurryAdNative flurryAdNative) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onAppExit");
                    }

                    @Override
                    public void onClicked(FlurryAdNative flurryAdNative) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onClicked");
                        //Si el usuario le dio click al anuncio, entonces se limpia el anuncio y se solicita otro
                        impressCounted=0;
                        _listDataChild.remove(AdGroup);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onImpressionLogged(FlurryAdNative flurryAdNative) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","onImpressionLogged");
                    }

                    @Override
                    public void onExpanded(FlurryAdNative flurryAdNative) {

                    }

                    @Override
                    public void onCollapsed(FlurryAdNative flurryAdNative) {

                    }

                    @Override
                    public void onError(FlurryAdNative flurryAdNative, FlurryAdErrorType flurryAdErrorType, int i) {
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","failed:"+"_"+String.valueOf(i)+"_"+flurryAdErrorType.name());
                        switch (i){
                            case 1://“Network Error"
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
                            case 20://No Fill
                                timer=new Timers(30000, new TimerTask() {
                                    @Override
                                    public void run() {
                                        refreshAd();
                                    }
                                });
                                timer.Start();
                                break;
                            case 22://“Load Too Frequently”(Device is locked during ad request)
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

                };

                refreshAd();
            }else if(nativeBannerAd!=null){
                if(nativeAdEventListener!=null)nativeAdEventListener.onFetched(nativeBannerAd);
            }
        }
        return result;
    }
    public void refreshAd(){
        if(nativeBannerAd!=null){
            if(AnalyticsApplication.debug)Log.d("yahoo_nativead","Unregister and destroy last Ad");
            nativeBannerAd.destroy();
        }
        //AdSettings.addTestDevice("137fad33-e3f7-4cc3-8dc6-773a8fc595d7");
        ((ActivityN)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{

                    nativeBannerAd = new FlurryAdNative(context, "MovListNativeAd");
                    //AdSettings.addTestDevice("38b48bf0-f34f-4aae-9ad2-821aed183c7c");
                   nativeBannerAd.setListener((FlurryAdNativeListener) nativeAdEventListener);
                    /*FlurryAdTargeting adTargeting = new FlurryAdTargeting();//MODIFICACION_DEBUG
                    adTargeting.setEnableTestAds(true);//PI_DEBUG
                    nativeBannerAd.setTargeting(adTargeting);*/
                    nativeBannerAd.fetchAd();
                    //nativeBannerAd.loadAd(NativeAdBase.MediaCacheFlag.ALL);
                    if(AnalyticsApplication.debug)Log.d("yahoo_nativead","Requesting ad");
                }catch (Exception exception){
                    if(AnalyticsApplication.debug)Log.d("yahoo_nativead","exception:"+exception.getMessage());
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


    @Override
    protected View getGroupNativeAdView(final Context context, View convertView, ViewGroup parent, int parentWidth) {
        if(nativeBannerAd!=null && !nativeBannerAd.isVideoAd() ){
            nativeBannerAd.removeTrackingView();       //Se libera el ultimo anuncio

            if(convertView==null || !(convertView instanceof RelativeLayout)){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nativeads_contentadview_yahoo_banner, parent, false);
            }
            final View convertedView=convertView;
            final ImageView canvas= (ImageView) convertView.findViewById(R.id.nativeadsyahoobanner_canvas);
            canvas.post(new Runnable() {
                @Override
                public void run() {

                    /*
                    LinearLayout icon_adchoices= (LinearLayout) convertedView.findViewById(R.id.nativeadsfacebookbanner_adchoices);
                    AdChoicesView adChoicesView = new AdChoicesView(context, nativeBannerAd, true);
                    icon_adchoices.addView(adChoicesView, 0);*/
                    if(nativeBannerAd.getAsset("secOrigImg")!=null)nativeBannerAd.getAsset("secOrigImg").loadAssetIntoView(convertedView.findViewById(R.id.nativeadsyahoobanner_canvas));
                    if(nativeBannerAd.getAsset("source")!=null)nativeBannerAd.getAsset("source").loadAssetIntoView(convertedView.findViewById(R.id.nativeadsyahoobanner_sponsorlabel));
                    if(nativeBannerAd.getAsset("secHqBrandingLogo")!=null)nativeBannerAd.getAsset("secHqBrandingLogo").loadAssetIntoView(convertedView.findViewById(R.id.nativeadsyahoobanner_adchoices));
                    //((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_sponsorlabel)).setText(nativeBannerAd.getAsset("source").ge);

                    if(nativeBannerAd.getAsset("headline")!=null)nativeBannerAd.getAsset("headline").loadAssetIntoView(convertedView.findViewById(R.id.nativeadsyahoobanner_title));
                    if(nativeBannerAd.getAsset("summary")!=null)nativeBannerAd.getAsset("summary").loadAssetIntoView(convertedView.findViewById(R.id.nativeadsyahoobanner_descripcion));

                    boolean isCTATypeForInstalls=false;
                    String categoria= "";
                    if(nativeBannerAd.getAsset("appCategory")!=null)categoria=nativeBannerAd.getAsset("appCategory").getValue();
                    if(categoria!=null && !categoria.matches("") ){
                        nativeBannerAd.getAsset("appCategory").loadAssetIntoView(convertedView.findViewById(R.id.nativeadsyahoobanner_category));
                        isCTATypeForInstalls=true;
                    }

                    String rating="";
                    if(nativeBannerAd.getAsset("appRating")!=null){
                        rating=nativeBannerAd.getAsset("appRating").getValue();
                        rating=rating.substring(0,rating.indexOf("/"));
                    }
                    if( rating!=null && !rating.matches("") ){
                        String showRating="";
                        if(nativeBannerAd.getAsset("showRating")!=null)showRating=nativeBannerAd.getAsset("showRating").getValue();
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","showRating_"+showRating);
                        if(AnalyticsApplication.debug)Log.d("yahoo_nativead","rated_"+rating);
                        if( showRating.matches("True")|| Float.valueOf(rating)>=60){
                            ((RatingBar)convertedView.findViewById(R.id.nativeadsyahoobanner_rating)).setMax(5);
                            ((RatingBar)convertedView.findViewById(R.id.nativeadsyahoobanner_rating)).setRating(Float.valueOf(rating)*5/100);//setPrnativeBannerAd.getAsset("secRatingImg").loadAssetIntoView();
                            convertedView.findViewById(R.id.nativeadsyahoobanner_rating).setVisibility(View.VISIBLE);
                        }else{
                            convertedView.findViewById(R.id.nativeadsyahoobanner_rating).setVisibility(View.GONE);
                        }
                        isCTATypeForInstalls=true;
                    }

                    if(isCTATypeForInstalls){
                        ((Button)convertedView.findViewById(R.id.nativeadsyahoobanner_ctabutton)).setText("Instalar");
                    }else{
                        ((Button)convertedView.findViewById(R.id.nativeadsyahoobanner_ctabutton)).setText("Más\n Información");
                    }


                    /*
                    ((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_title)).setText(nativeBannerAd.getAsset("headline").getValue());
                    ((TextView)convertedView.findViewById(R.id.nativeadsfacebookbanner_descripcion)).setText(nativeBannerAd.getAsset("summary").getValue());

                    */
                    nativeBannerAd.setTrackingView(convertedView.findViewById(R.id.nativeadsyahoobanner_list_group_card));
                }
            });


        }else {
            convertView=super.getChildNativeAdView(context,convertView,parent,parentWidth);
        }
        return convertView;

    }
}
