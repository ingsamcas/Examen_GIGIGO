package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 08/09/17.
 */

public class Parkimetros extends ObjectBackendless {
    private String status_;
    private String estacionamiento_;
    private String Pais_;
    private String Estado_;
    private String Ciudad_;
    private String Poblacion_;//Delegacion o municipio
    private String Colonia_;
    private String Cp_;
    private double Latx_;
    private double Longx_;
    private double Laty_;
    private double Longy_;

    public String getEstacionamiento_() {
        return estacionamiento_;
    }

    public void setEstacionamiento_(String estacionamiento_) {
        this.estacionamiento_ = estacionamiento_;
    }

    public String getStatus_() {
        return status_;
    }

    public void setStatus_(String status_) {
        this.status_ = status_;
    }


    public String getPais_() {
        return Pais_;
    }

    public void setPais_(String pais_) {
        this.Pais_ = pais_;
    }

    public String getEstado_() {
        return Estado_;
    }

    public void setEstado_(String estado_) {
        this.Estado_ = estado_;
    }

    public String getCiudad_() {
        return Ciudad_;
    }

    public void setCiudad_(String ciudad_) {
        this.Ciudad_ = ciudad_;
    }

    public String getPoblacion_() {
        return Poblacion_;
    }

    public void setPoblacion_(String poblacion_) {
        this.Poblacion_ = poblacion_;
    }

    public String getColonia_() {
        return Colonia_;
    }

    public void setColonia_(String colonia_) {
        this.Colonia_ = colonia_;
    }

    public String getCp_() {
        return Cp_;
    }

    public void setCp_(String cp_) {
        this.Cp_ = cp_;
    }

    public double getLatx_() {
        return Latx_;
    }

    public void setLatx_(double latx_) {
        this.Latx_ = latx_;
    }

    public double getLongx_() {
        return Longx_;
    }

    public void setLongx_(double longx_) {
        this.Longx_ = longx_;
    }

    public double getLaty_() {
        return Laty_;
    }

    public void setLaty_(double laty_) {
        this.Laty_ = laty_;
    }

    public double getLongy_() {
        return Longy_;
    }

    public void setLongy_(double longy_) {
        this.Longy_ = longy_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("Pais_",Types.VARCHAR);
        columnas.put( "Estado_",Types.VARCHAR);
        columnas.put("Ciudad_",Types.VARCHAR);
        columnas.put("Poblacion_",Types.VARCHAR);//Delegacion o municipio
        columnas.put("Colonia_",Types.VARCHAR);
        columnas.put("Cp_",Types.VARCHAR);
        columnas.put("Latx_",Types.REAL);
        columnas.put("Longx_",Types.REAL);
        columnas.put("Laty_",Types.REAL);
        columnas.put("Longy_",Types.REAL);
        //columnas.put("Idparkimetro_",Types.VARCHAR); Se utilizara objectId como Id parkimetro
        columnas.put("estacionamiento_",Types.VARCHAR);
        columnas.put("status_",Types.VARCHAR);
        return columnas;
    }
    public static class STATUS{
        public final static String Ocupado ="Ocupado";
        public final static String Libre="Libre";
    }
}
