package eclipseapps.financial.moneytracker.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import eclipseapps.android.FragmentN;
import eclipseapps.android.customviews.DatePickerEditText;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.customViews.SelectorCheckbox;

/**
 * Created by usuario on 25/07/18.
 */

public class Presupuesto_createdit_cotidiano extends FragmentN {
    DBSmartWallet db;
    public final static String TAG_FragmentName="Presupuesto_createdit_cotidiano";
    LinearLayout options;
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view= (LinearLayout) inflater.inflate(R.layout.f_f_presupuesto_createdit_cotidiano,container,false);
        SelectorCheckbox periodosSelector=view.findViewById(R.id.f_createdit_cotidiano_periodo);
        SelectorCheckbox cuentasSelector=view.findViewById(R.id.f_createdit_cotidiano_cuentas);
        SelectorCheckbox categoriasSelector=view.findViewById(R.id.f_createdit_cotidiano_categorias);
        options=view.findViewById(R.id.f_createdit_cotidiano_periodo_options_container);

        db=DBSmartWallet.getInstance(getActivity());
        String[] cuentas=db.GetStringColumn("SELECT * FROM cuentas",null,"cuenta_");
        List Cuentas=new ArrayList<String>(Arrays.asList(cuentas));
        cuentasSelector.setOptions(Cuentas);

        String[] periodos=getResources().getStringArray(R.array.Periodos);
        List Periodos=new ArrayList<String>(Arrays.asList(periodos));
        periodosSelector.setOptions(Periodos,false,0).selectMultiple(false).setOnselected(new SelectorCheckbox.onSelected() {
            @Override
            public void onSelected(List selected) {
                if(selected!=null && selected.size()>0){
                    switch ((String)selected.get(0)){
                        case "Diario"://En "diario" solo se quita la posibilidad de ingreas datos opcionales ya que no es necesario establecer fecjas de corte
                            options.removeAllViews();
                            break;
                        case "Semanal":
                            options.removeAllViews();
                            final LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.f_f_a_semanal,options,false);
                            options.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            options.addView(ll);
                            ll.post(new Runnable() {
                                @Override
                                public void run() {
                                  SelectorCheckbox date=ll.findViewById(R.id.f_f_a_date);
                                    List<String> dias = new LinkedList<String>(Arrays.asList(getDayName(getCurrentLocale(getActivity()))));
                                    if(dias.contains("")){
                                        dias.remove(dias.indexOf(""));
                                    }
                                    date.setOptions(dias,false,0).selectMultiple(false);
                                    date.setText((CharSequence) dias.get(0));
                                }
                            });
                        case "Quincenal":
                            break;
                        case "Mensual":
                            final LinearLayout ll2= (LinearLayout) inflater.inflate(R.layout.f_f_a_semanal,options,false);
                            options.addView(ll2);
                            ll2.post(new Runnable() {
                                @Override
                                public void run() {
                                    DatePickerEditText date=ll2.findViewById(R.id.f_f_a_date);
                                    date.setFecha(System.currentTimeMillis());
                                }
                            });
                            break;
                        case "Anual":
                            options.removeAllViews();
                            final LinearLayout ll3= (LinearLayout) inflater.inflate(R.layout.f_f_d_anual,options,false);
                            options.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            options.addView(ll3);
                            ll3.post(new Runnable() {
                                @Override
                                public void run() {
                                    DatePickerEditText date=ll3.findViewById(R.id.f_f_a_date);
                                    date.setDayAndfullMonth(true).setFecha(System.currentTimeMillis());
                                }
                            });
                            break;
                        case "Personalizado":
                            break;
                    }
                }
            }
        });

        String[] tags=db.GetStringColumn("SELECT * FROM motives",null,"motive_");
        List Tags=new ArrayList<String>(Arrays.asList(tags));
        categoriasSelector.setOptions(Tags);

        return view;
    }

    public boolean onBackPressed() {
        return false;
    }
    public static String[] getDayName(Locale locale) {
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        String[] dayNames = symbols.getWeekdays();
        return dayNames;
    }
    Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }
}
