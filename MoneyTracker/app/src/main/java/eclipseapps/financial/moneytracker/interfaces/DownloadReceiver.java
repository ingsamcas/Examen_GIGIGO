package eclipseapps.financial.moneytracker.interfaces;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import eclipseapps.android.FragmentN;

public abstract class DownloadReceiver extends ResultReceiver {
    public static final int UPDATE_PROGRESS =0 ;
    public static final int UPDATE_FINISH =1 ;
    public static final int DOWNLOAD_ERROR=2;
    public static final String failedFile="failedFile";
    public FragmentN.ProgressDialogFragment mProgressDialog;//Hay que hacer un progress dialog
    public DownloadReceiver(Handler handler) {
        super(handler);
    }


    public  abstract void onFinishDownload(String[] pathsToFiles);

    public  abstract void OnErrorDownload(String urlToFile);

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == UPDATE_PROGRESS) {
            int progress = resultData.getInt("progress");
            if (mProgressDialog!=null)mProgressDialog.setProgress(progress);
        }else if(resultCode == UPDATE_FINISH){
            if (mProgressDialog!=null)mProgressDialog.dismiss();
            onFinishDownload(resultData.getStringArray("pathsToFiles"));
        }else if(resultCode==DOWNLOAD_ERROR){
            OnErrorDownload(resultData.getString(failedFile));
        }
    }

    public void setmProgressDialog(FragmentN.ProgressDialogFragment mProgressDialog) {
        this.mProgressDialog = mProgressDialog;
    }


}
