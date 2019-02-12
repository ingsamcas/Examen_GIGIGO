package eclipseapps.mobility.parkeame.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;


import java.util.Calendar;
import java.util.TimerTask;

import eclipseapps.android.FragmentN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.libraries.library.general.functions.Timers;
import eclipseapps.libraries.library.general.functions.general;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.DBautos;

import static eclipseapps.mobility.parkeame.fragments.fragment_AutoManager_General.updateDefaultCar;

/**
 * Created by usuario on 29/08/17.
 */
public class createOrUpdateCar extends FragmentN {
    fragment_AutoManager_General.ManageAutos inter;
    DBParkeame DB;
    Autos auto=new Autos();
    Button ok;
    public createOrUpdateCar setNewQrCar(String qr, fragment_AutoManager_General.ManageAutos inter){
        auto.setQr_(qr);
        this.inter = inter;
        return this;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.g_fragment_nuevoauto,container,false);
        ok= (Button) ll.findViewById(R.id.g_button_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auto.getTipo_().matches("")){
                    showError("Selecciona el tipo de auto");
                    return;
                }else if (auto.getMarca_()==null || auto.getMarca_().matches("")){
                    showError("Selecciona la marca del auto");
                    return;
                }else if (auto.getModelo_()==null || auto.getModelo_().matches("")){
                    showError("Selecciona el modelo del auto");
                    return;
                }else if (auto.getFechamodelo_()==0){
                    showError("Selecciona el año del auto");
                    return;
                }else if (auto.getPlacas_()==null || auto.getPlacas_().matches("")){
                    showError("Ingresa las placas de tu vehiculo");
                    return;
                }

                DB=DBParkeame.getInstance(getActivity());

                if (auto.getObjectId()!=null && !auto.getObjectId().matches("")){
                    String placa=DB.GetStringScalar("SELECT * FROM Autos WHERE placas_='"+auto.getPlacas_()+"' AND objectId<>'"+auto.getObjectId()+"'",null,"placas_");
                    if(placa!=null && placa.matches(auto.getPlacas_())){
                        showError("Estas placas ya estan registradas con otro auto");
                    }else{
                        guardarenNube(auto);
                    }

                }else{

                    String[] placas=DB.GetStringColumn("SELECT * FROM Autos",null,"placa_");
                    if(general.ExistItem(placas,auto.getPlacas_())){
                        showError("Estas placas ya estan registradas");
                        return;
                    }
                    auto.setStatus_(Autos.Tipos.ACTIVE);
                    guardarenNube(auto);
                }

            }
        });
        if (auto.getObjectId()!=null && !auto.getObjectId().matches("")){
            ok.setText("Actualizar");
        }else{
            ok.setText("Guardar");
        }



        final String[] Tipos=getResources().getStringArray(R.array.Tipo);

        final DBautos dbAutos=DBautos.getInstance(getActivity());
        final String[] Marcas= general.concat(dbAutos.GetStringColumn("SELECT * FROM marcas",null,"Nombre"),new String[]{"Otro"});


        Spinner tipos= (Spinner) ll.findViewById(R.id.g_tipo);
        tipos.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.i_i_itemdrawerlist, Tipos));
        tipos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                auto.setTipo_(Tipos[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (auto.getObjectId()!=null && !auto.getObjectId().matches("")){
            tipos.setSelection(general.IndexAt(Tipos,auto.getTipo_()));
        }

        Spinner marcas= (Spinner) ll.findViewById(R.id.g_marca);
        marcas.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.i_i_itemdrawerlist, Marcas));
        if (auto.getObjectId()!=null && !auto.getObjectId().matches("")){
            int index=general.IndexAt(Marcas,auto.getMarca_());
            marcas.setSelection(index);
        }

        final AutoCompleteTextView modelos= (AutoCompleteTextView) ll.findViewById(R.id.g_modelo);
        marcas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                auto.setMarca_(Marcas[i]);
                String[] Modelos;
                if(Marcas[i].matches("Otro")){
                    Modelos=new String[]{""};
                }else{
                    Modelos=dbAutos.GetStringColumn("SELECT * FROM marcas INNER JOIN modelos WHERE modelos.id_marca=marcas.id AND marcas.Nombre='"+Marcas[i]+"'",null,"Nombre");
                }


                modelos.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, Modelos));

                modelos.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        auto.setModelo_(editable.toString());
                    }
                });

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (auto.getObjectId()!=null && !auto.getObjectId().matches("")){
            modelos.setText(auto.getModelo_());
        }

        String[] años=new String[150];
        for (int i=0;i<años.length;i++){
            años[i]=String.valueOf(2018-i);
        }
        final String[] añosFinal=años;
        final Spinner año=(Spinner)ll.findViewById(R.id.g_fechamodelo);
        año.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.i_i_itemdrawerlist, años));
        año.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                auto.setFechamodelo_(Integer.valueOf(añosFinal[i]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(auto.getFechamodelo_()==0)año.setSelection(0);
        else {
            año.setSelection(general.IndexAt(añosFinal,auto.getFechamodelo_()));
        }

        EditText placas=(EditText)ll.findViewById(R.id.g_placas);
        placas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                auto.setPlacas_(charSequence.toString().replace(" ",""));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (auto.getObjectId()!=null && !auto.getObjectId().matches("")){
            placas.setText(auto.getPlacas_());
        }

        return ll;
    }
    Dialogs dialog;
    public void showError(String mensaje){
        if (dialog==null){
            dialog=new Dialogs();
        }
        dialog.OkDialog(mensaje,"OK",null).show(getChildFragmentManager(),"MensajeError");
    }
    Timers timer;
    private void guardarenNube(final Autos auto) {
        wait(auto.getObjectId()!=null?"Actualizando...":"Guardando...",false);
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis()+4000);
       timer=new Timers(cal.getTime(),4000, new TimerTask() {
            @Override
            public void run() {
                if (timer !=null)timer.Stop();
                Dialogs dialog=new Dialogs().OkDialog("Ups. Algo va mal. Verifica tu conexión a internet y vuelve a intentarlo", "OK", null);
                dialog.show(getChildFragmentManager(), "PedidoEnviado_dialog");

            }
        });
        Backendless.Persistence.save(auto, new AsyncCallback<Autos>() {
            public void handleResponse(final Autos coche) {
                if (timer!=null)timer.Stop();
                // new Contact instance has been saved
                if(auto.getObjectId()!=null && !auto.getObjectId().matches("")){//Solo se actuliza
                    auto.setUpdated(coche.getUpdated());
                    auto.savein(DB);
                    inter.onCarEdited(auto);
                    dismissWait();
                }else{//Se crea
                    auto.setObjectId(coche.getObjectId());
                    auto.setCreated(coche.getCreated());
                    auto.setUpdated(coche.getUpdated());
                    auto.savein(DB);
                    updateDefaultCar(auto.getPlacas_(), DB, new AsyncCallback<Integer>() {
                        @Override
                        public void handleResponse(Integer response) {
                            dismissWait();
                            inter.onCarCreated(auto);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e( "MYAPP", "Server reported an error - " + fault );
                        }
                    });
                }






            }

            public void handleFault(BackendlessFault fault) {
                // an error has occurred, the error code can be retrieved with fault.
                if (timer!=null)timer.Stop();
                dismissWait();
                showError("Ups. Algo va mal. Verifica tu conexión a internet y vuelve a intentarlo");
            }
        });

    }

    public createOrUpdateCar updateCar(Autos auto, fragment_AutoManager_General.ManageAutoslisteners listener) {

        this.auto=auto;
        this.inter=listener;
        return this;
    }
}
