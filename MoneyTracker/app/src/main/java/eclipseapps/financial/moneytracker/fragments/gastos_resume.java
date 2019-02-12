package eclipseapps.financial.moneytracker.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet.datesFilter;
import eclipseapps.financial.moneytracker.customViews.MyPieChartRender;
import eclipseapps.libraries.library.general.functions.OrderMap;

public class gastos_resume extends baseFragment implements View.OnClickListener {
    public static String TAG_FragmentName ="Resumen";
    PieView pieView;
    double esteMes_cantidad;
    double estaSemana_cantidad;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ScrollView rootView= (ScrollView) inflater.inflate(R.layout.k_gastosresume,container,false);
        ((TextView)rootView.findViewById(R.id.k_hoy_cantidad)).setText(String.format("%.2f", db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.Hoy))).replace("-","-$"));
        ((TextView)rootView.findViewById(R.id.k_hoyhaceunasemana_cantidad)).setText(String.format("%.2f", db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.Hoy_haceUnaSemana))).replace("-","-$"));

        double ayer_cantidad= db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.Ayer));
        double ayer_cantidad_hace1semana=db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.Ayer_haceUnaSemana));
        double porcentaje=0;
        if(ayer_cantidad_hace1semana==0&&ayer_cantidad!=0)porcentaje=501;
        else if(ayer_cantidad_hace1semana!=0&&ayer_cantidad==0)porcentaje=-100;
        else if(ayer_cantidad_hace1semana!=0){
            porcentaje=ayer_cantidad/ayer_cantidad_hace1semana*100;
        }

        ((TextView)rootView.findViewById(R.id.k_ayer_cantidad)).setText(String.format("%.2f",ayer_cantidad).replace("-","-$"));
        ((TextView)rootView.findViewById(R.id.k_ayerhaceunasemana_cantidad)).setText(String.format("%.2f",ayer_cantidad_hace1semana).replace("-","-$"));
        if(porcentaje==0)((TextView)rootView.findViewById(R.id.k_ayerhaceunasemana_cantidad_porcentajedecambio)).setVisibility(View.INVISIBLE);
        else{
            ((TextView)rootView.findViewById(R.id.k_ayerhaceunasemana_cantidad_porcentajedecambio)).setVisibility(View.VISIBLE);
            ((TextView)rootView.findViewById(R.id.k_ayerhaceunasemana_cantidad_porcentajedecambio)).setText("("+(porcentaje>500?">500":String.format("-%.0f",porcentaje))+"%)");
            ((TextView)rootView.findViewById(R.id.k_ayerhaceunasemana_cantidad_porcentajedecambio)).setTextColor(porcentaje>100?Color.RED:reportsFragment.Denario_COLORS[2]);
        }


        estaSemana_cantidad=db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.EstaSemana));
        ((TextView)rootView.findViewById(R.id.k_estasemana_cantidad)).setText(String.format("%.2f", estaSemana_cantidad).replace("-","-$"));
        ((TextView)rootView.findViewById(R.id.k_semanapasadaalmismodia_cantidad)).setText(String.format("%.2f", db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.SemanaPasadaAlMismoDia))).replace("-","-$"));
        esteMes_cantidad=db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.EsteMes));
        ((TextView)rootView.findViewById(R.id.k_estemes_cantidad)).setText(String.format("%.2f", esteMes_cantidad).replace("-","-$"));
        ((TextView)rootView.findViewById(R.id.k_mespasadoalmismodia_cantidad)).setText(String.format("%.2f", db.getAllEgresoFromQuery(db.getQueryByDate(datesFilter.MesPasadoAlMismoDia))).replace("-","-$"));
        rootView.findViewById(R.id.cardView_hoy).setOnClickListener(this);
        rootView.findViewById(R.id.cardView_ayer).setOnClickListener(this);
        rootView.findViewById(R.id.cardView_estasemana).setOnClickListener(this);
        rootView.findViewById(R.id.cardView_estemes).setOnClickListener(this);
        pieView=rootView.findViewById(R.id.k_estemes_pieView);
        // Change the color fill of the bar representing the current percentage
        pieView.setPercentageBackgroundColor(getResources().getColor(R.color.colorAccent));
        return rootView;


    }
    public static void loadOnMain(MainActivity activity,int container){
        gastos_resume resume= (gastos_resume) activity.getSupportFragmentManager().findFragmentByTag(TAG_FragmentName);
        if (resume==null){resume= new gastos_resume();}
        resume.setDb(DBSmartWallet.getInstance(activity));
        FragmentTransaction fragmentransaction=activity.getSupportFragmentManager().beginTransaction();
        fragmentransaction.setCustomAnimations(R.anim.pushleftin,R.anim.pushleftout);
        fragmentransaction.replace(container,resume, TAG_FragmentName);
        fragmentransaction.commitAllowingStateLoss();
        activity.hideSlideUpPanel();
        activity.getToolBar().removeAllViews();
        activity.refreshToolbar(null);
        activity.setTitle(activity.getString(R.string.resumen));
        //activity.spinner.setVisibility(View.GONE);
       // activity.fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OrderMap<String,Double> mapByTag=db.getAllEgresoGroupByTagsSplited(db.getQueryByDate(datesFilter.EsteMes));
        if(mapByTag!=null && mapByTag.size()>0){
            double percentageByTag=((Double)mapByTag.get(0))/esteMes_cantidad*100;
            pieView.setPercentage((float) percentageByTag);
            pieView.setVisibility(View.VISIBLE);
            PieAngleAnimation animation = new PieAngleAnimation(pieView);
            animation.setDuration(5000); //This is the duration of the animation in millis
            pieView.startAnimation(animation);
            ((TextView)getView().findViewById(R.id.k_estemes_gastoprincipal)).setText((CharSequence) mapByTag.keyAt(0));
        }else{
            pieView.setVisibility(View.INVISIBLE);
        }

        final OrderMap<String,Double> mapByday=db.getAllEgresoGroupByDaySplited(db.getQueryByDate(datesFilter.EstaSemana));
        //mapByday.put(0,"Martes",78.0d);//PI_DEBUG
        if(mapByday.size()>0){
            final OrderMap<String,Double> mapByPrincipalDay=new OrderMap<>();

            if(mapByday.size()>1){
               double restoSemana=0;
               for(int i=mapByday.size()-1;i>=0;i--){
                   if(i>0)restoSemana=restoSemana+(double)mapByday.get(i);
               }
               mapByPrincipalDay.put("RestoSemana",restoSemana);
           }
            mapByPrincipalDay.put(mapByday.keyAt(0),mapByday.get(0));
            reportsFragment report=new reportsFragment();
            PieChart chart= (PieChart) report.plotToPie((Chart) getView().findViewById(R.id.k_semana_piebydayview),mapByPrincipalDay,Math.abs(estaSemana_cantidad));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final PieDataSet set= (PieDataSet) chart.getData().getDataSet();

                chart.getLegend().setEnabled(false);
                chart.setDrawEntryLabels(false);
                if(mapByPrincipalDay.size()==1){
                    chart.setCenterText(mapByPrincipalDay.keyAt(0)+"\n"+"100%");
                    chart.setCenterTextColor(getContext().getColor(android.R.color.white));
                   set.setDrawValues(false);
                    chart.setExtraOffsets(0, 15f, 0,  0);
                }else{
                    set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                    //set.setValueLinePart1OffsetPercentage(100f);
                    set.setValueLinePart1Length(0.6f);
                    set.setValueLinePart2Length(0.6f);
                    set.setValueTextColor(getContext().getColor(R.color.colorAccent));
                    set.setValueFormatter(new IValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                            if(set.getEntryIndex(entry)==mapByPrincipalDay.size()-1){
                                return mapByPrincipalDay.keyAt(mapByPrincipalDay.size()-1)+"\n"+String.format("%.0f",Math.floor(value))+"%";
                            }else{
                                return "Semana\nrestante";
                            }

                        }
                    });
                    chart.setRenderer(new MyPieChartRender(chart,chart.getAnimator(),chart.getViewPortHandler()));
                    chart.setExtraOffsets(7f, 5f, 7f,  5f);
                }

                chart.setDrawHoleEnabled(false);
                chart.setDrawSlicesUnderHole(false);
                chart.setUsePercentValues(true);
                chart.getData().getDataSet().setValueTextSize(12f);
                chart.getData().getDataSet().setLabel(null);
                chart.setDescription(null);
                chart.animateY(5000);
               // chart.spin( 5000,0,-360f, Easing.EasingOption.EaseInOutQuad);
                //chart.invalidate();
                //chart.getData().setValueTextColor(getContext().getColor(R.color.colorAccent));
            }
        }




    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cardView_hoy:
                ListaMovimientos.loadOnMain((MainActivity)getActivity(),R.id.content_main_maincontainer).setQuery(db.getQueryByDate(datesFilter.Hoy));
                break;
            case R.id.cardView_ayer:
                ListaMovimientos.loadOnMain((MainActivity)getActivity(),R.id.content_main_maincontainer).setQuery(db.getQueryByDate(datesFilter.Ayer));
                break;
            case R.id.cardView_estasemana:
                ListaMovimientos.loadOnMain((MainActivity)getActivity(),R.id.content_main_maincontainer).setQuery(db.getQueryByDate(datesFilter.EstaSemana));
                break;
            case R.id.cardView_estemes:
                ListaMovimientos.loadOnMain((MainActivity)getActivity(),R.id.content_main_maincontainer).setQuery(db.getQueryByDate(datesFilter.EsteMes));
                break;
        }
    }
}
