package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.activities.TutorialAcitvity;
import eclipseapps.financial.moneytracker.adapters.OnEditRFCListeners;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.rfc;
import eclipseapps.libraries.library.general.functions.general;

/**
 * Created by usuario on 14/01/18.
 */

public class RFC_manager extends baseFragment implements  OnEditRFCListeners{
    public static final String TAG_Fragment="RFCManager";
    DBSmartWallet db;
    private List<rfc> rfcs=new ArrayList<rfc>();
    protected OnEditRFCListeners editRFCListener=new OnEditRFCListeners() {
        @Override
        public List<rfc> getRFCs() {
            if(db==null)db=DBSmartWallet.getInstance(getActivity());
            return db.mapCursorToObjectList(db.getallfrom("rfc"),rfc.class);
        }

        @Override
        public boolean deleteRFCListener(String RFC) {
            return false;
        }

        @Override
        public void onNewRFCAdded() {

        }
    };
    RelativeLayout subfragscontainer;
    RelativeLayout listContainer;
    ListView RFCs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            Backendless.initApp(getActivity(),"C1D72711-B7EB-98DD-FFC7-23418D485000","32567822-9E88-074A-FF11-E5C773824200");
        }catch (BackendlessException e){
            if(!general.isOnline()){
                Toast.makeText(getActivity(),"Sin acceso a internet",Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.sp!=null && !MainActivity.sp.getBoolean(TutorialAcitvity.action_HOW_RECIVE_INVOICE,false)){
            Intent intent=new Intent(getActivity(),TutorialAcitvity.class);
            intent.setAction(TutorialAcitvity.action_HOW_RECIVE_INVOICE);
            startActivity(intent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db=DBSmartWallet.getInstance(getActivity());
        rfcs=editRFCListener.getRFCs();
        RelativeLayout GeneralView= (RelativeLayout) inflater.inflate(R.layout.fragment_factura_rfc_manager,container,false);
        listContainer= (RelativeLayout) GeneralView.findViewById(R.id.rfc_list_registrados_container);
        subfragscontainer= (RelativeLayout) GeneralView.findViewById(R.id.rfc_subfragments_container);
        RFCs = (ListView) GeneralView.findViewById(R.id.rfc_list_registrados);
        RFCs.setAdapter(new BaseAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return true;
            }

            @Override
            public boolean isEnabled(int i) {
                return true;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public int getCount() {
                return rfcs.size();
            }

            @Override
            public Object getItem(int i) {
                return rfcs.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view==null){
                    TextView v=new TextView(getActivity());
                    ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    v.setLayoutParams(params);
                    v.setText(rfcs.get(i).getRfc_());
                    view=v;
                }else{
                    ((TextView)view).setText(rfcs.get(i).getRfc_());
                }
                return view;
            }

            @Override
            public int getItemViewType(int i) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        });
        RFCs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listContainer.setVisibility(View.GONE);
                listContainer.invalidate();
                subfragscontainer.setVisibility(View.VISIBLE);
                setfragament(new RFC_editRFC().setRFC(rfcs.get(i)).setListener(editRFCListener),"EditRFC");
            }
        });
        final TextView agregarRFC= (TextView) GeneralView.findViewById(R.id.rfc_manager_label_agregar_rfc);
        agregarRFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listContainer.setVisibility(View.GONE);
                listContainer.invalidate();
                subfragscontainer.setVisibility(View.VISIBLE);
                subfragscontainer.post(new TimerTask() {
                    @Override
                    public void run() {//Una vez que ya aperecio el contenedor carga el fragmento para agreagr nueva cuenta
                        setfragament(new RFC_editRFC(),"New RFC");
                    }
                });
            }
        });
        return GeneralView;
    }

    public RFC_manager setOnEditAccountListener(OnEditRFCListeners listener) {
        this.editRFCListener = listener;
        return this;
    }

    @Override
    public List<rfc> getRFCs() {
        return null;
    }

    @Override
    public boolean deleteRFCListener(String RFC) {
        return false;
    }

    @Override
    public void onNewRFCAdded() {
        rfcs= editRFCListener.getRFCs();
        listContainer.setVisibility(View.VISIBLE);
        listContainer.post(new TimerTask() {
            @Override
            public void run() {
                RFCs.invalidateViews();
            }
        });
        listContainer.invalidate();
        subfragscontainer.setVisibility(View.GONE);
        /*
        subfragscontainer.post(new TimerTask() {
            @Override
            public void run() {//Una vez que ya aperecio el contenedor carga el fragmento para agreagr nueva cuenta
                setfragament(new RFC_editRFC(),"New RFC");
            }
        });
        */
       // Fragment fragment=getChildFragmentManager().findFragmentByTag("New RFC");
        //FragmentTransaction ft=getChildFragmentManager().beginTransaction();
        //ft.remove(fragment);
        //ft.commit();
       // listContainer.setVisibility(View.VISIBLE);
       // listContainer.invalidate();
       /*
        listContainer.post(new TimerTask() {
            @Override
            public void run() {
                RFCs.invalidateViews();
            }
        });
        */
    }

    private void setfragament(RFC_editRFC fragment, String tag){
        fragment.setListener(RFC_manager.this);
        FragmentTransaction ft=getChildFragmentManager().beginTransaction();
        ft.replace(subfragscontainer.getId(),fragment,tag);
        ft.commit();
    }
    public static class Brands{
        public final static String VISA="visa";
        public final static String MASTERCARD="mastercard";
    }
    public boolean onBackPressed(){

        Fragment fragment=getChildFragmentManager().findFragmentByTag("New RFC");
        if(fragment!=null && fragment instanceof RFC_editRFC){
            FragmentTransaction ft=getChildFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
            listContainer.setVisibility(View.VISIBLE);
            listContainer.invalidate();
            return true;
        }
        fragment=getChildFragmentManager().findFragmentByTag("EditRFC");
        if(fragment!=null && fragment instanceof RFC_editRFC){
            FragmentTransaction ft=getChildFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
            listContainer.setVisibility(View.VISIBLE);
            listContainer.invalidate();
            if (RFCs !=null){
                getActivity().runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        List<rfc> Rfcs= editRFCListener.getRFCs();
                        if (Rfcs.size()==rfcs.size()){
                            rfcs=Rfcs;
                            RFCs.invalidateViews();
                        }else if(Rfcs.size()<rfcs.size()){//Entonces se elimino la tarjeta
                            rfcs=Rfcs;
                            final BaseAdapter adapter= (BaseAdapter) RFCs.getAdapter();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
            return true;
        }

        return false;
    }

}
