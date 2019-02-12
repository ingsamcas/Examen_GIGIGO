package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 31/03/17.
 */
public class motives extends ObjectBackendless {

    protected String motive_;
    protected boolean enabled_;

    public String get_motive(){return motive_;}
    public void set_motive(String motive){motive_=motive;}

    public void set_enabled(boolean _enabled) {
        this.enabled_ = _enabled;
    }

    public boolean get_enabled_() {
        return enabled_;
    }



    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues cv=new ContentValues();
        cv.put("motive_",Types.VARCHAR);
        cv.put("enabled_",Types.INTEGER);
        return cv;
    }

    @Override
    public Map retriveUpdateColumnName(int oldversion,int newversion) {
        Map columnRelation=super.retriveUpdateColumnName(oldversion,newversion);
        if(oldversion==3){//Actualizacion de base de datos de 3 a 4
            columnRelation.put("_motive","motive_");
            columnRelation.put("_objectId","objectId");
            columnRelation.put("_created","created");
            columnRelation.put("_updated","updated");
        }
        return columnRelation;
    }


}
