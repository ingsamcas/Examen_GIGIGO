package eclipseapps.mobility.parkeame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * Created by usuario on 29/08/17.
 */
public class Barcode2 extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentIntegrator scanIntegrator = new IntentIntegrator(Barcode2.this);
        scanIntegrator.setPrompt("Scan a barcode");
        scanIntegrator.setBeepEnabled(true);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.setBarcodeImageEnabled(true);
        scanIntegrator.initiateScan();

    }
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode==RESULT_OK){
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        /*As an example in order to get the content of what is scanned you can do the following*/

            String scanContent = scanningResult.getContents().toString();
            Intent r=new Intent();
            r.putExtra("Id",scanContent);
            setResult(RESULT_OK,r);
            Barcode2.this.finish();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        this.finish();
    }
}
