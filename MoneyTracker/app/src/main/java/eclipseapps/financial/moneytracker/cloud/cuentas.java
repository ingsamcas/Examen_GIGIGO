package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 13/03/17.
 */
public class cuentas extends ObjectBackendless {
    public final static String CuentaEliminada="CuentaEliminada";
    public final static String NuevaCuenta="Nueva Cuenta ++";
    protected Double cantidad_;
    protected Double cantidadInicial_;
    protected String cuenta_;
    protected int id_;

    public Double getCantidadInicial_() {
        return cantidadInicial_;
    }

    public void setCantidadInicial_(Double cantidadInicial_) {
        this.cantidadInicial_ = cantidadInicial_;
    }

    public String get_cuenta() {
        return cuenta_;
    }

    public void set_cuenta(String _cuenta) {
        this.cuenta_ = _cuenta;
    }

    public int get_id() {
        return id_;
    }

    public void set_id(int _id) {
        this.id_ = _id;
    }

    public Double get_cantidad() {
        return cantidad_;
    }

    public void set_cantidad(Double _cantidad) {
        this.cantidad_ = _cantidad;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("id_",Types.INTEGER_PRIMARY_KEY);
        columnas.put("cuenta_",Types.VARCHAR);
        columnas.put("cantidad_",Types.REAL);
        columnas.put("cantidadInicial_",Types.REAL);
        return columnas;
    }
/*
Actualizacion de base de datos de version 3 a 4
 */
    @Override
    public Map retriveUpdateColumnName(int oldversion,int newversion) {
        Map columnRelation=super.retriveUpdateColumnName(oldversion,newversion);
        if(oldversion==3){
            columnRelation.put("_cantidad","cantidad_");
            columnRelation.put("_id","id_");
            columnRelation.put("_cuenta","cuenta_");
            columnRelation.put("_objectId","objectId");
            columnRelation.put("_created","created");
            columnRelation.put("_updated","updated");
        }else if(oldversion==7){
            columnRelation.put("cantidad_","cantidad_");
            columnRelation.put("id_","id_");//Esta se actualizo como primary key
            columnRelation.put("cuenta_","cuenta_");
            columnRelation.put("objectId","objectId");
            columnRelation.put("created","created");
            columnRelation.put("updated","updated");
        }
        return columnRelation;
    }
}
