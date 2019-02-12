package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 23/01/18.
 */

public class facturas extends ObjectBackendless {
    protected double id_;//Id of the basic movment
    protected String rfc_;
    protected String pdf_;
    protected String xml_;
    protected String user_;

    public double getId_() {
        return id_;
    }

    public void setId_(double id_) {
        this.id_ = id_;
    }

    public String getRfc_() {
        return rfc_;
    }

    public void setRfc_(String rfc_) {
        this.rfc_ = rfc_;
    }

    public String getPdf_() {
        return pdf_;
    }

    public void setPdf_(String pdf_) {
        this.pdf_ = pdf_;
    }

    public String getXml_() {
        return xml_;
    }

    public void setXml_(String xml_) {
        this.xml_ = xml_;
    }

    public String getUser_() {
        return user_;
    }

    public void setUser_(String user_) {
        this.user_ = user_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("id_",Types.INTEGER);//id del movimiento en local
        columnas.put("rfc_",Types.VARCHAR);//RFC previamente registrado que se utilizo al solicitar la factura
        columnas.put("pdf_",Types.VARCHAR);//Lugar en la nube donde se aloja el documento pdf
        columnas.put("xml_",Types.VARCHAR);//Lugar en la nube donde se aloja el xml
        columnas.put("user_",Types.VARCHAR);//Correo del usuario que solicito la factura

        return columnas;
    }

    @Override
    protected ContentValues ConstraintsRelation() {
        ContentValues contraints = super.ConstraintsRelation();
        contraints.put(Types.FOREIGN_KEY("id_"),Types.REFERENCES_ONDELETECASCADE("basics","id_"));
        return contraints;
    }


    @Override
    public Map retriveUpdateColumnName(int oldVersion, int newVersion) {
        Map columnRelation= super.retriveUpdateColumnName(oldVersion, newVersion);
        if(oldVersion==7){
            columnRelation.put("id_","id_");//Se coloca como foregin key
            columnRelation.put("rfc_","rfc_");
            columnRelation.put("pdf_","pdf_");
            columnRelation.put("xml_","xml_");
            columnRelation.put("user_","user_");
            columnRelation.put("objectId","objectId");
            columnRelation.put("created","created");
            columnRelation.put("updated","updated");
        }
        return columnRelation;
    }
}
