package eclipseapps.mobility.parkeame.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;
import java.util.Map;

import eclipseapps.library.backendless.UIUser.RecoveryBackendlessUserUI;
import eclipseapps.library.backendless.UIUser.RegisterBackendlessUserUI;
import eclipseapps.library.backendless.interfaces.onAccessResult;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.Parkimetros;
import eclipseapps.mobility.parkeame.cloud.user;
import eclipseapps.mobility.parkeame.customviews.NameRequest;
import eclipseapps.mobility.parkeame.fragments.LogIn;
import eclipseapps.mobility.parkeame.sync.SyncService;

/**
 * Created by usuario on 26/08/17.
 */
public class Credentials extends AppCompatActivity {
    public final static String Log_out="LogOut";
    public static user ActualUser;
    DBParkeame DB;
    RelativeLayout container;
    private String preregistredemail;
    private String preregistredPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getAction()!=null && getIntent().getAction().matches(Log_out)){
            logOut();
        }else{
            setContentView(R.layout.c_credentials_request);
            DB=DBParkeame.getInstance(this);
            container= (RelativeLayout) findViewById(R.id.c_credentials_container);

            if (savedInstanceState==null){
                createLoginFrag();
            }
        }



    }
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncService.ACTION_FIN);
        registerReceiver(mMessageReceiver,
                        intentFilter);
    }
    // Handling the received Intents for the "my-integer" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            if(intent.getAction().matches(SyncService.ACTION_FIN)){
                //listener.onSucessLogin(usuario);//Termina la sincronizacion y continua con las siguientes pantallas
            }
        }
    };

    private void logOut(){
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                DB=DBParkeame.getInstance(Credentials.this);
                DB.getDBInstance().execSQL("DELETE FROM "+user.USERS_NFGBackendless);
                DB.getDBInstance().execSQL("DELETE FROM Autos");
                DB.getDBInstance().execSQL("DELETE FROM parkeos");
                DB.getDBInstance().execSQL("DELETE FROM Tokens");
                SharedPreferences prefs = getSharedPreferences("eclipseapps.mobility.parkeame", MODE_PRIVATE);
                prefs.edit().putFloat("balance", 0).commit();
               //La tabla precios se queda
                Credentials.ActualUser=null;
                setContentView(R.layout.c_credentials_request);
                container= (RelativeLayout) findViewById(R.id.c_credentials_container);
                createLoginFrag();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(Credentials.this,fault.getMessage(),Toast.LENGTH_LONG);
                Credentials.this.finish();
            }
        });
    }

    public void createLoginFrag(){
        LogIn login;
        Fragment fragment=getSupportFragmentManager().findFragmentByTag("login");
        if (fragment!=null){
            login= (LogIn) fragment;
        }else{
            login=new LogIn();
        }
        login.setCredentialsIntent(preregistredemail,preregistredPassword);
        login.setListener(new LogIn.loginflow() {
            @Override
            public void onNewAccountLsitener() {
                final RegisterBackendlessUserUI registerUser;
                Fragment fragment=getSupportFragmentManager().findFragmentByTag("RegisterFrag");
                if (fragment!=null){
                    registerUser= (RegisterBackendlessUserUI) fragment;
                }else{
                    RegisterBackendlessUserUI.addCustomRegisterForm(new NameRequest(),0);
                    registerUser=new RegisterBackendlessUserUI().BasicsWith(false,false).setUser(new user(DB));
                }
                registerUser.confirmationmail(true).addinterface(new onAccessResult() {
                    @Override
                    public void onResult(boolean sucess) {
                        if (sucess)//Registro con exito
                        {
                            preregistredemail=registerUser.User.getEmail();
                            preregistredPassword=registerUser.User.getPassword();

                            onBackPressed();
                        }
                    }
                });
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                ft.replace(container.getId(), registerUser, "RegisterFrag");
                // Start the animated transition.
                ft.commit();
            }

            @Override
            public void onRetrivePasswordLsitener() {
                RecoveryBackendlessUserUI recoverUser;
                Fragment fragment=getSupportFragmentManager().findFragmentByTag("RecoverFrag");
                if (fragment!=null){
                    recoverUser= (RecoveryBackendlessUserUI) fragment;
                }else{
                    recoverUser=new RecoveryBackendlessUserUI().setUser(new user(DB)).setInter(new RecoveryBackendlessUserUI.onRecoveryListener() {
                        @Override
                        public void onEmailSent(String intentUserId) {
                            preregistredemail=intentUserId;
                            onBackPressed();
                        }
                    });
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                ft.replace(container.getId(), recoverUser, "RecoverFrag");
                // Start the animated transition.
                ft.commit();
            }

            @Override
            public void onSucessLogin(user usuario) {
                usuario.deleteAllUsersLocally();
                usuario.saveUser();
                Toast.makeText(Credentials.this,"User Loged",Toast.LENGTH_LONG);
               onSuccessIdentified(usuario);
            }
        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // ft.setCustomAnimations(R.anim.slide_out_left, R.anim.slide_in_right);
        ft.replace(container.getId(), login, "login");
        ft.commit();
    }

    @Override
    public void onBackPressed() {

        Fragment fragment=getSupportFragmentManager().findFragmentByTag("RegisterFrag");
        if (fragment!=null && fragment.isVisible()){
            createLoginFrag();
        }else{
            fragment=getSupportFragmentManager().findFragmentByTag("RecoverFrag");
            if (fragment!=null && fragment.isVisible()){
                createLoginFrag();
            }else{
                super.onBackPressed();
            }
        }
    }
    private void onSuccessIdentified(user User){
        Credentials.ActualUser=User;
        Credentials.this.setResult(MainActivity.RESULT_OK);
        Credentials.this.finish();
    }
}
