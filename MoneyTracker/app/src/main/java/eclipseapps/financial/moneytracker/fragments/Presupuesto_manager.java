package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import eclipseapps.android.FragmentN;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;

/**
 * Created by usuario on 23/07/18.
 */

public class Presupuesto_manager extends FragmentN {

    private final static String TAG_actualFragment="actualFragment";
    public final static String TAG_FragmentName="Presupuestos";

    private LinearLayout containerChilds;
    private LinearLayout mainScreen;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view= (LinearLayout) inflater.inflate(R.layout.f_d_presupuesto_manager,container,false);
        containerChilds=view.findViewById(R.id.d_child_container);
        mainScreen=view.findViewById(R.id.d_main_screen);

        TextView masPresupuesto=view.findViewById(R.id.d_agregarpresupuesto);
        masPresupuesto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)(Presupuesto_manager.this).getActivity()).getSupportActionBar().setTitle("Crear presupuesto");
                mainScreen.setVisibility(View.GONE);
                containerChilds.setVisibility(View.VISIBLE);

                final android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

                Fragment fragment=fragmentManager.findFragmentByTag(TAG_actualFragment);
                if (fragment == null || !(fragment instanceof Presupuesto_selectType)) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.d_child_container, new Presupuesto_selectType().setAction(new Action() {
                                @Override
                                public Object execute(Intent intent) {
                                    if(intent!=null){
                                        if(intent.getAction().matches(Presupuesto_selectType.Cotidiano)){
                                            Fragment fragment=fragmentManager.findFragmentByTag(Presupuesto_createdit_cotidiano.TAG_FragmentName);
                                            if (fragment == null) {
                                                fragmentManager.beginTransaction()
                                                        .replace(R.id.d_child_container,new Presupuesto_createdit_cotidiano(),Presupuesto_createdit_cotidiano.TAG_FragmentName).commit();
                                            }else{
                                                fragmentManager.beginTransaction()
                                                        .replace(R.id.d_child_container,fragment,Presupuesto_createdit_cotidiano.TAG_FragmentName).commit();
                                            }
                                        }
                                    }
                                    return null;
                                }
                            }),TAG_actualFragment).commit();
                }else if(fragment instanceof Presupuesto_selectType){
                    fragmentManager.beginTransaction()
                            .replace(R.id.d_child_container, fragment,TAG_actualFragment).commit();
                }

            }
        });
        return view;
    }
    public boolean onBackPressed(){
        Fragment fragment=getChildFragmentManager().findFragmentByTag(TAG_actualFragment);
        if (fragment!=null  && fragment instanceof Presupuesto_selectType){
            if(((Presupuesto_selectType)fragment).onBackPressed()){
                return true;
            }else{
                ((MainActivity)(Presupuesto_manager.this).getActivity()).getSupportActionBar().setTitle("Presupuestos");
                containerChilds.setVisibility(View.GONE);
                mainScreen.setVisibility(View.VISIBLE);
                return true;
            }

        } /*else if (fragment!=null && fragment instanceof createOrUpdateCar) {
            MainActivity.bar.setTitle("Autos");
            containerChilds.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            return true;
        }*/
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment=getChildFragmentManager().findFragmentByTag(TAG_actualFragment);
        if(fragment!=null && fragment instanceof Presupuesto_selectType)((Presupuesto_selectType)fragment).onActivityResult(requestCode,resultCode,data);
    }
}
