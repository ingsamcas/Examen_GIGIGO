package eclipseapps.financial.moneytracker.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import eclipseapps.financial.moneytracker.cloud.user;

import static android.content.Context.ACCOUNT_SERVICE;

/**
 * Created by usuario on 19/02/18.
 */

public class Authenticator extends AbstractAccountAuthenticator {
    Context mContext;
    public Authenticator(Context contex){
        super(contex);
        mContext=contex;
    }
    //No puede editar propiedades
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }
    //No puede crear una cuenta
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle loginOptions) throws NetworkErrorException {
       Log.d("SyncA","getAuthToken:"+authTokenType);
        if (!authTokenType.equals(Constants.AUTHTOKEN_TYPE_GENERICUSER)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE,
                    "invalid authTokenType");
            return result;
        }
        final AccountManager am = AccountManager.get(mContext);
        final String password = am.getPassword(account);
        if (password != null) {
            boolean verified =
                    onlineConfirmPassword(account.name, password);
            if (verified) {
                return AUTH_OK(account.name,password);
            }else{
                user User=new user();
                if(User.login(account.name,password,true)){
                    return AUTH_OK(account.name,password);
                }
            }
        }
        // the password was missing or incorrect, return an Intent to an
        // Activity that will prompt the user for the password.
        //En este caso debido a que solo hay una cuenta vuelve a intentar hacer el login


        //final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        //intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.TAG_FragmentName);
        //intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE,
        //        authTokenType);
        //intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
         //       response);
        //final Bundle bundle = new Bundle();
        //bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        loginOptions.putString(AccountManager.KEY_ERROR_CODE,"001");
        loginOptions.putString(AccountManager.KEY_ERROR_MESSAGE,"No se ha podido establecer el login");
        return loginOptions;
    }
    private Bundle AUTH_OK(String accountName,String password){
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE_GENERICUSER);
        result.putString(AccountManager.KEY_AUTHTOKEN, password);
        return result;
    }
    private boolean onlineConfirmPassword(String name, String password) {
        Log.d("Auth_user",name);
        Log.d("Auth_password",password);
        user User=new user();
        User.setEmail(name);
        User.setPassword(password);
        return User.isUserLogged();
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }
    //No permite actualizar propiedades
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }
    //No puede checar caracteristicas de la cuenta
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }
    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Get an instance of the Android account manager

        AccountManager accountManager =
                (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        Account[] acounts=accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if(acounts!=null && acounts.length>0){
            for (Account acount:acounts) {
                if(acount.name.matches(Constants.ACCOUNT)){
                    return acount;
                }
            }
        }
        // Create the account type and default account
        Account newAccount = new Account(Constants.ACCOUNT, Constants.ACCOUNT_TYPE);

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, "baraja02", null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Log.d("SyncA","Account added");
        } else {
            Log.d("SyncA","The account exists or some other error occurred");
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }
    public static class Constants{
        public static String AUTHTOKEN_TYPE_GENERICUSER="AUTHTOKEN_TYPE_GENERICUSER";//NO AUTH User
        // The authority for the sync adapter's content provider
        public static final String AUTHORITY = "eclipseapps.financial.moneytracker.datasync.provider";
        // An account type, in the form of a domain TAG_FragmentName
        public static final String ACCOUNT_TYPE = "denario.com";
        // The account TAG_FragmentName
        public static final String ACCOUNT = "account@denario.com";
    }

    /**
     * Auth for Google(Drive)
     */
    public static final int REQUEST_CODE_SIGN_IN = 10110;
    public static void googleAuth(final Activity context){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        // The result of the sign-in Intent is handled in onActivityResult.
        context.startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }
    public static void googleAuth(final Fragment context){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context.getActivity(), gso);
        // The result of the sign-in Intent is handled in onActivityResult.
        context.startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

}
