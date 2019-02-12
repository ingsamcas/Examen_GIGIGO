package eclipseapps.mobility.parkeame.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import eclipseapps.android.FragmentN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.RandomString;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.activities.MainActivity;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;

/**
 * Created by usuario on 04/06/18.
 */

public class fragment_auto_editordelete extends FragmentN {

    private RelativeLayout containerChild;
    private RelativeLayout containerCarInfo;
    private TextView modelo;
    private TextView marca;
    private TextView placas;
    private TextView year;
    private ImageView icono;
    private ImageView borraCuenta;
    private Button editButton;

    public  Autos auto;
    private fragment_AutoManager_General.ManageAutoslisteners listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,final @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout RL= (RelativeLayout) inflater.inflate(R.layout.f_a_editordelete_auto,container,false);
       containerChild=RL.findViewById(R.id.f_a_child_fragment_container);
        containerCarInfo=RL.findViewById(R.id.f_a_editordelete);
        if (auto!=null && !auto.getQr_().matches("")){


            modelo= (TextView) RL.findViewById(R.id.f_a_car_modelo);
            marca= (TextView) RL.findViewById(R.id.f_a_car_marca);
            placas= (TextView) RL.findViewById(R.id.f_a_car_placas);
            year=(TextView) RL.findViewById(R.id.f_a_car_year);

            icono=(ImageView)RL.findViewById(R.id.f_a_icono_car);

            final String[] tipos=getResources().getStringArray(R.array.Tipo);

            if (auto.getTipo_().matches(tipos[0])){
                icono.setImageResource(R.drawable.ic_directions_car_white_48dp);
            }else if (auto.getTipo_().matches(tipos[1])){
                icono.setImageResource(R.drawable.ic_local_shipping_white_48dp);
            }else if (auto.getTipo_().matches(tipos[2])){
                icono.setImageResource(R.drawable.ic_airport_shuttle_white_48dp);
            }else if (auto.getTipo_().matches(tipos[3])){
                icono.setImageResource(R.drawable.ic_motorcycle_white_48dp);
            }

            year.setText(String.valueOf(auto.getFechamodelo_()));
            modelo.setText(auto.getModelo_());
            marca.setText(auto.getMarca_());
            placas.setText(auto.getPlacas_());


            borraCuenta=RL.findViewById(R.id.f_a_car_delete);
            borraCuenta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Aqui se necesita hacer un check para que no lo pueda eliminar miesntras se encuentre parqueado

                    final Dialogs dialogs=new Dialogs();
                    dialogs.YesNoDialog("Deseas eliminar este auto?","Si","No",null).addinterface(new Dialogs.DialogsInterface() {
                        @Override
                        public void DialogFinish(final String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                            dialogs.dismiss();
                            if (sucess){
                                fragment_auto_editordelete.this.wait("Borrando auto...",false);
                                Backendless.Data.of(Autos.class).remove(auto, new AsyncCallback<Long>() {
                                    @Override
                                    public void handleResponse(Long response) {
                                        DBParkeame db=DBParkeame.getInstance(getActivity());
                                        db.getDBInstance().execSQL("DELETE FROM Autos WHERE placas_='"+auto.getPlacas_()+"'");
                                        dismissWait();
                                        getActivity().onBackPressed();
                                        listener.onCarDeleted(auto);
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        dismissWait();
                                        Log.d("BackendlessFault",fault.getDetail());

                                    }
                                });
                            }
                        }
                    }).show(getChildFragmentManager(),auto.getQr_());
                }
            });
            editButton =RL.findViewById(R.id.f_a_car_edit);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.bar.setTitle("Editar automovil");
                    containerChild.setVisibility(View.VISIBLE);
                    containerCarInfo.setVisibility(View.GONE);

                    android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
                    Fragment frag=fragmentManager.findFragmentByTag("ActualFragment");
                    if(frag==null){

                        fragmentManager.beginTransaction()
                                .replace(containerChild.getId(), new createOrUpdateCar().updateCar(auto, listener),"ActualFragment").commit();
                    }
                }
            });

        }
        return RL;

    }
    public fragment_auto_editordelete setAuto(Autos auto) {
        this.auto = auto;
        return this;
    }


    public fragment_auto_editordelete setListener(fragment_AutoManager_General.ManageAutoslisteners listener) {
        this.listener = listener;
        return this;
    }

    public boolean onBackPressed(){
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        Fragment frag=fragmentManager.findFragmentByTag("ActualFragment");
        if(frag!=null && frag instanceof createOrUpdateCar && containerChild.isShown()){
            MainActivity.bar.setTitle("Información del vehiculo");
            containerChild.setVisibility(View.GONE);
            containerCarInfo.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }
}
