package eclipseapps.mobility.parkeame.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.LoadRelationsQueryBuilder;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import eclipseapps.android.FragmentN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.libraries.library.general.functions.RandomString;
import eclipseapps.libraries.library.general.functions.Timers;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.activities.Credentials;
import eclipseapps.mobility.parkeame.backendservice.DemoService;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.Parkimetros;
import eclipseapps.mobility.parkeame.cloud.Precios;
import eclipseapps.mobility.parkeame.cloud.Estacionamientos;
import eclipseapps.mobility.parkeame.cloud.parkeos;
import eclipseapps.mobility.parkeame.services.Cronometro;

import static android.content.Context.MODE_PRIVATE;
import static eclipseapps.mobility.parkeame.R.id.a_map_container;
import static eclipseapps.mobility.parkeame.customviews.customActionBar.auto;


/**
 * Created by usuario on 28/08/17.
 */
public class fragment_main extends FragmentN implements PricesTabs.onSelectedTabListener{

    int timeSelected;
    int minRestantes=0;
    onOrderlistener listener;
    DBParkeame DB;
    private TextView HorasRestantes;
    private TextView MinRestantes;
    FrameLayout PricesContainer;

    FrameLayout GeneralView;
   // private Button ok;
    //private String StateService=Cronometro.ACTION_FIN;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       DB=DBParkeame.getInstance(getActivity());
        GeneralView= (FrameLayout) inflater.inflate(R.layout.a_fragmentmain,container,false);
        HorasRestantes=(TextView)GeneralView.findViewById(R.id.a_horas_restantes);
        MinRestantes=(TextView)GeneralView.findViewById(R.id.a_minutos_restantes);
        PricesContainer=GeneralView.findViewById(R.id.a_button_ok_container);
        return GeneralView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentTransaction FT=getChildFragmentManager().beginTransaction();
        FT.add(a_map_container,new fragment_map().setListener(new fragment_map.mapResult() {

            @Override
            public void onPlaceSelected(Address adress) {searchPrices(adress);}

            @Override
            public List<Estacionamientos> getEstacionamientos() {//Regresa el estacionamiento y su numero de lugares disponibles
                List<Estacionamientos> result=new ArrayList<Estacionamientos>();
                DB=DBParkeame.getInstance(getActivity());
                Cursor cur=DB.getDBInstance().rawQuery("SELECT count(status_) AS \"LugaresDisponibles\",Latx_,Longx_,estacionamiento_ FROM Parkimetros WHERE status_='Disponible' GROUP BY estacionamiento_ ",null);
                if(cur.moveToFirst()){
                    do{
                        float latx=cur.getFloat(cur.getColumnIndex("Latx_"));
                        float longx=cur.getFloat(cur.getColumnIndex("Longx_"));
                        LatLng ll=new LatLng(latx,longx);

                        Estacionamientos estacionamiento=new Estacionamientos();
                        estacionamiento.setUbicacion(ll);
                        estacionamiento.setParkimetrosDisponibles(cur.getInt(cur.getColumnIndex("LugaresDisponibles")));
                        estacionamiento.setAliasEstacionamiento(cur.getString(cur.getColumnIndex("estacionamiento_")));
                        result.add(estacionamiento);
                    }while (cur.moveToNext());
                }
                return result;
            }

            @Override
            public void onEstacionamientoSelected(Estacionamientos estacionamiento) {
                showPrices(estacionamiento.getAliasEstacionamiento());
            }



            @Override
            public List<Parkimetros> getParkings(LatLng ll) {
                return null;
            }
        }),"mapa");
        FT.commit();

    }
    public void showPrices(String estacionamiento){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_WEEK);//1:Domingo-->Sabado:7

        DB=DBParkeame.getInstance(getActivity());
        List<Precios> precios=DB.select("SELECT Precios FROM Precios INNER JOIN Parkimetros WHERE Precios.estacionamiento_=Parkimetros.estacionamiento_ AND Precios.estacionamiento_='"+estacionamiento+"' AND Parkimetros.status_='Disponible' AND diaSemana_ IN ("+day+") AND TiempoInferior_<="+calendar.get(Calendar.HOUR_OF_DAY)+" AND TiempoSuperior_>="+calendar.get(Calendar.HOUR_OF_DAY),Precios.class);
        if(precios.size()==0){//Si no hay precios especiales ese dia a esa hora para ese parkimetro, toma los precios genericos
            precios=DB.select("SELECT Precios FROM Precios INNER JOIN Parkimetros WHERE Precios.estacionamiento_=Parkimetros.estacionamiento_ AND Precios.estacionamiento_='"+estacionamiento+"' AND Parkimetros.status_='Disponible' AND diaSemana_ IN (0)",Precios.class);
        }
        PricesContainer.setVisibility(View.VISIBLE);
        PricesTabs tabs=new PricesTabs().setPrecios(precios, this);
        FragmentTransaction FT=getChildFragmentManager().beginTransaction();
        FT.add(R.id.a_price_container,tabs ,"Prices");
        FT.commit();
    }



    public fragment_main setAuto(Autos Auto, onOrderlistener listener) {
        auto = Auto;
        this.listener = listener;
        return this;
    }

    @Override
    public void onTabselected(List<Precios> precios,int position) {
        timeSelected=precios.get(position).getTiempoEstacionamiento_();
    }

    @Override
    public void onTabClick(float price, int timeInMin) {

        if (auto==null){
            new Dialogs().OkDialog("Necesitas seleccionar un auto",getString(R.string.Ok),null).show(getChildFragmentManager(),"SelectCar");
            return;
        }
        wait("Parkeando...",false);
        //Revisa el saldo
        SharedPreferences prefs = getActivity().getSharedPreferences("eclipseapps.mobility.parkeame", MODE_PRIVATE);

        if (prefs.getFloat("balance",0)>=price){//Tiene saldo entonces hace transferencia             //ingsamcas@gmail.com
            DemoService.getInstance().transferAsync(price, Credentials.ActualUser.getpayId(), "akbeppi40ujerv3zjqou", "Parkear " + String.valueOf(timeInMin) + "min por $" + String.valueOf(price), new RandomString().nextString(), new AsyncCallback<String>() {
                @Override
                public void handleResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        String transferId=json.getString("id");
                        parking(transferId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    dismissWait();
                }
            });
        }else{ //Sin saldo suficiente
           //Si tiene activado la recarga automatica entonces se intenta hacer el cargo
            //Sino le pide al usuario que recargue el saldo
            dismissWait();
            return;
        }


    }
    public void parking(String Id){
        SharedPreferences prefs = getActivity().getSharedPreferences("eclipseapps.mobility.parkeame", getActivity().MODE_PRIVATE);
        if (Cronometro.stateService.matches(Cronometro.ACTION_PROGRESO)){
            parkeos parkeo=new parkeos();
            parkeo.setQr_(auto.getQr_());
            parkeo.setStatus_(prefs.getString("OnParkimetroId","Error"));
            parkeo.setTiempo_(timeSelected);
            parkeo.setIdParkimetro_(prefs.getString("OnProgressId","Error"));//Se guarda el siguiente parkeo con el ID que ya se habia Generado
            guardarenNube(parkeo);
        }else if(Cronometro.stateService.matches(Cronometro.ACTION_FIN)){
            parkeos parkeo=new parkeos();
            parkeo.setQr_(auto.getQr_());
            parkeo.setStatus_("Parkimetro");
            parkeo.setTiempo_(timeSelected);
            parkeo.setIdParkimetro_(Id);
            guardarenNube(parkeo);
        }
    }

    public interface onOrderlistener{
        void onParkeoScheduled(parkeos parkeo);
    }
    public void setTime(int min){
        minRestantes=min;
        int horas;
        for(horas=0;min>=60;horas++){
            min=min-60;
        }
        HorasRestantes.setText(String.valueOf(horas)+ " hrs");
        MinRestantes.setText(" "+String.valueOf(min)+ " min");
    }
    private void guardarenNube(parkeos parkeo) {
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis()+10000);
        final Timers timer=new Timers(cal.getTime(),10000, new TimerTask() {
            @Override
            public void run() {
                dismissWait();
                new Dialogs().OkDialog("Ups. Algo va mal. Verifica tu conexión a internet y vuelve a intentarlo", "OK", null).show(getChildFragmentManager(), "PedidoEnviado_dialog");
                //if (timer!=null)timer.Stop();
            }
        });
        Backendless.Persistence.save(parkeo, new AsyncCallback<parkeos>() {
            public void handleResponse(parkeos parkeo) {
                // new Contact instance has been saved
                if (timer!=null)timer.Stop();
                parkeo.savein(DB);
                SharedPreferences prefs = getActivity().getSharedPreferences("eclipseapps.mobility.parkeame", getActivity().MODE_PRIVATE);
                if(Cronometro.stateService.matches(Cronometro.ACTION_FIN)){
                    prefs.edit().putString("OnProgressId", parkeo.getIdParkimetro_()).commit();
                    prefs.edit().putString("OnParkimetroId", parkeo.getStatus_()).commit();
                    prefs.edit().putInt("UpdateTime",0).commit();
                    Intent msgIntent = new Intent(getActivity(), Cronometro.class);
                    msgIntent.putExtra(Cronometro.Tiempo,timeSelected);
                    getActivity().startService(msgIntent);
                }else if (Cronometro.stateService.matches(Cronometro.ACTION_PROGRESO)){
                    prefs.edit().putInt("UpdateTime", timeSelected).commit();//El servicio ya esta en uso, tan solo se envia actualizacion de tiempo
                }
                dismissWait();
                listener.onParkeoScheduled(parkeo);
            }

            public void handleFault(BackendlessFault fault) {
                // an error has occurred, the error code can be retrieved with fault.
                dismissWait();
                if (timer!=null)timer.Stop();
                new Dialogs().OkDialog("Ups. Algo va mal. Verifica tu conexión a internet y vuelve a intentarlo", "OK", null).show(getChildFragmentManager(), "PedidoEnviado_dialog");

            }
        });
    }
    void searchPrices(Address adress){
        double ptLat=adress.getLatitude();
        double ptLng=adress.getLongitude();
        Parkimetros parkimetro=new Parkimetros();
       /* parkimetro.setPais_("MEX");
        parkimetro.setEstado_("MEX");
        parkimetro.setCiudad_("");
        parkimetro.setPoblacion_("Tlalnepantla");
        parkimetro.setColonia_("Hab Los Reyes Ixtacala");
        parkimetro.setCp_("54090");*/
        parkimetro.setLatx_(ptLat); //latx=Latitud del punto Sur
        parkimetro.setLongx_(ptLng);//longx=Longitud del punto Este
        parkimetro.setLaty_(ptLat);//Lat=y=Latitud del punto Norte
        parkimetro.setLongy_(ptLng);//longx=Longitud del punto Oeste
        String Where=parkimetro.retriveWhereClause(false,"Pais_=","Estado_=","Ciudad_=","Poblacion_=","Colonia_=","Cp_=","Latx_<=","Laty_>=","Longx_<=","Longy_>=");//solo si la longitud es negativa

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(Where);

        Backendless.Data.of("Parkimetros").find( queryBuilder,
                new AsyncCallback<List<Map>>(){
                    @Override
                    public void handleResponse( List<Map> foundParkimetros )
                    {
                        List <Map> resultado=foundParkimetros;
                        if (resultado.size()>0){
                            Map parkimetro=resultado.get(0);
                            String parentObjectId = (String) parkimetro.get("objectId");// removed for brevity
                            LoadRelationsQueryBuilder<Map<String, Object>> loadRelationsQueryBuilder;
                            loadRelationsQueryBuilder = LoadRelationsQueryBuilder.ofMap();
                            loadRelationsQueryBuilder.setRelationName("Precio_");

                                    Backendless.Data.of("Parkimetros").loadRelations( parentObjectId,
                                            loadRelationsQueryBuilder,
                                            new AsyncCallback<List<Map<String, Object>>>()
                                            {
                                                @Override
                                                public void handleResponse( List<Map<String, Object>> precios )
                                                {
                                                    boolean EsTiempolibre=true;
                                                    long HoraActual = System.currentTimeMillis();
                                                    long tiempoInferior;
                                                    long tiempoSuperior;

                                                    do{
                                                        List<Precios> PreciosActuales=new ArrayList<Precios>();
                                                        for( Map precio: precios ){
                                                            tiempoInferior= (long)((double) precio.get("TiempoInferior_"));
                                                            tiempoSuperior= (long)((double) precio.get("TiempoSuperior_"));
                                                            if (HoraActual>=tiempoInferior && HoraActual<=tiempoSuperior){//Busca el precio en base a la hora actual en la que se encuentra.Debe encontrar varias filas que solo varian en el tiempo de estacionamiento y el precio
                                                                EsTiempolibre=false;
                                                                Precios p=new Precios();
                                                                p.setEstacionamiento_((String) precio.get("Id_"));
                                                                p.setPrecio_((float)((double) precio.get("Precio_")));
                                                                p.setTiempoEstacionamiento_((Integer) precio.get("TiempoEstacionamiento_"));
                                                                PreciosActuales.add(p);
                                                            }
                                                        }
                                                        if (EsTiempolibre){//Si es false significa que el usuario llego a estacionarse en el tiempo libre del lugar. Se comienza a cobrar a partir de la primera hora del parkimetro activo
                                                            Precios p=new Precios();
                                                            long HoraDeInicio=HoraActual;
                                                            for( Map precio: precios ){
                                                                tiempoInferior= (long)((double)precio.get("TiempoInferior_"));
                                                                if (tiempoInferior<HoraDeInicio){//Busca el precio en base a la hora actual en la que se encuentra.Debe encontrar varias filas que solo varian en el tiempo de estacionamiento y el precio
                                                                    HoraDeInicio=tiempoInferior;
                                                                }
                                                            }
                                                            HoraActual=HoraDeInicio;

                                                        }else{//en esta punto ya cuenta con una tabla de precios en base al tiempo que se estacionara ligada a la hora en la que pidio el servicio
                                                            if(PreciosActuales.size()>0){
                                                                PricesTabs tabs;
                                                                Fragment frag=getChildFragmentManager().findFragmentByTag("Prices");
                                                                if (frag!=null){
                                                                    tabs= (PricesTabs) frag;
                                                                }else{
                                                                    tabs=new PricesTabs();
                                                                }
                                                                tabs.setPrecios(PreciosActuales,fragment_main.this);
                                                                tabs.updatePrices();
                                                                //Desaparece el mensaje de "Solicitando precios"
                                                            }
                                                        }
                                                    }while (EsTiempolibre);

                                                }

                                                @Override
                                                public void handleFault( BackendlessFault fault )
                                                {
                                                    Log.e( "MYAPP", "server reported an error - " + fault.getMessage() );
                                                }
                                            } );
                        }else{
                            new Dialogs().OkDialog("Parkeame no puede ayudarte a estacionarte en este lugar. Seguimos trabajando para ampliar nuestra cobertura",getString(R.string.ok),null).show(getChildFragmentManager(),"NoParkear");
                        }
                    }
                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        Toast.makeText(getActivity(),fault.getMessage(),Toast.LENGTH_LONG);
                        // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                });


    }
}


