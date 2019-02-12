package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import eclipseapps.library.backendless.ObjectBackendless;
import eclipseapps.library.backendless.SyncUser;

/**
 * Created by usuario on 28/08/17.
 */
public class Autos extends ObjectBackendless {

    private String qr_;
    private String tipo_;//Camioneta,sedan...etc
    private String marca_;
    private String modelo_;
    private String placas_;
    private int fechaModelo_;
    private String status_;

    public String getStatus_() {
        return status_;
    }

    public void setStatus_(String _status) {
        this.status_ = _status;
    }

    public String getMarca_() {
        return marca_;
    }

    public void setMarca_(String marca_) {
        this.marca_ = marca_;
    }

    public String getTipo_() {
        return tipo_;
    }

    public void setTipo_(String tipo) {
        this.tipo_ = tipo;
    }

    public String getQr_() {
        return qr_;
    }

    public void setQr_(String qr_) {
        this.qr_ = qr_;
    }

    public String getModelo_() {
        return modelo_;
    }

    public void setModelo_(String modelo_) {
        this.modelo_ = modelo_;
    }

    public String getPlacas_() {
        return placas_;
    }

    public void setPlacas_(String placas_) {
        this.placas_ = placas_;
    }

    public int getFechamodelo_() {
        return fechaModelo_;
    }

    public void setFechamodelo_(int fechamodelo_) {
        this.fechaModelo_ = fechamodelo_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("qr_",Types.VARCHAR);
        columnas.put("modelo_",Types.VARCHAR);//
        columnas.put("fechaModelo_",Types.INTEGER);//año del modelo
        columnas.put("placas_",Types.VARCHAR);//
        columnas.put("tipo_",Types.VARCHAR);//
        columnas.put("marca_",Types.VARCHAR);//
        columnas.put("status_",Types.VARCHAR);//
        return columnas;
    }
   public  static class Tipos{
        public static String DEFAULT="DEFAULT";
       public static String ACTIVE="ACTIVE";
   }

}
