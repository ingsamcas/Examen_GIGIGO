package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.libraries.library.general.functions.general;

/**
 * Created by usuario on 31/01/18.
 */

public class acerca_de extends baseFragment {
    public static String TAG_FragmentName="Acerca de";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView ll= (ScrollView) inflater.inflate(R.layout.activity_acerca_de,container,false);
        TextView acerca_de= (TextView) ll.findViewById(R.id.acerca_de_texto_2);
        final WebView webView=ll.findViewById(R.id.acerca_de_aviso_privacidad);
        RelativeLayout view= (RelativeLayout) ll.findViewById(R.id.aviso_privacidad);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webView.isShown()){
                    webView.setVisibility(View.GONE);
                }else{
                    if(!general.isOnline()){
                        Toast.makeText(getActivity(),"Verifica tu conexión a internet y vuelve a intentarlo",Toast.LENGTH_LONG).show();
                        return;
                    }
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl("https://api.backendless.com/C1D72711-B7EB-98DD-FFC7-23418D485000/0756ACFC-9311-6E20-FF8E-428B4BCC5A00/files/web/Privacidad.html");
                }

                /*
                String url = "https://play.google.com/store/apps/details?id=eclipseapps.ambiente.recicla&hl=es";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);*/
            }
        });
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "Agradecemos a:\n\n ");
        spanTxt.append("Henning Dodenhof por su excelente librería");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = "https://github.com/hdodenhof/CircleImageView";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, spanTxt.length() - "librería".length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), spanTxt.length() - "por su excelente librería".length(), spanTxt.length() - "librería".length(), 0);

        spanTxt.append("\n\n Yarelegovich por su excelente librería");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = "https://github.com/yarolegovich/SlidingRootNav";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, spanTxt.length() - "librería".length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), spanTxt.length() - "por su excelente librería".length(), spanTxt.length() - "librería".length(), 0);

        spanTxt.append("\n\n Philipp Jahoda por su excelente librería");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = "https://github.com/PhilJay/MPAndroidChart";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, spanTxt.length() - "librería".length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), spanTxt.length() - "por su excelente librería".length(), spanTxt.length() - "librería".length(), 0);

        acerca_de.setMovementMethod(LinkMovementMethod.getInstance());
        acerca_de.setText(spanTxt, TextView.BufferType.SPANNABLE);
        return ll;
    }
    public static acerca_de loadOnMain(MainActivity activity){
        Fragment acercade=activity.getSupportFragmentManager().findFragmentByTag(TAG_FragmentName);
        if(acercade==null){
            acercade=new acerca_de();
        }
        FragmentTransaction FT=activity.getSupportFragmentManager().beginTransaction();
        FT.setCustomAnimations(R.anim.pushleftin,R.anim.pushleftout);
        FT.replace(R.id.content_main_maincontainer,acercade,"acerca_de");
        FT.commit();
        activity.getSupportActionBar().setTitle(R.string.acerca_de);
        activity.getSupportActionBar().setIcon(null);
        activity.getSlidingRootNav().closeMenu(true);
        activity.getToolBar().removeAllViews();
        activity.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        return (acerca_de) acercade;
    }
}
