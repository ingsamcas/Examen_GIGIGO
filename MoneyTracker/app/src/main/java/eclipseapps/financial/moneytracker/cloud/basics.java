package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.HashMap;
import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

public class basics extends ObjectBackendless {

private int idcuenta_;
private int id_;
private long tiempo_;
private Double cantidad_;
private String cuenta_;
private String descripcion_;

public int getId_(){return id_;}
public void setId_(int ID){
    id_ =ID;}

public long getTiempo_(){return tiempo_;}
public void setTiempo_(long TIEMPO){
    tiempo_ =TIEMPO;}

public Double getCantidad_(){return cantidad_;}
public void setCantidad_(Double CANTIDAD){
    cantidad_ =CANTIDAD;}

public String getCuenta_(){return cuenta_;}
public void setCuenta_(String CUENTA){
    cuenta_ =CUENTA;}

public String getDescripcion_(){return descripcion_;}
public void setDescripcion_(String DESCRIPCION){
    descripcion_ =DESCRIPCION;}

    public int getIdcuenta_() {
        return idcuenta_;
    }

    public basics setIdcuenta_(int idcuenta_) {
        this.idcuenta_ = idcuenta_;
        return this;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("idcuenta_",Types.INTEGER);
        columnas.put("id_",Types.INTEGER_PRIMARY_KEY);
        columnas.put("tiempo_",Types.INTEGER);
        columnas.put("cantidad_",Types.REAL);
        columnas.put("cuenta_",Types.VARCHAR);
        columnas.put("descripcion_",Types.VARCHAR);
        return columnas;
    }

    @Override
    protected ContentValues ConstraintsRelation() {
        ContentValues relations=super.ConstraintsRelation();
        relations.put(Types.FOREIGN_KEY("idcuenta_"),Types.REFERENCES_ONDELETECASCADE("cuentas","id_"));
        return relations;
    }

    /*
        Actualizacion de base de datos version 3 a 4
         */
    @Override
    public Map retriveUpdateColumnName(int oldversion,int newVersion){

        Map columnRelation=new HashMap();
        if(oldversion==3){
            columnRelation.put("_cantidad","cantidad_");
            columnRelation.put("_id","id_");
            columnRelation.put("_tiempo","tiempo_");
            columnRelation.put("_cuenta","cuenta_");
            columnRelation.put("_descripcion","descripcion_");
            columnRelation.put("_objectId","objectId");
            columnRelation.put("_created","created");
            columnRelation.put("_updated","updated");
        }else if(oldversion==7){
            columnRelation.put("cantidad_","cantidad_");
            columnRelation.put("id_","id_");//Aunque no hubo cambio de nombre se coloco como Primary Key
            columnRelation.put("tiempo_","tiempo_");
            columnRelation.put("cuenta_","cuenta_");
            columnRelation.put("descripcion_","descripcion_");
            columnRelation.put("objectId","objectId");
            columnRelation.put("created","created");
            columnRelation.put("updated","updated");
        }

        return columnRelation;
    }

    class retriveQuerys{
        final static public String sqlGetAllActiveAtTime0="SELECT * FROM basics WHERE cuenta_=? AND anuario=? AND mes=? AND dia=? AND hora=? AND minutos=? AND ampm=?";
        final static public String sqlGetAllActiveAtTime1="SELECT * FROM basics WHERE cuenta_=? AND anuario=? AND mes=? AND dia=? AND hora=?";
        final static public String sqlGetAllActiveAtTime2="SELECT * FROM basics WHERE cuenta_=? AND anuario=? AND mes=? AND dia=?";
        final static public String sqlGetAllActiveAtTime3="SELECT * FROM basics WHERE cuenta_=? AND anuario=? AND mes=?";
        final static public String sqlGetAllActiveAtTime4="SELECT * FROM basics WHERE cuenta_=? AND anuario=?";
        final static public String sqlGetAll="SELECT * FROM basics";
        final static public String sqlGetAllIdsDistinctDate="SELECT * FROM basics GROUP BY anuario,mes,dia ORDER BY anuario,mes,dia ASC";

    }
}
