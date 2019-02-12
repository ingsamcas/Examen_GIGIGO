package eclipseapps.financial.moneytracker.activities;

import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.TimerTask;

import eclipseapps.android.FragmentN;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;

/**
 * Created by usuario on 17/11/17.
 */

public class TutorialAcitvity extends trackedActivity{

    float width;
    float heigh;
    Button entendido;
    AnimatorSet set;
    public final static String x_layoutParam="x_layoutParam";
    public final static String y_layoutParam="y_layoutParam";
    public final static String x_padding="x_padding";
    public final static String y_padding="y_padding";
    public final static String action_HOW_EDIT="action_HOW_EDIT";
    public final static String action_HOW_ADD="action_HOW_ADD";
    public final static String action_HOW_RECIVE_INVOICE="action_HOW_RECIVE_INVOICE";
    public final static String Preference_tutoEntendido="TutorialEntendido";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //android O fix bug orientation

        if(getIntent().getAction().matches(action_HOW_ADD)){
            howAdd();
        } else if(getIntent().getAction().matches(action_HOW_EDIT)){
            howEditGif();
        }else if(getIntent().getAction().matches(action_HOW_RECIVE_INVOICE)){
            howReceiveInvoiceGif();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getAction().matches(action_HOW_ADD)){
            howAddOnResume();
        }
    }

    @Override
    public void onBackPressed() {
        if(sp.getBoolean(Preference_tutoEntendido,false)){
            super.onBackPressed();
        }
    }
    private void howReceiveInvoiceGif(){

        final float DEFAULT_CURRENT_PAGE_SCALE = 1f;
        final float DEFAULT_TOP_STACKED_SCALE = .3f;
        final float DEFAULT_OVERLAP_FACTOR = 0.4f;
        setContentView(R.layout.activity_tutorial_recive_invoice);
        TextView title=findViewById(R.id.turorial_functions_title);
        title.setText("Solicita las facturas de tus consumos!");

        final FlippableStackView stack=findViewById(R.id.turorial_functions);

        ArrayList imagesFroTutorial=new ArrayList();
        imagesFroTutorial.add(R.drawable.comofacturar);

        ArrayList descrpitions=new ArrayList();
        descrpitions.add("Además de llevar un control de gastos, Denario tambien te permite solicitar las facturas de tus consumos.\n\n" +
                "Tan solo guarda el movimiento tomando una o más fotografias del ticket que contenga la informacion de facturación y selecciona el RFC al que quieres facturar el consumo.\n" +
                "La factura te sera enviada más tarde a Denario y a tu e-mail");

        stack.initStack(imagesFroTutorial.size(), StackPageTransformer.Orientation.VERTICAL, DEFAULT_CURRENT_PAGE_SCALE, DEFAULT_TOP_STACKED_SCALE, DEFAULT_OVERLAP_FACTOR, StackPageTransformer.Gravity.BOTTOM);
        stack.initStack(imagesFroTutorial.size());
        stack.setAdapter(new gallery_activity.MyAdapter(getSupportFragmentManager()).setImagenes(imagesFroTutorial).setDescriptions(descrpitions).setAction(new FragmentN.Action() {
            @Override
            public Object execute(Intent intent) {
                //Click en el boton de siguiente
                int count=stack.getChildCount();
                if(stack.getCurrentItem()==0){
                    usabilityAppTracking(AnalyticsApplication.Usability.Features,action_HOW_RECIVE_INVOICE,"true");
                    sp.edit().putBoolean(action_HOW_RECIVE_INVOICE,true).commit();
                    TutorialAcitvity.this.finish();
                }else{
                    stack.setCurrentItem(stack.getCurrentItem()-1,true);
                }
                return null;
            }
        })); //assuming mStackAdapter contains your initialized adapter
    }
    private void howEditGif(){
        final float DEFAULT_CURRENT_PAGE_SCALE = 1f;
        final float DEFAULT_TOP_STACKED_SCALE = .3f;
        final float DEFAULT_OVERLAP_FACTOR = 0.4f;
        setContentView(R.layout.activity_tutorial_recive_invoice);
        TextView title=findViewById(R.id.turorial_functions_title);
        title.setText("Ingresaste tu primer movimiento!");
        final FlippableStackView stack=findViewById(R.id.turorial_functions);

        ArrayList imagesFroTutorial=new ArrayList();
        imagesFroTutorial.add(R.drawable.editdelete);

        ArrayList descrpitions=new ArrayList();
        descrpitions.add("\n\nPara editar o borrar un movimiento tan solo manten presionado el globo del movimiento, esto hara que" +
               " aparezcan las opciones en la barra superior");

        stack.initStack(imagesFroTutorial.size(), StackPageTransformer.Orientation.VERTICAL, DEFAULT_CURRENT_PAGE_SCALE, DEFAULT_TOP_STACKED_SCALE, DEFAULT_OVERLAP_FACTOR, StackPageTransformer.Gravity.BOTTOM);
        stack.initStack(imagesFroTutorial.size());
        stack.setAdapter(new gallery_activity.MyAdapter(getSupportFragmentManager()).setImagenes(imagesFroTutorial).setDescriptions(descrpitions).setAction(new FragmentN.Action() {
            @Override
            public Object execute(Intent intent) {
                //Click en el boton de siguiente
                int count=stack.getChildCount();
                if(stack.getCurrentItem()==0){
                    usabilityAppTracking(AnalyticsApplication.Usability.Features,action_HOW_EDIT,"true");
                    sp.edit().putBoolean(action_HOW_EDIT,true).commit();
                    TutorialAcitvity.this.finish();
                }else{
                    stack.setCurrentItem(stack.getCurrentItem()-1,true);
                }
                return null;
            }
        })); //assuming mStackAdapter contains your initialized adapter
    }
    private void howEdit(){

        setContentView(R.layout.activity_tutorial_howedit);
        int x=getIntent().getIntExtra(x_layoutParam,0);
        int y=getIntent().getIntExtra(y_layoutParam,0);
        int ypadding=getIntent().getIntExtra(y_padding,0)/2;

        if(y!=0){
            RelativeLayout reveal=findViewById(R.id.activity_tutorial_reveal);
            float xbefore=reveal.getX();
            float ybefore=reveal.getY();

            reveal.setX(x);
            reveal.setY(y-ypadding);
            reveal.invalidate();
            float xafter=reveal.getX();
            float yafter=reveal.getY();
        }
        entendido= (Button) findViewById(R.id.activity_tutorial_entendido);
        entendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putBoolean(action_HOW_EDIT,true).commit();
                TutorialAcitvity.this.finish();
            }
        });
    }
    private void howAdd(){
        setContentView(R.layout.activity_tutorial);
        entendido= (Button) findViewById(R.id.activity_tutorial_entendido);
        entendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usabilityAppTracking(AnalyticsApplication.Usability.Features,action_HOW_ADD,"true");
                sp.edit().putBoolean(action_HOW_ADD,true).commit();
                TutorialAcitvity.this.finish();
            }
        });
        ImageLoader.getInstance().displayImage("drawable://"+R.drawable.flecha1, (ImageView) findViewById(R.id.howadd_flecha));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x*.9f;
        heigh = size.y*.8f;
    }

    private void howAddOnResume(){
        final View myView = findViewById(R.id.activity_tutorial_tip);

        // get the center for the clipping circle
        final int cx = (myView.getLeft() + myView.getRight()) / 2;
        final int cy = (myView.getTop() + myView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int dx = Math.max(cx, myView.getWidth() - cx);
        int dy = Math.max(cy, myView.getHeight() - cy);
        final float finalRadius = (float) Math.hypot(dx, dy);

        myView.post(new TimerTask() {
            @Override
            public void run() {
                animFor_howToAdd(myView);
            }
        });
    }

    private void howEditOnResume(){
        final View myView = findViewById(R.id.activity_tutorial_tip);



        myView.post(new TimerTask() {
            @Override
            public void run() {
                animFor_howToEdit(myView);
            }
        });
    }
    public void animFor_howToAdd(final View myView){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom_rigth);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation anim = AnimationUtils.loadAnimation(TutorialAcitvity.this,R.anim.from_bottom_rigth);
                anim.setRepeatCount(1000);
                anim.setAnimationListener(this);
                myView.startAnimation(anim);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(1000);
        myView.startAnimation(animation);
    }
    public void animFor_howToEdit(View myView){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.from_exact_middle);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation.setRepeatCount(1000);
        myView.startAnimation(animation);
    }
}
