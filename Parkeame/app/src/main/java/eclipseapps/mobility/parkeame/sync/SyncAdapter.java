package eclipseapps.mobility.parkeame.sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.files.router.OutputStreamRouter;
import com.backendless.persistence.DataQueryBuilder;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.backendservice.DemoService;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.Defaults;
import eclipseapps.mobility.parkeame.cloud.Parkimetros;
import eclipseapps.mobility.parkeame.cloud.Precios;
import eclipseapps.mobility.parkeame.cloud.Tokens;
import eclipseapps.mobility.parkeame.cloud.parkeos;
import eclipseapps.mobility.parkeame.cloud.user;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by usuario on 19/02/18.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private DBParkeame db;
    int indexCloud;
    SyncService.onActionlistener listener;

   // List<String> imagesOnCloud=new ArrayList<String>();
    public static String fault;

    ContentResolver mContentResolver;
    Context mcontext;
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mcontext=context;
    }
    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mcontext=context;
    }
    public SyncAdapter setonActionListener(SyncService.onActionlistener listener){
        this.listener=listener;
        return this;
    }
    /*
    * Specify the code you want to run in the sync adapter. The entire
    * sync adapter runs in a background thread, so you don't have to set
    * up your own background processing.
    */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
    /*
     * Put the data transfer code here.
     */
        android.os.Debug.waitForDebugger();
        if(!Backendless.isInitialized()){
            Backendless.initApp( this,
                    Defaults.APPLICATION_ID,
                    Defaults.API_KEY );
        }

        enciendeNotificacion(false,"Sincronizando...");


        SharedPreferences prefs = mcontext.getSharedPreferences("eclipseapps.mobility.parkeame", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("lastUpdate",0);

        //Primero busca en la nube y descarga los precios creados y actualizado de los parquimetros despues de la ultima fecha de actualizacion
        syncPrices(lastUpdate);

        //Ahora revisa el estado actual de la disponibilidad de cajones de estacionamiento y los cambios creados y actualizado en la agenda de cada uno de ellos
        syncParkimetros(lastUpdate);

        //Ahora revisa de manera local si existen parkeos abiertos solicitadas y que aún no se haya subido la peticion
        syncParkeos(lastUpdate);

        if(extras.containsKey("user")){
            String User=extras.getString("user");
            if(User!=null){
                syncAutos(User);
                if(extras.containsKey("payId")){
                    syncSaldo(extras.getString("payId"));
                }

            }
        }

        if(listener!=null)listener.sendAction(SyncService.ACTION_FIN);

        apagaNotificacion();
    }


    public void enciendeNotificacion(boolean exito,String Mensaje){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mcontext)
                        .setSmallIcon(exito? R.drawable.ic_local_parking_white_48dp:R.drawable.logo)
                        .setContentTitle(mcontext.getString(R.string.app_name))
                        .setContentText(Mensaje);
        final NotificationManager mNotificationManager = (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }
    public void apagaNotificacion(){
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) mcontext.getSystemService(ns);
        nMgr.cancel(001);
    }
    public void syncParkeos(long lastUpdate){
        if(db==null)db=DBParkeame.getInstance(mcontext);
        String query="created >"+String.valueOf(lastUpdate)+" OR updated>"+String.valueOf(lastUpdate);
        List<parkeos> Parkeos=new parkeos().findSyncInCloud(query);
        for (parkeos parkeo:Parkeos) {
            parkeo.savein(db);
        }
    }
    public void syncParkimetros(long lastUpdate){
        if(db==null)db=DBParkeame.getInstance(mcontext);
        String query="created >"+String.valueOf(lastUpdate)+" OR updated>"+String.valueOf(lastUpdate);
        List<Parkimetros> parkimentros=new Parkimetros().findSyncInCloud(query);
        if(parkimentros!=null){
            for (Parkimetros parkimetro:parkimentros) {
                parkimetro.savein(db);
            }
        }
    }
    public void syncPrices(long lastUpdate){
        if(db==null)db=DBParkeame.getInstance(mcontext);
        String query="created >"+String.valueOf(lastUpdate)+" OR updated>"+String.valueOf(lastUpdate);
        List<Precios> precios=new Precios().findSyncInCloud(query);
        if(precios!=null){
            for (Precios precio:precios) {
                precio.savein(db);
            }
        }
    }
    public void syncAutos(String UserId){
        List<Autos> autos=new Autos().findSyncInCloud("ownerId='"+UserId+"'");
        for (Autos auto:autos) {
            auto.savein(db);
        }
    }
    public void syncSaldo(final String payId){
        DemoService.getInstance().getCustomerAsync(payId, new AsyncCallback<String>() {
            @Override
            public void handleResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    float saldo= (float) json.getDouble("balance");
                    SharedPreferences prefs = db.getcontext().getSharedPreferences("eclipseapps.mobility.parkeame", MODE_PRIVATE);
                    prefs.edit().putFloat("balance", saldo).commit();
                    syncCustomerTokens(payId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }

    private void syncCustomerTokens(String payId){
        DemoService.getInstance().getCustomerTokens(payId, new AsyncCallback<String>() {
            @Override
            public void handleResponse(String response) {
                if (response==null) return;
                try {
                    Cursor cur;
                    String Selectedtoken=db.GetStringScalar("SELECT * FROM Tokens WHERE Selected_='1'",null,"Token_");
                    Tokens token=new Tokens();
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject json= (JSONObject) jsonArray.get(i);
                        token.setToken_(json.getString("id"));
                        token.setHolderName_(json.getString("holder_name"));
                        token.setCardNumber_(json.getString("card_number"));
                        token.setBrand_(json.getString("brand"));
                        String string = json.getString("creation_date");
                        DateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa", Locale.ENGLISH);
                        try {
                            Date date = format.parse(string);
                            token.setCreationDate_(date.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        token.setSelected_(Selectedtoken==token.getToken_());
                        cur=db.getDBInstance().rawQuery("SELECT * FROM Tokens WHERE Token_='"+token.getToken_()+"'",null);
                        if (cur.getCount()>0){
                            token.update(db,"Token_='"+token.getToken_()+"'");
                        }else{
                            token.savein(db);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("user","Problema al recuperar los tokens");
            }
        });
    }




}
