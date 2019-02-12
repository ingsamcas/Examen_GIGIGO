package eclipseapps.financial.moneytracker.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import eclipseapps.android.FragmentN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.cloud.ventas;
import eclipseapps.financial.moneytracker.fragments.rewardedFragment;
import eclipseapps.financial.moneytracker.utils.IabBroadcastReceiver;
import eclipseapps.financial.moneytracker.utils.IabHelper;
import eclipseapps.financial.moneytracker.utils.IabResult;
import eclipseapps.financial.moneytracker.utils.Inventory;
import eclipseapps.financial.moneytracker.utils.Purchase;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.RandomString;
import eclipseapps.libraries.library.general.functions.Timers;
import eclipseapps.libraries.library.general.functions.general;

import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.Payload;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.Premium;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumPrice;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumPriceDiscountMicros;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.PremiumPriceMicros;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.SKU_PREMIUM;
import static eclipseapps.financial.moneytracker.cloud.ventas.sellsPreferences.SKU_PREMIUM_Discount;
import static eclipseapps.financial.moneytracker.cloud.ventas.states.Compra_exitosa;


/**
 * Created by usuario on 07/03/18.
 */

public abstract class baseActivity extends trackedActivity implements IabBroadcastReceiver.IabBroadcastListener {
    public static Location location;

    IabHelper mHelper;// The helper object
    IabBroadcastReceiver mBroadcastReceiver;// Provides purchase notification while this app is running
    //

    // Does the user have the premium upgrade?
    public static boolean checkingForPremium=false;
    public static boolean mIsPremium = false;
    Thread checkForPremium;

    private boolean requestResultNow=false;
    private boolean isRegistred=false;
    Timers timerForCheckPremium;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsPremium=sp.getBoolean(Premium,false);
        if(!mIsPremium && general.isOnline()){
            timerForCheckPremium=new Timers(2500, new TimerTask() {
                @Override
                public void run() {

                    checkForPremiumNow();AnalyticsApplication.logD("baseActivity","Checking for Premium");
                }
            });
            timerForCheckPremium.Start();
        }


    }

    @Override
    protected void onStop() {
        if(mBroadcastReceiver!=null && isRegistred){

            unregisterReceiver(mBroadcastReceiver);
        }
        if(timerForCheckPremium!=null){
            timerForCheckPremium.Stop();
        }
        super.onStop();

    }

    public void checkForPremiumNow(){
        checkForPremium=new Thread(new Runnable() {
            @Override
            public void run() {
                checkingForPremium=true;
                if(mHelper==null || !mHelper.isSetupDone()){
                    launchInAppBilling(new FragmentN.Action() {
                        @Override
                        public Object execute(Intent data) {//Una vez que finaliza el setup de mHelper ejecuta la siguiente accion:
                            if(data!=null){//data==null==Error
                                //Request inventory
                                reviewFromInventoryIfIsPremium();// Comprueba desde el servidor de google si es que compro el upgrade
                            }else{
                                isPremium(false);//Como no pudo arrancar el setup(por ejemplo por falta de internet) no puede comprobar si es premium y lo considera usuario normal

                            }
                            return null;
                        }
                    });
                }else{
                    //Request inventory
                    reviewFromInventoryIfIsPremium();
                }

            }
        });
        checkForPremium.start();
    }
    public rewardedFragment getRewardedFragment(){
        Fragment f=null;
        FragmentManager FM=getSupportFragmentManager();
        f=FM.findFragmentByTag(rewardedFragment.Name);
        if (f==null) {
            f = new rewardedFragment();
            FM.beginTransaction().add(f, rewardedFragment.Name).commitAllowingStateLoss();
        }
        return (rewardedFragment) f;
    }

    public void askForPremium(){
        askForPremium(null);
    }
    public void askForPremium(final String tag){
        Toast.makeText(this,"Cargando...",Toast.LENGTH_LONG).show();
        getPremiumPrice(new FragmentN.Action() {
            @Override
            public Object execute(Intent data) {
                Intent intent=new Intent(baseActivity.this,dialogActivity.class);
                if(data!=null && data.getStringExtra(PremiumPrice)!=null){
                    intent.putExtra(PremiumPrice,data.getStringExtra(PremiumPrice));
                    intent.putExtra(PremiumPriceDiscountMicros,data.getLongExtra(PremiumPriceDiscountMicros,0));
                    intent.putExtra(PremiumPriceMicros,data.getLongExtra(PremiumPriceMicros,0));
                    SellsTracking("askForPremium",data.getStringExtra(PremiumPrice));
                    intent.setAction(dialogActivity.action_PremiumDialog);
                    startActivityForResult(intent, ventas.sellsPreferences.PREMIUM_REQUEST);
                }else{
                    Toast.makeText(baseActivity.this,"Es necesario tener Google Play instalado y una conexion a internet...",Toast.LENGTH_LONG).show();
                    SellsTracking("askForPremium",tag!=null?tag:"NoPriceReturned");
                }
                return null;
            }
        });
    }
    public void getPremiumPrice(final FragmentN.Action action){
        if(mHelper==null){
            requestResultNow=true;//El usuario exige el precio, si ahy algun error debe devolver un mensaje
            launchInAppBilling(new FragmentN.Action() {
                @Override
                public Object execute(Intent data) {//Una vez que finaliza el setup de mHelper ejecuta la siguiente accion:
                    requestPrice(action);
                    return null;
                }
            });
        }else{
            requestPrice(action);
        }
    }
    private void requestPrice(final FragmentN.Action action){
        //Request inventory
        try {
            ArrayList<String> skuList = new ArrayList<String>();
            skuList.add(SKU_PREMIUM);
            skuList.add(SKU_PREMIUM_Discount);
            mHelper.queryInventoryAsync(true, skuList, null, new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                    final Intent data=new Intent();
                    if(inv!=null && inv.getSkuDetails(SKU_PREMIUM)!=null){
                        data.putExtra(PremiumPrice,inv.getSkuDetails(SKU_PREMIUM).getPrice());
                        data.putExtra(PremiumPriceMicros,inv.getSkuDetails(SKU_PREMIUM).getPriceAmountMicros());
                        data.putExtra(PremiumPriceDiscountMicros,inv.getSkuDetails(SKU_PREMIUM_Discount).getPriceAmountMicros());
                    }
                    action.execute(data);
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            sendLogAsError("Inventory",e.getMessage());
        }
    }
    public static boolean isTest(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getBoolean("Developer", false);
    }
    public synchronized void launchInAppBilling(final FragmentN.Action action){
        AnalyticsApplication.logD("baseActivity","Setup AppBilling");
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp3I2OMP7tBJwpg" +
                "fI61La9tmLEPqDLAH3N0fVjTSjMC1dMhxG1gU8DpralecvU4MfVyVfXPPKnM/KVXkbYLIfLX8sPt+F7F0Uh" +
                "gOod/tXQ55QnnZarE3ly/oiJbNRMUI8dUNz4NN2ZO7VIWulXyVG7a4Jw1gfVJHgbwEuPY4qAwm/Pu8hbTbz" +
                "jjHuXQxkZxyrQoRMpLZtGAY5SySeVS9UJUV3JupfoMgp4ovtO+Wenncs/jjS+klUryZJBCxQCC/01Lh+a+V" +
                "6Wqd6MicArLKeeDEMjMfFcys76l9W4XH1cSEmv4mDx6DcfQcbZ98vEPYkaPNBhcs3RhtJtB+49amtwQIDAQAB";
        // Create the helper, passing it our context and the public key to verify signatures with

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("Premium","InAppBillingFault:"+result.getMessage());
                    sendLogAsError("Setup",result.getMessage());
                    action.execute(null);//null==error
                    dismisswait();
                    if(requestResultNow){
                        showOkDialog("Ups hay un problema, revisa tu conexion a internet y vuelve a intentarlo","ok",true,null);
                        requestResultNow=false;
                    }

                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null){
                    AnalyticsApplication.sendLogAsError("Purchasing","InAppBillingDisposed");
                    action.execute(null);//null==error
                    dismisswait();
                    return;
                }

                //Listener for messages
                if(!isRegistred){
                    mBroadcastReceiver = new IabBroadcastReceiver(baseActivity.this);
                    IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                    registerReceiver(mBroadcastReceiver, broadcastFilter);
                }
                action.execute(new Intent());
            }
        });
    }
    private void reviewFromInventoryIfIsPremium(){
        try {
            mHelper.queryInventoryAsync(mGotInventoryForPremiumListener );

        } catch (IabHelper.IabAsyncInProgressException e) {
            sendLogAsError("Inventory",e.getMessage());
            isPremium(false);//Si hay error lo considera usuario normal
        }
    }
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryForPremiumListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            AnalyticsApplication.logD("baseActivity","Querying inventory");
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null){
                AnalyticsApplication.logD("baseActivity","mHelper == null");
                isPremium(false);
                return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                AnalyticsApplication.logD("baseActivity","InAppBillingFault:Failed to query inventory:"+result);
                sendLogAsError("Inventory",result.getMessage());
                dismisswait();
                isPremium(false);
                return;
            }

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(ventas.sellsPreferences.SKU_PREMIUM);
            if(premiumPurchase==null)premiumPurchase=inventory.getPurchase(ventas.sellsPreferences.SKU_PREMIUM_Discount);
            mIsPremium = (premiumPurchase != null && premiumPurchase.getPurchaseState()==0);//2:INAPP_PURCHASE_STATE_REFUNDED
            AnalyticsApplication.logD("baseActivity","Premium_"+String.valueOf(mIsPremium));
            sp.edit().putBoolean(Premium,mIsPremium).apply();
            dismisswait();
            isPremium(mIsPremium);

        }
    };
    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
    }
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        if(sp==null)sp= getApplicationContext().getSharedPreferences("eclipseapps.financial.moneytracker",MODE_PRIVATE);
        String payloadSavedOnMyServer=sp.getString(Payload,"");
        if(payloadSavedOnMyServer.matches(p.getDeveloperPayload())){
            return true;//Verificado
        }else{
            ventas venta=new ventas();
            List<ventas> result=venta.findSyncInCloud("orderId='"+p.getOrderId()+"'");
            if(result!=null && result.size()>0){
                venta=result.get(0);
            }
            if(venta.getObjectId()!=null && venta.getObjectId().matches(p.getDeveloperPayload())){
                sp.edit().putString(Payload,p.getDeveloperPayload());//Se guarda el string para que en un futuro no sea necesario conectarse al servidor
                return true;//Verificado
            }else{
                return false;//No se ha podio verificar e pedido ni localmente ni en la nube de backendless
            }
        }
    }
    ventas venta;
    // User clicked the "Upgrade to Premium" button.
    public void onUpgrade(final boolean isOffer) {
        if(!general.isOnline()){
            AnalyticsApplication.sendLogAsError("Purchasing","No Internet");
            showOkDialog("Sin conexion a internet.\nRevisa tu conexion y vuelve a intentarlo.","ok",true,null);
            return;
        }else{
            wait("espere...",false);
        }
        if(mHelper==null){
            requestResultNow=true;//Si hay algun error en el setup entonces si debe devolver un mensaje
            launchInAppBilling(new FragmentN.Action() {
                @Override
                public Object execute(Intent data) {
                    if(data!=null){ //No Error
                        onUpgrading(isOffer);
                    }else{
                        Toast.makeText(baseActivity.this,
                                "Problema al intentar conectarse con el servidor, verifica tu conexion",
                                Toast.LENGTH_LONG).show();
                    }
                    return null;
                }
            });
        }else{
            onUpgrading(isOffer);
        }
    }
    private void onUpgrading(final boolean isOffer){
        try {
            mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null ){
                        AnalyticsApplication.sendLogAsError("Purchasing","InAppBillingDisposed");
                        return;
                    }

                    // Do we have the premium upgrade?
                    Purchase premiumPurchase = inventory.getPurchase(ventas.sellsPreferences.SKU_PREMIUM);
                    if(premiumPurchase==null)premiumPurchase=inventory.getPurchase(ventas.sellsPreferences.SKU_PREMIUM_Discount);
                    if(premiumPurchase!=null){
                        if(premiumPurchase.getSku().matches("android.test.purchased")|| premiumPurchase.getPurchaseState()==2) {//Si es un SKU de prueba se consume antes//PI_DEBUG
                            try {
                                mHelper.consumeAsync(premiumPurchase , new IabHelper.OnConsumeFinishedListener() {
                                    @Override
                                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                                        if(result.isSuccess()){
                                            mIsPremium=false;
                                            isPremium(false);
                                            sp.edit().putBoolean(Premium,mIsPremium).apply();
                                            purchasingPremium(isOffer);
                                        }else{
                                            AnalyticsApplication.sendLogAsError("Purchasing","CanNotPurchaseTest");
                                        }
                                    }
                                });
                            } catch (IabHelper.IabAsyncInProgressException e) {
                                e.printStackTrace();
                                AnalyticsApplication.sendLogAsError("Purchasing",e.getMessage());
                            }
                        }else {//El usurio ya posee el item, senillamente se le informa y se coloca mIsPremium=true
                            dismisswait();
                            mIsPremium = true;
                            sp.edit().putBoolean(Premium,mIsPremium).apply();
                            showOkDialog("Ya eres premium, esperamos disfrutes de la app","ok",true,null);
                            AnalyticsApplication.sendLogAsError("Purchasing","UserAlreadyPremium");
                        }
                    }else{
                       purchasingPremium(isOffer);
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            AnalyticsApplication.sendLogAsError("Purchasing",e.getMessage());
        }

    }
    private void purchasingPremium(final boolean isOffer){


        venta=new ventas();

        venta.setOrderId_(new RandomString().nextString());//Se crea la orden con un aleatorio temporal.
        Backendless.Data.save(venta, new AsyncCallback<ventas>() {
            @Override
            public void handleResponse(ventas response) {
                venta =response;//El payload es el objectId generado por backendless
                try {
                    String pricetype=ventas.sellsPreferences.SKU_PREMIUM;
                    if(isOffer){
                        pricetype=SKU_PREMIUM_Discount;
                    }
                    mHelper.launchPurchaseFlow(baseActivity.this, pricetype, ventas.sellsPreferences.RC_REQUEST,
                            mPurchaseFinishedListener, venta.getObjectId());
                } catch (IabHelper.IabAsyncInProgressException e) {
                    venta.setEstado_(e.getMessage());
                    Backendless.Data.save(venta,null);
                    dismisswait();
                    showOkDialog("Ha ocurrido un problema, no se ha realizado ningun cobro","ok",true,null);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                sendLogAsError("Purchasing:BackendlessFault",fault.getMessage());
                dismisswait();
                showOkDialog("Ha ocurrido un problema, no se ha realizado ningun cobro","ok",true,null);
            }
        });

    }
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener  mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, final Purchase purchase) {
            venta.setOrderId_("");
            if(purchase!=null)venta.setOrderId_(purchase.getOrderId());
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()&&(purchase==null || !purchase.getSku().matches("android.test.purchased"))){
                venta.setExtraInfo_(result.getMessage());
                sendLogAsError("Purchasing",result.getMessage());
                dismisswait();
                String messageError="Error al momento de realizar la compra.";
                if(result.getResponse()==-1003){
                    messageError="No se ha podido verificar la firma de su compra.\n";
                }else if(result.getResponse()==-1008){
                    messageError="Su pedido fue cancelado por Google Payments.\n";
                }else if(result.getResponse()==-1005){
                    messageError="El pedido no esta disponible por el momento..\n"+"No se realizo ningun cargo.";
                    showOkDialog(messageError,"ok",false,null);
                    return;
                }
                showOkCancelDialog(messageError +
                        "Si su cuenta fue cargada puede solicitar un reembolso.", "Solicitar reembolso", "No por ahora", false, new Dialogs.DialogsInterface() {
                    @Override
                    public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                        if (sucess){
                            askForRefound(venta);
                        }else{
                            if(venta.getOrderId_().matches("")){
                                showOkDialog("Tiene 3 días para solicitar cualquier aclaración a eclipseappslatam@gmail.com\n" ,"ok",true,null);
                            }else{
                                showOkDialog("Tiene 3 días a partir de su compra para solicitar un reembolso a eclipseappslatam@gmail.com\n" +
                                        "Su numero de pedido es " + venta.getOrderId_(),"ok",true,null);
                            }

                        }
                    }
                });
                return;
            }

            if (purchase.getSku().equals(ventas.sellsPreferences.SKU_PREMIUM)||purchase.getSku().equals(ventas.sellsPreferences.SKU_PREMIUM_Discount) ) {
                venta.setOrderId_(purchase.getOrderId());
                venta.setEstado_(Compra_exitosa);
                Backendless.Data.save(venta, new AsyncCallback<ventas>() {
                    @Override
                    public void handleResponse(ventas response) {
                        dismisswait();
                        mIsPremium = true;
                        sp.edit().putBoolean(Premium,mIsPremium).apply();
                        venta.setEstado_(response.getEstado_());
                        venta.setOrderId_(response.getOrderId_());
                        venta.setCreated(response.getCreated());
                        venta.setObjectId(response.getObjectId());
                        venta.setUpdated(response.getUpdated());
                        venta.setExtraInfo_(response.getExtraInfo_());
                        venta.savein(db);
                        SellsTracking("UpgradePremium",purchase.getSku().equals(ventas.sellsPreferences.SKU_PREMIUM)?10:5);
                        showOkDialog("Gracias por subir a premium, esperamos disfrutes la app","ok",true,null);
                        isPremium(true);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        sendLogAsError("UpdateOrderId",fault.getMessage());
                        dismisswait();
                        mIsPremium = true;
                        sp.edit().putBoolean(Premium,mIsPremium);
                        SellsTracking("UpgradePremium",purchase.getSku().equals(ventas.sellsPreferences.SKU_PREMIUM)?10:5);
                        showOkDialog("Gracias por subir a premium, esperamos disfrutes de la app","ok",true,null);
                        isPremium(true);
                    }
                });

            }
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("InAppBilling", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }else {
            Log.d("InAppBilling", "onActivityResult handled by IABUtil.");
        }
    }
    Button positiveButton;
    public void askForRefound(final ventas venta){
        final EditText edittext = new EditText(this);
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(positiveButton!=null)positiveButton.setEnabled(isValidEmail(edittext.getText()));
            }
        });
        edittext.setHint("micuenta@micorreo.com");
        AlertDialog alert = new AlertDialog.Builder(this)
        .setCancelable(false)
        .setMessage("En caso de necesitar comunicarnos con usted ingrese un correo.")
        .setTitle("Reembolso")
        .setView(edittext)
        .setPositiveButton("Solicitar reembolso", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                venta.setEstado_(ventas.states.Solicitud_Reembolso);
                venta.setExtraInfo_(edittext.getText().toString()+"//"+venta.getExtraInfo_());
                if(!Backendless.isInitialized()){
                    Backendless.initApp(this,"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
                }
                Backendless.Data.save(venta, new AsyncCallback<ventas>() {
                    @Override
                    public void handleResponse(ventas response) {
                        venta.setEstado_(response.getEstado_());
                        venta.setOrderId_(response.getOrderId_());
                        venta.setCreated(response.getCreated());
                        venta.setObjectId(response.getObjectId());
                        venta.setUpdated(response.getUpdated());
                        venta.setExtraInfo_(response.getExtraInfo_());
                        venta.savein(db);
                        showOkDialog("Tu solicitud ha sido enviada,en breve recibiras respuesta. Dudas y aclaraciones" +
                                " comunicate con nosotros a eclipseappslatam@gmail.com","ok",true,null);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        sendLogAsError("RefaundFault",fault.getMessage());
                        showOkDialog("Ha ocurrido un problema, revisa tu conexion a internet y vuelve a intentarlo" +
                                " o comunicate con nosotros a eclipseappslatam@gmail.com","ok",true,null);
                    }
                });
            }
        })
        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(venta.getOrderId_().matches("")){
                    showOkDialog("Tiene 3 días para solicitar cualquier aclaración a eclipseappslatam@gmail.com\n" ,"ok",true,null);
                }else{
                    showOkDialog("Tiene 3 días a partir de su compra para solicitar un reembolso a eclipseappslatam@gmail.com\n" +
                            "Su numero de pedido es " + venta.getOrderId_(),"ok",true,null);
                }

            }
        })
        .create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                positiveButton = ((AlertDialog) dialog)
                        .getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(false);
            }
        });
        alert.show();
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public void askForPremiumFeature(){
        final EditText edittext = new EditText(this);
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(positiveButton!=null)positiveButton.setEnabled(edittext.getText()!=null && !edittext.getText().toString().matches(""));
            }
        });
        edittext.setHint("Escribe aqui tu petición");
        AlertDialog alert = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Que caracteristica te gustaria que agregaramos? Tu mandas.")
                .setTitle("Hola Premium")
                .setView(edittext)
                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        venta.setEstado_(ventas.states.Solicitud_Reembolso);
                        venta.setExtraInfo_(edittext.getText().toString()+"//"+venta.getExtraInfo_());
                        if(!Backendless.isInitialized()){
                            Backendless.initApp(this,"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
                        }
                        Backendless.Data.save(venta, new AsyncCallback<ventas>() {
                            @Override
                            public void handleResponse(ventas response) {
                                venta.setEstado_(response.getEstado_());
                                venta.setOrderId_(response.getOrderId_());
                                venta.setCreated(response.getCreated());
                                venta.setObjectId(response.getObjectId());
                                venta.setUpdated(response.getUpdated());
                                venta.setExtraInfo_(response.getExtraInfo_());
                                venta.savein(db);
                                showOkDialog("Tu solicitud ha sido enviada,en breve recibiras respuesta. Dudas y aclaraciones" +
                                        " comunicate con nosotros a eclipseappslatam@gmail.com","ok",true,null);
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                sendLogAsError("RefaundFault",fault.getMessage());
                                showOkDialog("Ha ocurrido un problema, revisa tu conexion a internet y vuelve a intentarlo" +
                                        " o comunicate con nosotros a eclipseappslatam@gmail.com","ok",true,null);
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(venta.getOrderId_().matches("")){
                            showOkDialog("Tiene 3 días para solicitar cualquier aclaración a eclipseappslatam@gmail.com\n" ,"ok",true,null);
                        }else{
                            showOkDialog("Tiene 3 días a partir de su compra para solicitar un reembolso a eclipseappslatam@gmail.com\n" +
                                    "Su numero de pedido es " + venta.getOrderId_(),"ok",true,null);
                        }

                    }
                })
                .create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                positiveButton = ((AlertDialog) dialog)
                        .getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(false);
            }
        });
        alert.show();
    }
    boolean isPremium(boolean isPremium){
        checkingForPremium=false;
        if (general.isOnline()){
            return isPremium;
        }else{
            return sp.getBoolean(Premium,false);
        }
    }

    public interface onTypeUserListener{
        public void isPremium(boolean type);//True:IsPremium
    }

    @Override
    public void onBackPressed() {
        showOkCancelDialog("Deseas salir de la app?", "Si", "No", true, new Dialogs.DialogsInterface() {
            @Override
            public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                if(sucess)baseActivity.super.onBackPressed();
            }
        });
    }
}
