package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 17/10/17.
 */

public class Saldos extends ObjectBackendless {
    private float Cantidad_;
    private float Saldo_;
    private String Descripcion_;

    public float getCantidad_() {
        return Cantidad_;
    }

    public void setCantidad_(float cantidad_) {
        Cantidad_ = cantidad_;
    }

    public float getSaldo_() {
        return Saldo_;
    }

    public void setSaldo_(float saldo_) {
        Saldo_ = saldo_;
    }

    public String getDescripcion_() {
        return Descripcion_;
    }

    public void setDescripcion_(String descripcion_) {
        Descripcion_ = descripcion_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas = new ContentValues();
        columnas.put("Cantidad_", Types.REAL);//Cantidad del movimiento, puede ser positiva o negativa
        columnas.put("Saldo_", Types.REAL);//Saldo despues del movimiento
        columnas.put("Descripcion_", Types.VARCHAR);//Deposito:Id del deposito,Retiro:Id del parkeo
        return columnas;
    }
}
