package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 28/08/17.
 */
public class parkeos extends ObjectBackendless {
    private String IdParkimetro_;
    private long Tiempo_;
    private String status_;
    private String Qr_;

    public String getIdParkimetro_() {
        return IdParkimetro_;
    }

    public void setIdParkimetro_(String idParkimetro_) {
        this.IdParkimetro_ = idParkimetro_;
    }

    public long getTiempo_() {
        return Tiempo_;
    }

    public void setTiempo_(long tiempo_) {
        this.Tiempo_ = tiempo_;
    }

    public String getStatus_() {
        return status_;
    }

    public void setStatus_(String status_) {
        this.status_ = status_;
    }

    public String getQr_() {
        return Qr_;
    }

    public void setQr_(String qr_) {
        this.Qr_ = qr_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("IdParkimetro_",Types.VARCHAR);//Id local que se genera en el pedido por parkeo.1 Parkeo=Mulriples pedidos=1 Solo ID
        columnas.put("Tiempo_",Types.INTEGER);//La hora en la que comienza el contador
        columnas.put("status_",Types.VARCHAR);//Abierto o cerrado(Si se encuentra actualmente en  contador por cobro o no)
        columnas.put("Qr_",Types.VARCHAR);//Identificacion del auto
        return columnas;
    }
}
