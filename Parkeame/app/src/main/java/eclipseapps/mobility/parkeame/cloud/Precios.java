package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 03/09/17.
 */

public class Precios extends ObjectBackendless {

    private String estacionamiento_;
    private int TiempoEstacionamiento_;
    private float Precio_;
    private int TiempoInferior_;
    private int TiempoSuperior_;
     private int diaSemana_;

    public int getDiaSemana_() {
        return diaSemana_;
    }

    public void setDiaSemana_(int diaSemana_) {
        this.diaSemana_ = diaSemana_;
    }

    public String getEstacionamiento_() {
        return estacionamiento_;
    }

    public void setEstacionamiento_(String estacionamiento_) {
        this.estacionamiento_ = estacionamiento_;
    }

    public int getTiempoEstacionamiento_() {
        return TiempoEstacionamiento_;
    }

    public void setTiempoEstacionamiento_(int tiempoEstacionamiento_) {
        this.TiempoEstacionamiento_ = tiempoEstacionamiento_;
    }

    public float getPrecio_() {
        return Precio_;
    }

    public void setPrecio_(float precio_) {
        this.Precio_ = precio_;
    }

    public int getTiempoInferior_() {
        return TiempoInferior_;
    }

    public void setTiempoInferior_(int tiempoInferior_) {
        TiempoInferior_ = tiempoInferior_;
    }

    public int getTiempoSuperior_() {
        return TiempoSuperior_;
    }

    public void setTiempoSuperior_(int tiempoSuperior_) {
        TiempoSuperior_ = tiempoSuperior_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas = new ContentValues();
        columnas.put("estacionamiento_", Types.VARCHAR);//Id de parkimetro, de la zona geografica(Este Id debe estar ligado a una calle o a un codigo postal
        columnas.put("TiempoEstacionamiento_", Types.INTEGER);//tiempo en minutos de estacionamiento
        columnas.put("TiempoInferior_", Types.INTEGER);//tiempo en horas(sistema 24hrs)
        columnas.put("TiempoSuperior_", Types.INTEGER);//tiempo en horas(sistema 24 hrs)
        columnas.put("diaSemana_", Types.INTEGER);//0: Todos los dias,1:Lunes....7:Domingo
        columnas.put("Precio_", Types.REAL);//Precio de este parkimetro en el intervalo temporal dado

        return columnas;
    }

    public static String[] retriveTimeFormated(int min) {
        String[] formated=new String[2];//[0]:Horas,[1]:Minutos
        int horas;
        for (horas = 0; min >= 60; horas++) {
            min = min - 60;
        }
        formated[0]=String.valueOf(horas) + " hr";
        formated[1]=String.valueOf(min) + " min";
        return formated;
    }

    public static class TimeExpressions {

        public static final int treinta_min = 30;
        public static final int Una_hora = 60;
        public static final int HorayMedia = 90;
        public static final int DosHoras = 120;
        public static final int DosHorasyMedia = 150;
        public static final int TresHoras = 180;
    }

}
