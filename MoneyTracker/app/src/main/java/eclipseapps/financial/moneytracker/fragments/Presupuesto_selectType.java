package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import eclipseapps.android.ActivityN;
import eclipseapps.android.FragmentN;
import eclipseapps.android.customviews.ImageViewProportion;
import eclipseapps.android.customviews.TextViewRoboto;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.baseActivity;
import eclipseapps.libraries.library.general.functions.OrderMap;

import static android.app.Activity.RESULT_OK;
import static eclipseapps.financial.moneytracker.AnalyticsApplication.TweetRequest;

/**
 * Created by usuario on 25/07/18.
 */

public class Presupuesto_selectType extends FragmentN {
    public final static String Cotidiano="Cotidiano";
    public final static String Tweeted="Tweeted";
    public final static String OneEvent_TweetPromo="OneEvent_TweetPromo";

    private Action action;
    ImageLoader imageLoader;

    ImageViewProportion eventoUnicoImage;
    TextViewRoboto tituloOneEvent;
    TextViewRoboto goSignalOneEvent;
    CardView cardViewunico;

    public Action getAction() {
        return action;
    }

    public Presupuesto_selectType setAction(Action action) {
        this.action = action;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view= (LinearLayout) inflater.inflate(R.layout.f_e_presupuesto_type,container,false);
        imageLoader = ImageLoader.getInstance();
        if(!imageLoader.isInited()){
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
        }
        cardViewunico=view.findViewById(R.id.e_eventounico_cardview);
        tituloOneEvent=cardViewunico.findViewById(R.id.e_eventounico_title);
        goSignalOneEvent=cardViewunico.findViewById(R.id.e_eventounico_go);
        eventoUnicoImage=view.findViewById(R.id.e_eventounico_imageview);

        boolean hasTweeted=((baseActivity)getActivity()).sp.getBoolean(Tweeted,false);
        boolean hasOneEventTweetPromo=((baseActivity)getActivity()).sp.getBoolean(OneEvent_TweetPromo,true);


        if(((baseActivity)getActivity()).mIsPremium || hasOneEventTweetPromo){
           oneEventEnabled();
        }else{
            oneEventDisabled();
            if(hasTweeted){
                premiumOrTweet();
            }else{
                justPremium();
            }
        }
        cardViewunico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animOneEventToDisabled();
            }
        });



        ImageViewProportion eventocotidianoImage=view.findViewById(R.id.e_eventocotidiano_imageview);
        imageLoader.displayImage("drawable://" + R.drawable.eventocotidiano, eventocotidianoImage);
        CardView cardViewCotidiano=view.findViewById(R.id.e_eventocotidiano_cardview);
        cardViewCotidiano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(action!=null){
                    Intent intent=new Intent();
                    intent.setAction(Cotidiano);
                    action.execute(intent);
                }
            }
        });
        return view;
    }

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==TweetRequest){
            boolean hasOneEventTweetPromo=((baseActivity)getActivity()).sp.getBoolean(OneEvent_TweetPromo,true);
            if (((baseActivity)getActivity()).mIsPremium || hasOneEventTweetPromo){
                animOneEventToEnabled();
            }
        }

    }
    public void animOneEventToDisabled(){
        Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.to_middle);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardViewunico.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(((baseActivity)getActivity()).sp.getBoolean("Tweeted",false)){
                            justPremium();
                        }else{
                            premiumOrTweet();//Si no ha tuiteado aun puede hacer y ganar un credito mas
                        }

                    }
                });
                oneEventDisabled();
                Animation back = AnimationUtils.loadAnimation(getActivity(),R.anim.from_middle);
                back.setFillAfter(true);
                cardViewunico.startAnimation(back);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        cardViewunico.startAnimation(animation);

    }
    public void animOneEventToEnabled(){
        Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.to_middle);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                oneEventEnabled();
                   cardViewunico.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           //Se emula que ya se consumio su prespuesto gratis
                           animOneEventToDisabled();
                       }
                   });

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        eventoUnicoImage.startAnimation(animation);
    }
    public void oneEventEnabled(){
        imageLoader.displayImage("drawable://" + R.drawable.eventounico, eventoUnicoImage);
        tituloOneEvent.setTextColor(getResources().getColor(R.color.colorPrimary));
        goSignalOneEvent.setTextColor(getResources().getColor(R.color.colorPrimary));
    }
    public void oneEventDisabled(){
        tituloOneEvent.setTextColor(Color.GRAY);
        goSignalOneEvent.setTextColor(Color.TRANSPARENT);
        imageLoader.displayImage("drawable://" + R.drawable.eventounicogris, eventoUnicoImage);
    }
    public void premiumOrTweet(){
        showOkCancelDialog("En la versión gratuita, solo puedes crear un presupuesto o comparte la app en Twitter y " +
                "podras establecer un presupuesto más", "Obtener Premium", "Compartir en Twitter", true, new Dialogs.DialogsInterface() {
            @Override
            public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                if(sucess){
                    ((baseActivity)getActivity()).askForPremium();
                }else{
                    AnalyticsApplication.shareAppOnTwitter((ActivityN) getActivity());
                }
            }
        });
    }
    public void justPremium(){
        showOkCancelDialog("En la versión gratuita solo puedes crear un presupuesto, te invitamos a adquirir Premium", "Obtener Premium", "No gracias", true, new Dialogs.DialogsInterface() {
            @Override
            public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                if(sucess){
                    ((baseActivity)getActivity()).askForPremium();
                }
            }
        });
    }
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
