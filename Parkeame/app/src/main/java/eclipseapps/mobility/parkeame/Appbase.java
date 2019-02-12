package eclipseapps.mobility.parkeame;

import android.app.Application;

import pay.openpay.Openpay;
import pay.openpay.implementation.OpenPayApp;


/**
 * Created by usuario on 06/10/17.
 */

public class Appbase extends OpenPayApp {

    @Override
    public String getClientId() {
        return "myjax4kra6gknjnlldk9";
    }

    @Override
    public String getPublicKey() {
        return "pk_093744e3e51243bea62352d503d67933";
    }

    @Override
    public boolean isProduction() {
        return false;
    }


    public Openpay getOpenpay() {
        return this.openpay;
    }
}
