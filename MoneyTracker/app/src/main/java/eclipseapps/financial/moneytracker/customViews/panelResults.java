package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import eclipseapps.android.customviews.FontFitTextView;
import eclipseapps.financial.moneytracker.R;

public class panelResults extends RelativeLayout{
    private FontFitTextView total;
    private FontFitTextView ingreso;
    private FontFitTextView egreso;

    private double Total=0d;
    private double Ingreso=0d;
    private double Egreso=0d;

    public panelResults(Context context) {
        super(context);
        init();
    }

    public panelResults(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public panelResults(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        inflate(getContext(), R.layout.customviews_panelresult,this);
        total=findViewById(R.id.customviews_panelresults_total);
        ingreso=findViewById(R.id.customviews_panelresults_ingreso);
        egreso=findViewById(R.id.customviews_panelresults_egresos);

        this.total.setText(String.format("%.2f", Total).replace("-","-$"));
        this.ingreso.setText("Ingresos: $"+String.format("%.2f", Ingreso));
        this.egreso.setText("Egresos:"+String.format("%.2f", Egreso).replace("-","-$"));
    }

    public double getTotal() {
        return Total;
    }

    public panelResults setTotal(double total) {
        Total = total;
        if(this.total!=null)this.total.setText(String.format("%.2f", total).replace("-","-$"));
        return this;
    }

    public double getIngreso() {
        return Ingreso;
    }

    public panelResults setIngreso(double ingreso) {
        Ingreso = ingreso;
        if(this.ingreso!=null)this.ingreso.setText("Ingresos: $"+String.format("%.2f", ingreso));
        return this;
    }

    public double getEgreso() {
        return Egreso;
    }

    public panelResults setEgreso(double egreso) {
        Egreso = egreso;
        if(this.egreso!=null)this.egreso.setText("Egresos:"+String.format("%.2f", egreso).replace("-","-$"));
        return this;
    }
    /*

        ReusumePanel.setOnTouchListener(new View.OnTouchListener() {
                                            float x1, y1, x2, y2;
                                            long t1, t2;
                                            final long CLICK_DURATION=500;
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {

                                                switch (event.getAction()) {

                                                    case MotionEvent.ACTION_DOWN:
                                                        x1 = event.getX();
                                                        y1 = event.getY();
                                                        t1 = System.currentTimeMillis();
                                                        return true;
                                                    case MotionEvent.ACTION_UP:
                                                        x2 = event.getX();
                                                        y2 = event.getY();
                                                        t2 = System.currentTimeMillis();

                                                        if (Math.abs(x1 - x2)<30 && Math.abs(y1-y2)<30) {
                                                            if((t2 - t1) < CLICK_DURATION){
                                                                ((MainActivity)getActivity()).usabilityAppTracking(trackedActivity.Gestures.Click,"ResumePanel");
                                                            }else{
                                                                ((MainActivity)getActivity()).usabilityAppTracking(trackedActivity.Gestures.LongClick,"ResumePanel");
                                                            }
                                                        } else if(Math.abs(x1-x2)>Math.abs(y1-y2)){//El desplazamiento fue primordialmente horizontal
                                                            if (x1 > x2) {
                                                                ((MainActivity)getActivity()).usabilityAppTracking(trackedActivity.Gestures.LeftSwipe,"ResumePanel");
                                                            } else if (x2 > x1) {
                                                                ((MainActivity)getActivity()).usabilityAppTracking(trackedActivity.Gestures.RightSwipe,"ResumePanel");
                                                            }
                                                        }else{
                                                            if (y1 > y2) {//En la pantalla los pixeles superioires tienen valores mas pequeños
                                                                ((MainActivity)getActivity()).usabilityAppTracking(trackedActivity.Gestures.UpSwipe,"ResumePanel");
                                                            } else if (y2 > y1) {
                                                                ((MainActivity)getActivity()).usabilityAppTracking(trackedActivity.Gestures.downSwipe,"ResumePanel");
                                                            }
                                                        }


                                                        return true;
                                                }

                                                return false;
                                            }
                                        });
     */
}
