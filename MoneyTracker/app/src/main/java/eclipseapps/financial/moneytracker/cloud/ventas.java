package eclipseapps.financial.moneytracker.cloud;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 16/07/18.
 */

public class ventas extends ObjectBackendless {
    private String orderId_;
    private String estado_;
    private String extraInfo_;

    public String getExtraInfo_() {
        return extraInfo_;
    }

    public ventas setExtraInfo_(String extraInfo_) {
        this.extraInfo_ = extraInfo_;
        return this;
    }

    public String getEstado_() {
        return estado_;
    }

    public ventas setEstado_(String estado_) {
        this.estado_ = estado_;
        return this;
    }

    public ventas setOrderId_(String orderId_) {
        this.orderId_ = orderId_;
        return this;
    }

    public String getOrderId_() {
        return orderId_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("orderId_",Types.VARCHAR);
        columnas.put("estado_",Types.VARCHAR);
        columnas.put("extraInfo_",Types.VARCHAR);
        return columnas;
    }
    public class states{
       public final static String Solicitud_Reembolso="Reembolso_solicitado";
        public final static String Compra_exitosa="Compra_exitosa";
    }
    public class sellsPreferences{
        public static final int PREMIUM_REQUEST= 10002;// (arbitrary) request code for the purchase flow
        public static final int RC_REQUEST = 10001;// (arbitrary) request code for the purchase flow
        public static final String SKU_PREMIUM = "upgradepremium";//"android.test.purchased";//"upgradepremium";//"android.test.item_unavailable";//"android.test.canceled";//
        public static final String SKU_PREMIUM_Discount = "upgradepremiumdiscount";//"android.test.purchased";//"upgradepremium";//"android.test.item_unavailable";//"android.test.canceled";//
        public static final String Premium="Premium";
        public static final String PremiumPrice="PremiumPrice";
        public static final String PremiumPriceDiscount="PremiumPriceDiscount";
        public static final String PremiumPriceMicros="PremiumPriceMicros";
        public static final String PremiumPriceDiscountMicros="PremiumPriceDiscountMicros";
        public static final String PremiumOffer="PremiumOffer";
        public static final String Payload="payload";
        public static final String hasCredit="hasCredit";
        public static final String TimeToEditEnable ="TimeToEditEnable";
    }
    public static class UI{

        public void animMedalToXY(final Activity activity, final ImageView medal){
            viewMedalFront(medal);
            Animation animation = AnimationUtils.loadAnimation(activity, R.anim.to_littler);
            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                boolean isInMidle=true;
                boolean isfront=true;
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {


                    //float actualX= medal.getX();
                    if(isInMidle){
                        animation.cancel();
                        medal.getLayoutParams().height= (int) (medal.getHeight()*.99);
                        medal.getLayoutParams().width= (int) (medal.getWidth()*.99);

                       // medal.setX(actualX+100);
                        medal.requestLayout();
                        if(isfront){
                            viewMedalBack(medal);
                            isfront=false;
                        }else{
                            viewMedalFront(medal);
                            isfront=true;
                        }
                        Animation toShow = AnimationUtils.loadAnimation(activity,R.anim.from_middle);
                        isInMidle=false;
                        toShow.setFillAfter(true);
                        toShow.setAnimationListener(this);

                        medal.startAnimation(toShow);
                    }else{

                       // TranslateAnimation transAnimation= new TranslateAnimation(0, 100, 0, 0);
                        //transAnimation.setDuration(1000);
                       // transAnimation.setFillAfter(true);

                        Animation toMidle = AnimationUtils.loadAnimation(activity, R.anim.to_middle);
                        isInMidle=true;
                        toMidle.setFillAfter(true);
                        //AnimationSet s = new AnimationSet(false);//false means don't share interpolators
                       // s.addAnimation(toMidle);
                        //s.addAnimation(transAnimation);
                        toMidle.setAnimationListener(this);
                        medal.startAnimation(toMidle);
                    }



                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setAnimationListener(null);
            medal.startAnimation(animation);
            ObjectAnimator transAnimationX= ObjectAnimator.ofFloat(medal, "X", 0, 1000);
            transAnimationX.setDuration(300);//set duration
            //AnimationSet s = new AnimationSet(false);//false means don't share interpolators
            // s.addAnimation(toMidle);
            //s.addAnimation(transAnimation);
            transAnimationX.start();//start animation

            ObjectAnimator transAnimationY= ObjectAnimator.ofFloat(medal, "Y", 0, -800);
            transAnimationY.setDuration(300);//set duration
            transAnimationY.start();
        }
        public void viewMedalFront(ImageView medal){
            ImageLoader.getInstance().displayImage("drawable://" + R.drawable.premium_icon, medal);
        }
        public void viewMedalBack(ImageView medal){
            ImageLoader.getInstance().displayImage("drawable://" + R.drawable.premium_icon_back, medal);
        }
        public void onMedalFullScreen(){

        }

    }
}
