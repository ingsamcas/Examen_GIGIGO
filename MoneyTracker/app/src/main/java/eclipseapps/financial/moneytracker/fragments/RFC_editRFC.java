package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;

import java.util.TimerTask;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.trackedActivity;
import eclipseapps.financial.moneytracker.adapters.OnEditRFCListeners;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.rfc;
import eclipseapps.libraries.library.general.functions.general;

/**
 * Created by usuario on 15/10/17.
 */

public class RFC_editRFC extends baseFragment {
    DBSmartWallet DB;
    private OnEditRFCListeners listener;
    private rfc rfc=new rfc();
    private EditText rfcHolder;
    private EditText nombreHolder;
    private EditText calleHolder;
    private EditText numextHolder;
    private EditText numIntHolder;
    private EditText coloniaHolder;
    private EditText municipioHolder;
    private EditText cpHolder;
    private EditText ciudadHolder;
    private EditText estadoHolder;
    private EditText emailHolder;

    private ImageView borraCuenta;
    private Button selectButton;
    private RelativeLayout progress;
    private TextView progressText;
    boolean checked;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("rfc",rfc.getRfc_());
        outState.putString("nombre",rfc.getNombre_());
        outState.putString("calle",rfc.getCalle_());
        outState.putString("numext",rfc.getNumeroext_());
        outState.putString("numInt",rfc.getNumeroInt_());
        outState.putString("colonia",rfc.getColonia_());
        outState.putString("municipio",rfc.getMunicipio_());
        outState.putString("cp",rfc.getCp_());
        outState.putString("ciudad",rfc.getCiudad_());
        outState.putString("estado",rfc.getEstado_());
        outState.putString("email",rfc.getEmail_());
        outState.putString("objectId",rfc.getObjectId());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB=DBSmartWallet.getInstance(getActivity());
        if (savedInstanceState!=null){
            rfc.setRfc_(savedInstanceState.getString("rfc",""));
            rfc.setNombre_(savedInstanceState.getString("nombre",""));
            rfc.setCalle_(savedInstanceState.getString("calle",""));
            rfc.setNumeroext_(savedInstanceState.getString("numext",""));
            rfc.setNumeroInt_(savedInstanceState.getString("numInt",""));
            rfc.setColonia_(savedInstanceState.getString("colonia",""));
            rfc.setMunicipio_(savedInstanceState.getString("municipio",""));
            rfc.setCp_(savedInstanceState.getString("cp",""));
            rfc.setCiudad_(savedInstanceState.getString("ciudad",""));
            rfc.setEstado_(savedInstanceState.getString("estado",""));
            rfc.setEmail_(savedInstanceState.getString("email",""));
            rfc.setObjectId(savedInstanceState.getString("objectId",""));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ScrollView layout= (ScrollView) inflater.inflate(R.layout.fragment_factura_rfc_editrfc,container,false);
        rfcHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_rfcHolder);
        nombreHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_nombreHolder);
        calleHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_calleHolder);
        numextHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_numextHolder);
        numIntHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_numIntHolder);
        coloniaHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_coloniaHolder);
        municipioHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_municipioHolder);
        cpHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_cpHolder);
        ciudadHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_ciudadHolder);
        estadoHolder= (EditText) layout.findViewById(R.id.rfc_editrfc_estadoHolder);
        emailHolder=(EditText)layout.findViewById(R.id.rfc_editrfc_email);
        progress= (RelativeLayout) layout.findViewById(R.id.rfc_editrfc_progress);
        progressText= (TextView) layout.findViewById(R.id.rfc_editrfc_progress_text);

        TextView TerminosyCondiciones=(TextView)layout.findViewById(R.id.rfc_terminos_condiciones);
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "Acepto el Aviso de privacidad");
       // spanTxt.append("Aviso de privacidad");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = "https://api.backendless.com/C1D72711-B7EB-98DD-FFC7-23418D485000/0756ACFC-9311-6E20-FF8E-428B4BCC5A00/files/web/Privacidad.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, spanTxt.length() - "Aviso de privacidad".length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), spanTxt.length() - "Aviso de privacidad".length(), spanTxt.length() - "Aviso de privacidad".length(), 0);

        spanTxt.append(" y los Terminos y condiciones");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = "https://api.backendless.com/C1D72711-B7EB-98DD-FFC7-23418D485000/0756ACFC-9311-6E20-FF8E-428B4BCC5A00/files/web/TerminosYCondiciones.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, spanTxt.length() - "Terminos y condiciones".length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), spanTxt.length() - "Terminos y condiciones".length(), spanTxt.length() - "Terminos y condiciones".length(), 0);

        spanTxt.append(" de este servicio.");

        TerminosyCondiciones.setMovementMethod(LinkMovementMethod.getInstance());
        TerminosyCondiciones.setText(spanTxt, TextView.BufferType.SPANNABLE);

        CheckBox aceptacionCondiciones=layout.findViewById(R.id.rfc_terminos_condiciones_aceptacion);
        aceptacionCondiciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checked=b;
                SharedPreferences.Editor edit=((trackedActivity)getActivity()).sp.edit();
                edit.putBoolean("TerminosyCondiciones",checked);
                edit.commit();
            }
        });

        checked=((trackedActivity)getActivity()).sp.getBoolean("TerminosyCondiciones",false);
        aceptacionCondiciones.setChecked(checked);

        rfcHolder.setText(rfc.getRfc_());
        nombreHolder.setText(rfc.getNombre_());
        calleHolder.setText(rfc.getCalle_());
        numextHolder.setText(rfc.getNumeroext_());
        numIntHolder.setText(rfc.getNumeroInt_());
        coloniaHolder.setText(rfc.getColonia_());
        municipioHolder.setText(rfc.getMunicipio_());
        cpHolder.setText(rfc.getCp_());
        ciudadHolder.setText(rfc.getCiudad_());
        estadoHolder.setText(rfc.getEstado_());
        emailHolder.setText(rfc.getEmail_());
        /*
        borraCuenta=layout.findViewById(R.id.c_borracuenta);
        borraCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialogs dialogs=new Dialogs();
                dialogs.YesNoDialog("Deseas borrar este RFC?","Si","No",null).addinterface(new Dialogs.DialogsInterface() {
                    @Override
                    public void DialogFinish(final String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                        dialogs.dismiss();
                        if (sucess && onClickListener!=null){
                            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                            ProgressBar progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
                            builder.setView(progressBar);
                            builder.setTitle("Borrando RFC...");
                            final AlertDialog AD=builder.create();
                            AD.show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                     if (onClickListener.deleteRFCListener(rfc.getRfc_())){
                                        AD.dismiss();
                                        getActivity().onBackPressed();
                                     }
                                }
                            }, 1000);
                        }
                    }
                }).show(getChildFragmentManager(),rfc.getObjectId());
            }
        });
        */
        selectButton= (Button) layout.findViewById(R.id.rfc_editrfc_guardarcambios);
        if (rfc.getObjectId()!=null && !rfc.getObjectId().matches("")){
            if(selectButton.getText().toString().matches("Guardar") || selectButton.getText().toString().matches("")){
                lockForm();//Desactivar vistas
                selectButton.setText("Editar RFC");
                selectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        unlockForm();//Activar vistas
                        selectButton.setText("Guardar cambios");
                        selectButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Guardar();
                            }
                        });
                    }
                });
            }else if(selectButton.getText().toString().matches("Guardar cambios")){
                selectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Guardar();
                    }
                });
            }

        }else{
            unlockForm();//Activar vistas
            selectButton.setText("Guardar");
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   Guardar();
                }
            });
        }
        return layout;
    }
    public void Guardar(){
        if(!general.isOnline()){
            showOkDialog("Revisa tu conexion a internet e intenta de nuevo","Entiendo",true,null);
            return;
        }
        try{
            Backendless.initApp(getActivity(),"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
        }catch (BackendlessException e){
            if(!general.isOnline()){
                Toast.makeText(getActivity(),"Sin acceso a internet",Toast.LENGTH_LONG);
            }
        }
        if(!ReviewForm())return;
        lockForm();//Desactivar vistas

        rfc.setRfc_(rfcHolder.getText().toString());
        rfc.setNombre_(nombreHolder.getText().toString());
        rfc.setCalle_(calleHolder.getText().toString());
        rfc.setNumeroext_(numextHolder.getText().toString());
        rfc.setNumeroInt_(numIntHolder.getText().toString());
        rfc.setColonia_(coloniaHolder.getText().toString());
        rfc.setMunicipio_(municipioHolder.getText().toString());
        rfc.setCiudad_(ciudadHolder.getText().toString());
        rfc.setEstado_(estadoHolder.getText().toString());
        rfc.setCp_(cpHolder.getText().toString());
        rfc.setEmail_(emailHolder.getText().toString());
        selectButton.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);//Activar vista de espera
        progress.post(new TimerTask() {
            @Override
            public void run() {//Guardar cambios en nube
                Backendless.Data.save(rfc, new AsyncCallback<eclipseapps.financial.moneytracker.cloud.rfc>() {
                    @Override
                    public void handleResponse(rfc response) {
                        rfc.setCreated(response.getCreated());
                        rfc.setUpdated(response.getUpdated());
                        rfc.setObjectId(response.getObjectId());
                        rfc.savein(DB);//Guardar cambio en DB local
                        listener.onNewRFCAdded();//Avisa al onClickListener
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        if(fault.getCode().matches("Internal client exception")){
                            showOkDialog("Verifica tu conexion a internet y vuelve a intentarlo",getResources().getString(R.string.accept),true,null);
                        }else{
                            showOkDialog(fault.getMessage(),getResources().getString(R.string.accept),true,null);
                        }
                    }
                });


            }
        });
    }

    private boolean ReviewForm() {
        //str.matches("\\d{2}-\\d{2}")
        if (!rfcHolder.getText().toString().matches("[A-Z]{4}[0-9]{6}[A-Z0-9]{3}")){
            if(!rfcHolder.getText().toString().matches("[A-Z]{3}[0-9]{6}[A-Z0-9]{3}")){
                showOkDialog("Debes colocar un RFC valido","ok",true,null);
                return false;
            }
        }
        if (nombreHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar una razón social valida","ok",true,null);
            return false;
        }
        if (calleHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar la calle del domicilio fiscal","ok",true,null);
            return false;
        }
        if (numextHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar el numero exterior del domicilio fiscal","ok",true,null);
            return false;
        }
        if (coloniaHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar la colonia del domicilio fiscal","ok",true,null);
            return false;
        }
        if (municipioHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar el municipio o delegación del domicilio fiscal","ok",true,null);
            return false;
        }
        String cp=cpHolder.getText().toString();
        if (!cpHolder.getText().toString().matches("[0-9]{5}")){
            showOkDialog("Debes colocar un codigo postal valido","ok",true,null);
            return false;
        }
        if (ciudadHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar la ciudad o población del domicilio fiscal","ok",true,null);
            return false;
        }
        if (estadoHolder.getText().toString().matches("")){
            showOkDialog("Debes colocar el estado del domicilio fiscal","ok",true,null);
            return false;
        }

        if (!emailHolder.getText().toString().matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}")){
            showOkDialog("Debes colocar un email de contacto valido para este RFC","ok",true,null);
            return false;
        }
        String[] rfcRegistred=DB.GetStringColumn("SELECT * FROM rfc",null,"rfc_");
        if(rfcRegistred!=null && rfcRegistred.length>0){
            if(general.ExistItem(rfcRegistred,rfcHolder.getText().toString())){
                showOkDialog("Este RFC ya se encuentra registrado","ok",true,null);
                return false;
            }
        }
        if(!checked){
            showOkDialog("Debes aceptar los terminos y condiciones del servicio","ok",true,null);
            return false;
        }
        return true;
    }

    public OnEditRFCListeners getListener() {
        return listener;
    }

    public RFC_editRFC setListener(OnEditRFCListeners listener) {
        this.listener = listener;
        return this;
    }

    public rfc getRFC() {
        return rfc;
    }

    public RFC_editRFC setRFC(rfc rfc) {
        this.rfc = rfc;
        return this;
    }

    public void lockForm(){
        rfcHolder.setEnabled(false);
        nombreHolder.setEnabled(false);
        calleHolder.setEnabled(false);
        numextHolder.setEnabled(false);
        numIntHolder.setEnabled(false);
        coloniaHolder.setEnabled(false);
        municipioHolder.setEnabled(false);
        cpHolder.setEnabled(false);
        ciudadHolder.setEnabled(false);
        estadoHolder.setEnabled(false);
        emailHolder.setEnabled(false);
    }

    public void unlockForm(){
        rfcHolder.setEnabled(true);
        nombreHolder.setEnabled(true);
        calleHolder.setEnabled(true);
        numextHolder.setEnabled(true);
        numIntHolder.setEnabled(true);
        coloniaHolder.setEnabled(true);
        municipioHolder.setEnabled(true);
        cpHolder.setEnabled(true);
        ciudadHolder.setEnabled(true);
        estadoHolder.setEnabled(true);
        emailHolder.setEnabled(true);
    }
}
