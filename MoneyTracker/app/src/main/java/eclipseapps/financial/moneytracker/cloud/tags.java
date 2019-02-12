package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 13/03/17.
 */
public class tags extends ObjectBackendless {
    private String tag_;
    private int id_;

    public String getTag_() {
        return tag_;
    }

    public void setTag_(String tag_) {
        this.tag_ = tag_;
    }

    public int getId_() {
        return id_;
    }

    public void setId_(int id_) {
        this.id_ = id_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas=new ContentValues();
        columnas.put("id_",Types.INTEGER);
        columnas.put("tag_",Types.VARCHAR);
        columnas.put(Types.FOREIGN_KEY("id_"),Types.REFERENCES_ONDELETECASCADE("basics","id_"));
        return columnas;
    }
/*
Actualizacion de base de datos de 3 a 4
 */
  @Override
    public Map retriveUpdateColumnName(int oldversion,int newVersion) {
        Map columnRelation=super.retriveUpdateColumnName(oldversion,newVersion);
      if(oldversion==3){
          columnRelation.put("_tag","tag_");
          columnRelation.put("_id","id_");
          columnRelation.put("_objectId","objectId");
          columnRelation.put("_created","created");
          columnRelation.put("_updated","updated");
      }else if(oldversion==7){
          columnRelation.put("tag_","tag_");
          columnRelation.put("id_","id_");//Aunque no hubo cambio se coloco esta como foraing key
          columnRelation.put("objectId","objectId");
          columnRelation.put("created","created");
          columnRelation.put("updated","updated");
      }

        return columnRelation;
    }
}
