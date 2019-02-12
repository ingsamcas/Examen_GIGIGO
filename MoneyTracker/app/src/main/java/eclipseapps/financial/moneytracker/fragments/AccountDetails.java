package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import eclipseapps.android.FragmentN;
import eclipseapps.android.customviews.FontFitTextView;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.activities.baseActivity;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.cuentas;
import eclipseapps.libraries.library.general.functions.OrderMap;

public class AccountDetails extends baseFragment {
    public final static String name="AccountDetails";
    FontFitTextView saldoActual;
    cuentas cuenta;
    private final static String KeyInstanceState_cuenta="cuenta";
    DBSmartWallet db;
    ViewPager tabs;
    private int containerForReport;
    private FragmentManager fm;

    public AccountDetails setDb(DBSmartWallet db) {
        this.db = db;
        return this;
    }

    public AccountDetails setCuenta(cuentas cuenta) {
        this.cuenta = cuenta;
        return this;
    }

    public cuentas getCuenta() {
        return cuenta;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout LL= (RelativeLayout) inflater.inflate(R.layout.accountdetails,container,false);
       saldoActual=LL.findViewById(R.id.accountdetails_saldo);
        tabs=LL.findViewById(R.id.accountdetails_pager);
        return  LL;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            cuenta= (cuentas) savedInstanceState.getSerializable(KeyInstanceState_cuenta);
        }
        setHasOptionsMenu(true);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tabs.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            fragment_movements_list list;
            @Override
            public CharSequence getPageTitle(int position) {
                if(position==0){
                    return getString(R.string.resumen);
                }else{
                    return getString(R.string.movimientos);
                }

            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Fragment getItem(int position) {
                Fragment f=new FragmentN();
                if(position==0){
                    final resume resumen=new resume();
                    resumen.setOnLifeCycleListener(new LifeCycleObserver() {
                        @Override
                        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                            resumen.name.setText(cuenta.get_cuenta());
                            resumen.saldoInicial.setText(format(cuenta.getCantidadInicial_()));
                            resumen.ingresos.setText(format(db.getAllIngresoFrom(cuenta.get_cuenta())));
                            resumen.egresos.setText(format(db.getAllEgresoFrom(cuenta.get_cuenta())));
                        }

                        @Override
                        public void onResume() {

                        }
                    });
                    resumen.setAction(new Action() {
                        @Override
                        public Object execute(Intent intent) {
                            if(intent!=null && intent.hasExtra(name)){
                                String trackerChange="";
                                cuentas Cuenta= (cuentas) intent.getSerializableExtra(name);
                                if(cuenta.get_cuenta()!=Cuenta.get_cuenta()){
                                    trackerChange="Account_";
                                }
                                if(cuenta.getCantidadInicial_()!=Cuenta.getCantidadInicial_()){
                                    trackerChange=trackerChange+"Amount_";
                                }
                                ((MainActivity)getActivity()).AcountTracking("EditAccount",trackerChange);
                                AccountDetails.this.cuenta=Cuenta;
                                AccountDetails.this.saldoActual.setText(AccountDetails.format(cuenta.get_cantidad()));
                                ((MainActivity)getActivity()).setTitle(" "+cuenta.get_cuenta());
                                list.LoadQuery("SELECT * FROM basics WHERE cuenta_='"+cuenta.get_cuenta()+"'",null);
                                list.refreshData();

                            }
                            return null;
                        }
                    });
                    f=resumen;
                }else if(position==1){
                    list=new fragment_movements_list();
                    if(cuenta!=null){
                        list.LoadQuery("SELECT * FROM basics WHERE cuenta_='"+cuenta.get_cuenta()+"'",null);
                        list.setOnRefreshDataListener(new FragmentN.Action() {
                            @Override
                            public Object execute(Intent intent) {
                                if(intent!=null) {
                                    ((MainActivity)getActivity()).refreshReportForQuery(list.Queryonbasics,intent);
                                }
                                return null;
                            }
                        });
                    }
                    f=list;
                }
                return f;
            }
        });
        if(cuenta!=null){
            String cantidadActual=String.format("%.2f", cuenta.get_cantidad()).replace("-","-$");
            if(!cantidadActual.contains("$"))cantidadActual="$ "+cantidadActual;
            saldoActual.setText(cantidadActual);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(KeyInstanceState_cuenta,cuenta);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_reportsfragment, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //MenuItem menuItem = menu.findItem(R.id.menu_item_to_change_icon_for); // You can change the state of the menu item here if you call getActivity().supportInvalidateOptionsMenu(); somewhere in your code
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Handle actions based on the id field.
        if(item.getItemId()==R.id.action_borrar_cuenta){
            showOkCancelDialog("Estas seguro que deseas eliminar esta cuenta? Todos los datos se perderan y esta acción no se podra deshacer",
                    "Si Eliminar", "No", true, new Dialogs.DialogsInterface() {
                        @Override
                        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                            if(sucess){
                                if(db.deleteAllFrom(cuenta)>0){
                                    Intent intent=new Intent();
                                    intent.setAction(cuentas.CuentaEliminada);
                                    if(getAction()!=null)getAction().execute(intent);
                                }
                            }
                        }
                    });
            return true;
        }
        return false;
    }
  public static String format(double quantity){

      String format=String.format("%.2f", quantity).replace("-","-$");
      if(!format.contains("$"))format="$ "+format;
      return format;
  }

    public void setContainerForReport(FragmentManager fm, int containerForReport) {
        this.containerForReport = containerForReport;
        this.fm=fm;
    }

    public static class resume extends baseFragment implements View.OnClickListener, Dialogs.DialogsInterface {
        public EditText name;
        EditText saldoInicial;
        FontFitTextView ingresos;
        FontFitTextView egresos;
        Button deleteAccount;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            RelativeLayout LL= (RelativeLayout) inflater.inflate(R.layout.accountdetails_resume,container,false);
            name=LL.findViewById(R.id.accountdetails_resume_edit_name);
            saldoInicial=LL.findViewById(R.id.accountdetails_resume_edit_saldo_inicial);
            ingresos=LL.findViewById(R.id.accountdetails_resume_ingresos_saldo);
            egresos=LL.findViewById(R.id.accountdetails_resume_egresos_saldo);
            deleteAccount=LL.findViewById(R.id.accountdetails_resume_edit_account);
            deleteAccount.setOnClickListener(this);
            return LL;
        }

        @Override
        public void onClick(View view) {
            ((baseActivity) getActivity()).usabilityAppTracking(AnalyticsApplication.Gestures.Click,"onEditAccount");
           boolean isPremium=((baseActivity)getActivity()).mIsPremium;
            if(isPremium){
                CreateOrUpdateCuenta.DialogFragment editAccount=new CreateOrUpdateCuenta.DialogFragment().setCuenta(((AccountDetails)getParentFragment()).cuenta);
                editAccount.onResultlistener(new CreateOrUpdateCuenta.DialogFragment.onResult() {
                    @Override
                    public void onResult(cuentas cuenta) {
                        if(cuenta!=null){
                            name.setText(cuenta.get_cuenta());
                            saldoInicial.setText(AccountDetails.format(cuenta.getCantidadInicial_()));
                            if(resume.this.getAction()!=null){
                                Intent intent=new Intent();
                                intent.putExtra(AccountDetails.name,cuenta);
                                getAction().execute(intent);
                            }
                        }
                    }
                });
                editAccount.show(getChildFragmentManager(),"EditAccount");
            }else {
                showOkDialog("Esta función es exclusiva de la versión premium", "Premium", true, this);
            }
        }

        @Override
        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
            if(sucess){
                ((baseActivity) getActivity()).askForPremium("ForEditAccount");
            }
        }
    }
}
