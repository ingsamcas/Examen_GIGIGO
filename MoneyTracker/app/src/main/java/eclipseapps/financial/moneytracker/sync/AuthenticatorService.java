package eclipseapps.financial.moneytracker.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.backendless.Backendless;


/**
 * Created by usuario on 19/02/18.
 */

public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        Log.d("SyncA","AuthCreated");
        mAuthenticator = new Authenticator(this);
        Backendless.initApp(this,"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SyncA","AUthServiceLaunchedBinded");
        return mAuthenticator.getIBinder();
    }

}
