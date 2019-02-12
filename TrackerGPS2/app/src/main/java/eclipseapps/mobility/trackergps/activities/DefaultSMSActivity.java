package eclipseapps.mobility.trackergps.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import eclipseapps.mobility.trackergps.R;

/**
 * Created by usuario on 03/04/18.
 */

public class DefaultSMSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.defaultsmsactivity);
//Launch sms activity
        String number = "12346556";  // The number on which you want to send SMS
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
        //code for sending sms and use ui
    }
}
