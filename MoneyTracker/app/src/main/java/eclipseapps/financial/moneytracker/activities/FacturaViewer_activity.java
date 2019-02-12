package eclipseapps.financial.moneytracker.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.LinearLayout;

import java.io.File;

import eclipseapps.android.FragmentN;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.facturas;
import eclipseapps.financial.moneytracker.interfaces.DownloadReceiver;
import eclipseapps.financial.moneytracker.sync.SyncService;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.general;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static eclipseapps.financial.moneytracker.sync.SyncService.PATHInsideApp;
import static eclipseapps.financial.moneytracker.sync.SyncService.URLToDownload;

/**
 * Created by usuario on 05/03/18.
 */

public class FacturaViewer_activity extends trackedActivity{
    public static String objectId_Fatura="objectIdFatcura";;
    BroadcastReceiver brSms;
    private String objectIdFactura="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        option1();
    }

    public void option1(){
        objectIdFactura=getIntent().getStringExtra(objectId_Fatura);
        setContentView(R.layout.activity_pdfviewer);
        final LinearLayout pdfViewContainer=findViewById(R.id.pdfView_container);
        DBSmartWallet db=DBSmartWallet.getInstance(FacturaViewer_activity.this);
        facturas factura=db.selectFirst("SELECT * FROM facturas WHERE objectId='"+objectIdFactura+"'",facturas.class);
        if(factura==null)finish();
        final String facturaPdfRuta=factura.getPdf_();
        if (facturaPdfRuta.contains("http")&&facturaPdfRuta.contains(".pdf")){
            if(!general.isOnline()){
                showOkDialog("Ups! Verifica tu conexión a internet e intenta de nuevo", "Aceptar", false, new Dialogs.DialogsInterface() {
                    @Override
                    public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                        finish();
                    }
                });
                return;
            }
            final FragmentN.ProgressDialogFragment PD= FragmentN.ProgressDialogFragment.newInstanceHorizontal("Descargando factura...",false);
            PD.setOnWindowAttached(new FragmentN.LifeCycleObserver() {
                @Override
                public void onActivityCreated(@Nullable Bundle savedInstanceState) {

                }

                @Override
                public void onResume() {
                    Intent intent = new Intent(FacturaViewer_activity.this, SyncService.class);
                    intent.putExtra(URLToDownload, facturaPdfRuta);
                    intent.putExtra(PATHInsideApp,"/files/facturas/");
                    FacturaPDFDownloadReceiver DR=new FacturaPDFDownloadReceiver(new Handler());
                    DR.setmProgressDialog(PD,objectIdFactura,FacturaViewer_activity.this);
                    intent.putExtra("receiver", DR);
                    intent.setAction(SyncService.ACTION_DOWNLOAD_FILE);
                    startService(intent);
                }
            });
            PD.show(getSupportFragmentManager(),"descargando...");
        }else{

            final File file=new File(facturaPdfRuta);
            if(file.exists()){
                file.setReadable(true, false);
                Uri pdfURI = FileProvider.getUriForFile(this,
                        "eclipseapps.financial.moneytracker.fileprovider",
                        file);

                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                // I am opening a PDF file so I give it a valid MIME type
                viewIntent.setDataAndType(pdfURI, "application/pdf");
                startActivity(viewIntent);
                finish();
            }
        }
    }
    public class FacturaPDFDownloadReceiver extends DownloadReceiver {
        String objectIdFactura;
        private Context cont;

        public FacturaPDFDownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onFinishDownload(final String[] paths) {
            if(!objectIdFactura.matches("")){
                DBSmartWallet db=DBSmartWallet.getInstance(mProgressDialog.getActivity());
                db.getDBInstance().execSQL("UPDATE facturas SET pdf_='"+paths[0]+"' WHERE objectId='"+objectIdFactura+"'");
                facturas factura=db.selectFirst("SELECT * FROM facturas WHERE objectId='"+objectIdFactura+"'",facturas.class);
                String facturaPdfRuta=factura.getPdf_();
                if (!facturaPdfRuta.contains("http")&&facturaPdfRuta.contains(".pdf")){
                    final File file=new File(facturaPdfRuta);
                    file.setReadable(true, false);
                    Uri pdfURI = FileProvider.getUriForFile(cont,
                            "eclipseapps.financial.moneytracker.fileprovider",
                            file);

                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                    // I am opening a PDF file so I give it a valid MIME type
                    viewIntent.setDataAndType(pdfURI, "application/pdf");
                    mProgressDialog.dismiss();
                    ((Activity)cont).startActivity(viewIntent);
                    finish();
                }
            }
        }

        @Override
        public void OnErrorDownload(String urlToFile) {
            //UpdateRequire
            AnalyticsApplication.logD("FacturaPDFDownloadReceiver","FailedToDownload:"+urlToFile);
        }

        public void setmProgressDialog(FragmentN.ProgressDialogFragment mProgressDialog,String objectIdFactura,Context context) {
            this.mProgressDialog = mProgressDialog;
            this.objectIdFactura=objectIdFactura;
            this.cont=context;
        }
    }
}
