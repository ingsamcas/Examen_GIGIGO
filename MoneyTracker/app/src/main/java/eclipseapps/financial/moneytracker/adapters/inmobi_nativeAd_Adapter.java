package eclipseapps.financial.moneytracker.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.ads.listeners.NativeAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.libraries.library.general.functions.general;

public class inmobi_nativeAd_Adapter extends movementsListAdpter  {

    static int impressCounted=0;
    InMobiNative nativeAd;
    //private final List<InMobiNative> mNativeAds = new ArrayList<>();
    private int AD_POSITION_ON_CHILD=2;
    private int AD_POSITION_ON_PARENT=0;
    public static NativeAdEventListener nativeAdEventListener;

    public inmobi_nativeAd_Adapter(Context context) {
        super(context);
        JSONObject consentObject = new JSONObject();
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InMobiSdk.init(context, "b314bde1fea14d7fa3029428116e7f92",consentObject);
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);

        /*
        if(baseActivity.location!=null)InMobiSdk.setLocation(baseActivity.location);
        if(Locale.getDefault().getISO3Country().matches("ESP")){
            InMobiSdk.setEthnicity(InMobiSdk.Ethnicity.CAUCASIAN);
        }else{
            InMobiSdk.setEthnicity(InMobiSdk.Ethnicity.HISPANIC);
        }
        InMobiSdk.setAgeGroup(InMobiSdk.AgeGroup.BETWEEN_30_AND_34);
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);*/
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
            if(nativeAd==null || impressCounted>5){//Si el usuario ya vie el anuncio 5 veces y no le dio click se solicita otro anuncio
                impressCounted=0;
                nativeAdEventListener=new NativeAdEventListener() {
                    @Override
                    public void onAdLoadSucceeded(InMobiNative inMobiNative) {
                       // super.onAdLoadSucceeded(inMobiNative);
                        Log.d("inmobi_nativead/Succes","onAdLoadSucceeded");
                        if(!inMobiNative.isReady()){
                            Log.d("inmobi_nativead/Succes","Ad not ready. Call again load(context)");
                            return;
                        }
                        if(_listDataChild.size()>0){
                            nativeAd=inMobiNative;
                            boolean isappDownLoad=inMobiNative.isAppDownload();
                            //isappDownLoad=true;
                            if(isappDownLoad){
                                Log.d("inMobi native ad","isAppDownload");
                                if(nativeAd!=null){//Si llega un nuevo anuncio se reemplaza con el anterior
                                    _listDataChild.remove(AdGroup);
                                }
                                if(AD_POSITION_ON_PARENT==0)AD_POSITION_ON_PARENT++;//El anuncio no puede aparecer en la primera posicion
                                _listDataChild.put(AD_POSITION_ON_PARENT,AdGroup,_listDataChild.get(AD_POSITION_ON_PARENT-1));// Se crea una posicion para el anuncio en la vista por días
                            }else{
                                Log.d("inMobi native ad","isNotAppDownload");
                                RowData lastContentRow= ((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).get(AD_POSITION_ON_CHILD-1);
                                if(lastContentRow.type==RowData.Type.Ad){//Si llega un nuevo anuncio se reemplaza con el anterior
                                    ((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).remove(AD_POSITION_ON_CHILD);
                                }
                                RowData Row= (RowData) lastContentRow.clone();
                                Row.setType(RowData.Type.Ad);
                                ((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).add(AD_POSITION_ON_CHILD,Row);//Se crea una posicion para el anuncio en la vista por cada movimeinto
                            }
                            notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        super.onAdLoadFailed(inMobiNative, inMobiAdRequestStatus);
                        Log.d("Inmobi Status:"+inMobiAdRequestStatus.getStatusCode(),"Message:"+inMobiAdRequestStatus.getMessage());
                    }

                    @Override
                    public void onAdFullScreenDismissed(InMobiNative inMobiNative) {
                        super.onAdFullScreenDismissed(inMobiNative);
                    }

                    @Override
                    public void onAdFullScreenWillDisplay(InMobiNative inMobiNative) {
                        super.onAdFullScreenWillDisplay(inMobiNative);
                    }

                    @Override
                    public void onAdFullScreenDisplayed(InMobiNative inMobiNative) {
                        super.onAdFullScreenDisplayed(inMobiNative);
                    }

                    @Override
                    public void onUserWillLeaveApplication(InMobiNative inMobiNative) {
                        super.onUserWillLeaveApplication(inMobiNative);
                    }

                    @Override
                    public void onAdImpressed(InMobiNative inMobiNative) {
                        super.onAdImpressed(inMobiNative);
                    }

                    @Override
                    public void onAdClicked(InMobiNative inMobiNative) {
                        super.onAdClicked(inMobiNative);
                        Log.d("inmobi_nativead/Succes","onAdClicked");
                        //Si el usuario le dio click al anuncio, entonces se limpia el anuncio y se solicita otro
                        impressCounted=0;
                        boolean isappDownLoad=nativeAd.isAppDownload();
                        if(isappDownLoad){
                            _listDataChild.remove(AdGroup);
                        }else{
                            ((List<RowData>)_listDataChild.get(AD_POSITION_ON_PARENT)).remove(AD_POSITION_ON_CHILD);
                        }
                        notifyDataSetChanged();
                        InMobiNative mynativeAd = new InMobiNative(context, 1536386885544l, nativeAdEventListener);
                        mynativeAd.load(context);
                    }

                    @Override
                    public void onAdStatusChanged(InMobiNative inMobiNative) {
                        super.onAdStatusChanged(inMobiNative);
                    }

                    @Override
                    public void onRequestPayloadCreated(byte[] bytes) {
                        super.onRequestPayloadCreated(bytes);
                    }

                    @Override
                    public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
                        super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
                    }
                };
                if(nativeAd!=null)nativeAd.destroy();
                InMobiNative mynativeAd = new InMobiNative(context, 1536386885544l, nativeAdEventListener);
                mynativeAd.load();
            }else if(nativeAd!=null){
                if(nativeAdEventListener!=null)nativeAdEventListener.onAdLoadSucceeded(nativeAd);
            }
        }
        return result;
    }

    @Override
    public boolean hasAd() {
        return super.hasAd();
    }

    @Override
    public View getChildNativeAdView(final Context context, View convertView, ViewGroup parent, long parentWitdth) {
        if(nativeAd!=null){

            if(convertView==null || !(convertView instanceof RelativeLayout)){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nativeads_contentadview_1, parent, false);
            }
            final View convertedView=convertView;
            JSONObject customContent =nativeAd.getCustomAdContent();
            boolean isBackFillBanner = false;
           /* try {
                isBackFillBanner = customContent.getBoolean("isHTMLResponse");
            } catch (JSONException e) {
                isBackFillBanner = false;
            }*/
            if(isBackFillBanner){
                View primaryView = nativeAd.getPrimaryViewOfWidth(context, convertView, parent, Math.round(context.getResources().getDisplayMetrics().density*250));
                ((RelativeLayout)convertView.findViewById(R.id.nativeads_canvas)).addView(primaryView);
                return convertView;
            }
            else {
                final RelativeLayout canvas=((RelativeLayout)convertView.findViewById(R.id.nativeads_canvas));
                canvas.post(new Runnable() {
                    @Override
                    public void run() {
                        int width=canvas.getWidth();

                        View primaryView = nativeAd.getPrimaryViewOfWidth(context, convertedView, canvas, width);
                        if(primaryView!=null){
                            canvas.addView(primaryView);

                            ImageView icon= (ImageView) convertedView.findViewById(R.id.nativeads_icon);
                            String url=nativeAd.getAdIconUrl();
                            if(url!=null && !url.matches("")){
                                ImageLoader imageLoader = ImageLoader.getInstance();
                                imageLoader.displayImage(url, icon);

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
            }


        }else {
            convertView=super.getChildNativeAdView(context,convertView,parent,parentWitdth);
        }
        return convertView;
    }

    @Override
    protected View getGroupNativeAdView(final Context context, View convertView, ViewGroup parent, int parentWidth) {
        if(nativeAd!=null){

            if(convertView==null || !(convertView instanceof RelativeLayout)){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nativeads_contentadview_2, parent, false);
            }
            final View convertedView=convertView;
            final ImageView canvas= (ImageView) convertView.findViewById(R.id.nativeads2_canvas);
            canvas.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout icon_adchoices= (LinearLayout) convertedView.findViewById(R.id.nativeads2_adchoices);
                    View primaryView = nativeAd.getPrimaryViewOfWidth(context, convertedView, icon_adchoices, 25);
                    icon_adchoices.addView(primaryView);
                    String url=nativeAd.getAdIconUrl();
                   if(url!=null && !url.matches("")){
                       ImageLoader imageLoader = ImageLoader.getInstance();
                       imageLoader.displayImage(url, canvas);

                   }
                    ((TextView)convertedView.findViewById(R.id.nativeads2_title)).setText(nativeAd.getAdTitle());
                    ((TextView)convertedView.findViewById(R.id.nativeads2_descripcion)).setText(nativeAd.getAdDescription());
                    ((Button)convertedView.findViewById(R.id.nativeads2_ctabutton)).setText(nativeAd.getAdCtaText());
                    ((Button)convertedView.findViewById(R.id.nativeads2_ctabutton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nativeAd.reportAdClickAndOpenLandingPage();
                        }
                    });
                    ((RatingBar)convertedView.findViewById(R.id.nativeads2_rating)).setRating(nativeAd.getAdRating());//nativeAd.getAdRating()

                }
            });


        }else {
            convertView=super.getChildNativeAdView(context,convertView,parent,parentWidth);
        }
        return convertView;

    }
    @Override
    public void onResume(){
        if(nativeAd!=null)nativeAd.resume();
    }
    @Override
    public void onPause(){
        if(nativeAd!=null)nativeAd.pause();
    }
    @Override
    public void onDestroy(){
        if(nativeAd!=null)nativeAd.destroy();
    }
}
