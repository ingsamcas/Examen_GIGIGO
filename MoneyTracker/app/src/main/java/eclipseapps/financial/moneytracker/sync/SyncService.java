package eclipseapps.financial.moneytracker.sync;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;

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

import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.basics;
import eclipseapps.financial.moneytracker.cloud.cuentas;
import eclipseapps.financial.moneytracker.cloud.facturas;
import eclipseapps.financial.moneytracker.interfaces.DownloadReceiver;

import static eclipseapps.financial.moneytracker.interfaces.DownloadReceiver.failedFile;


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
    public static final String ACTION_INTERRUPT =
            "eclipseapps.financial.moneytracker.action.INTERRUPT";
    public static final String ACTION_SAVE_LOCAL= "eclipseapps.financial.moneytracker.action.SAVELOCAL" ;
    public static final String ACTION_DOWNLOAD_FILE= "eclipseapps.financial.moneytracker.action.DOWNLOADFILE" ;
    public static final String ACTION_DISPLAY_NOTIFICATION="eclipseapps.financial.moneytracker.action.DISPLAYNOTIFICATION";

    public static final String URLSToDownload= "URLSToDownload" ;
    public static final String URLToDownload= "URLToDownload" ;
    public static final String PATHInsideApp="PATHInsideApp";

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
        Log.d("SyncService","CreatingService");
        synchronized (sSyncAdapterLock) {
            AnalyticsApplication.startDefaultTracker(this);
            if(!Backendless.isInitialized())Backendless.initApp(this,"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
            if (sSyncAdapter == null) sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            sSyncAdapter.setonActionListener(new SyncServiceAux.onActionlistener() {
                @Override
                public void sendAction(String action) {
                    //sendAction(action);
                    stateService=action;
                    Intent bcIntent = new Intent();
                    bcIntent.setAction(stateService);
                    sendBroadcast(bcIntent);
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
        Log.d("SyncService","Binded");
        return sSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent!=null && !intent.getAction().matches("") && intent.getAction().matches(ACTION_SAVE_LOCAL)){
            String rfc=intent.getStringExtra(RFC);
            basics BasicInfo= (basics) intent.getSerializableExtra(basics);
            if( intent.getLongExtra(movimiento,-1)!=-1){

            }
            BasicInfo.setId_((int) intent.getLongExtra(movimiento,-1));

            cuentas Account= (cuentas) intent.getSerializableExtra(cuentas);;
            List Movtags=intent.getStringArrayListExtra(tags);
            List imagepaths=intent.getStringArrayListExtra(paths);

            stateService=ACTION_PROGRESO;
            Intent bcIntent = new Intent();
            bcIntent.setAction(ACTION_PROGRESO);
            sendBroadcast(bcIntent);

            //Guarda Todo de manera Local
            SyncAdapter sync=new SyncAdapter(this,true).setonActionListener(new SyncServiceAux.onActionlistener() {
                @Override
                public void sendAction(String action) {
                    sendAction(action);
                }
            });

            //Debug.waitForDebugger();
            basics rowAfected=sync.guardaBasicsEnLocal(BasicInfo);
            sync.guardaTagsEnLocal(rowAfected.getId_(),Movtags);
            sync.remplazaImagesEnLocal(rowAfected.getId_(),imagepaths);
            if (!rfc.matches("")) {
                DBSmartWallet db=DBSmartWallet.getInstance(this);
                facturas factura=db.selectFirst("SELECT * FROM facturas WHERE id_="+rowAfected.getId_(),facturas.class);
                if(factura==null)factura=new facturas();
                factura.setId_(rowAfected.getId_());
                factura.setRfc_(rfc);
                factura.setUser_("user");
                sync.guardaFacturanEnLocal(factura);
                AnalyticsApplication.writeMovementsTracking(AnalyticsApplication.Write.Create, AnalyticsApplication.Action.Factura,rfc);
            }
            if(rowAfected.getId_()==BasicInfo.getId_()){
                AnalyticsApplication.logD("SavingLocal","Regreseando la cuenta anterior a su estado antes del movimiento_"+String.valueOf(rowAfected.getCantidad_()*-1));
                cuentas cuenta=sync.actualizaSaldo(rowAfected.getCantidad_()*-1,rowAfected.getCuenta_());//Regresa la cuenta al estado anterior antes de que se realizara el movimiento
                if(cuenta.get_cuenta().matches(Account.get_cuenta()))Account=cuenta;
            }
            AnalyticsApplication.logD("SavingLocal","Actualizando saldo");
            sync.actualizaSaldo(BasicInfo.getCantidad_(),Account);
            sendAction(ACTION_FIN);//Termina de manera local
        }else if(intent!=null && !intent.getAction().matches("") && intent.getAction().matches(ACTION_DOWNLOAD_FILE)){

            if(intent.hasExtra(URLSToDownload)){
                boolean sucessAllDownload=true;
                String[] urlsToDownload = intent.getStringArrayExtra(URLSToDownload);
                String pathInsideApp = intent.getStringExtra(PATHInsideApp);
                ResultReceiver receiver =  intent.getParcelableExtra("receiver");
                String[] pathsToFiles=new String[urlsToDownload.length];
                for(int i=0;i<urlsToDownload.length;i++){
                    AnalyticsApplication.logD("SyncService","UrlToDownload:"+urlsToDownload[i]);
                    pathsToFiles[i]=downloadFile(urlsToDownload[i],pathInsideApp,receiver);
                    if(pathsToFiles[i].matches("")){
                        AnalyticsApplication.logD("SyncService","Failed trying to Download");
                        Bundle resultData = new Bundle();
                        resultData.putString(failedFile,urlsToDownload[i]);
                        receiver.send(DownloadReceiver.DOWNLOAD_ERROR, resultData);
                        sucessAllDownload=false;
                        break;
                    }
                }
                if(sucessAllDownload){
                    Bundle resultData = new Bundle();
                    resultData.putInt("progress" ,100);
                    resultData.putStringArray("pathsToFiles",pathsToFiles);
                    receiver.send(DownloadReceiver.UPDATE_FINISH, resultData);
                }
            }else{
                String urlToDownload = intent.getStringExtra(URLToDownload);
                String pathInsideApp=intent.getStringExtra(PATHInsideApp);
                ResultReceiver receiver = intent.getParcelableExtra("receiver");
                String pathToFile=downloadFile(urlToDownload,pathInsideApp,receiver);
                if(pathToFile.matches("")){
                    Bundle resultData = new Bundle();
                    resultData.putString(failedFile,urlToDownload);
                    receiver.send(DownloadReceiver.DOWNLOAD_ERROR, resultData);
                }else{
                    Bundle resultData = new Bundle();
                    resultData.putInt("progress" ,100);
                    resultData.putStringArray("pathsToFiles" ,new String[]{pathToFile});
                    receiver.send(DownloadReceiver.UPDATE_FINISH, resultData);
                }

            }
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }





    private String downloadFile(String urlToDownload,String pathInsideApp,ResultReceiver receiver){
        try {
            URL url = null;
            try {
                url = new URL(urlToDownload);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if(url==null)return "";
            URLConnection connection = null;
            try {
                connection = url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(connection==null)return "";
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            String path= this.getApplicationInfo().dataDir+pathInsideApp;
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
                if(receiver!=null)receiver.send(DownloadReceiver.UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
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
    public static Intent getDownLoadFileIntent(Context context,String[] URLSToDownload,String PATHInsideApp,DownloadReceiver DR){
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(SyncService.URLSToDownload, URLSToDownload);
        intent.putExtra(SyncService.PATHInsideApp,PATHInsideApp);
        intent.putExtra("receiver",  DR);
        intent.setAction(SyncService.ACTION_DOWNLOAD_FILE);
        return intent;
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
    public static class notificationCenterPreferences {
        public static final String CHANNEL_ID_API26="SabiasQue";
        public static final String lastMessage="lastMessage";
        public static final String NotificationId="NotificationId";
        public static void createNotificationChannel(Context context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.channel_name);
                String description = context.getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID_API26, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
