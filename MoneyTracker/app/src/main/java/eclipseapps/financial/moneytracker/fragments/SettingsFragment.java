package eclipseapps.financial.moneytracker.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.interfaces.onAlarmReceiver;
import eclipseapps.financial.moneytracker.sync.Authenticator;
import eclipseapps.libraries.library.general.functions.OrderMap;

import static eclipseapps.financial.moneytracker.sync.Authenticator.REQUEST_CODE_SIGN_IN;

public class SettingsFragment extends baseFragment {
    public static String TAG_FragmentName="Configuración";
    Toolbar aBar;
    RelativeLayout child;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout parent=new RelativeLayout(getActivity());
        parent.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));

        child=new RelativeLayout(getActivity());
        child.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        child.setId(R.id.container_fragment);
        parent.addView(child);

        return parent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setGeneralPreferenceScreen();
    }
    public void setGeneralPreferenceScreen(){
        Setts setts=new Setts();
        setts.setAcercaDeListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                FragmentTransaction FT=getChildFragmentManager().beginTransaction();
                FT.replace(child.getId(),new acerca_de(),"acerca_de");
                FT.commit();
                if(aBar!=null){
                    aBar.setTitle("Acerca de");
                    aBar.setNavigationIcon(R.drawable.baseline_arrow_back_white_36);
                    aBar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBackPressed();
                        }
                    });
                }

                return true;
            }
        });
        setts.setFeedBackListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onAlarmReceiver.showNotification(getActivity());
                ((MainActivity)getActivity()).usabilityAppTracking(AnalyticsApplication.Gestures.Click,"RateApp");
                ((MainActivity)getActivity()).showRatingDialog();
                return false;
            }
        });
        setts.setBackUpListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showOkDialog("Es necesario iniciar sesión para utilizar esta caracteristica", "Ok", true, new Dialogs.DialogsInterface() {
                    @Override
                    public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                        Toast.makeText(getActivity(),"Respaldo activado",Toast.LENGTH_LONG).show();
                        //((MainActivity)getActivity()).
                    }
                });
                return false;
            }
        });
        getChildFragmentManager()
                .beginTransaction()
                .replace(child.getId(), setts,Setts.TAG_FragmentName)
                .commit();
        aBar.setTitle(TAG_FragmentName);
    }

    public SettingsFragment setaBar(Toolbar aBar) {
        this.aBar = aBar;
        return this;
    }
    public boolean onBackPressed(){
        Fragment frament=getChildFragmentManager().findFragmentByTag("acerca_de");
        if(frament!=null && frament.isVisible()){
            ((MainActivity)getActivity()).refreshToolbar(null);
            setGeneralPreferenceScreen();
            return true;//Consume el evento
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getChildFragmentManager().findFragmentByTag(Setts.TAG_FragmentName);
        if(fragment!=null)fragment.onActivityResult(requestCode, resultCode, data);
    }

    public static class Setts extends PreferenceFragmentCompat implements  Preference.OnPreferenceChangeListener{
        public static String TAG_FragmentName="Setts";
        private Preference.OnPreferenceClickListener acercaDeListener;
        private Preference.OnPreferenceClickListener feedBackListener;
        private Preference.OnPreferenceClickListener backUpListener;
        SwitchPreferenceCompat backupAppPref;


        public Setts setAcercaDeListener(Preference.OnPreferenceClickListener acercaDeListener) {
            this.acercaDeListener = acercaDeListener;
            return this;
        }

        public Setts setFeedBackListener(Preference.OnPreferenceClickListener feedBackListener) {
            this.feedBackListener = feedBackListener;
            return this;
        }
        public Setts setBackUpListener(Preference.OnPreferenceClickListener backUpListener) {
            this.backUpListener = backUpListener;
            return this;
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            Preference feesBackPref = (Preference) findPreference("feedback");
            feesBackPref.setOnPreferenceClickListener(feedBackListener);
            Preference aboutAppPref = (Preference) findPreference("aboutapp");
            aboutAppPref.setOnPreferenceClickListener(acercaDeListener);
            backupAppPref = (SwitchPreferenceCompat) findPreference("backup");
            backupAppPref.setOnPreferenceChangeListener(this);

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_SIGN_IN && data != null) {
                Toast.makeText(getActivity(),"Configurando la copia de seguridad...",Toast.LENGTH_LONG).show();
                //((FragmentN)getParentFragment()).sho
                ((MainActivity)getActivity()).handleSignInResult(data,new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        backupAppPref.setOnPreferenceChangeListener(null);
                        backupAppPref.setChecked(true);
                        backupAppPref.setOnPreferenceChangeListener(Setts.this);
                    }
                });
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if((boolean)newValue){
                Authenticator.googleAuth(getActivity());
                return false;
            }else{
                return true;
            }
        }
    }
}
