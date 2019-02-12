package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.SyncUser;
import eclipseapps.library.databases.databaseHelper;

/**
 * Created by usuario on 02/07/17.
 */
public class user extends SyncUser {
    public user(){super();}
    public user(databaseHelper DB) {super(DB);}

    @Override
    public ContentValues getSpecialProperties() {
        ContentValues cv=new ContentValues();
        cv.put("purchaseId","varchar");
        //cv.put("socialAccount","varchar");//Version 5 de la base de datos. Se elimina para la version 6 ya que se hereda
        //del codigo de SyncUser por defecto
        return cv;
    }

    public user setPurchaseId(String payLoad){
        setProperty("purchaseId",payLoad);
        return this;
    }
    public String getPurchaseId(){
        String payload= (String) getProperty("purchaseId");
        return payload;
    }
    public user setSocialAccount(String socialAccount){
        setProperty("socialAccount",socialAccount);
        return this;
    }
    public String getSocialAccount(){
        String socialAccount= (String) getProperty("socialAccount");
        return socialAccount;
    }
}
