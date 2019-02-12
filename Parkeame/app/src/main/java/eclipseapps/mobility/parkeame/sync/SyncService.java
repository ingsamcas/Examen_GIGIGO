package eclipseapps.mobility.parkeame.sync;

import android.accounts.Account;
import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.splunk.mint.Mint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.List;

import eclipseapps.mobility.parkeame.cloud.user;

import static eclipseapps.mobility.parkeame.sync.Authenticator.Constants.AUTHORITY;
import static eclipseapps.mobility.parkeame.sync.Authenticator.CreateSyncAccount;


/**
 * Created by usuario on 19/02/18.
 */

public class SyncService extends IntentService {
    public static final String cuentas="cuentas";
    public static final String basics="basics";
    public static final String tags="tags";
    public static final String paths="paths";
    public static final String movimiento="Movimiento";
    public static final String RFC="RFC";

    public static final String ACTION_PROGRESO =
            "eclipseapps.financial.moneytracker.action.PROGRESO";
    public static final String ACTION_FIN =
            "eclipseapps.financial.moneytracker.action.FIN";
    public static final String ACTION_SYNC=
            "eclipseapps.mobility.parkeame.action.FORCE_SYNC";
    public static final String ACTION_SyncAutos=
            "eclipseapps.mobility.parkeame.action.SYNCAUTOS";
    public static final String ACTION_DOWNLOAD_FILE= "eclipseapps.financial.moneytracker.action.DOWNLOADFILE" ;

    public static String stateService=ACTION_FIN;


    // Storage for an instance of the sync adapter
    private static SyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    public SyncService(String name) {
        super(name);
    }
    public SyncService() {
        super("SyncService");
    }

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        super.onCreate();

        synchronized (sSyncAdapterLock) {
           // Mint.initAndStartSession(this.getApplication(), "32776d7d");
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
            sSyncAdapter.setonActionListener(new onActionlistener() {
                @Override
                public void sendAction(String action) {
                    //sendAction(action);
                    stateService=action;
                    Intent bcIntent = new Intent();
                    bcIntent.setAction(stateService);
                    LocalBroadcastManager.getInstance(SyncService.this).sendBroadcast(bcIntent);
                }
            });
        }
    }
    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        android.os.Debug.waitForDebugger();
        if(intent.getAction().matches(ACTION_SYNC)){
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);


           if(intent.hasExtra("user")){
               settingsBundle.putString("user",intent.getStringExtra("user"));
               settingsBundle.putBoolean("SyncAutos",intent.getBooleanExtra("SyncAutos",false));
               settingsBundle.putBoolean("SyncSaldo",intent.getBooleanExtra("SyncSaldo",false));
           }


        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
            Account mAccount = CreateSyncAccount(this);
            ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);

        }else if(intent.getAction().matches(ACTION_SyncAutos)){
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
            sSyncAdapter.syncAutos(((user)intent.getParcelableExtra("user")).getObjectId());
        }

/*
        if(intent.getAction().matches(ACTION_SAVE_LOCAL)){
            String rfc=intent.getStringExtra(RFC);
            basics BasicInfo= (basics) intent.getSerializableExtra(basics);
            if( intent.getLongExtra(movimiento,-1)!=-1){

            }
            BasicInfo.setEstacionamiento_((int) intent.getLongExtra(movimiento,-1));

            cuentas Account= (cuentas) intent.getSerializableExtra(cuentas);;
            List Movtags=intent.getStringArrayListExtra(tags);
            List imagepaths=intent.getStringArrayListExtra(paths);

            stateService=ACTION_PROGRESO;
            Intent bcIntent = new Intent();
            bcIntent.setAction(ACTION_PROGRESO);
            sendBroadcast(bcIntent);

            //Guarda Todo de manera Local
            SyncAdapter sync=new SyncAdapter(this,true).setonActionListener(new onActionlistener() {
                @Override
                public void sendAction(String action) {
                    sendAction(action);
                }
            });
            basics movmentBeforeOperation=sync.guardaBasicsEnLocal(BasicInfo);
            sync.guardaTagsEnLocal(BasicInfo.getEstacionamiento_(),Movtags);
            sync.remplazaImagesEnLocal(BasicInfo.getEstacionamiento_(),imagepaths);
            if (!rfc.matches("")) {
                DBSmartWallet db=DBSmartWallet.getInstance(this);
                facturas factura=db.selectFirst("SELECT * FROM facturas WHERE id_="+BasicInfo.getEstacionamiento_(),facturas.class);
                if(factura==null)factura=new facturas();
                factura.setEstacionamiento_(BasicInfo.getEstacionamiento_());
                factura.setRfc_(rfc);
                factura.setUser_("user");
                sync.guardaFacturanEnLocal(factura);
            }
            if(movmentBeforeOperation!=null){
                cuentas cuenta=sync.actualizaSaldo(movmentBeforeOperation.getCantidad_()*-1,movmentBeforeOperation.getCuenta_());//Regresa la cuenta al estado anterior antes de que se realizara el movimiento
                if(cuenta.get_cuenta().matches(Account.get_cuenta()))Account=cuenta;
            }
            sync.actualizaSaldo(BasicInfo.getCantidad_(),Account);
            sendAction(ACTION_FIN);//Termina de manera local
        }else if(intent.getAction().matches(ACTION_DOWNLOAD_FILE)){

            String urlToDownload = intent.getStringExtra("url");
            String objectId=intent.getStringExtra("objectId");
            ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
            try {
                URL url = null;
                try {
                    url = new URL(urlToDownload);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if(url==null)return;
                URLConnection connection = null;
                try {
                    connection = url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(connection==null)return;
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

               String path= this.getApplicationInfo().dataDir+"/files/facturas/";
                File ruta=new File(path);
                if (!ruta.exists()){
                    ruta.mkdirs();
                }
                 path=path+urlToDownload.substring(urlToDownload.lastIndexOf("/")+1);
                File factura=new File(path);
                if (factura.exists())factura.delete();
                // download the file
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    Bundle resultData = new Bundle();
                    resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                    receiver.send(FacturaViewer_activity.DownloadReceiver.UPDATE_PROGRESS, resultData);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                DBSmartWallet db=DBSmartWallet.getInstance(this);
                db.getDBInstance().execSQL("UPDATE facturas SET pdf_='"+path+"' WHERE objectId='"+objectId+"'");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bundle resultData = new Bundle();
            resultData.putInt("progress" ,100);
            receiver.send(FacturaViewer_activity.DownloadReceiver.UPDATE_PROGRESS, resultData);


        }*/
    }
   public static String getFault(){
       return SyncAdapter.fault;
   }
    public void sendAction(String action){
        stateService=action;
        Intent bcIntent = new Intent();
        bcIntent.setAction(stateService);
        sendBroadcast(bcIntent);
    }
    public interface onActionlistener{
        public void sendAction(String action);
    }
    public static void copyFile(Context context)
    {
        boolean bool=false;
        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                String currentDBPath = "/data/data/eclipseapps.financial.moneytracker/databases/DBSmartWallet.db3";
                String backupDBPath = "DBSmartWallet1Marzo2018.db3";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    bool=true;
                }

            }
        } catch (Exception e) {
            Log.w("Settings Backup", e);
        }
        if(bool == true)
        {
            Toast.makeText(context, "Backup Complete", Toast.LENGTH_SHORT).show();
            bool = false;
        }
    }
    public static void sendDB(Activity activity){
        String filename="/data/data/eclipseapps.financial.moneytracker/databases/DBSmartWallet.db3";
        File filelocation = new File(filename);
        Uri path = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
        String to[] = {"ingsamcas@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "DBSmartWallet");
        activity.startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

}
