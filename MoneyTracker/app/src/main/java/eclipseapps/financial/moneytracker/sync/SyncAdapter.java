package eclipseapps.financial.moneytracker.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.files.router.OutputStreamRouter;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.cloud.DBMessages;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.basics;
import eclipseapps.financial.moneytracker.cloud.cuentas;
import eclipseapps.financial.moneytracker.cloud.descripimagenes;
import eclipseapps.financial.moneytracker.cloud.facturas;
import eclipseapps.financial.moneytracker.cloud.rfc;
import eclipseapps.financial.moneytracker.cloud.tags;
import eclipseapps.financial.moneytracker.interfaces.DownloadReceiver;
import eclipseapps.financial.moneytracker.interfaces.onAlarmReceiver;
import eclipseapps.financial.moneytracker.utils.DriveServiceHelper;

import static android.content.Context.MODE_PRIVATE;
import static eclipseapps.financial.moneytracker.sync.SyncService.ACTION_FIN;
import static eclipseapps.financial.moneytracker.sync.SyncService.ACTION_INTERRUPT;


/**
 * Created by usuario on 19/02/18.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    SharedPreferences sp;
    public final static String preferences_lastTime_Messages_Updated="preferences_lastTime_Messages_Updated";
    public final static String preferences_lastTime_BackUpOnDrive="preferences_lastTime_BackUpOnDrive";
    private DBSmartWallet db;
    int indexCloud;
    SyncServiceAux.onActionlistener listener;

   // List<String> imagesOnCloud=new ArrayList<String>();
    public static String fault;

    ContentResolver mContentResolver;
    Context mcontext;

    private static final Object sSyncAdapterLock = new Object();


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
    public SyncAdapter setonActionListener(SyncServiceAux.onActionlistener listener){
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
            Bundle extras1,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
    /*
     * Put the data transfer code here.
     */
        synchronized (sSyncAdapterLock) {
            enciendeNotificacion(false,"Sincronizando contenido...");
            sp=mcontext.getSharedPreferences("eclipseapps.financial.moneytracker",MODE_PRIVATE);
            SyncMessages();
            SyncDBInDrive(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    updateLastTimeUpdatedForDriveBackup(sp);
                }
            });


            //Primero busca el la nube y descarga los links de aquellas facturas con las que ya se cuenta
            if(db==null)db=DBSmartWallet.getInstance(mcontext);
            final List<facturas> SolicitudesSinRespuesta=db.select("Select * FROM facturas WHERE objectId is not null AND objectId <> '' AND ((pdf_ is null or pdf_ = ''))",facturas.class);
            String query="objectId IN (";
            for (facturas factura:SolicitudesSinRespuesta) {
                query=query+"'"+factura.getObjectId()+"',";
            }
            if(query.contains(",")){
                query=query.substring(0,query.lastIndexOf(","))+")";
                DataQueryBuilder queryBuilder=DataQueryBuilder.create();
                queryBuilder.setPageSize( 25 ).setOffset( 0 ).setWhereClause(query);
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Hay peticiones de factura sin respuesta");
                Backendless.Data.find(facturas.class, queryBuilder, new AsyncCallback<List<facturas>>() {
                    @Override
                    public void handleResponse(List<facturas> response) {
                        if(response.size()>0){//Si ya hay alguna factura la actualiza
                            for (facturas factura:response) {
                                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Actualizando localmente la peticion de factura");
                                guardaFacturanEnLocal(factura);
                                //descargaFacturas
                            }
                        }
                        buscarFacturasPorSubir();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        if(AnalyticsApplication.debug)Log.d("SyncAdapter","fault:"+fault.getMessage());
                        //Dependiendo del error continua con el proceso
                        apagaNotificacion();
                        AnalyticsApplication.sendLogAsError("BackendlessFault",fault.getCode()+":"+fault.getMessage());
                        SyncAdapter.this.fault=fault.getCode()+":"+fault.getMessage();
                        if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
                    }
                });
            }else{
                buscarFacturasPorSubir();
            }
        }


    }
    private void buscarFacturasPorSubir(){
        if(AnalyticsApplication.debug)Log.d("SyncAdapter","Buscando nuevas peticiones de facturas");
        //Ahora revisa de manera local si existen facturas solicitadas y que aún no se haya subido la peticion
        final List<facturas> FacturasASolicitar=db.select("Select * FROM facturas WHERE facturas.objectId is null or facturas.objectId=''",facturas.class);//"Select * FROM facturas WHERE facturas.objectId is null or facturas.objectId=''"
        if(FacturasASolicitar!=null && FacturasASolicitar.size()>0){//Si las encuentra entonces sube peticion por peticion
            if(AnalyticsApplication.debug)Log.d("SyncAdapter","Se subiran "+String.valueOf(FacturasASolicitar.size())+" peticiones");
            indexFacturaToCloud=0;
            sincronizaFacturasEnCloud(FacturasASolicitar);
        }else{
            if(AnalyticsApplication.debug)Log.d("SyncAdapter","No hay peticiones de facturas por subir");
            if (listener!=null)listener.sendAction(ACTION_FIN);
        }
    }
    int indexFacturaToCloud=0;
    private void sincronizaFacturasEnCloud(final List<facturas> Facturas){
        if(indexFacturaToCloud<Facturas.size()){
            if(AnalyticsApplication.debug)Log.d("SyncAdapter","Peticion para el ID local:"+Facturas.get(indexFacturaToCloud).getId_());
            saveAndRequestFactura(Facturas.get(indexFacturaToCloud),new AsyncCallback() {
                @Override
                public void handleResponse(Object response) {
                    indexFacturaToCloud++;
                    if(indexFacturaToCloud<Facturas.size()){
                        sincronizaFacturasEnCloud(Facturas);
                    }else{
                        apagaNotificacion();
                        if(AnalyticsApplication.debug)Log.d("SyncAdapter","Fin");
                        if(listener!=null)listener.sendAction(ACTION_FIN);
                    }

                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    if(AnalyticsApplication.debug)Log.d("SyncAdapter","fault:"+fault.getMessage());
                    apagaNotificacion();
                    AnalyticsApplication.sendLogAsError("BackendlessFault",fault.getCode()+":"+fault.getMessage());
                    SyncAdapter.this.fault=fault.getCode()+":"+fault.getMessage();
                    if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
                }
            });
        }

    }
    private void saveAndRequestFactura(final facturas Factura, final AsyncCallback result) {
        final rfc rfcInfo=db.selectFirst("Select * FROM rfc WHERE rfc_='"+Factura.getRfc_()+"'",rfc.class);
        final basics basicInfo= db.selectFirst("Select * FROM basics WHERE id_="+Factura.getId_(),basics.class);
        final List<descripimagenes> images=db.select("Select * FROM descripimagenes WHERE descripimagenes.id_="+basicInfo.getId_(),descripimagenes.class);
        basicInfo.setObjectId(null);
        Backendless.Data.save(basicInfo, new AsyncCallback<basics>() {//Guarda basics en la nube
            @Override
            public void handleResponse(basics response) {
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Se guardo con exito la informacion basica en la nube");
                basicInfo.setObjectId(response.getObjectId());
                basicInfo.setCreated(response.getCreated());
                basicInfo.setUpdated(response.getUpdated());
                guardaBasicsEnLocal(basicInfo);//Actualiza basics en local en base al Id_(local)
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Se guardo con exito la informacion basica localmente");

                if(images.size()==0){//Si no tiene imagenes entonces lo unico que guarda es la factura y coloca el movimiento como child
                    if(AnalyticsApplication.debug)Log.d("SyncAdapter","No tiene imagenes entonces lo unico que guarda es la factura y coloca el movimiento como child");
                    guardaFacturaEnNube(Factura,basicInfo.getObjectId(),result);
                    return;
                }
                Calendar cal=Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                indexCloud=0;
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Subiendo "+String.valueOf(images.size())+ " imagenes");
                UpdloadImages("tickets/"+rfcInfo.getRfc_()+"/"+rfcInfo.getEmail_().substring(0,rfcInfo.getEmail_().indexOf("@"))+"/"+ String.valueOf(cal.get(Calendar.YEAR))+"/"+String.valueOf(cal.get(Calendar.MONTH))+"/",images, new onUploadResult() {//Sube las imagenes
                    @Override
                    public void onSucess(final List<descripimagenes> images) {//Regresa los objetos <descripimages> ya con la ruta de las imagenes en la nube
                        if(AnalyticsApplication.debug)Log.d("SyncAdapter","Las imagenes se han subido con exito, guardando las rutas en la nube ");
                        indexCloud=0;
                        savePathImagesInCloud(images, new onUploadResult() {//Guarda los objetos en la tabla de la nube
                            @Override
                            public void onSucess(List<descripimagenes> images) {//Regresa los objetos <descripimages> ya con el objectId,created,updated
                                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Las rutas se han guardado con exito ");
                                ArrayList<Map> children = new ArrayList<Map>();
                                for (descripimagenes image:images) {
                                    guardaImagenEnLocal(image);//Actualiza el objeto image en base de datos local
                                    HashMap<String, Object> childObject = new HashMap<String, Object>();
                                    childObject.put( "objectId",image.getObjectId());//Crea un objeto hijo para la relacion en la nube
                                    children.add(childObject);
                                }
                                HashMap<String, Object> parentObject = new HashMap<String, Object>();
                                parentObject.put( "objectId",basicInfo.getObjectId());//Crea un objeto padre para la relacion en la nube
                                Backendless.Data.of("basics").setRelation(parentObject, "images_", children, new AsyncCallback<Integer>() {//realacion basic[Padre]--->images[hijos]
                                    @Override
                                    public void handleResponse(Integer response) {///--->En este punto tabla basics y descripimages sincronizadas
                                       guardaFacturaEnNube(Factura,basicInfo.getObjectId(),result);
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        AnalyticsApplication.sendLogAsError("BackendlessFault",fault.getCode()+":"+fault.getMessage());
                                        apagaNotificacion();
                                        SyncAdapter.this.fault=fault.getCode()+":"+fault.getMessage();
                                        if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
                                    }
                                });

                            }

                            @Override
                            public void onError(String fault) {
                                AnalyticsApplication.sendLogAsError("SyncAdapterFault","Error al intetar guardar las rutas en la nube:"+fault);
                                apagaNotificacion();
                                SyncAdapter.this.fault=fault;
                                if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
                            }
                        });
                    }

                    @Override
                    public void onError(String fault) {
                        AnalyticsApplication.sendLogAsError("SyncAdapterFault","Error al intentar subir las imagenes: "+fault);
                        apagaNotificacion();
                        SyncAdapter.this.fault=fault;
                        if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                AnalyticsApplication.sendLogAsError("BackendlessFault",fault.getCode()+":"+fault.getMessage());
                apagaNotificacion();
                SyncAdapter.this.fault=fault.getCode()+":"+fault.getMessage();
                if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
            }
        });
    }
    public basics guardaBasicsEnLocal(final basics basicInfo){
        basics AccountReference=null;//Cuenta a la que se encontraba apuntando el movimiento en caso de que se ejecute la instruccion update
        //Aqui se agrgan todos los items, por el momento solo efectivo
        db= DBSmartWallet.getInstance(mcontext);
        if (basicInfo.getId_()==-1){
            //Ejecuta instruccion insertar
            AccountReference= (basics) basicInfo.clone();
            List<String> FieldsToExclude=new ArrayList<String>();
            FieldsToExclude.add("id_");//Se excluye id_ por que se autoicremnet en la base de datos
            String insertsentence=basicInfo.retriveInsertSentence(FieldsToExclude);
            db.getDBInstance().execSQL(insertsentence);
            long[] id=db.GetLongColumn("SELECT * FROM basics WHERE id_=last_insert_rowid()",null,"id_");
            AccountReference.setId_((int) id[0]);
        }else{
            //Ejecuta instruccion update
            basics[] result=db.getbasicDataMovements("SELECT * FROM basics WHERE id_="+basicInfo.getId_(),null);
            AccountReference=result[0];
            basicInfo.update(db,"id_='"+basicInfo.getId_()+"'");
        }
        return AccountReference;
    }
    public void UpdloadImages(final String Ruta,final List<descripimagenes> images, final onUploadResult Result){
        if(images.size()==0){
            Result.onSucess(images);//Si no hay imagenes simplemente continua con la siguiente seccion
            return;
        }
        if(images.get(indexCloud).getImagen_().toString().contains("content://")){//Uri image
            BackendlessFile file=UploadImageFromUri(Ruta,images.get(indexCloud).getImagen_().toString(),Result);
            if(file!=null){
                images.get(indexCloud).setImagenCloud_(file.getFileURL());
                indexCloud++;
                if(indexCloud <images.size()){
                    UpdloadImages(Ruta,images,Result);
                }else{
                    Result.onSucess(images);
                }
            }
        }else{
            if(AnalyticsApplication.debug)Log.d("SyncAdapter","Subiendo imagen desde camara: "+images.get(indexCloud).getImagen_().toString());
            Backendless.Files.upload(new File(images.get(indexCloud).getImagen_().toString()), Ruta,true, new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(BackendlessFile response) {
                    if(AnalyticsApplication.debug)Log.d("SyncAdapter","Se ha subido la imagen");
                    images.get(indexCloud).setImagenCloud_(response.getFileURL());
                    indexCloud++;
                    if(indexCloud <images.size()){
                        UpdloadImages(Ruta,images,Result);
                    }else{
                        Result.onSucess(images);
                    }
                }
                @Override
                public void handleFault(BackendlessFault fault) {
                    if(AnalyticsApplication.debug)Log.d("SyncAdapter","Ha ocurrido un error");
                    Result.onError(fault.getCode()+":"+fault.getMessage());
                }
            });
        }

    }
    BackendlessFile file=null;
    public BackendlessFile UploadImageFromUri(final String Ruta,final String imagePath,final onUploadResult Result){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Subiendo imagen desde: "+Ruta);
                final Uri myUri = Uri.parse(imagePath);
                OutputStreamRouter output= new OutputStreamRouter() {
                    @Override
                    public void writeStream(OutputStream outputStream) throws IOException {


                        InputStream input = null;
                        try {
                            input=mcontext.getContentResolver().openInputStream(myUri);
                        } catch (FileNotFoundException e) {
                            Result.onError("internal error:"+e.getMessage());
                        }

                        int data = input.read();
                        while(data != -1) {
                            outputStream.write(data);
                            data = input.read();
                        }
                    }
                };
                try {
                    String extension=getMimeType(mcontext,myUri);
                    file=Backendless.Files.uploadFromStream(output,imagePath.substring(imagePath.lastIndexOf("/")+1).replace("%","_")+"."+extension,Ruta,true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Result.onError("internal error:"+e.getMessage());
                }
                if(file!=null){
                    if(AnalyticsApplication.debug)Log.d("SyncAdapter","Imagen subida, ruta en la nube: "+file.getFileURL());
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return file;
    }
    public void savePathImagesInCloud(final List<descripimagenes> images,final onUploadResult Result){

        if(images.size()==0){
            Result.onSucess(images);
            return;
        }
        if(AnalyticsApplication.debug)Log.d("SyncAdapter","Guardando ruta en la nube:"+images.get(indexCloud).getImagenCloud_());
        Backendless.Data.save(images.get(indexCloud), new AsyncCallback<descripimagenes>() {
            @Override
            public void handleResponse(descripimagenes response) {
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Ruta guardada");
                images.get(indexCloud).setObjectId(response.getObjectId());
                images.get(indexCloud).setCreated(response.getCreated());
                images.get(indexCloud).setUpdated(response.getUpdated());
                indexCloud++;
                if(indexCloud <images.size()){
                    savePathImagesInCloud(images,Result);
                }else{
                    Result.onSucess(images);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if(AnalyticsApplication.debug)Log.d("SyncAdapter","Error");
                if (fault.getCode().matches("1000")){//Ya hay un objectId Asociado y no lo encuentra eb la nube
                    images.get(indexCloud).setObjectId(null);
                    savePathImagesInCloud(images,Result);//Quita el objectId y vuelve a intentarlo
                }else{
                    Result.onError(fault.getCode()+":"+fault.getMessage());
                }

            }
        });
    }
    public void guardaTagsEnLocal(int Id,List<String> tags){
        //Borra todos los tags actuales
        db.getDBInstance().execSQL("DELETE FROM tags WHERE id_=" + Id);
        //Inserta los ultimos tags
        for (String tag : tags) {
            eclipseapps.financial.moneytracker.cloud.tags T = new tags();
            T.setId_(Id);
            T.setTag_(tag);
            T.savein(db);
        }
    }
    public void remplazaImagesEnLocal(int Id,List<String> paths ){
        //Borra todas los imagenes actuales
        db.getDBInstance().execSQL("DELETE FROM descripimagenes WHERE id_=" + Id);
        if (paths != null && paths.size()>0) {
            //Inserta las ultimas imagenes
            for (String imagen : paths) {
                descripimagenes image = new descripimagenes();
                image.setId_(Id);
                image.setImagen_(imagen);
                image.savein(db);
            }
        }
    }
    public void guardaImagenEnLocal(descripimagenes image){
        if (db==null)db= DBSmartWallet.getInstance(mcontext);
        List<descripimagenes> result=db.select("SELECT * FROM descripimagenes WHERE imagen_='"+image.getImagen_()+"' AND id_="+image.getId_(),descripimagenes.class);
        if(result!=null && result.size()>0){
            //Ejecuta instruccion update
            image.update(db,"imagen_='"+image.getImagen_()+"' AND id_="+image.getId_());
        }else{
            //Ejecuta instruccion insertar
            db.getDBInstance().execSQL(image.retriveInsertSentence());
        }
    }
    public void guardaFacturanEnLocal(facturas factura){
        if (db==null)db= DBSmartWallet.getInstance(mcontext);
        List<facturas> result=db.select("SELECT * FROM facturas WHERE id_="+factura.getId_(),facturas.class);
        if(result!=null && result.size()>0){
            //Ejecuta instruccion update
            factura.update(db,"Id_='"+factura.getId_()+"'");
        }else{
            //Ejecuta instruccion insertar
            db.getDBInstance().execSQL(factura.retriveInsertSentence());
        }
    }
    public void guardaFacturaEnNube(facturas Factura, final String ObjectIdChild, final AsyncCallback result){
        Backendless.Data.save(Factura, new AsyncCallback<facturas>() {
            @Override
            public void handleResponse(final facturas facturaenNube) {
                guardaFacturanEnLocal(facturaenNube);//Actualiza la factura en base de datos local ya con los campos ObjectId,created y updated,esto lo hace una vez que en la nube ya se establecio la relacion con el movimiento a modo de hijo
                HashMap<String, Object> parentObject = new HashMap<String, Object>();
                parentObject.put( "objectId",facturaenNube.getObjectId());
                HashMap<String, Object> childObject = new HashMap<String, Object>();
                childObject.put( "objectId",ObjectIdChild);//basicInfo.getObjectId()
                ArrayList<Map> children = new ArrayList<Map>();
                children.add(childObject);
                Backendless.Data.of("facturas").setRelation(parentObject, "idbasic_", children, result);//realacion factura[Padre]--->basic[hijo]
                apagaNotificacion();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                AnalyticsApplication.sendLogAsError("BackendlessFault",fault.getCode()+":"+fault.getMessage());
                apagaNotificacion();
                SyncAdapter.this.fault=fault.getCode()+":"+fault.getMessage();
                if (listener!=null)listener.sendAction(ACTION_INTERRUPT);
            }
        });
    }
    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public cuentas actualizaSaldo(Double cantidad,cuentas Account){
        AnalyticsApplication.logD("SavingLocal","Actualizando saldo. saldo anterior:"+String.valueOf(Account.get_cantidad()));
        AnalyticsApplication.logD("SavingLocal","Actualizando saldo. cantidad a sumar:"+String.valueOf(cantidad));
        Account.set_cantidad(Account.get_cantidad() + cantidad);
        Account.update(db, "id_=" + Account.get_id());
        AnalyticsApplication.logD("SavingLocal","Actualizando saldo. nuevo saldo:"+String.valueOf(Account.get_cantidad()));
        return Account;
    }
    public cuentas actualizaSaldo(Double cantidad,String Account){
        cuentas cuenta=db.selectFirst("SELECT * FROM cuentas WHERE cuenta_='"+Account.replace("'","''")+"'",cuentas.class);
        if(cuenta!=null){
            return actualizaSaldo(cantidad,cuenta);
        }
        return cuenta;
    }
    public void enciendeNotificacion(boolean exito,String Mensaje){
        Log.d("SyncAdapter","Sincronizando contenido...");
        /*
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mcontext)
                        .setSmallIcon(exito?R.drawable.ic_action_good:R.drawable.logo)
                        .setContentTitle(mcontext.getString(R.string.app_name))
                        .setContentText(Mensaje);
        final NotificationManager mNotificationManager = (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());*/
    }
    public void apagaNotificacion(){
        Log.d("SyncAdapter","Terminando de sincronizar contenido...");
        /*
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) mcontext.getSystemService(ns);
        nMgr.cancel(001);*/
    }

   public void SyncMessages(){
       //Primero actualiza la base de datos de mensajes cada 7 dias
       Log.d("SyncAdapter","SyncMessages");
       final SharedPreferences sharedPreferences=sp;
       long lastTimeUpdated=sharedPreferences.getLong(preferences_lastTime_Messages_Updated,SystemClock.elapsedRealtime());
       long millisFrequency=1000*60*60*24*7;//Milisegundos en una semana
       if(lastTimeUpdated+millisFrequency>=SystemClock.elapsedRealtime()){
           Log.d("SyncAdapter","itsTimeTosync");
           boolean lock=sharedPreferences.getBoolean(onAlarmReceiver.lockAccessDBMessages,false);
           Log.d("SyncAdapter","checkingForLock:"+String.valueOf(lock));
           if(!lock){//Si no hay candado
               Log.d("SyncAdapter","Access to update");
               onAlarmReceiver.setLock(sharedPreferences);
               Looper.prepare();
               DownloadReceiver DR=new DownloadReceiver(new Handler(Looper.getMainLooper())) {
                   @Override
                   public void onFinishDownload(String[] pathsToFiles) {//Se ha terminado de actualizar
                       updateLastTimeUpdated(sharedPreferences);
                       DBMessages messages=DBMessages.getInstance(mcontext);
                       int version=messages.getVersion();
                       Tracker mTracker= AnalyticsApplication.getDefaultTracker(mcontext);
                       if(mTracker!=null)mTracker.send(new HitBuilders.EventBuilder()
                               .setCategory(AnalyticsApplication.Usability.NotificationRetention.getValue())
                               .setAction("Update Messages")
                               .setLabel(String.valueOf(version))
                               .build());
                       Log.d("SyncAdapter","UpdateCompleted");
                       onAlarmReceiver.removeLock(sharedPreferences);
                   }

                   @Override
                   public void OnErrorDownload(String urlToFile) {

                      //Aqui no actualiza el tiempo updateLastTime para que lo intente la siguiente vez inmediata
                       DBMessages messages=DBMessages.getInstance(mcontext);
                       int version=0;
                       try{
                           version=messages.getVersion();
                       }catch (SQLiteException e){

                       }
                       Tracker mTracker=AnalyticsApplication.getDefaultTracker(mcontext);
                       if(mTracker!=null)mTracker.send(new HitBuilders.EventBuilder()
                               .setCategory(AnalyticsApplication.Usability.NotificationRetention.getValue())
                               .setAction("Update Messages Error")
                               .setLabel(String.valueOf(version))
                               .build());
                       Log.d("SyncAdapter","onErrorUpdate");
                       onAlarmReceiver.removeLock(sharedPreferences);
                   }
               };
               String[] urlMessage=new String[]{"https://api.backendless.com/C1D72711-B7EB-98DD-FFC7-23418D485000/0756ACFC-9311-6E20-FF8E-428B4BCC5A00/files/messages/Mensajes.sqlite"};
               Intent downLoadFileIntent= SyncService.getDownLoadFileIntent(mcontext,urlMessage,"/databases/",DR);
               JobIntentService.enqueueWork(mcontext,SyncServiceAux.class,100,downLoadFileIntent);
              // mconteuxt.startService(downLoadFileIntent);
           }
       }
   }
    public void SyncDBInDrive(final OnSuccessListener<Void> onSuccess){
        //Primero actualiza la base de datos de mensajes cada 7 dias
        AnalyticsApplication.logD("SyncAdapter","SyncDBInDrive");
        final SharedPreferences sharedPreferences=sp;
        long lastTimeUpdated=sharedPreferences.getLong(preferences_lastTime_BackUpOnDrive,SystemClock.elapsedRealtime());
        long millisFrequency=1000*60*2;//60*24;//Milisegundos en un día//PI_DEBUG
        if(lastTimeUpdated+millisFrequency>=SystemClock.elapsedRealtime()){
            AnalyticsApplication.logD("SyncAdapter","Is time to BackUp");
            GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(getContext());
            if(account!=null && !account.isExpired()){
                // Use the authenticated account to sign in to the Drive service.
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                                getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());

                Drive googleDriveService =
                        new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("Denario")
                                .build();

                // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                // Its instantiation is required before handling any onClick actions.
                final DriveServiceHelper mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

                if (mDriveServiceHelper != null) {
                    searchForAlreadyDB(mDriveServiceHelper, new OnSuccessListener<com.google.api.services.drive.model.File>() {
                        @Override
                        public void onSuccess(com.google.api.services.drive.model.File file) {
                            if(file==null){
                                createDBBackUp(mDriveServiceHelper,onSuccess);
                            }else{
                                savingDB(mDriveServiceHelper,file.getId(),onSuccess);
                            }
                        }
                    });

                }
            }
        }
    }
    public void searchForAlreadyDB(final DriveServiceHelper mDriveServiceHelper, final OnSuccessListener<com.google.api.services.drive.model.File> DBListener){
        AnalyticsApplication.logD("Drive","Searching for already DB");
        mDriveServiceHelper.queryFiles()
                .addOnSuccessListener(new OnSuccessListener<FileList>() {
                    @Override
                    public void onSuccess(FileList fileList) {
                        com.google.api.services.drive.model.File db=null;
                        for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                            if(file.getName().matches("DBSmartWallet.db3"))db=file;
                        }
                        DBListener.onSuccess(db);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e!=null&& e.getMessage()!=null)
                            AnalyticsApplication.sendLogAsError("Unable to query files.",e.getMessage());
                    }
                });
    }
    public void createDBBackUp(final DriveServiceHelper mDriveServiceHelper, final OnSuccessListener<Void> onSucess){
        AnalyticsApplication.logD("Drive","Creating a file");
        mDriveServiceHelper.createFile("appDataFolder")
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String mFileId) {
                        savingDB(mDriveServiceHelper,mFileId,onSucess);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AnalyticsApplication.sendLogAsError("Create Drive File",e.getMessage());
                    }
                });
    }
    public void savingDB(DriveServiceHelper mDriveServiceHelper,String mFileId,OnSuccessListener<Void> onSucess){
        if (mFileId != null) {
            AnalyticsApplication.logD("handleSignInResult","Saving content to file");

            String fileName = "DBSmartWallet.db3";
            String filename="/data/data/eclipseapps.financial.moneytracker/databases/DBSmartWallet.db3";
            File filelocation = new File(filename);
            if(filelocation.exists()) AnalyticsApplication.logD("handleSignInResult","Database Exist on path");

            mDriveServiceHelper.saveFile(mFileId, fileName, filelocation)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AnalyticsApplication.sendLogAsError("handleSignInResult","Unable to save file via REST:"+e.getMessage());
                        }
                    })
                    .addOnSuccessListener(onSucess);


        }
    }
    private void updateLastTimeUpdated(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(preferences_lastTime_Messages_Updated, SystemClock.elapsedRealtime());
        editor.commit();
    }
    private void updateLastTimeUpdatedForDriveBackup(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(preferences_lastTime_BackUpOnDrive, SystemClock.elapsedRealtime());
        editor.commit();
    }

    public interface onUploadResult{
        public void onSucess(List<descripimagenes> imagesInCloud);
        public void onError(String fault);
    }

    public class syncPreference{
        public final static String lastMessageUpdate="lastMessageUpdate";
    }

}
