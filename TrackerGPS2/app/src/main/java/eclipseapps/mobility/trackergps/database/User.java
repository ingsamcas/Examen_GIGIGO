package eclipseapps.mobility.trackergps.database;

import android.content.ContentValues;

import eclipseapps.library.backendless.SyncUser;
import eclipseapps.library.databases.databaseHelper;

/**
 * Created by usuario on 10/12/16.
 */
public class User extends SyncUser {
    public User(databaseHelper DB) {
        super(DB);
        // TODO Auto-generated constructor stub
    }
    public User(){
        super();
    }
    @Override
    public ContentValues getSpecialProperties() {
        ContentValues cv=new ContentValues();
        cv.put("payId","varchar(50)");
        cv.put("telefono","varchar(20)");
        cv.put("saldo","REAL");
        return cv;
    }

    public User setPayId(String payId){
        setProperty("payId",payId);
        return this;
    }
    public String getpayId(){
        return (String) getProperty("payId");
    }

    public User setSaldo(float saldo){
        setProperty("saldo",saldo);
        return this;
    }
    public User setTelefono(String telefono){
        setProperty("telefono",telefono);
        return this;
    }
    public String getTelefono(){return (String) getProperty("telefono");}
}
