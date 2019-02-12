package eclipseapps.mobility.parkeame.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backendless.Backendless;
import com.splunk.mint.Mint;

import java.util.List;
import java.util.Random;

import eclipseapps.android.ActivityN;
import eclipseapps.libraries.library.general.functions.general;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.Defaults;
import eclipseapps.mobility.parkeame.cloud.Precios;
import eclipseapps.mobility.parkeame.cloud.parkeos;
import eclipseapps.mobility.parkeame.cloud.user;
import eclipseapps.mobility.parkeame.customviews.customActionBar;
import eclipseapps.mobility.parkeame.dialogs.NoCorrectTime;
import eclipseapps.mobility.parkeame.fragments.fragment_AutoManager_General;
import eclipseapps.mobility.parkeame.fragments.fragment_main;
import eclipseapps.mobility.parkeame.fragments.fragment_pago;
import eclipseapps.mobility.parkeame.services.Cronometro;
import eclipseapps.mobility.parkeame.sync.SyncService;
import eclipseapps.payments.UI.Fragments.AccountManager;


public class MainActivity extends ActivityN {

    DBParkeame DB;
    public static int RequestCredentials=10001;
    private CharSequence mTitle;
    private boolean isOnline=true;
    public static customActionBar bar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!eclipseapps.libraries.library.general.functions.general.isOnline()){
            isOnline=false;
            return;
        }else{
            isOnline=true;
            Mint.initAndStartSession(this.getApplication(), "2fc67e27");

            loadBroadcastCronoReciver();
            int isAutoTime=android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0);
            if (isAutoTime==0){
                Intent intent=new Intent(this, NoCorrectTime.class);
                startActivity(intent);
            }
            Backendless.initApp( getApplicationContext(),
                    Defaults.APPLICATION_ID,
                    Defaults.API_KEY );
            DB=DBParkeame.getInstance(this);
            user usuario=new user(DB);
            if(usuario.findUserinLocalIfExist()){
                boolean result=usuario.login();
                if (result&&usuario.isUserLogged()){
                    Credentials.ActualUser=usuario;
                    Intent intent=new Intent(this, SyncService.class);
                    intent.setAction(SyncService.ACTION_SYNC);
                    startService(intent);
                    loadview(savedInstanceState);
                }else{
                    if (!general.isOnline()){
                        isOnline=false;
                        return;
                    }else{
                        Intent intent=new Intent(this, Credentials.class);//lo manda a la pagina de credenciales
                        startActivityForResult(intent,RequestCredentials);
                    }
                }
            }else{
                Intent intent=new Intent(this, Credentials.class);
                startActivityForResult(intent,RequestCredentials);
            }
        }

    }
    public void loadview(Bundle savedInstanceState){
        bar=new customActionBar(this).setListener(new customActionBar.onCarsCallback() {
                                                                      @Override
                                                                      public void NoCarAvailable() {
                                                                          selectItem(1);
                                                                      }
                                                                  });
        setContentView(R.layout.i_navigationdrawerlayout);
        android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.FILL_PARENT, android.support.v7.app.ActionBar.LayoutParams.FILL_PARENT);
        getSupportActionBar().setCustomView(bar,layout);
        getSupportActionBar().setDisplayShowCustomEnabled(true);



        optionsDrawer = getResources().getStringArray(R.array.optionsDrawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.i_drawer_layout);
        mDrawerview = (RelativeLayout) findViewById(R.id.i_drawer_view);
        mDrawerList = (ListView) findViewById(R.id.i_list_left);
        CerrarSesion=(TextView)findViewById(R.id.i_cerrar_sesion);
        CerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, Credentials.class);//lo manda a la pagina de credenciales
                intent.setAction(Credentials.Log_out);
                startActivityForResult(intent,RequestCredentials);
            }
        });
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.i_i_itemdrawerlist, optionsDrawer));
        // Set the list's click editAccountListener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
       if(savedInstanceState==null || !savedInstanceState.getBoolean("online")){
            selectItem(0);
        }else{
            selectItem(-1);//Retrive LastFragment
        }
        setUpDrawerToggle();
        // Defer code dependent on restoration of previous instance state.
        // NB: required for the drawer indicator to show up!
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }



    void loadBroadcastCronoReciver(){//Este se registra aqui por que va a modificar el UI en cada tic.tac sin modificar la base de datos
        IntentFilter filter = new IntentFilter();
        filter.addAction(Cronometro.ACTION_PROGRESO);
        filter.addAction(Cronometro.ACTION_FIN);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Fragment fragment=MainActivity.this.getSupportFragmentManager().findFragmentByTag("ActualFragment");
                if (fragment!=null && fragment instanceof fragment_main && fragment.isVisible()){
                    if(intent.getAction().equals(Cronometro.ACTION_PROGRESO)) {
                        int prog = intent.getIntExtra("progreso", 0);
                        ((fragment_main) fragment).setTime(prog);
                    }else if(intent.getAction().equals(Cronometro.ACTION_FIN)) {
                        ((fragment_main) fragment).setTime(0);
                    }
                }
            }
        }, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnline){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error de conexión");
            builder.setMessage("No se ha podido conectar con el servidor. Comprueba tu conexion a internet y vuelve a intentarlo");
            builder.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.recreate();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("online",isOnline);
        super.onSaveInstanceState(outState);
    }

    String[] optionsDrawer;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;
    RelativeLayout mDrawerview;
    TextView CerrarSesion;



    private class DrawerItemClickListener implements ListView.OnItemClickListener,ListView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            return false;
        }

    }

    /** Swaps fragments in the main content view */
    public void selectItem(int position) {
        Fragment fragment=new Fragment();
        // Create a new fragment and specify the planet to show based on position
        switch (position){
            case -1:
                fragment=getSupportFragmentManager().findFragmentByTag("ActualFragment");
                break;
            case 0:
                setTitle("Parkear");
                if (fragment_AutoManager_General.actualAuto==null){
                    List<Autos> auto=DB.select("SELECT * FROM Autos WHERE status_='"+Autos.Tipos.DEFAULT+"'",Autos.class);
                    if (auto!=null && auto.size()>0){
                        fragment_AutoManager_General.actualAuto=auto.get(0);
                    }
                }
                 fragment= new fragment_main().setAuto(fragment_AutoManager_General.actualAuto, new fragment_main.onOrderlistener() {


                     @Override                           //este parkeo ya esta registrado en la nube
                     public void onParkeoScheduled(parkeos parkeo) {

                     }
                 });
                break;
            case 1:
                bar.clear().setTitle("Autos").setActionBarColor(android.R.color.holo_blue_dark);
                //setTitle("Autos");
                fragment=new fragment_AutoManager_General();
                break;
            case 2:
                setTitle("Pagos");
                fragment=new fragment_pago();
                break;

        }



        // Insert the fragment by replacing any existing fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.i_mainscreen, fragment,"ActualFragment")
                .commitAllowingStateLoss();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerview);
        getSupportActionBar().setTitle(mTitle);
    }
    String random(int randomLength) {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString().replace("'","");
    }
    private void setUpDrawerToggle(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout objectId */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                getSupportActionBar().setTitle(mTitle);
                //Este metodo es llamado despues del metodo de actividad setTitle
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                getSupportActionBar().setTitle(Credentials.ActualUser.getNombre());
            }
        };


        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        //getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode==RESULT_OK && requestCode==RequestCredentials){
            loadview(null);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onBackPressed() {
        Fragment fragment=getSupportFragmentManager().findFragmentByTag("ActualFragment");
        if (fragment!=null && fragment instanceof fragment_AutoManager_General){
            if (!((fragment_AutoManager_General)fragment).onBackPressed()){
                selectItem(0);
            }
        }else if (fragment!=null && fragment instanceof AccountManager){
            if (!((AccountManager)fragment).onBackPressed()){
                selectItem(0);
            }
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
