package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 14/01/18.
 */

public class rfc extends ObjectBackendless {
    protected String rfc_;
    protected String nombre_;
    protected String calle_;
    protected String numeroext_;
    protected String numeroInt_;
    protected String colonia_;
    protected String municipio_;//Delegacion
    protected String cp_;
    protected String ciudad_;
    protected String estado_;
    protected String email_;//email asociado
    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues cv=new ContentValues();
        cv.put("rfc_",Types.VARCHAR);
        cv.put("nombre_",Types.VARCHAR);
        cv.put("calle_",Types.VARCHAR);
        cv.put("numeroext_",Types.VARCHAR);
        cv.put("numeroInt_",Types.VARCHAR);
        cv.put("colonia_",Types.VARCHAR);
        cv.put("municipio_",Types.VARCHAR);
        cv.put("cp_",Types.VARCHAR);
        cv.put("ciudad_",Types.VARCHAR);
        cv.put("estado_",Types.VARCHAR);
        cv.put("email_",Types.VARCHAR);
        cv.put("id_",Types.INTEGER_PRIMARY_KEY);
        return cv;
    }

    @Override
    public Map retriveUpdateColumnName(int oldVersion, int newVersion) {
        Map columnRelation=super.retriveUpdateColumnName(oldVersion,newVersion);
        if(oldVersion==7){
            columnRelation.put("rfc_","rfc_");
            columnRelation.put("nombre_","nombre_");
            columnRelation.put("calle_","calle_");
            columnRelation.put("numeroext_","numeroext_");
            columnRelation.put("numeroInt_","numeroInt_");
            columnRelation.put("colonia_","colonia_");
            columnRelation.put("municipio_","municipio_");
            columnRelation.put("cp_","cp_");
            columnRelation.put("ciudad_","ciudad_");
            columnRelation.put("estado_","estado_");
            columnRelation.put("email_","email_");
            columnRelation.put("objectId","objectId");
            columnRelation.put("created","created");
            columnRelation.put("updated","updated");
        }
        return columnRelation;
    }

    public String getRfc_() {
        return rfc_;
    }

    public rfc setRfc_(String rfc_) {
        this.rfc_ = rfc_;
        return this;
    }

    public String getNombre_() {
        return nombre_;
    }

    public void setNombre_(String nombre_) {
        this.nombre_ = nombre_;
    }

    public String getCalle_() {
        return calle_;
    }

    public void setCalle_(String calle_) {
        this.calle_ = calle_;
    }

    public String getNumeroext_() {
        return numeroext_;
    }

    public void setNumeroext_(String numeroext_) {
        this.numeroext_ = numeroext_;
    }

    public String getNumeroInt_() {
        return numeroInt_;
    }

    public void setNumeroInt_(String numeroInt_) {
        this.numeroInt_ = numeroInt_;
    }

    public String getColonia_() {
        return colonia_;
    }

    public void setColonia_(String colonia_) {
        this.colonia_ = colonia_;
    }

    public String getMunicipio_() {
        return municipio_;
    }

    public void setMunicipio_(String municipio_) {
        this.municipio_ = municipio_;
    }

    public String getCp_() {
        return cp_;
    }

    public void setCp_(String cp_) {
        this.cp_ = cp_;
    }

    public String getEstado_() {
        return estado_;
    }

    public void setEstado_(String estado_) {
        this.estado_ = estado_;
    }

    public String getCiudad_() {
        return ciudad_;
    }

    public void setCiudad_(String ciudad_) {
        this.ciudad_ = ciudad_;
    }

    public String getEmail_() {
        return email_;
    }

    public void setEmail_(String email_) {
        this.email_ = email_;
    }
}
