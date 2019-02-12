package eclipseapps.mobility.trackergps.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import eclipseapps.android.FragmentN;
import eclipseapps.mobility.trackergps.GPS103ABTracker;
import eclipseapps.mobility.trackergps.R;
import eclipseapps.mobility.trackergps.services.SMSreceiver;

/**
 * Created by usuario on 03/04/18.
 */

public class edit_gps extends FragmentN {
    Button conectar;
    EditText cellNumber;
    GPS103ABTracker gps;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        gps=new GPS103ABTracker();
        RelativeLayout View= (RelativeLayout) inflater.inflate(R.layout.fragment_edit_gps,container,false);
        conectar=View.findViewById(R.id.fragment_edit_gps_button_conectar);
        cellNumber=View.findViewById(R.id.fragment_edit_gps_edittext_cellnumber);
        conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!"Iniciado".matches("Iniciado")){
                    gps.Init().addListener(SMSreceiver.Init,new ResultReceiver(new Handler()){
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if(resultCode==SMSreceiver.Init){
                                if(resultData.getString("sms","").matches("begin ok")){//Se inicializo correctamente
                                    //Cambiar status en la tabla configuracion a Iniciado
                                    authNumber();
                                }else{//Ocurrio un error
                                    //mint
                                }
                                SMSreceiver.getInstace().listeners.remove(SMSreceiver.Init);
                            }
                        }
                    });
                }else{
                    authNumber();
                }
            }
        });
        conectar.setEnabled(!(cellNumber.getText()==null || cellNumber.getText().toString().matches("")));
        return View;
    }
    public void authNumber(){
        gps.setAuth(cellNumber.getEditableText().toString()).addListener(SMSreceiver.Auth,new ResultReceiver(new Handler()){
            @Override
            public void send(int resultCode, Bundle resultData) {
                if(resultCode==SMSreceiver.Auth){
                    if(resultData.getString("sms","").matches("admin OK")){//Se añadio el numero como numero autorizado/hasta 5 diposnibles
                        //Agregar en la tabla los numeros de numeros autorizados
                        showOkDialog("Se ha agregado el "+cellNumber.getEditableText().toString()+" a tu lista de numeros asociados para este GPS","Aceptar",true,null);
                    }else{//Ocurrio un error
                        //mint
                    }
                    SMSreceiver.getInstace().listeners.remove(SMSreceiver.Init);
                }
            }
        });
    }
}
