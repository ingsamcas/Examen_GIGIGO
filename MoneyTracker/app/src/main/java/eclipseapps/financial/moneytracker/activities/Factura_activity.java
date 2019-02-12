package eclipseapps.financial.moneytracker.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import eclipseapps.android.ActivityN;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.fragments.RFC_manager;

/**
 * Created by usuario on 12/01/18.
 */

public class Factura_activity extends trackedActivity {

    RFC_manager managerfc;
    Menu _menu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managerfc);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.misrfcs);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rfc, menu);
        _menu=menu;
        return true;
    }
    @Override
    public void onBackPressed() {
        Fragment fragment=getSupportFragmentManager().findFragmentById(R.id.rfc_manager);
        if (fragment==null){
            super.onBackPressed();
        }else{
            managerfc=(RFC_manager) fragment;
         if(!managerfc.onBackPressed())super.onBackPressed();
        }
    }

}
