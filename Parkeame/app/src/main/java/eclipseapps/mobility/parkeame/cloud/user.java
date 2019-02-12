package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eclipseapps.library.backendless.SyncUser;
import eclipseapps.library.databases.databaseHelper;
import eclipseapps.mobility.parkeame.activities.Credentials;
import eclipseapps.mobility.parkeame.backendservice.DemoService;
import eclipseapps.mobility.parkeame.fragments.fragment_pago;
import eclipseapps.mobility.parkeame.sync.SyncAdapter;
import eclipseapps.mobility.parkeame.sync.SyncService;
import pay.openpay.model.Card;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by usuario on 26/08/17.
 */
public class user extends SyncUser {

    public user(databaseHelper DB) {super(DB);}
    public user() {super();}
    @Override
    public ContentValues getSpecialProperties() {
        ContentValues cv=new ContentValues();
        cv.put("payId","varchar(50)");
        cv.put("lastsessionId","varchar");
        return cv;
    }

    public user setPayId(String payId){
        setProperty("payId",payId);
        return this;
    }
    public String getpayId(){
        return (String) getProperty("payId");
    }

    public user setlastSessionId(String sessionId){
        setProperty("lastsessionId",sessionId);
        return this;
    }
    public String getlastSessionId(){
        return (String) getProperty("lastsessionId");
    }
    @Override
    public synchronized boolean login(String login, String Password, boolean stayLoggedIn) {
        final boolean result=super.login(login, Password, stayLoggedIn);
        return result;
    }
}
