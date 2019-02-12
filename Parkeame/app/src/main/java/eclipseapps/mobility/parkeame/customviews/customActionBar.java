package eclipseapps.mobility.parkeame.customviews;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.List;

import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.activities.MainActivity;
import eclipseapps.mobility.parkeame.cloud.Autos;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;

import static android.content.Context.MODE_PRIVATE;
import static eclipseapps.mobility.parkeame.cloud.Autos.Tipos.DEFAULT;
import static eclipseapps.mobility.parkeame.fragments.fragment_AutoManager_General.updateDefaultCar;

/**
 * Created by usuario on 06/09/17.
 */

public class customActionBar extends RelativeLayout {
    public static Autos auto;
    private TextView modelo;
    private TextView marca;
    private TextView placas;
    private ImageView icono;
    private RelativeLayout layContainer;
    private DBParkeame DB;
    private onCarsCallback listener;
    public customActionBar(Context context) {
        super(context);
        init();
    }

    public customActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public customActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private  void init(){
        DB= DBParkeame.getInstance(getContext());
        inflate(getContext(), R.layout.j_actionbar_customview, this);
        layContainer= (RelativeLayout) findViewById(R.id.j_car_container);
        modelo= (TextView) findViewById(R.id.j_car_modelo);
        marca= (TextView) findViewById(R.id.j_car_marca);
        placas= (TextView) findViewById(R.id.j_car_placas);
        icono=(ImageView)findViewById(R.id.j_icono_car);

        layContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Cursor cur=DB.getallfrom("Autos");

                if (cur.getCount()>0){
                    final List<Autos> AutosRegistrados=DB.mapCursorToObjectList(cur,Autos.class);
                    final CharSequence autos[]=new CharSequence[AutosRegistrados.size()];
                    for (int i=0;i<AutosRegistrados.size();i++){
                        autos[i]=AutosRegistrados.get(i).getPlacas_()+"   "+AutosRegistrados.get(i).getModelo_();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Selecciona un auto");
                    builder.setItems(autos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,final int which) {
                            // the user clicked on colors[which]
                            ((MainActivity)getContext()).wait("Espere...",false);
                            updateDefaultCar(AutosRegistrados.get(which).getPlacas_(), DB, new AsyncCallback<Integer>() {
                                @Override
                                public void handleResponse(Integer updateToDefault) {
                                    auto=AutosRegistrados.get(which);
                                    SelectAuto();
                                    ((MainActivity)getContext()).dismisswait();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    ((MainActivity)getContext()).dismisswait();
                                    ((MainActivity)getContext()).showOkDialog("Ha ocurrido un problema, revisa tu conexión a internet","ok",true,null);
                                    Log.e( "MYAPP", "Server reported an error - " + fault );
                                }
                            });

                        }
                    });
                    builder.show();
                }else{
                    listener.NoCarAvailable();//Se dispara la interface
                }
            }
        });
        List<Autos> autos=DB.select("SELECT * FROM Autos WHERE status_='"+DEFAULT+"'",Autos.class);
        if(autos!=null && autos.size()>0){
            auto=autos.get(0);
        }
        SelectAuto();
    }
    private void SelectAuto() {
        if (auto!=null && !auto.getQr_().matches("")){
            final String[] tipos=getResources().getStringArray(R.array.Tipo);

            if (auto.getTipo_().matches(tipos[0])){
                icono.setImageResource(R.drawable.ic_local_shipping_white_48dp);
            }else if (auto.getTipo_().matches(tipos[1])){
                icono.setImageResource(R.drawable.ic_directions_car_white_48dp);
            }else if (auto.getTipo_().matches(tipos[2])){
                icono.setImageResource(R.drawable.ic_motorcycle_white_48dp);
            }else if (auto.getTipo_().matches(tipos[3])){
                icono.setImageResource(R.drawable.ic_airport_shuttle_white_48dp);
            }

            modelo.setText(auto.getModelo_());
            marca.setText(auto.getMarca_());
            placas.setText(auto.getPlacas_());
            setActionBarColor(android.R.color.holo_blue_dark);
        }else{
            setActionBarColor(android.R.color.holo_red_dark);
            modelo.setText("Aun no cuentas con autos registrados");
            marca.setText("Toca aqui para registrar un auto");
            placas.setText("");
        }
    }

    public customActionBar setListener(onCarsCallback listener) {
        this.listener = listener;
        return this;
    }

    public customActionBar clear(){
        setTitle("");
        setSubtitle("");
        placas.setText("");
        setIcon(android.R.color.transparent);
        return this;
    }
    public customActionBar setTitle(String title) {
        modelo.setText(title);
        return this;
    }
    public customActionBar setSubtitle(String subtitle){
        marca.setText(subtitle);
        if(subtitle==null || subtitle.matches("")){
            marca.setVisibility(GONE);
        }else{
            marca.setVisibility(VISIBLE);
        }
        return this;
    }
    public customActionBar setIcon(int resource){
        icono.setImageResource(android.R.color.transparent);
        if(resource==android.R.color.transparent){
            icono.setVisibility(GONE);
        }else{
            icono.setVisibility(VISIBLE);
        }
        return this;
    }

    public interface onCarsCallback{
        void NoCarAvailable();
    }
    public customActionBar setActionBarColor(int ResourceColor){
        if (Build.VERSION.SDK_INT>=23){
            ((AppCompatActivity)getContext()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(ResourceColor,null)));
        }else{
            ((AppCompatActivity)getContext()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(ResourceColor)));
        }
        layContainer.setBackgroundResource(ResourceColor);
        return this;
    }
}
