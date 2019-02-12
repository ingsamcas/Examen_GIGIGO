package eclipseapps.financial.moneytracker.activities;

import android.accounts.Account;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncRequest;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;
import com.yarolegovich.slidingrootnav.callback.DragStateListener;

import java.util.List;

import eclipseapps.android.FragmentN;
import eclipseapps.android.customviews.FontFitTextView;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.cuentas;
import eclipseapps.financial.moneytracker.cloud.user;
import eclipseapps.financial.moneytracker.cloud.ventas;
import eclipseapps.financial.moneytracker.customViews.panelResults;
import eclipseapps.financial.moneytracker.fragments.AccountDetails;
import eclipseapps.financial.moneytracker.fragments.ListaMovimientos;
import eclipseapps.financial.moneytracker.fragments.Presupuesto_manager;
import eclipseapps.financial.moneytracker.fragments.RFC_manager;
import eclipseapps.financial.moneytracker.fragments.SettingsFragment;
import eclipseapps.financial.moneytracker.fragments.baseFragment;
import eclipseapps.financial.moneytracker.fragments.categoryManager;
import eclipseapps.financial.moneytracker.fragments.gastos_resume;
import eclipseapps.financial.moneytracker.fragments.reportsFragment;
import eclipseapps.financial.moneytracker.sync.Authenticator;
import eclipseapps.financial.moneytracker.sync.SyncAdapter;
import eclipseapps.libraries.library.general.functions.OrderMap;

import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.Premium;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumOffer;
import static eclipseapps.financial.moneytracker.fragments.ListaMovimientos.menuItem_filtrar;
import static eclipseapps.financial.moneytracker.fragments.fragment_movements_list.result_egreso;
import static eclipseapps.financial.moneytracker.fragments.fragment_movements_list.result_ingreso;
import static eclipseapps.financial.moneytracker.fragments.fragment_movements_list.result_total;
import static eclipseapps.financial.moneytracker.sync.Authenticator.REQUEST_CODE_SIGN_IN;

public class MainActivity extends baseActivity{


    Toolbar toolbar;
    LinearLayout slidingPanel;
    public SlidingUpPanelLayout slidingUpPanelLayout;




    //final static int menuItem_managmentAccount=3;


    Menu _menu;
    String Query="";
    final static String QueryDefault="SELECT * FROM basics ORDER BY tiempo_ DESC";

    List<cuentas> Cuentas;
    private SlidingRootNav slidingRootNav;

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;
    // A content resolver for accessing the provider
    ContentResolver mResolver;
    user usuario;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Base);
        super.onCreate(savedInstanceState);
        Bundle settingsBundle = new Bundle();
        //settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); Para sincronizar ahora
        //settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        // Get the content resolver for your app
        mResolver = getContentResolver();
        Account account=Authenticator.CreateSyncAccount(this);
        String AUTHORITY=Authenticator.Constants.AUTHORITY;
        long syncInterval=3600*3;//Segundos
        long flexTime=60;//500;//Segundos

        ContentResolver.setIsSyncable(account, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        // We can enable inexact timers in our periodic sync (better for batter life)
        SyncRequest request = new SyncRequest.Builder().
                syncPeriodic(syncInterval, flexTime).
                setSyncAdapter(account, AUTHORITY).
                setExtras(settingsBundle).build();
        ContentResolver.requestSync(request);


        setContentView(R.layout.activity_main);

        slidingUpPanelLayout=findViewById(R.id.content_main_slidingup);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        refreshToolbar(savedInstanceState);


        //Time 2
        gastos_resume.loadOnMain(this,R.id.content_main_maincontainer);
        //loadList();
         //MobileAds.initialize(this, "ca-app-pub-7245032634198204~4288416777");

    }
    public void showSlidePanel(){
        slidingRootNav.openMenu(true);
    }







    @Override
    protected void onResume() {
        super.onResume();

        //load new movement about link
      if (getIntent().getAction().matches(Intent.ACTION_SEND)){
            if (getIntent().getType().matches("text/plain")){
                Query="";
                ClipData Data=getIntent().getClipData();
                int s=Data.getItemCount();
                ClipData.Item item=Data.getItemAt(0);
                String data=item.getText().toString();
                Intent I=new Intent(MainActivity.this,Movement.class);
                I.putExtra("LinkTo",data);
                getIntent().setAction(Intent.ACTION_DEFAULT);
                startActivityForResult(I, 1);
            }
        }

        //Log.d("RT.MainAct.onResume",String.valueOf(System.currentTimeMillis()-Runtime));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        _menu=menu;
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(resultCode==RESULT_OK && requestCode==ventas.sellsPreferences.PREMIUM_REQUEST){
            boolean sucess=data.getBooleanExtra(Premium,false);
            if(sucess){
                onUpgrade(data.getBooleanExtra(PremiumOffer,false));
            }
       }else if (resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_SIGN_IN && data != null) {
           Fragment fragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG_FragmentName);
           if(fragment!=null)fragment.onActivityResult(requestCode, resultCode, data);
           //handleSignInResult(data);
       }
    }
    @Override
    public void onBackPressed() {
        baseFragment fragment=getActualFragment();
        if (fragment==null){
            if(slidingRootNav.isMenuOpened()){
                slidingRootNav.closeMenu(true);
            }else{
                gastos_resume.loadOnMain(this,R.id.content_main_maincontainer);
            }
            return;
        }else if(fragment.isVisible() && fragment instanceof baseFragment){
            if(!fragment.onBackPressed()){
                gastos_resume.loadOnMain(this,R.id.content_main_maincontainer);
                return;
            }
        }
        super.onBackPressed();
    }

    public baseFragment getActualFragment(){
        Fragment fragment=getSupportFragmentManager().findFragmentByTag(RFC_manager.TAG_Fragment);
        if(fragment==null)fragment=getSupportFragmentManager().findFragmentByTag(Presupuesto_manager.TAG_FragmentName);
        if(fragment==null)fragment=getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG_FragmentName);
        if(fragment==null)fragment=getSupportFragmentManager().findFragmentByTag(gastos_resume.TAG_FragmentName);
        return (baseFragment) fragment;
    }

    public void handleSignInResult(Intent result, final OnSuccessListener<Void> onSuccess) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleAccount) {
                        AnalyticsApplication.logD("handleSignInResult",googleAccount.getEmail());
                        SyncAdapter syncAdapter=new SyncAdapter(MainActivity.this,true);
                        syncAdapter.SyncDBInDrive(onSuccess);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AnalyticsApplication.sendLogAsError("handleSignInResult","unableToSignIn:"+e.getMessage());
            }
        });
    }


    public Toolbar getToolBar() {
        return toolbar;
    }

    public SlidingRootNav getSlidingRootNav() {
        return slidingRootNav;
    }

    public void refreshToolbar(Bundle savedInstanceState){
        setSupportActionBar(toolbar);
        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withMenuLocked(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(null)
                .withMenuLayout(R.layout.menu_left)
                .inject();
        slidingRootNav.getLayout().findViewById(R.id.menu_left_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingRootNav.closeMenu(true);
                gastos_resume.loadOnMain(MainActivity.this,R.id.content_main_maincontainer);
            }
        });
        slidingRootNav.getLayout().findViewById(R.id.menu_left_movimientos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingRootNav.closeMenu(true);
                ListaMovimientos.loadOnMain(MainActivity.this,R.id.content_main_maincontainer);
            }
        });
        slidingRootNav.getLayout().findViewById(R.id.menu_left_movimientos_filtrar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent I=new Intent(MainActivity.this,filterActivity.class);
                startActivityForResult(I, menuItem_filtrar);

            }
        });
        slidingRootNav.getLayout().findViewById(R.id.menu_left_movimientos_exportar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long numRows= DatabaseUtils.queryNumEntries(db.getDBInstance(),"basics");
                if(numRows<=0){
                    MainActivity.this.showOkDialog("No cuentas con ningun gasto o ingreso para exportar a Excel","ok",true,null);
                    return;
                }
                ReadMovementsTracking("ExportMovments","Intent");
                if(mIsPremium){
                    db.exportToExcel(MainActivity.this,Query,0);//0:No limit
                }else if(!mIsPremium){


                    showOkCancelDialog("La versión gratuita solo permite exportar los primeros 10 movimientos de tu consulta al mismo tiempo", "Obtener Premium", "Exportar", true, new Dialogs.DialogsInterface() {
                        @Override
                        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                            if(sucess){
                                (MainActivity.this).askForPremium("ForExportMovements");
                            }else{
                                db.exportToExcel(MainActivity.this,Query,10);
                            }
                        }
                    });

                    //getRewardedFragment();//Prepara el video
                }

            }


        });

        slidingRootNav.getLayout().findViewById(R.id.menu_left_cuentas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usabilityAppTracking(AnalyticsApplication.Gestures.Click,"AccountsLabel");
                ImageView imagePremiumReward=slidingRootNav.getLayout().findViewById(R.id.imageview_premiumreward);
                imagePremiumReward.setVisibility(View.VISIBLE);
                ventas.UI ui=new ventas.UI();
                ui.animMedalToXY(MainActivity.this,imagePremiumReward);

            }
        });
        slidingRootNav.getLayout().findViewById(R.id.menu_left_categorias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction FT=getSupportFragmentManager().beginTransaction();
                Fragment fragment=getSupportFragmentManager().findFragmentByTag(categoryManager.name);
                if(fragment==null){
                    FT.replace(R.id.content_main_maincontainer,new categoryManager().setDb(db),categoryManager.name);
                }else{
                    FT.replace(R.id.content_main_maincontainer,((categoryManager)fragment).setDb(db),categoryManager.name);
                }
                FT.commit();
                getSupportActionBar().setTitle(R.string.categorias);
                getSupportActionBar().setIcon(R.drawable.baseline_category_white_24);
                //spinner.setVisibility(View.GONE);
                //fab.setVisibility(View.INVISIBLE);
                slidingRootNav.closeMenu(true);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                //slidingUpPanelLayout.set
            }
        });
        slidingRootNav.getLayout().findViewById(R.id.menu_left_presupuestos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction FT=getSupportFragmentManager().beginTransaction();
                Fragment fragment=getSupportFragmentManager().findFragmentByTag(Presupuesto_manager.TAG_FragmentName);
                if(fragment==null){
                    FT.replace(R.id.content_main_maincontainer,new Presupuesto_manager(),Presupuesto_manager.TAG_FragmentName);
                }else{
                    FT.replace(R.id.content_main_maincontainer,fragment,Presupuesto_manager.TAG_FragmentName);
                }
                FT.commit();
                getSupportActionBar().setTitle(R.string.presupuesto);
                getSupportActionBar().setIcon(null);
                //spinner.setVisibility(View.GONE);
                //fab.setVisibility(View.INVISIBLE);
                slidingRootNav.closeMenu(true);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
        //tiempo 6



        if (countryCodeValue.matches("mx")){
            slidingRootNav.getLayout().findViewById(R.id.menu_left_facturas).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction FT=getSupportFragmentManager().beginTransaction();
                    FT.replace(R.id.content_main_maincontainer,new RFC_manager(),"RFCManager");
                    FT.commit();
                    getSupportActionBar().setTitle(R.string.facturas);
                    getSupportActionBar().setIcon(null);
                    //spinner.setVisibility(View.GONE);
                    //fab.setVisibility(View.INVISIBLE);
                    slidingRootNav.closeMenu(true);
                }
            });
        }else{
            ((LinearLayout)slidingRootNav.getLayout().findViewById(R.id.menu_left_facturas).getParent()).setVisibility(View.INVISIBLE);
        }
        //Time 5
        slidingRootNav.getLayout().findViewById(R.id.menu_left_premium).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsPremium){
                    ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setText("¡Ya eres Premium!");
                    ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setEnabled(false);
                }else{
                    askForPremium();
                }
            }
        });
        slidingRootNav.getLayout().findViewById(R.id.menu_left_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_main_maincontainer, new SettingsFragment().setaBar(getToolBar()),SettingsFragment.TAG_FragmentName)
                        .commit();
                //spinner.setVisibility(View.GONE);
                getSupportActionBar().setTitle(SettingsFragment.TAG_FragmentName);
                getSupportActionBar().setIcon(null);
                //fab.setVisibility(View.INVISIBLE);
                slidingRootNav.closeMenu(true);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            }
        });
        if(mIsPremium){
            ((ProgressBar)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_wait)).setVisibility(View.INVISIBLE);
            ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setText("¡Ya eres Premium!");
            ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setEnabled(false);
        }

        //Time4
        final ListView menuleft_listaccount=slidingRootNav.getLayout().findViewById(R.id.menu_left_cuentas_lista);
        Cuentas=  db.getAllAccount();
        menuleft_listaccount.setAdapter(new ArrayAdapter<cuentas>(this,R.layout.menu_left_account_list_element){
            float minimumTextSize=0;
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView==null)
                    convertView=getLayoutInflater().inflate(R.layout.menu_left_account_list_element,null);

                TextView signo= (TextView) convertView.findViewById(R.id.menu_left_account_list_element_signo_dinero);

                TextView account= (TextView) convertView.findViewById(R.id.menu_left_account_list_element_cuenta);
                account.setText(getItem(position).get_cuenta());

                FontFitTextView quantity= (FontFitTextView) convertView.findViewById(R.id.menu_left_account_list_element_cuenta_cantidad);
                if(quantity.getTextSize()==0||quantity.getTextSize()<minimumTextSize){
                    minimumTextSize=quantity.getTextSize();

                }
                String q=String.format("%.2f",getItem(position).get_cantidad());
                if(q.contains("-")){
                    quantity.setText(q.replace("-",""));
                    signo.setText("-$");
                }else{
                    quantity.setText(q);
                    signo.setText(" $");
                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        usabilityAppTracking(AnalyticsApplication.Gestures.Click,"OnAccountLabel");
                        final AccountDetails detailsAccount=new AccountDetails().setCuenta(Cuentas.get(position)).setDb(db);
                        detailsAccount.setOnLifeCycleListener(new FragmentN.LifeCycleObserver() {
                            @Override
                            public void onActivityCreated(@Nullable Bundle savedInstanceState) {

                            }

                            @Override
                            public void onResume() {

                            }
                        });
                        detailsAccount.setAction(new FragmentN.Action() {
                            @Override
                            public Object execute(Intent intent) {
                                if(intent.getAction()!=null && intent.getAction().matches(cuentas.CuentaEliminada)){
                                    gastos_resume.loadOnMain(MainActivity.this,R.id.content_main_maincontainer);
                                    showOkDialog("Cuenta Eliminada","Ok",true,null);
                                }
                                return null;
                            }
                        });
                        detailsAccount.setContainerForReport(getSupportFragmentManager(),R.id.content_main_slidecontainer_hided);
                        FragmentTransaction FT=getSupportFragmentManager().beginTransaction();
                        FT.replace(R.id.content_main_maincontainer,detailsAccount,AccountDetails.name);
                        FT.commit();
                        getSupportActionBar().setTitle("  "+getItem(position).get_cuenta());
                        getSupportActionBar().setIcon(R.drawable.baseline_account_balance_white_24);
                        //spinner.setVisibility(View.GONE);
                        //fab.setVisibility(View.INVISIBLE);
                        slidingRootNav.closeMenu(true);
                       // slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        //list.LoadQuery("SELECT * FROM basics WHERE cuenta_='"+getItem(position).get_cuenta()+"'",null);
                        // list.refreshData();

                    }
                });

                return convertView;
            }


            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public cuentas getItem(int position) {
                return Cuentas.get(position);
            }

            @Override
            public int getCount() {
                return Cuentas.size();
            }

        });

        slidingRootNav.getLayout().addDragStateListener(new DragStateListener() {
            @Override
            public void onDragStart() {

            }

            @Override
            public void onDragEnd(boolean isMenuOpened) {
                if(isMenuOpened){
                    Cuentas=  db.getAllAccount();
                    ((ArrayAdapter)menuleft_listaccount.getAdapter()).clear();
                    ((ArrayAdapter)menuleft_listaccount.getAdapter()).addAll(Cuentas);
                    ((ArrayAdapter)menuleft_listaccount.getAdapter()).notifyDataSetChanged();
                }
            }
        });
    }





    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
        super.setTitle(title);
    }


    float rated=0;
    public void showRatingDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .threshold(4)
                .title(getString(R.string.que_te_ha_parecido))
                .positiveButtonText("Tal véz después")
                .formTitle("Tu opinion es muy valiosa")
                .formHint("En que podemos mejorar?")
                .ratingBarColor(R.color.colorAccent)
                .onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
                    @Override
                    public void onRatingSelected(float rating, boolean thresholdCleared) {
                        rated=rating;
                        sp.edit().putBoolean("hasRated",true).apply();
                    }
                })
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        usabilityAppTracking(AnalyticsApplication.Usability.UserExperience,"RateApp",String.valueOf((int)rated)+":"+feedback);
                    }
                }).build();

        ratingDialog.show();
        usabilityAppTracking(AnalyticsApplication.Usability.UserExperience,"RateAppIntent",String.valueOf(trackedActivity.sp.getLong("Sessions",0)/7));
    }




    @Override
    boolean isPremium(final boolean isPremium) {
        boolean result=super.isPremium(isPremium);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isPremium){
                    ((ImageView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_icon)).setVisibility(View.VISIBLE);
                    ((ProgressBar)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_wait)).setVisibility(View.INVISIBLE);
                    ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setText("¡Ya eres Premium!");
                    ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setEnabled(false);
                    FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                    //list.refreshdataAndCleanToolBar
                    Fragment fragment=getSupportFragmentManager().findFragmentByTag(AccountDetails.name);
                    if(fragment!=null && fragment.isVisible()){
                        ft.detach(fragment);
                        ft.attach(fragment);
                        ft.commitAllowingStateLoss();
                    }
                }else{
                    ((ImageView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_icon)).setVisibility(View.VISIBLE);
                    ((ProgressBar)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_wait)).setVisibility(View.INVISIBLE);
                    ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setText("Versión Premium");
                    ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setEnabled(true);
                }
            }
        });

        return result;

    }

    @Override
    public void checkForPremiumNow() {
        super.checkForPremiumNow();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_icon)).setVisibility(View.INVISIBLE);
                ((ProgressBar)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium_wait)).setVisibility(View.VISIBLE);
                ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setText("Verificando versión...");
                ((TextView)slidingRootNav.getLayout().findViewById(R.id.menu_left_premium)).setEnabled(false);
            }
        });

    }

    public View getSlideHided(){
        return findViewById(R.id.content_main_slidecontainer_hided);
    }
    public void hideSlideUpPanel(){
        findViewById(R.id.content_main_maincontainer).setPadding(0,0,0,0);
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }
    public  void refreshReportForQuery(final String Query,Intent intent) {

        LinearLayout slidingPanel = findViewById(R.id.content_main_slidecontainer_showed);
        panelResults results = new panelResults(this);
        double total=intent.getDoubleExtra(result_total, 0);
        double ingreso=intent.getDoubleExtra(result_ingreso, 0);
        double egreso=intent.getDoubleExtra(result_egreso, 0);
        results.setTotal(total);
        results.setIngreso(ingreso);
        results.setEgreso(egreso);
        slidingPanel.removeAllViews();
        slidingPanel.addView(results);
        slidingPanel.invalidate();

        if (ingreso==0 && egreso==0){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            slidingUpPanelLayout.setTouchEnabled(false);
        }else{
            if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                reportsFragment report = reportsFragment.attachReportWithQuery(getSupportFragmentManager(), R.id.content_main_slidecontainer_hided
                        , db, Query).setReportPlotType(reportsFragment.ReportsPlot.LineChart).setReportDataType(reportsFragment.ReportsData.EgresoAcumulado);
                report.withExtraInfo(report.getEgressReportList());
            }
            slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                boolean isclosed=true;
                @Override
                public void onPanelSlide(View panel, float slideOffset) {

                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                    if(isclosed && newState== SlidingUpPanelLayout.PanelState.EXPANDED){
                        reportsFragment report=reportsFragment.attachReportWithQuery(getSupportFragmentManager(),R.id.content_main_slidecontainer_hided
                                ,db,Query).setReportPlotType(reportsFragment.ReportsPlot.LineChart).setReportDataType(reportsFragment.ReportsData.EgresoAcumulado);
                        report.withExtraInfo(report.getEgressReportList());
                        isclosed=false;
                    }else if(newState==SlidingUpPanelLayout.PanelState.COLLAPSED){
                        isclosed=true;
                    }
                }
            });
            slidingUpPanelLayout.setTouchEnabled(true);
        }
    }
}
