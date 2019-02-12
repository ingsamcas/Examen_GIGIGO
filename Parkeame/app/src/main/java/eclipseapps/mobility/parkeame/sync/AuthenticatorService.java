package eclipseapps.mobility.parkeame.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.backendless.Backendless;

import eclipseapps.mobility.parkeame.cloud.Defaults;


/**
 * Created by usuario on 19/02/18.
 */

public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
        Backendless.initApp( getApplicationContext(),
                Defaults.APPLICATION_ID,
                Defaults.API_KEY );
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
