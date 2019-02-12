package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import eclipseapps.android.FragmentN;
import eclipseapps.android.customviews.TextViewRoboto;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.activities.Movement;
import eclipseapps.financial.moneytracker.adapters.yahoo_nativeAd_Adapter;
import eclipseapps.financial.moneytracker.cloud.ventas;
import eclipseapps.financial.moneytracker.customViews.FilterSelector;
import eclipseapps.financial.moneytracker.customViews.cronoCounterDown;
import eclipseapps.libraries.library.general.functions.OrderMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class ListaMovimientos extends baseFragment {
    String Query;
    public final static int menuItem_filtrar=2;
    boolean ratedShowedBefore=false;
    public final static String TAG_Fragment="ListaMovimientos";

    final static int menuItem_editar=0;//La posicion de el item del menu dentro del menu xml
    final static int menuItem_borrar=1;//La posicion de el item del menu dentro del menu xml
    private Menu _menu;


    final static int Action_agregarMovimiento=100;
    public final static String Last_Spinner_Position="Last_Spinner_Position";
    public static ListaMovimientos listaMovimientos;
    MainActivity activity;
    FilterSelector spinner;
    private fragment_movements_list list;
    FloatingActionButton fab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout rootView= (RelativeLayout) inflater.inflate(R.layout.b_listonmain,container,false);
        fab=rootView.findViewById(R.id.b_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query="";
                Intent I=new Intent(activity,Movement.class);
                startActivityForResult(I, Action_agregarMovimiento);
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list=loadList();

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        _menu=menu;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_borrar) {
            showOkCancelDialog("Seguro deseas eliminar estos movimientos?", "Si", "No", true, new Dialogs.DialogsInterface() {
                @Override
                public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                    if(sucess){
                        String condition="";
                        for (int i=0;i<list.getSelecteditems().size();i++){
                            Object j=list.getSelecteditems().get(i);
                            condition=condition+"id_="+(long)j;
                            if (i<list.getSelecteditems().size()-1)condition=condition+ " OR ";
                        }
                        String select="SELECT DISTINCT cuenta_ FROM basics WHERE ";
                        Cursor cuentasafectadas=db.getDBInstance().rawQuery(select+condition,null);
                        cuentasafectadas.moveToFirst();
                        do{
                            String cuenta=cuentasafectadas.getString(cuentasafectadas.getColumnIndex("cuenta_"));
                            cuenta="'"+cuenta+"'";
                            float balance=0;
                            Cursor cur=db.getDBInstance().rawQuery("SELECT cantidad_ FROM basics WHERE cuenta_="+cuenta +" AND "+condition,null);
                            cur.moveToFirst();
                            do{
                                balance=balance+cur.getFloat(cur.getColumnIndex("cantidad_"))*-1;
                            }while (cur.moveToNext());
                            Cursor cantidad=db.getDBInstance().rawQuery("SELECT cantidad_ FROM cuentas WHERE cuenta_="+cuenta,null);
                            cantidad.moveToFirst();
                            float cantidadactual=cantidad.getFloat(0);
                            db.getDBInstance().execSQL("UPDATE cuentas SET cantidad_="+(cantidadactual+balance)+" WHERE cuenta_="+cuenta);
                        }while (cuentasafectadas.moveToNext());

                        String Borrar="DELETE FROM basics WHERE ";
                        Borrar=Borrar+condition;
                        db.getDBInstance().execSQL(Borrar);//La basede datos esta programada para funcionar en cascada (borra todos los registros relacionados a este movimiento)

                        refresDataAndCleanToolBar();
                        activity.writeMovementsTracking(AnalyticsApplication.Write.Delete, AnalyticsApplication.Action.Movement);
                    }
                }
            });
            return true;
        }else if (id == R.id.action_edit) {
            if(activity.mIsPremium){
                Intent I=new Intent(activity,Movement.class);
                I.putExtra("Movimiento", (Long) list.getSelecteditems().get(0));
                startActivityForResult(I, menuItem_editar);
            }else{
                long timeforEditEnabled=activity.sp.getLong(ventas.sellsPreferences.TimeToEditEnable, SystemClock.elapsedRealtime());
                long timenow= SystemClock.elapsedRealtime();
                boolean timeReached=timeforEditEnabled<=timenow;
                if(timeReached){
                    Intent I=new Intent(activity,Movement.class);
                    I.putExtra("Movimiento", (Long) list.getSelecteditems().get(0));
                    startActivityForResult(I, menuItem_editar);
                }else{
                    cronoCounterDown crono=new cronoCounterDown(activity);
                    crono.setCounterDownTimer(timeforEditEnabled-timenow,1000);
                    showOkDialogFromView(crono, "EDITAR YA", true, new Dialogs.DialogsInterface() {
                        @Override
                        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                            if(sucess){
                                activity.askForPremium("ForEnableEdit");
                            }
                        }
                    });

                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Action_agregarMovimiento && resultCode==RESULT_OK){
            showListWithDefaultSpinner();
        }else if (requestCode==menuItem_editar){
            clearSelectedItems();
            if(resultCode==RESULT_OK){
                list.refreshData();
                activity.writeMovementsTracking(AnalyticsApplication.Write.Update, AnalyticsApplication.Action.Movement);
            }
        }else if(resultCode==RESULT_OK && requestCode==menuItem_filtrar){
            Query=data.getStringExtra("Filtro");
            clearSelectedItems();
            loadList().LoadQuery(Query,null);
            activity.getSlidingRootNav().closeMenu(true);
            activity.getSupportActionBar().setTitle("Filtro");
            activity.getSupportActionBar().setIcon(R.drawable.icono_filtro);
            fab.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.GONE);
            activity.ReadMovementsTracking("Filter",Query);
        }else if(resultCode==RESULT_CANCELED && list!=null && list.Queryonbasics!=null && !list.Queryonbasics.matches("")){
           // spinner.getOnItemSelectedListener().onItemSelected(null,null,spinner.getSelectedItemPosition(),0);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (list.getSelecteditems().size()>0){
            list.getSelecteditems().clear();
            list.refreshData();
            MenuItem menu=_menu.getItem(menuItem_borrar);
            menu.setVisible(false);
            MenuItem menu2=_menu.getItem(menuItem_editar);
            menu2.setVisible(false);
            spinner.setVisibility(View.VISIBLE);
            activity.getSupportActionBar().setIcon(R.drawable.ic_date_range_white_36dp);
            return true;
        }
        return super.onBackPressed();
    }
    public static ListaMovimientos loadOnMain(final MainActivity activity, int container){
        Fragment fragment=activity.getSupportFragmentManager().findFragmentByTag(TAG_Fragment);
        if(fragment==null)fragment=new ListaMovimientos().setActivity(activity);
        FragmentTransaction ft=activity.getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.pushleftin,R.anim.pushleftout);
        ft.replace(container,fragment,TAG_Fragment);
        ft.commitAllowingStateLoss();
        listaMovimientos= (ListaMovimientos) fragment;


        activity.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        activity.getToolBar().removeAllViews();
        activity.refreshToolbar(null);
        FilterSelector spinner=new FilterSelector(activity);
        spinner.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        activity.getToolBar().addView(spinner);
        /*
        spinner.setAdapter(new datesFilter());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            Calendar calendar=Calendar.getInstance();
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(datesFilter.options[i].matches(datesFilter.Hoy)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    String Query="SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+System.currentTimeMillis()+" ORDER BY tiempo_ DESC";
                    listaMovimientos.list.setQuery(Query,null);
                    listaMovimientos.list.refreshData();
                    activity.ReadMovementsTracking("Filter","Hoy");
                }else if(datesFilter.options[i].matches(datesFilter.Ayer)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    long limiteSup=calendar.getTimeInMillis();
                    calendar.add(Calendar.HOUR_OF_DAY,-24);
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    String Query="SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
                    listaMovimientos.list.setQuery(Query,null);
                    listaMovimientos.list.refreshData();
                    activity.ReadMovementsTracking("Filter","Ayer");
                }else if(datesFilter.options[i].matches(datesFilter.sieteDias)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    long limiteSup=calendar.getTimeInMillis();
                    calendar.add(Calendar.HOUR_OF_DAY,-24*6);
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    String Query="SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
                    if(listaMovimientos.list!=null){
                        listaMovimientos.list.setQuery(Query,null);
                        listaMovimientos.list.refreshData();
                    }
                    activity.ReadMovementsTracking("Filter","sieteDias");
                }else if(datesFilter.options[i].matches(datesFilter.esteMes)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    long limiteSup=calendar.getTimeInMillis();
                    calendar.set(Calendar.DAY_OF_MONTH,1);
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    String Query="SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
                    listaMovimientos.list.setQuery(Query,null);
                    listaMovimientos.list.refreshData();
                    activity.ReadMovementsTracking("Filter","esteMes");
                }else if(datesFilter.options[i].matches(datesFilter.treintaDias)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    long limiteSup=calendar.getTimeInMillis();
                    calendar.add(Calendar.HOUR_OF_DAY,-24*29);
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    String Query="SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
                    if(listaMovimientos.list!=null){
                        listaMovimientos.list.setQuery(Query,null);
                        listaMovimientos.list.refreshData();
                    }
                    activity.ReadMovementsTracking("Filter","treintaDias");
                }else if(datesFilter.options[i].matches(datesFilter.mesPasado)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.DAY_OF_MONTH,1);
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    long limiteSup=calendar.getTimeInMillis();
                    calendar.add(Calendar.MONTH,-1);
                    calendar.set(Calendar.HOUR_OF_DAY,0);
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND,0);
                    String Query="SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
                    listaMovimientos.list.setQuery(Query,null);
                    listaMovimientos.list.refreshData();
                    activity.ReadMovementsTracking("Filter","mesPasado");
                }else if(datesFilter.options[i].matches(datesFilter.total)){
                    String Query="SELECT * FROM basics ORDER BY tiempo_ DESC";
                    listaMovimientos.list.setQuery(Query,null);
                    listaMovimientos.list.refreshData();
                    activity.ReadMovementsTracking("Filter","total");
                }
                activity.sp.edit().putInt(Last_Spinner_Position,i).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner.setSelection(activity.sp.getInt(Last_Spinner_Position,2),true);*/
        listaMovimientos.setSpinner(spinner);
        listaMovimientos.setActivity(activity);
        return listaMovimientos;
    }
    public ListaMovimientos setActivity(MainActivity activity) {
        this.activity = activity;
        return this;
    }
    public void setSpinner(FilterSelector spinner){
        this.spinner=spinner;
    }
    public void showListWithDefaultSpinner(){
        clearSelectedItems();
        loadList();
      //  int selected=spinner.getSelectedItemPosition();
//        spinner.setAdapter(new datesFilter());
 //       spinner.setSelection(selected);
        spinner.setVisibility(View.VISIBLE);
        activity.getSupportActionBar().setTitle("");
        activity.getSupportActionBar().setIcon(R.drawable.ic_date_range_white_36dp);
        fab.setVisibility(View.VISIBLE);
        activity.getSlidingRootNav().closeMenu(true);
    }
    public void refresDataAndCleanToolBar(){
        clearSelectedItems();
        list.refreshData();
        if(list!=null && list.getAdapter()!=null &&  list.getAdapter() instanceof yahoo_nativeAd_Adapter && list.isVisible()){
            FragmentTransaction ft=getChildFragmentManager().beginTransaction();
            ft.detach(list);
            ft.attach(list);
            ft.commitAllowingStateLoss();
        }
    }
    public void clearSelectedItems(){
        if (list!=null && list.getSelecteditems()!=null && list.getSelecteditems().size()>0){
            list.getSelecteditems().clear();
            MenuItem menu=_menu.getItem(menuItem_borrar);
            menu.setVisible(false);
            MenuItem menu2=_menu.getItem(menuItem_editar);
            menu2.setVisible(false);
            spinner.setVisibility(View.VISIBLE);
            activity.getSupportActionBar().setIcon(R.drawable.ic_date_range_white_36dp);
        }
    }
    public fragment_movements_list loadList(){
        list= (fragment_movements_list) getChildFragmentManager().findFragmentByTag("MovementsList");
        if (list==null){
            list=new fragment_movements_list().LoadQuery(Query, null);
        }
        FragmentTransaction fragmentransaction=getChildFragmentManager().beginTransaction();
        fragmentransaction.setCustomAnimations(R.anim.pushleftin,R.anim.pushleftout);
        fragmentransaction.replace(R.id.b_listcontainer, list,"MovementsList");
        fragmentransaction.commitAllowingStateLoss();
        list.setOnClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if ((list.getSelecteditems().size()>0)){
                    MenuItem menu=_menu.getItem(menuItem_borrar);
                    menu.setVisible(true);
                    if(!activity.getSupportActionBar().getTitle().toString().matches("Filtro")){
                        spinner.setVisibility(View.GONE);
                        activity.getSupportActionBar().setIcon(null);
                    }

                }else{
                    MenuItem menu=_menu.getItem(menuItem_borrar);
                    menu.setVisible(false);
                    if(!activity.getSupportActionBar().getTitle().toString().matches("Filtro")){
                        spinner.setVisibility(View.VISIBLE);
                        activity.getSupportActionBar().setIcon(R.drawable.ic_date_range_white_36dp);
                    }
                }
                if (list.getSelecteditems().size()==1){
                    MenuItem menu=_menu.getItem(menuItem_editar);
                    menu.setVisible(true);
                }else{
                    MenuItem menu=_menu.getItem(menuItem_editar);
                    menu.setVisible(false);
                }

                return false;
            }
        });
        list.setOnRefreshDataListener(new FragmentN.Action() {
            @Override
            public Object execute(Intent intent) {
                if(intent!=null) {
                    activity.refreshReportForQuery(list.Queryonbasics,intent);

                }
                return null;
            }
        });
        list.setAction(new FragmentN.Action() {
            @Override
            public Object execute(Intent intent) {
                if(intent!=null && intent.getAction().matches("RatedFalse")){
                    if(!ratedShowedBefore){
                        ratedShowedBefore=true;
                        activity.showRatingDialog();
                    }
                }
                return null;
            }
        });
        return list;
    }


    public void setQuery(String query) {
        Query=query;
    }

    public static class datesFilter implements SpinnerAdapter {
        public final static String Hoy="Hoy";
        public final static String Ayer="Ayer";
        public final static String sieteDias="Últimos 7 días";
        public final static String esteMes="Este mes";
        public final static String treintaDias="Últimos 30 días";
        public final static String mesPasado="El mes pasado";
        public final static String total="Total";
        public final static String[] options=new String[]{Hoy,Ayer,sieteDias,esteMes,treintaDias,mesPasado,total};


        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int i) {
            return options[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
        //Viaticando
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if(view==null){
                LayoutInflater layoutInflater=LayoutInflater.from(viewGroup.getContext());
                view=layoutInflater.inflate(R.layout.datespinner_element,viewGroup,false);
            }
            TextViewRoboto V= (TextViewRoboto) view;
            if(Build.VERSION.SDK_INT>=23){
                V.setBackgroundColor(viewGroup.getContext().getResources().getColor(android.R.color.transparent,null));
                V.setTextColor(viewGroup.getContext().getResources().getColor(R.color.cardview_light_background,null));
            }else{
                V.setBackgroundColor(viewGroup.getContext().getResources().getColor(android.R.color.transparent));
                V.setTextColor(viewGroup.getContext().getResources().getColor(R.color.cardview_light_background));
            }

            V.setText((String)getItem(position));
            return V;
        }
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
                convertView=layoutInflater.inflate(R.layout.datespinner_element,parent,false);
            }
            TextViewRoboto V= (TextViewRoboto) convertView;
            V.setText((String)getItem(position));
            return V;
        }
        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
