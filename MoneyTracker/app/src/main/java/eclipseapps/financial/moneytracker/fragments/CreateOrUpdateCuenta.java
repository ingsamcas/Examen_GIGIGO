package eclipseapps.financial.moneytracker.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.cuentas;

/**
 * Created by usuario on 14/03/17.
 */
public class CreateOrUpdateCuenta extends baseFragment {
    public final static String name="CreateOrUpdateCuenta";
    private EditText cuenta,cantidad;
    private SwitchCompat saldo;
    private TextView saldo_textview;
    private int Direction;
    private DBSmartWallet db;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.fragment_nueva_cuenta,null);
        cuenta= (EditText) ll.findViewById(R.id.frag_nuevacuenta_editText_cuenta);
        cantidad= (EditText) ll.findViewById(R.id.frag_nuevacuenta_editText_quantity);
        saldo= (SwitchCompat) ll.findViewById(R.id.frag_nuevacuenta_switch_income_outcome);
        saldo_textview= (TextView) ll.findViewById(R.id.frag_nuevacuenta_switch_income_outcome_textview);
        CompoundButton.OnCheckedChangeListener Listener=new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){saldo.setThumbResource(R.drawable.switch_income);saldo_textview.setText("Saldo a favor");Direction=1;}
                else {saldo.setThumbResource(R.drawable.switch_outcome);saldo_textview.setText("Saldo en contra");Direction=-1;}
            }
        };
        saldo.setOnCheckedChangeListener(Listener);
        Listener.onCheckedChanged(saldo,true);
        return ll;
    }
    public String getCuenta(){
        return cuenta.getEditableText().toString();
    }
    public Double getCantidad(){
        if (!cantidad.getEditableText().toString().matches(""))
        return Double.valueOf(cantidad.getEditableText().toString());
        else return 0.0;
    }
    public int getDirection(){
        return Direction;
    }

    public cuentas addAccount(Context context){
        if (!(getCantidad()==0)&&!getCuenta().matches("")){
            if (db==null)db=DBSmartWallet.getInstance(context);
            cuentas Cuenta=new cuentas();
            Cuenta.set_id(db.NextId(cuentas.class.getSimpleName()));
            Cuenta.set_cuenta(getCuenta());
            Cuenta.set_cantidad(getCantidad()*getDirection());
            Cuenta.setCantidadInicial_(getCantidad()*getDirection());
            Cuenta.savein(db);
            return Cuenta;
        }else{
           return null;
        }
    }
    public cuentas editAccount(Context context,cuentas Cuenta){
        if (Cuenta!=null && !(getCantidad()==0)&&!getCuenta().matches("")){
            if (db==null)db=DBSmartWallet.getInstance(context);
            double ingresos=db.getAllIngresoFrom(Cuenta.get_cuenta());
            double egresos=db.getAllEgresoFrom(Cuenta.get_cuenta());
            String oldAccount=Cuenta.get_cuenta();
            Cuenta.set_cuenta(getCuenta());
            Cuenta.set_cantidad(getCantidad()*getDirection()+ingresos+egresos);
            Cuenta.setCantidadInicial_(getCantidad()*getDirection());
            Cuenta.update(db,"id_="+Cuenta.get_id());
            db.updateBasicsTable("cuenta_",getCuenta(),"cuenta_='"+oldAccount+"'");
            return Cuenta;
        }else{
            return null;
        }
    }
    public static class DialogFragment extends android.support.v4.app.DialogFragment{
        DialogType type=DialogType.AddAccount;

        CreateOrUpdateCuenta modifyCuenta;
        onResult _listener;
        public cuentas Cuenta;

        public DialogFragment setType(DialogType type) {
            this.type = type;
            return this;
        }

        public DialogFragment setCuenta(cuentas cuenta) {
            Cuenta = cuenta;
            this.type =DialogType.EditAccount;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder build=new AlertDialog.Builder(getActivity());
            build.setPositiveButton(R.string.dialogfragment_acept,null);
            build.setNegativeButton(R.string.dialogfragment_cancel, null);
            if(type==DialogType.AddAccount){
                build.setTitle(R.string.cuenta_nueva);
            }else if(type==DialogType.EditAccount){
                build.setTitle(R.string.editar_cuenta);
            }
            modifyCuenta =new CreateOrUpdateCuenta();
            build.setView(modifyCuenta.onCreateView(getActivity().getLayoutInflater(),null,null));
            return build.create();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if(type==DialogType.EditAccount && Cuenta!=null){
                modifyCuenta.cuenta.setText(Cuenta.get_cuenta());
                modifyCuenta.cantidad.setText(String.valueOf(Math.abs(Cuenta.getCantidadInicial_())));
               if(Cuenta.getCantidadInicial_()<0){
                   modifyCuenta.Direction=-1;
                   modifyCuenta.saldo.setChecked(false);
               }else{
                   modifyCuenta.Direction=1;
                   modifyCuenta.saldo.setChecked(true);
               }
            }
        }

        public void onStart()
        {
            super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
            AlertDialog d = (AlertDialog)getDialog();
            if(d != null)
            {
                Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (type==DialogType.AddAccount){
                            Cuenta= modifyCuenta.addAccount(getActivity());
                            if (Cuenta!=null){
                                dismiss();
                                if(_listener!=null)_listener.onResult(Cuenta);
                            }else{
                                return;
                            }
                        }else if(type==DialogType.EditAccount){
                            Cuenta= modifyCuenta.editAccount(getActivity(),Cuenta);
                            if (Cuenta!=null){
                                dismiss();
                                if(_listener!=null)_listener.onResult(Cuenta);
                            }else{
                                return;
                            }
                        }


                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });
            }

        }
        public DialogFragment onResultlistener(onResult listener){
            _listener=listener;
            return this;
        }
        public interface onResult{
            public void onResult(cuentas cuenta);
        }
        public enum DialogType{
            AddAccount("AddAccount"),EditAccount("EditAccount");
            DialogType(String dialogType) {
                value=dialogType;
            }
            private String value;
            public String getValue() {
                return value;
            }
        }
    }

}
