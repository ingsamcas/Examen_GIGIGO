package eclipseapps.mobility.parkeame.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;


import eclipseapps.mobility.parkeame.cloud.user;

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
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
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
        public static final String AUTHORITY = "eclipseapps.mobility.parkeame.datasync.provider";
        // An account type, in the form of a domain name
        public static final String ACCOUNT_TYPE = "parkeame.com";
        // The account name
        public static final String ACCOUNT = "dummyaccount";
    }
}
