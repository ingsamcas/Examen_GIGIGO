package eclipseapps.mobility.trackergps.activities;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;

import java.util.ArrayList;

import eclipseapps.android.ActivityN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.general;
import eclipseapps.library.backendless.UIUser.LogInBackendlessUser;
import eclipseapps.library.backendless.interfaces.onAccessResult;
import eclipseapps.mobility.trackergps.MainActivity;
import eclipseapps.mobility.trackergps.R;
import eclipseapps.mobility.trackergps.database.User;
import eclipseapps.mobility.trackergps.database.trackerDB;
import eclipseapps.mobility.trackergps.fragments.edit_user_account;


//import eclipseapps.medical.codigoinfarto.intro.intro_activity;
//import eclipseapps.medical.codigoinfarto.mails.confirmationMAIL;

public class login_activity extends ActivityN {

    public static int LoginUser=0;
    trackerDB db=trackerDB.getInstance(this);
	public static boolean loged=false;
	LogInBackendlessUser loginBackend;
	User user=new User(db);
	@Override
	protected void onCreate(Bundle savedbundlestate) {
		// TODO Auto-generated method stub
		super.onCreate(savedbundlestate);
        checkForConnection();
	}

    public void checkForConnection(){
        if(general.isOnline()){
            MainActivity.InitCloud(this);
            initScreen();
        }else{
            showOkDialog("Parece que no tienes conexión a internet. Revisa tu conexión", "Entiendo", false, new Dialogs.DialogsInterface() {
                @Override
                public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                    checkForConnection();
                }
            });
        }
    }
    public void initScreen(){
        setContentView(R.layout.login_activity);
        FragmentTransaction F=getSupportFragmentManager().beginTransaction();
        if(user.findUserinLocalIfExist() && user.isUserLogged()){
            Toast.makeText(this,user.getEmail()+":"+user.getPassword(),Toast.LENGTH_LONG).show();
            F.replace(R.id.login_activity_container, new edit_user_account().setEmail(user.getEmail()), "user_account");
            F.commit();
        }else{
            int color;
            if(Build.VERSION.SDK_INT>23){
                color=getResources().getColor(android.R.color.holo_blue_light,null);
            }else{
                color=getResources().getColor(android.R.color.holo_blue_light);
            }
            loginBackend=new LogInBackendlessUser().setBackendlessUser(user, new onAccessResult() {
                @Override
                public void onResult(boolean sucess) {
                    // TODO Auto-generated method stub
                    Log.d("Login_Activity","OnResult");
                    loged=true;
                    SharedPreferences preferences=getSharedPreferences("Insulin",MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("Useremail",user.getEmail());
                    editor.commit();
                    if (sucess&&loginBackend.isnewUser()){
                        loginBackend.clearnewUser();
                        // sync request. HTML messahe to multiple recipients
                        ArrayList<String> recipients = new ArrayList<String>();
                        recipients.add(user.getEmail());
                        Backendless.Messaging.sendHTMLEmail("Usuario Registardo en Glikho", getMailBody(), recipients, new AsyncCallback<MessageStatus>() {
                            @Override
                            public void handleResponse(MessageStatus response) {
                                showOkDialog("Hemos enviado un correo de bienvenida a " + user.getEmail(), "Entiendo", true, new Dialogs.DialogsInterface() {
                                    @Override
                                    public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                                        loginBackend.setcredentials(user.getEmail(), user.getPassword());
                                        login_activity.this.finish();
                                    }
                                });
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(login_activity.this,fault.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });




                    }else{
                        login_activity.this.setResult(RESULT_OK);
                        login_activity.this.finish();
                    }


                }
            }).setColor(color);//.addCustomRegisterForm(new fragment_registro_doctor(), 0);

            F.replace(R.id.login_activity_container, loginBackend, "Login");
            F.commit();
        }
    }

    private String getMailBody(){
        String mailBody = "<p>Hola,<br /><br />Te agradecemos por crear una cuenta en GPSTracker." +
                "Tu usuario y clave son:</p><p>Usuario: {USUARIO_APP}</p><p>Clave: {CLAVE_APP}</p>"+
                "<p>Esta versión de la aplicación cuenta con las siguientes características:<br /><br />" +
                "-Puedes revisar en todo momento la localización precisa de tu GPS. " +
                "a través de  nuestro personal altamente capacitado para brindarte el mejor servicio y el mas alto confort.</p>" +
                "<p>-Podras seguir en todo momento el recorrido de nuestro personal para que tengas la confianza que llegara a tiempo contigo." +
                "</p><p>-Podras solicitarlo todos los dias de de 10 am a 5 pm.</p>" +
                "<p> Te damos una coordial bienvenida a este esfuerzo por mejorar la calidad de vida de las personas con Diabetes en México." +
                " Esperamos que te sientas orgulloso de pertencer a esta comudiad y nos encantaria saber que en un futuro" +
                " has quedado absolutamente satidfecho con nuestro servicio y nos recomiendas. Te invitamos a que nos comentes que " +
                "te ha parecido la aplicación y como podemos mejorarla.</p><p>Sinceramente,<br />GLIKHO</p>"+
                "<p>Powered by Eclipse Apps;</p>";

        mailBody=mailBody.replace("{USUARIO_APP}", user.getEmail());
        mailBody=mailBody.replace("{CLAVE_APP}", user.getPassword());
        return mailBody;
    }
}
