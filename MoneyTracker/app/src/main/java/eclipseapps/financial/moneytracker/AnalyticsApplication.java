package eclipseapps.financial.moneytracker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.FileProvider;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import eclipseapps.android.ActivityN;
import eclipseapps.financial.moneytracker.interfaces.DownloadReceiver;
import eclipseapps.financial.moneytracker.sync.SyncService;
import eclipseapps.library.databases.Dir;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static eclipseapps.financial.moneytracker.AnalyticsApplication.Write.Delete;
import static eclipseapps.financial.moneytracker.interfaces.onAlarmReceiver.removeLock;
import static eclipseapps.financial.moneytracker.interfaces.onAlarmReceiver.setAlarms;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class AnalyticsApplication extends MultiDexApplication {
    public static boolean debug=true;
    private final  static String hasOpenedBefore="hasOpenedBefore";
    public static Tracker mTracker;

   // @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();


        if(!Backendless.isInitialized()){
            Backendless.initApp(this,"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
        }
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                .withContinueSessionMillis(10000)
                .withLogLevel(Log.VERBOSE)
                .build(this, "497XYR6TR3M564S8W2F6");
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("eclipseapps.financial.moneytracker",MODE_PRIVATE);
        boolean OpenedBefore=sharedPreferences.getBoolean(hasOpenedBefore,false);
        if(!OpenedBefore || BuildConfig.DEBUG){
            String pathTo= this.getApplicationInfo().dataDir+"/databases/";
            Dir.copyAssetsResourceToInternalDir(this,pathTo,"Mensajes.sqlite");
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean(hasOpenedBefore,true);
            editor.apply();
            removeLock(sharedPreferences);
            setAlarms(this);//Al ser la primera vez que se inicia la app se setean las alarmas
        }
        //Se setea por defaul las notificaciones en true
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.contains("notifications")){
            SharedPreferences.Editor editor=preferences.edit();
            editor.putBoolean("notifications", true);
            editor.apply();
        }
        File cacheDir = StorageUtils.getOwnCacheDirectory(
                getApplicationContext(),this.getApplicationInfo().dataDir+"cache_images");

                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true).cacheOnDisc(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(options)
                .diskCache(new LimitedAgeDiskCache(cacheDir, 100))
                .build();

        ImageLoader.getInstance().init(config);
        sharedPreferences.edit().putLong("Sessions",sharedPreferences.getLong("Sessions",0)+1).apply();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SharedPreferences sp = getApplicationContext().getSharedPreferences("eclipseapps.financial.moneytracker", MODE_PRIVATE);
        /*
        if(sp.getLong("Sessions",0)%7==0 && !sp.getBoolean("hasRated",false)){
            showRatingDialog();
        }else{
            super.onDestroy();
        }*/
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized static public Tracker getDefaultTracker(Context context) {
        boolean debug=BuildConfig.DEBUG;
        if (mTracker == null &&  !debug) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAdvertisingIdCollection(true);
        }
        return mTracker;
    }
    synchronized static public void startDefaultTracker(Context context) {
        boolean debug=BuildConfig.DEBUG;
        if (mTracker == null &&  !debug) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAdvertisingIdCollection(true);
        }
    }

    /**
     * Metodo que coloca el trace en google analitycs y el log de manera local
     * @param event
     * @param trace
     */
    public static void sendLogAsError(String event,String trace){
        if(AnalyticsApplication.debug)Log.d(event,trace);
        if (!BuildConfig.DEBUG && mTracker!=null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Error")
                    .setAction(event)
                    .setLabel(trace)
                    .build());
        }
    }
    public static void sendTrack(String category,String Action,String Tag){//mTrakcer puede ser mull si la app esta en debug mode
        if (!BuildConfig.DEBUG && mTracker!=null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(Action)
                    .setLabel(Tag)
                    .build());
        }
    }
    public static void writeMovementsTracking(AnalyticsApplication.Write Category, AnalyticsApplication.Action Action, @Nullable String Tag){
        if(Category.getValue().matches(Delete.getValue()) || Category.getValue().matches(AnalyticsApplication.Write.Update.getValue())){
            eclipseapps.financial.moneytracker.activities.trackedActivity.sp.edit().putLong(Category.getValue(), eclipseapps.financial.moneytracker.activities.trackedActivity.sp.getLong(Category.getValue(),0)+1).apply();
            AnalyticsApplication.sendTrack(Category.getValue(),Action.getValue(),String.valueOf(eclipseapps.financial.moneytracker.activities.trackedActivity.sp.getLong(Category.getValue(),0)));
        }else{
            AnalyticsApplication.sendTrack(Category.getValue(),Action.getValue(),Tag);
        };
    }

    @Override

    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);

        MultiDex.install(this);

    }
    public static void logD(String Header,String Message){
        if(debug) Log.d(Header,Message);
    }
    public static final int TweetRequest=666;
    static ResolveInfo TwitterInfo;
    public static void shareAppOnTwitter(final ActivityN context) {
        boolean resolved = false;
        PackageManager packManager = context.getPackageManager();
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, "This is a Test.");
        tweetIntent.setType("text/plain");
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.contains("twitter")){
                TwitterInfo=resolveInfo;
                resolved = true;
                break;
            }
        }
        if(!resolved){
            Toast.makeText(context, "No se encontro la app de Twitter", Toast.LENGTH_LONG).show();
            return;
        }
        String[] urls=new String[]{
                "https://api.backendless.com/C1D72711-B7EB-98DD-FFC7-23418D485000/0756ACFC-9311-6E20-FF8E-428B4BCC5A00/files/media/PromoTwitter/promoTwitter1.png",
                "https://api.backendless.com/C1D72711-B7EB-98DD-FFC7-23418D485000/0756ACFC-9311-6E20-FF8E-428B4BCC5A00/files/media/PromoTwitter/ad.txt"
        };

        DownloadReceiver DR= new DownloadReceiver(new Handler()) {
            @Override
            public void onFinishDownload(String[] fileNames) {
                try {
                    //Get the text file
                    File tweetFile = new File(fileNames[1]);

                    //Read text from file
                    StringBuilder text = new StringBuilder();

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(tweetFile));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                    }
                    //Prepare File
                    File imageFile = new File(fileNames[0]);
                    imageFile.setReadable(true, false);

                    //share file
                    Uri photoURI = FileProvider.getUriForFile(context,
                            "eclipseapps.financial.moneytracker.fileprovider",
                            imageFile);

                    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                    //hareIntent.setDataAndType(photoURI, "image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    shareIntent.putExtra(Intent.EXTRA_TEXT,text.toString());
                    shareIntent.setType("image/*");
                    if(TwitterInfo!=null){
                        shareIntent.setClassName(TwitterInfo.activityInfo.packageName, TwitterInfo.activityInfo.name);
                    }
                    context.startActivityForResult(shareIntent,TweetRequest);

                }
                catch (Exception e) { Toast.makeText(context, "error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void OnErrorDownload(String urlToFile) {
                //UpdateRequired
            }
        };
        DR.setmProgressDialog((context).wait("Preparando Tweet...",false));
        Intent downLoadIntent= SyncService.getDownLoadFileIntent(context,urls,"/files/marketing/",DR);
        context.startService(downLoadIntent);
    }

    public enum Usability{
        Interaction("Interaction"), NotificationRetention("Notification_Retention"),UserExperience("UserExperience"),
        Features("Features");
        Usability(String interaction) {
            value=interaction;
        }
        private String value;
        public String getValue() {
            return value;
        }
    }

    public enum Gestures{//Action for usability
        Click("Click"),LongClick("LongClick"),LeftSwipe("LeftSwipe"),RightSwipe("RightSwipe"),UpSwipe("UpSwipe"),downSwipe("downSwipe");
        Gestures(String gesture) {
            value=gesture;
        }
        private String value;
        public String getValue() {
            return value;
        }
    }

    public enum SourceUsers{
        Notification("Notification");
        SourceUsers(String source) {value=source;}
        private String value;
        public String getValue() {
            return value;
        }
    }

    public enum Write {
        Create("Create"), Update("Update"),Delete("Delete");
        Write(String v) {
            value = v;
        }
        private String value;
        public String getValue() {
            return value;
        }
    }


    public enum Action {
        Movement("Movement"), Image("Image"),Tag("Tag"),Met("Met"),Factura("Factura");
        Action(String v) {
            value = v;
        }
        private String value;
        public String getValue() {
            return value;
        }
    }

    public enum Tag {
        FromGallery("FromGallery"), FromCamera("FromCamera");
        Tag(String v) {
            value = v;
        }
        private String value;
        public String getValue() {
            return value;
        }
    }
}
