package eclipseapps.financial.moneytracker.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import eclipseapps.financial.moneytracker.R;

/**
 * Created by usuario on 08/07/17.
 */
public class billing_acountSelect extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.fragment_billing_accountselect,null);
        LinearLayout inAccount= (LinearLayout) ll.findViewById(R.id.f_billing_acountSelect_introduceacount);
        LinearLayout selectAcount=(LinearLayout)ll.findViewById(R.id.f_billing_acountSelect_listAccounts);
        ListView listAccounts=(ListView)ll.findViewById(R.id.f_billing_acountSelect_listAccounts_list);
        final EditText editTextAccount= (EditText) ll.findViewById(R.id.f_billing_acountSelect_editText_email);
        Button okbutton=(Button)ll.findViewById(R.id.f_billing_acountSelect_button_ok);
        Account[] accounts= AccountManager.get(inflater.getContext()).getAccountsByType("com.google");
        if (accounts.length > 0) {
            inAccount.setVisibility(View.GONE);
            selectAcount.setVisibility(View.VISIBLE);
            final List<String> lista=new ArrayList<>();
            for(int i=0;i<=accounts.length;i++){
                lista.add(accounts[i].name);
            }
            ArrayAdapter adapter=new ArrayAdapter(inflater.getContext(), R.layout.layout_spinner_iten,lista);
            listAccounts.setAdapter(adapter);
            listAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    showConfirmMessage(lista.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    //Si por alguna razon no completo el usuario
                }
            });
        } else {//Si no hay cuenta de google le permite ingresar un correo electronico
            inAccount.setVisibility(View.VISIBLE);
            selectAcount.setVisibility(View.GONE);
            okbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(android.util.Patterns.EMAIL_ADDRESS.matcher(editTextAccount.getText().toString()).matches()){
                        showConfirmMessage(editTextAccount.getText().toString());
                    }
                }
            });
        }
        return ll;
    }
    public void showConfirmMessage(String Account){
        AlertDialog.Builder AD= new AlertDialog.Builder(getContext());
        AD.setMessage(getResources().getString(R.string.RegistroGracias)+Account);
        AD.setPositiveButton(R.string.Ok,null);
        AD.show();
    }
    public static class Dialog_accountSelect extends DialogFragment {
        billing_acountSelect billSelect;
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            billSelect=new billing_acountSelect();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = billSelect.onCreateView(inflater,null,null);
            builder.setView(view);
            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            //Button positiveButton;
            //Button negativeButton;
            //AlertDialog d = (AlertDialog) getDialog();
            //if (d != null) {
            //    positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            //    positiveButton.setEnabled(false);
            //    negativeButton = d.getButton(AlertDialog.BUTTON_NEGATIVE);
            //    negativeButton.setEnabled(false);
            //}
        }
    }
}
