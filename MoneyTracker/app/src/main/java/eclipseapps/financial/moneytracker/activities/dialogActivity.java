package eclipseapps.financial.moneytracker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import eclipseapps.android.customviews.FontFitTextView;
import eclipseapps.android.customviews.TextViewRoboto;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.adapters.OnEditRFCListeners;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.customViews.SelectorCheckbox;
import eclipseapps.financial.moneytracker.customViews.premiumDialog;
import eclipseapps.financial.moneytracker.fragments.RFC_editRFC;

import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.Premium;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumOffer;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumPrice;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumPriceDiscountMicros;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumPriceMicros;

/**
 * Created by usuario on 19/07/18.
 */

public class dialogActivity extends trackedActivity {
    public final static String action_PremiumDialog="action_PremiumDialog";
    public final static int action_PremiumDialog_requestCode=1;
    public final static String action_RFCDialog="action_RFCDialog";
    public final static int action_RFCDialog_requestCode=20;
    public final static String action_SetAlarm="action_SetAlarm";
    public final static int action_SetAlarm_requestCode=40;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if(getIntent().getAction().matches(action_PremiumDialog)){
            premiumDialog();
        }else if(getIntent().getAction().matches(action_RFCDialog)){
            setContentView(R.layout.rfcdialog);
            setTitle(getString(R.string.registra_rfc));
            RFC_editRFC rfc= (RFC_editRFC) getSupportFragmentManager().findFragmentByTag("RFC");
            rfc.setListener(new OnEditRFCListeners() {
                @Override
                public List<eclipseapps.financial.moneytracker.cloud.rfc> getRFCs() {
                    DBSmartWallet db= DBSmartWallet.getInstance(dialogActivity.this);
                    return db.mapCursorToObjectList(db.getallfrom("rfc"), eclipseapps.financial.moneytracker.cloud.rfc.class);
                }

                @Override
                public boolean deleteRFCListener(String RFC) {
                    return false;
                }

                @Override
                public void onNewRFCAdded() {

                    dialogActivity.this.setResult(RESULT_OK,new Intent());
                    dialogActivity.this.finish();
                }
            });
        }else if(getIntent().getAction().matches(action_SetAlarm)){
            setTitle("Se repite...");
            final Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            List<String> options=new ArrayList<>();
            options.addAll(Arrays.asList(new String[]{"días","semanas","meses","años","de cada mes","de cada "+month}));

            setContentView(R.layout.repeat_alarm);
            final EditText cantidad=findViewById(R.id.k_editText1);
            final SelectorCheckbox selector=findViewById(R.id.k_selector);
            selector.setOptions(options,false,1);
            selector.insertTilteGroup(0,"Repetir con frecuencia");
            selector.insertTilteGroup(4,"Repetir en la fecha exacta");
            selector.selectMultiple(false);
            selector.setOnselected(new SelectorCheckbox.onSelected() {
                @Override
                public void onSelected(List selected) {
                    if(selected.contains("de cada mes")||selected.contains("de cada Diciembre")){
                        cantidad.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                        cantidad.setEnabled(false);
                    }else{
                        cantidad.setText("1");
                        cantidad.setEnabled(true);
                    }
                }
            });
            TextView aceptar=findViewById(R.id.k_btnaceptar);
            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cantidad.getText().toString()==null || cantidad.getText().toString().matches("")|| cantidad.getText().toString().matches("0")){
                        return;
                    }
                    Intent intent=new Intent();
                    String c=cantidad.getText().toString();
                    intent.putExtra("freq",c);
                    intent.putExtra("timeLapse",selector.getText());
                    dialogActivity.this.setResult(RESULT_OK, intent);
                    dialogActivity.this.finish();
                }
            });
        }


    }
    horizontalPageradapter adapter=new horizontalPageradapter(this);
    boolean continueSliding=true;
    public void premiumDialog(){
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialogactivity_premium);
        final FontFitTextView precio=findViewById(R.id.premium_precio);
        precio.setText("Comprar");
        precio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra(Premium,true);
                dialogActivity.this.setResult(RESULT_OK,intent);
                dialogActivity.this.finish();
            }
        });
        final TextView descuento=findViewById(R.id.premium_discount);
        double regularPriceMicros=(double)(getIntent().getLongExtra(PremiumPriceMicros,0));
        double discountPriceMicros=(double)(getIntent().getLongExtra(PremiumPriceDiscountMicros,0));
        long discountPrecentage= (long) Math.abs((regularPriceMicros-discountPriceMicros)/regularPriceMicros*100);
        descuento.setText(descuento.getText().toString().replace("%",String.valueOf(discountPrecentage)+"%"));


        LinearLayout shareOntwitter=findViewById(R.id.sharereontwitterbutton);
        shareOntwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnalyticsApplication.shareAppOnTwitter(dialogActivity.this);
            }
        });

        final View progressBar=findViewById(R.id.premium_title_progressbar);
        final HorizontalInfiniteCycleViewPager viewPager=findViewById(R.id.premium_benefitsContainer);
        final TextViewRoboto title=findViewById(R.id.premium_title);
        if(getIntent().hasExtra(PremiumPrice)){
            title.setText("Pago unico de "+getIntent().getStringExtra(PremiumPrice));
        }else{
            title.setText("Pago unico de 1.5 USD");
        }
        title.post(new Runnable() {
            @Override
            public void run() {
                setHorizontalInfiniteViewPager(viewPager,title);

            }
        });

    }
    void waitForChange(final ViewPager viewPager,final View progressBar){

        final LinearLayout parent=findViewById(R.id.premium_title_container);
        new CountDownTimer(3000, 10) {

            public void onTick(long millisUntilFinished) {
                final int parentWidth=parent.getWidth();
                float res=(6000f-millisUntilFinished)/6000f;
                int width= (int) (res*parentWidth);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,20);
                progressBar.setLayoutParams(parms);
            }

            public void onFinish() {
                if(continueSliding){
                    if(viewPager.getCurrentItem()>=viewPager.getAdapter().getCount()-1){
                        viewPager.setCurrentItem(0,false);
                    }else{
                        viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
                    }
                    waitForChange(viewPager,progressBar);
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra(Premium,false);
        dialogActivity.this.setResult(RESULT_OK,intent);
        super.onBackPressed();
    }
    private void setHorizontalInfiniteViewPager(HorizontalInfiniteCycleViewPager infiniteCycleViewPager,final  TextView title){
        infiniteCycleViewPager.setAdapter(adapter);
        infiniteCycleViewPager.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.overshoot_interpolator));
    }
    public class horizontalPageradapter extends PagerAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;



        public horizontalPageradapter(Context context){
            super();
            mContext=context;
        }



        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            premiumDialog view=new premiumDialog(mContext,container);
            switch (position){
                case 0:
                    view.setPromoImage(R.drawable.byeads);
                    view.setOkButton("Elimina la\n publicidad",null);

                    //Personaliza el dialog 1
                    break;
                case 1:
                    //Personaliza el dialog 2
                    view.setPromoImage(R.drawable.unblock_edit);
                    view.setOkButton("Edita sin limites\nLas veces que quieras",null);
                    break;
                case 2:
                    //Personaliza el dialog 2
                    view.setPromoImage(R.drawable.unblock_export);
                    view.setOkButton("Exporta a Excel\nSin restricciones",null);
                    break;
            }
            view.setTag(position);
            container.addView(view);
            return view;
        }


        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public boolean isViewFromObject(final View view, final Object object) {
            return view.equals(object);
        }

        @Override
        public int getItemPosition(final Object object) {
            return POSITION_NONE;
        }


        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
           container.removeView((View) object);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AnalyticsApplication.TweetRequest&& resultCode==RESULT_OK){
            MarketingTracking("TweetForDiscount",1);
            Intent intent=new Intent();
            intent.putExtra(Premium,true);
            intent.putExtra(PremiumOffer,true);
            dialogActivity.this.setResult(RESULT_OK,intent);
            dialogActivity.this.finish();
        }
    }
}
