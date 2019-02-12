package eclipseapps.mobility.parkeame.cloud;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by usuario on 13/06/18.
 */

public class Estacionamientos {//Un estacionamiento es un lugar donde se encuentran reunidos uno o mas parkimetros
                                //Estos solo existen en un nivel local para propositos de la aplicacion pero no en la nube
                                //ni en la base de datos local, el campo estacionamiento_ de la tabla Parkimetros es de donde se saca
                                //que un parkimetro pertenece a uno u otro estacionamiento

    private LatLng ubicacion;    //Ubicacion en latitud y longitud del estacionamiento
    private int ParkimetrosDisponibles; //
    private String AliasEstacionamiento;

    public LatLng getUbicacion() {
        return ubicacion;
    }

    public Estacionamientos setUbicacion(LatLng ubicacion) {
        this.ubicacion = ubicacion;
        return this;
    }

    public int getParkimetrosDisponibles() {
        return ParkimetrosDisponibles;
    }

    public Estacionamientos setParkimetrosDisponibles(int parkimetrosDisponibles) {
        ParkimetrosDisponibles = parkimetrosDisponibles;
        return this;
    }

    public String getAliasEstacionamiento() {
        return AliasEstacionamiento;
    }

    public Estacionamientos setAliasEstacionamiento(String aliasEstacionamiento) {
        AliasEstacionamiento = aliasEstacionamiento;
        return this;
    }
}
