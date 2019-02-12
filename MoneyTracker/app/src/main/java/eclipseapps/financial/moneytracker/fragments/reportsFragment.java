package eclipseapps.financial.moneytracker.fragments;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.general;

public class reportsFragment extends baseFragment {
    private Chart Chart;
    private View extraInfo;
    private long animationDuration=2000;


    public enum  ReportsData{EgresosCombinedByTag,EgresoAcumulado}
    public enum ReportsPlot{Pie,LineChart}
    public static final int[] Denario_COLORS = {
            Color.rgb(0, 121, 107),Color.rgb(217, 242, 239), Color.rgb(0, 105, 92), Color.rgb(0, 191, 165),
             Color.rgb(100, 100, 100)
    };
    ReportsData reportDataType =ReportsData.EgresoAcumulado;
    ReportsPlot reportPlotType = ReportsPlot.Pie;

    public final static String name="reportsFragment";
    String Query;
    private RelativeLayout plotContainer;
    private RelativeLayout extraInfoContainer;
    public reportsFragment setDb(DBSmartWallet db) {
        this.db = db;
        return this;
    }

    public reportsFragment setQuery(String query) {
        Query = query;
        return this;
    }
    public void withExtraInfo(View extraInfo) {
        this.extraInfo=extraInfo;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout LL= (RelativeLayout) inflater.inflate(R.layout.i_reportsfragment,container,false);
        plotContainer=LL.findViewById(R.id.i_plot_container);
        extraInfoContainer = LL.findViewById(R.id.i_extrainfo_container);
        switch (reportPlotType){
            case Pie:
                plotContainer.removeAllViews();
                Chart =new PieChart(getActivity());
                Chart.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                plotContainer.addView(Chart);
                break;
            case LineChart:
                plotContainer.removeAllViews();
                Chart =new LineChart(getActivity());
               // Chart.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                //plotContainer.addView(Chart);
                break;
        }
        if(extraInfoContainer!=null && extraInfo!=null){
            if(extraInfo.getParent()!=null)((ViewGroup)extraInfo.getParent()).removeAllViews();
            extraInfoContainer.removeAllViews();
            extraInfoContainer.addView(extraInfo);
            extraInfoContainer.invalidate();
        }
        return LL;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(db==null)db=DBSmartWallet.getInstance(getActivity());
       OrderMap resultToPlot=new OrderMap();
       String formatlabel="dd/MM";
       switch (reportDataType){
           case EgresosCombinedByTag:
               resultToPlot=db.getAllEgresoGroupByTagsCombined(Query);
               break;
           case EgresoAcumulado:
               resultToPlot=db.getEgresoAcumuladoByDate(Query);
               if (resultToPlot.size()==1){
                   formatlabel="HH:mm";
                   resultToPlot=db.getEgresoAcumuladoByHour(Query);
               }
               if(resultToPlot.size()==1){
                   formatlabel="HH:mm";
                   resultToPlot=db.getEgresoAcumuladoByMinutes(Query);
               }
               break;
       }
        switch (reportPlotType){
            case Pie:
                double total=db.getAllEgresoFromQuery(Query);
                plotToPie(Chart,resultToPlot,total);
                break;
            case LineChart:
                plotToLineChart(resultToPlot,formatlabel);
                break;
        }


    }
    public reportsFragment setReportDataType(ReportsData reports){
        reportDataType =reports;
        return this;
    }

    public reportsFragment setReportPlotType(ReportsPlot reportPlotType) {
        this.reportPlotType = reportPlotType;
        return this;
    }

    public static reportsFragment attachReportWithQuery(FragmentManager fm, int containerForReport, DBSmartWallet db, String Query){
        FragmentTransaction FT=fm.beginTransaction();
        Fragment report=fm.findFragmentByTag(reportsFragment.name);
        if(report==null){
            report=new reportsFragment();
            reportsFragment reports= (reportsFragment) report;
            reports.setDb(db).setQuery(Query);
            FT.replace(containerForReport,reports,reportsFragment.name);
        }else{
            reportsFragment reports= (reportsFragment) report;
            reports.setDb(db).setQuery(Query);
            FT.detach(reports);
            FT.attach(reports);
        }
        FT.commitAllowingStateLoss();
        return (reportsFragment) report;
    }

    public Chart plotToPie(Chart chart,OrderMap data,double total){
        if(data!=null && data.size()>0){

            List<PieEntry> entries = new ArrayList<PieEntry>();
            for (int i=0;i<data.size();i++) {
                // turn your data into Entry objects
                double quantity=(double)data.get(i);
                quantity=quantity/total;
                quantity=quantity*100;
                float toGraph= (float) quantity;
                entries.add(new PieEntry(toGraph,(data.keyAt(i)+"%").replace(",",",\n")));
            }
            PieDataSet pieDataSet=new PieDataSet(entries,null);
            pieDataSet.setColors(Denario_COLORS);
            pieDataSet.setValueTextColor(Color.WHITE);
            pieDataSet.setValueTextSize(20f);
            pieDataSet.setSliceSpace(5f);
            PieData pieData=new PieData(pieDataSet);


            chart.setData(pieData);
            chart.invalidate();

        }else{
            // piechart.setDescription(new Description());
            ((ViewGroup)(chart.getParent())).removeAllViews();
            //(Chart).setNoDataTextColor(R.color.colorAccent);
            //(Chart).setNoDataText("No se cuentan con datos"); // this is the top line
            //(Chart).invalidate();
        }
        return chart;
    }

    private void plotToLineChart(final OrderMap data, final String formatLabelX){
         if(data!=null && data.size()>1){

            List<Entry> entries = new ArrayList<Entry>();
            for (int i=0;i<data.size();i++) {
                // turn your data into Entry objects
                double quantity=(double)data.get(i);
                float toGraph= (float) quantity;
                entries.add(new Entry(i,Math.abs(toGraph)));
            }
            LineDataSet lineDataSet=new LineDataSet(entries,null);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setCubicIntensity(.09f);
            lineDataSet.setFillColor(R.color.colorAccent);
            lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.fondo_rectangulo_verde_accent));
            lineDataSet.setDrawFilled(true);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawIcons(false);
            lineDataSet.setColor(Color.rgb(255, 255, 255));
            lineDataSet.setCircleColor(Denario_COLORS[1]);



            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineData lineData=new LineData(lineDataSet);
            ((LineChart)Chart).setData(lineData);


            IAxisValueFormatter formatter = new IAxisValueFormatter() {
                final DateFormat df = new SimpleDateFormat(formatLabelX);
                Calendar cal=Calendar.getInstance();
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    cal.setTimeInMillis((Long) data.keyAt((int)value));
                    String format=df.format(cal.getTime());
                    if(value==data.size()-1){
                        format=format+"        .";
                    }else if(value==0){
                        format=".        "+format;
                    }
                    return format;
                }

            };

            XAxis xAxis = ((LineChart)Chart).getXAxis();
            xAxis.setValueFormatter(formatter);

            ((LineChart)Chart).setDrawGridBackground(false);
            ((LineChart)Chart).setGridBackgroundColor(android.R.color.transparent);
            Description description=new Description();
            description.setText("Egresos ");
            description.setTextColor(Color.rgb(255, 255, 255));
            description.setTextSize(21);
            ((LineChart)Chart).setDescription(description);

            xAxis.setDrawGridLines(false);
            //xAxis.setCenterAxisLabels(true);
            if(data.size()>4){
                xAxis.setLabelCount(4,true);
                //xAxis.setCenterAxisLabels(true);
            }else  if(data.size()>1){
                xAxis.setLabelCount(1,true);
            }
            xAxis.setAvoidFirstLastClipping(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(R.color.colorPrimary);
            xAxis.setTextSize(15);

            ((LineChart)Chart).getAxisRight().setEnabled(false);
            ((LineChart)Chart).getAxisLeft().setLabelCount(3,true);
            ((LineChart)Chart).getAxisLeft().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            ((LineChart)Chart).getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    if(value==axis.getAxisMinimum())return "";
                    return "$ "+String.valueOf((int)value);
                }
            });
            ((LineChart)Chart).getAxisLeft().setTextColor(R.color.colorPrimary);
            ((LineChart)Chart).getAxisLeft().setTextSize(15);
            ((LineChart)Chart).getAxisLeft().setYOffset(-8);
            ((LineChart)Chart).getAxisLeft().enableGridDashedLine(10,10,1);
            ((LineChart)Chart).setViewPortOffsets(0, general.dpsToPixels(getActivity(),30), 0, general.dpsToPixels(getActivity(),30));
            Chart.getLegend().setEnabled(false);
            Chart.animateY((int) animationDuration);
            //Chart.animateX(4000);
            Chart.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            plotContainer.addView(Chart);
            Chart.post(new Runnable() {
                @Override
                public void run() {
                    ((LineChart)Chart).invalidate();
                }
            });

        }else{
            // piechart.setDescription(new Description());
            ((LineChart)Chart).setNoDataTextColor(R.color.colorAccent);
            ((LineChart)Chart).setNoDataText("No se cuentan con datos"); // this is the top line
            ((LineChart)Chart).invalidate();
        }
    }

    boolean withAnimation=true;
    public ListView getEgressReportList(){
        OrderMap egressSplitedByTag=db.getAllEgresoGroupByTagsSplited(Query);
        List<absoluteByCategory> absolutes=new ArrayList<>();
        for (int i=0;i<egressSplitedByTag.size();i++){
            double value= (double)egressSplitedByTag.get(i);
            absolutes.add(new absoluteByCategory().setCategoryName((String) egressSplitedByTag.keyAt(i)).setAbsolute(Math.abs((float) value)));
        }

        withAnimation=true;
        final ListView list=new ListView(db.getcontext());
        list.setAdapter(new ArrayAdapter<absoluteByCategory>(db.getcontext(),R.layout.i_a_egressbycategory,absolutes){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                if(convertView==null){
                    convertView=getLayoutInflater().inflate(R.layout.i_a_egressbycategory,parent,false);
                }
                TextView categoryName=convertView.findViewById(R.id.i_a_categoryname);
                TextView percent=convertView.findViewById(R.id.i_a_categorypercent);
                ProgressBar bar=convertView.findViewById(R.id.i_a_categorypercentbar);
                absoluteByCategory data=getItem(position);
                categoryName.setText(data.getCategoryName());
                percent.setText("-$ "+String.format("%.2f", data.getAbsolute()));
                setProgressMax(bar,(int) getItem(0).getAbsolute());
                if(withAnimation){
                    setProgressAnimate(bar,(int) data.getAbsolute());
                }else {
                    bar.setProgress((int) data.getAbsolute() * 100);
                }

                return convertView;
            }
            private void setProgressMax(ProgressBar pb, int max) {
                pb.setMax(max * 100);
            }

            private void setProgressAnimate(ProgressBar pb, int progressTo)
            {
                ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo * 100);
                animation.setDuration(animationDuration);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
            }
        });
        list.post(new Runnable() {
            @Override
            public void run() {
                list.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public int firstVisibleItem;
                    private int mLastFirstVisibleItem=-1;
                    @Override
                    public void onScrollStateChanged(AbsListView absListView, int i) {
                        if (i == 1) {
                            withAnimation=false;
                        } else {
                          //  withAnimation=false;
                        }
                    }

                    @Override
                    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                    }
                });
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MainActivity)getActivity()).usabilityAppTracking(AnalyticsApplication.Gestures.Click,"OnEgressReportListElement");
            }
        });
        return list;
    }

    public static class absoluteByCategory {
        private float absolute;
        private String categoryName;

        public float getAbsolute() {
            return absolute;
        }

        public absoluteByCategory setAbsolute(float percent) {
            this.absolute = percent;
            return this;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public absoluteByCategory setCategoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }
    }
}
