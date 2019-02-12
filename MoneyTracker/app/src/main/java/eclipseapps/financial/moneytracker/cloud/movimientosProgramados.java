package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

public class movimientosProgramados extends ObjectBackendless {
    protected double id_;//Id of the basic movment
    protected int frecuencia_;
    protected String periodo_;

    public double getId_() {
        return id_;
    }

    public movimientosProgramados setId_(double id_) {
        this.id_ = id_;
        return this;
    }

    public int getFrecuencia_() {
        return frecuencia_;
    }

    public movimientosProgramados setFrecuencia_(int frecuencia_) {
        this.frecuencia_ = frecuencia_;
        return this;
    }

    public String getPeriodo_() {
        return periodo_;
    }

    public movimientosProgramados setPeriodo_(String periodo_) {
        this.periodo_ = periodo_;
        return this;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("id_",Types.INTEGER);//id del movimiento en local(movimiento padre)
        columnas.put("frecuencia_",Types.INTEGER);//Cada "n" periodo en el que se debe repetir el gasto
        columnas.put("periodo_",Types.VARCHAR);//Base de tiempo transcurrido (día,semana,mes,año,etc)
        return columnas;
    }

    @Override
    protected ContentValues ConstraintsRelation() {
        ContentValues contraints = super.ConstraintsRelation();
        contraints.put(Types.FOREIGN_KEY("id_"),Types.REFERENCES_ONDELETECASCADE("basics","id_"));
        return contraints;
    }
}
