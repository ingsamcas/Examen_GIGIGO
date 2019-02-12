package eclipseapps.financial.moneytracker.adapters;

import java.util.List;

import eclipseapps.financial.moneytracker.cloud.rfc;

/**
 * Created by usuario on 14/01/18.
 */

public interface OnEditRFCListeners {
    public List<rfc> getRFCs();
    public boolean deleteRFCListener(String RFC);
    public void onNewRFCAdded();
}
