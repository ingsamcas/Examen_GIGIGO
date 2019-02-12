package eclipseapps.mobility.parkeame.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.library.backendless.UIUser.RegisterBackendlessUserUI;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.activities.MainActivity;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.user;
import eclipseapps.mobility.parkeame.sync.SyncService;

/**
 * Created by usuario on 26/08/17.
 */
public class LogIn extends Fragment {


    loginflow listener;
    DBParkeame DB;
    Button aceptar;
    EditText email;
    EditText password;
    TextView newAccount;
    TextView retrivePass;
    private boolean flag=true;
    private String preregistredemail;
    private String preregistredPassword;
    private user usuario;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout viewRoot= (RelativeLayout) inflater.inflate(R.layout.d_credentials_requestlogin,container,false);
        DB= DBParkeame.getInstance(getActivity());
        email= (EditText) viewRoot.findViewById(R.id.d_email_input);
        email.setText(preregistredemail);
        password=(EditText) viewRoot.findViewById(R.id.d_password_input);
        password.setText(preregistredPassword);
        aceptar= (Button) viewRoot.findViewById(R.id.d_aceptar_button);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag){
                    aceptar.setEnabled(false);
                    if (!isEmailValid(email.getEditableText().toString())){
                        Dialogs noValidEmailDialog=new Dialogs().OkDialog(getActivity(),R.string.noEmailValid,R.string.Ok,null);
                        noValidEmailDialog.show(getChildFragmentManager(),"noValid");
                        aceptar.setEnabled(true);
                        flag=true;
                        return;
                    }else if (password.getEditableText().toString().matches("")){
                        Dialogs noValidPassDialog=new Dialogs().OkDialog(getActivity(),R.string.noPasswordValid,R.string.Ok,null);
                        noValidPassDialog.show(getChildFragmentManager(),"noValid");
                        aceptar.setEnabled(true);
                        flag=true;
                        return;
                    }
                    usuario=new user(DB);

                    boolean loginResult=usuario.login(email.getEditableText().toString(),password.getEditableText().toString(),true);
                    if (loginResult){
                        //Bienvenido Usuario.Comienza a Disfrutar de Parkeo y despreocupate del parkimetro
                        //Comienza la sincronizacion de la informacion y espera a que el servicio mande resultados
                        //al listener que se encuentra en onResume
                        syncAccount(usuario);
                        
                    }else{
                        Dialogs noValidEmailDialog;
                        String error=usuario.getError();
                        if (error!=null && error.matches("400")){
                            if (!eclipseapps.libraries.library.general.functions.general.isOnline()){
                                noValidEmailDialog=new Dialogs().OkDialog(getActivity(),R.string.sinInternet,R.string.Ok,null);
                            }else{
                                noValidEmailDialog=new Dialogs().OkDialog(getActivity(),R.string.credenciales_email_no_confirmado,R.string.Ok,null);
                            }
                        }else if(error!=null && error.matches("3087")) {
                            noValidEmailDialog=new Dialogs().OkDialog(getActivity(),R.string.credenciales_email_no_confirmado,R.string.Ok,null);
                        }else{
                            noValidEmailDialog=new Dialogs().OkDialog(getActivity(),R.string.credenciales_incorrectas,R.string.Ok,null);
                        }
                        noValidEmailDialog.show(getChildFragmentManager(),"noValid");
                        aceptar.setEnabled(true);
                        flag=true;
                        return;
                    }
                }
                flag=false;

            }
        });
        newAccount= (TextView) viewRoot.findViewById(R.id.d_crearCuenta);
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNewAccountLsitener();
            }
        });

        retrivePass= (TextView) viewRoot.findViewById(R.id.d_recuperarPass);
        retrivePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRetrivePasswordLsitener();
            }
        });
        return viewRoot;
    }
    private void syncAccount(user Usuario){
        Intent intent=new Intent(getActivity(), SyncService.class);
        intent.setAction(SyncService.ACTION_SYNC);
        intent.putExtra("SyncAutos",true);
        intent.putExtra("SyncSaldo",true);
        String obId=Usuario.getObjectId();
        intent.putExtra("user",obId);
        intent.putExtra("payId",Usuario.getpayId());
        getActivity().startService(intent);
    }
    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void setListener(loginflow listener) {
        this.listener = listener;
    }

    public void setCredentialsIntent(String preregistredemail, String preregistredPassword) {
        this.preregistredemail=preregistredemail;
        this.preregistredPassword=preregistredPassword;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncService.ACTION_FIN);

        getActivity()
                .registerReceiver(mMessageReceiver,
                        intentFilter);
    }
    // Handling the received Intents for the "my-integer" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            if(intent.getAction().matches(SyncService.ACTION_FIN)){
                listener.onSucessLogin(usuario);//Termina la sincronizacion y continua con las siguientes pantallas
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        getActivity()
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
    public interface loginflow{
        void onNewAccountLsitener();
        void onRetrivePasswordLsitener();
        void onSucessLogin(user usuario);

    }
}
