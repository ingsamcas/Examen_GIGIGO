package eclipseapps.mobility.parkeame.fragments;

import android.content.ContentValues;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eclipseapps.libraries.library.general.functions.RandomString;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.activities.Credentials;
import eclipseapps.mobility.parkeame.activities.MainActivity;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.user;
import eclipseapps.mobility.parkeame.customviews.listElementCar;

/**
 * Created by usuario on 28/08/17.
 */
public class fragment_AutoManager_General extends Fragment {
    public static Autos actualAuto;
    private static final int RC_BARCODE_CAPTURE = 9001;
    ManageAutos listener;
    ListView autosListView;
    LinearLayout containerChilds;
    LinearLayout mainScreen;
    List<Autos> autos;
    ArrayAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.f_fragment_automanager,container,false);
        containerChilds=ll.findViewById(R.id.f_child_container);
        mainScreen=ll.findViewById(R.id.f_main_screen);
        autosListView=mainScreen.findViewById(R.id.f_listaautosregistrados);
        DBParkeame db=DBParkeame.getInstance(getActivity());
        final ManageAutoslisteners listener=new ManageAutoslisteners();

        autos=db.select("SELECT * FROM Autos",Autos.class);
        adapter=new ArrayAdapter<Autos>(getActivity(),0,autos) {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
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
                return autos.size();
            }

            @Override
            public Autos getItem(int i) {
                return autos.get(i);
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                listElementCar car;
                if (view==null){
                    car=new listElementCar(getActivity());
                }else{
                    car=((listElementCar)view);
                }
                car.setTipo(autos.get(i).getTipo_());
                car.setModelo(autos.get(i).getModelo_());
                car.setPlacas(autos.get(i).getPlacas_());
                car.Selected(autos.get(i).getStatus_().matches(Autos.Tipos.DEFAULT));
                car.refresh();
                return car;
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
        };
        autosListView.setAdapter(adapter);
        autosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.bar.setTitle("Información del vehiculo");
                mainScreen.setVisibility(View.GONE);
                containerChilds.setVisibility(View.VISIBLE);
                android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.f_child_container, new fragment_auto_editordelete().setAuto((Autos) autos.get(i).clone()).setListener(listener),"ActualFragment")
                        .commit();
            }
        });
        TextView nuevoAuto= (TextView) mainScreen.findViewById(R.id.f_agregarauto);
        nuevoAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.bar.setTitle("Agregar auto");
                mainScreen.setVisibility(View.GONE);
                containerChilds.setVisibility(View.VISIBLE);

                RandomString random=new RandomString(30);

                android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.f_child_container, new createOrUpdateCar().setNewQrCar(random.nextString(), listener),"ActualFragment").commit();
            }
        });

                /*
                Intent intent = new Intent(getActivity(), Barcode2.class);
                //intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                //intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                fragment_AutoManager_General.this.startActivityForResult(intent, RC_BARCODE_CAPTURE);*/
        return ll;
    }

    public fragment_AutoManager_General setListener(ManageAutos listener) {
        this.listener = listener;
        return this;
    }

    public boolean onBackPressed(){
        Fragment fragment=getChildFragmentManager().findFragmentByTag("ActualFragment");
        if (fragment instanceof fragment_auto_editordelete){
           if(((fragment_auto_editordelete)fragment).onBackPressed()){
               return true;
           }else{
               MainActivity.bar.setTitle("Autos");
               containerChilds.setVisibility(View.GONE);
               mainScreen.setVisibility(View.VISIBLE);
               return true;
           }

        } else if (fragment!=null && fragment instanceof createOrUpdateCar) {
            MainActivity.bar.setTitle("Autos");
            containerChilds.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }
    public static void updateDefaultCar(final String placas,final DBParkeame DB,final AsyncCallback<Integer> callBack){
        Map<String, Object> changes = new HashMap<>();
        changes.put( "status_", Autos.Tipos.DEFAULT);
        Backendless.Data.of(Autos.class).update( "placas_= '"+placas+"'", changes, new AsyncCallback<Integer>()
        {
            @Override
            public void handleResponse( final Integer UpdatedToDefault )
            {
                if(UpdatedToDefault==0){
                    return;
                }
                Map<String, Object> changes = new HashMap<>();
                changes.put( "status_", Autos.Tipos.ACTIVE);
                Backendless.Data.of(Autos.class).update("placas_!='" + placas + "'", changes, new AsyncCallback<Integer>() {
                    @Override
                    public void handleResponse(Integer response) {
                        // new Contact instance has been saved
                        ContentValues cv=new ContentValues();
                        cv.put("status_", Autos.Tipos.ACTIVE);
                        DB.getDBInstance().update("Autos",cv,"placas_!='"+placas+"'",null);

                        ContentValues cv2=new ContentValues();
                        cv2.put("status_", Autos.Tipos.DEFAULT);
                        DB.getDBInstance().update("Autos",cv2,"placas_='"+placas+"'",null);

                        if(callBack!=null)callBack.handleResponse(UpdatedToDefault);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        if(callBack!=null)callBack.handleFault(fault);
                    }
                });
            }

            @Override
            public void handleFault( BackendlessFault fault )
            {
                if(callBack!=null)callBack.handleFault(fault);
            }
        } );
    }
    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE && resultCode== Activity.RESULT_OK) {
            if (data != null) {
                listener.OnNewQr(data.getStringExtra("Id"));
            } else {
                //Se necesita capturar un codigo QR
                Log.d("Code QR", "No barcode captured, intent data is null");
            }

        }
    }
    */
public class ManageAutoslisteners implements ManageAutos{

        @Override
        public void OnNewQr(String Id) {

        }

        @Override
        public void onCarCreated(Autos auto) {
            fragment_AutoManager_General.actualAuto=auto;
            for (Autos car:autos) {
                car.setStatus_(Autos.Tipos.ACTIVE);
            }
            autos.add(auto);
            MainActivity.bar.setTitle("Autos");
            containerChilds.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            autosListView.refreshDrawableState();
        }

        @Override
        public void onCarDeleted(Autos autoDeleted) {
            int indexToremove=0;
            for (int i=0;i<autos.size();i++){
                if(autos.get(i).getObjectId()==autoDeleted.getObjectId()){
                    indexToremove=i;
                }
            };
            autos.remove(indexToremove);
            MainActivity.bar.setTitle("Autos");
            containerChilds.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            autosListView.refreshDrawableState();
            autosListView.invalidateViews();
        }

        @Override
        public void onCarEdited(Autos autoUpdated) {
            for (Autos auto:autos) {
                if(auto.getObjectId()==autoUpdated.getObjectId()){
                    auto=autoUpdated;
                }
            }
            MainActivity.bar.setTitle("Autos");
            containerChilds.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            autosListView.refreshDrawableState();
            autosListView.invalidateViews();
        }
    }
    public interface ManageAutos{
        void OnNewQr(String Id);
        void onCarCreated(Autos auto);
        void onCarDeleted(Autos autoDeleted);
        void onCarEdited(Autos autoUpdated);
    }
}
