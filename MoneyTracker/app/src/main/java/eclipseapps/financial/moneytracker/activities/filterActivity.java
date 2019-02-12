package eclipseapps.financial.moneytracker.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eclipseapps.android.customviews.DatePickerEditText;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.customViews.spinnerCheckbox;

/**
 * Created by usuario on 11/06/17.
 */
public class filterActivity extends trackedActivity {
    private DBSmartWallet db;
    boolean busquedaExacta=false;
    spinnerCheckbox cuentasFilterView;
    spinnerCheckbox tagFilterView;
    DatePickerEditText desde;
    DatePickerEditText hasta;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_filter);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (db==null)db= DBSmartWallet.getInstance(this);

        String[] tags=db.GetStringColumn("SELECT * FROM motives",null,"motive_");
        List Tags=new ArrayList();
        if(tags!=null && tags.length>0){
            Tags=new ArrayList<String>(Arrays.asList(tags));
        }

        tagFilterView= (spinnerCheckbox)findViewById(R.id.fragment_filter_spinner_tags);
        tagFilterView.setOptions(Tags);


        String[] cuentas=db.GetStringColumn("SELECT * FROM cuentas",null,"cuenta_");
        List Cuentas=new ArrayList();
        if(cuentas!=null && cuentas.length>0){
            Cuentas=new ArrayList<String>(Arrays.asList(cuentas));
        }
        cuentasFilterView= (spinnerCheckbox)findViewById(R.id.fragment_filter_spinner_cuentas);
        cuentasFilterView.setOptions(Cuentas);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fragment_filter_busquedaexacta);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                busquedaExacta=!busquedaExacta;
                if(busquedaExacta){
                    fab.setImageResource(R.drawable.candado);
                    Toast.makeText(filterActivity.this, "Que contenga todas las etiquetas seleccionadas", Toast.LENGTH_SHORT).show();
                } else {
                    fab.setImageResource(R.drawable.candadoabierto);
                    Toast.makeText(filterActivity.this, "Que contenga al menos una de las etiquetas seleccionadas", Toast.LENGTH_SHORT).show();
                }
            }
        });

        busquedaExacta=false;
        fab.setImageResource(R.drawable.candadoabierto);
        if (Build.VERSION.SDK_INT <= 19) {
            float myMarginPx = getResources().getDimension(R.dimen.margin_actionbuttonKitkat);
            fab.setX((int) myMarginPx);
            float mytopPx = getResources().getDimension(R.dimen.fragfilter_actionbuttonkitkatNegativeMargin);
            fab.setY((int) mytopPx);
        }


        final long[] principio=db.GetLongColumn("SELECT MIN(tiempo_) AS tiempo_ FROM basics",null,"tiempo_");
        final long[] fin=db.GetLongColumn("SELECT MAX(tiempo_) AS tiempo_ FROM basics",null,"tiempo_");
        desde= (DatePickerEditText) findViewById(R.id.fragment_filter_editTextDate_Desde);
        hasta= (DatePickerEditText) findViewById(R.id.fragment_filter_editTextDate_hasta);
        CheckBox alldates= (CheckBox) findViewById(R.id.fragment_filter_alldates);
        alldates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    desde.setFecha(principio[0]);
                    desde.setEnabled(false);
                    hasta.setFecha(fin[0]);
                    hasta.setEnabled(false);
                }else{
                    desde.setEnabled(true);
                    hasta.setEnabled(true);
                }
            }
        });
        alldates.setChecked(true);
        desde.setFecha(principio[0]);
        hasta.setFecha(fin[0]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter_ok) {
            String WHERETag=createFilterOR(tagFilterView,"tag_");
            String WHEREAccount=createFilterOR(cuentasFilterView,"cuenta_");
            String where="WHERE (tiempo_>='"+desde.getFecha().getTime()+"' AND tiempo_<='"+hasta.getFecha().getTime()+"')";
            if(!WHERETag.matches(""))where=where+" AND "+WHERETag;
            if(!WHEREAccount.matches(""))if (!where.matches("WHERE"))where=where+" AND "+WHEREAccount;else where=where+WHEREAccount;

            String filtro="SELECT  * FROM basics INNER JOIN tags on basics.id_=tags.id_ " +
                    where +" GROUP BY basics.id_ "+(busquedaExacta?"HAVING count(DISTINCT tag_)="+tagFilterView.getSelected().size():"")+" ORDER BY tiempo_ DESC";
            Intent intent=new Intent();
            intent.putExtra("Filtro",filtro);
            setResult(RESULT_OK,intent);
            filterActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String createFilterOR(spinnerCheckbox option, String columnName) {
        List<String> selected=option.getSelected();
        String WHERETag="";
        if (selected.size()>0){
            WHERETag=" (";
            for (String select:selected) {
                WHERETag=WHERETag+columnName+"='"+select+"' OR ";
            }
            WHERETag=WHERETag.substring(0,WHERETag.lastIndexOf(" OR "))+")";
        }else{
            WHERETag="";
        }
        return WHERETag;
    }

}
