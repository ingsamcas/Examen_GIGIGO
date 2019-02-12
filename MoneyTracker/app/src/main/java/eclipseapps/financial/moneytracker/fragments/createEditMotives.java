package eclipseapps.financial.moneytracker.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;
import java.util.regex.Pattern;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.motives;

/**
 * Created by usuario on 14/03/17.
 */
public class createEditMotives extends baseFragment {
    private EditText Tag;
    private DBSmartWallet db;
    private static validatorTag _inter;
    private List<motives> m;
    private motives categoria;
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.fragment_nuevo_tag,null);
        Tag= (EditText) ll.findViewById(R.id.frag_nuevotag_editText_tag);
        if(categoria!=null){
            Tag.setHint(categoria.get_motive());
            Tag.setText(categoria.get_motive());
        }
        if (db==null)db=DBSmartWallet.getInstance(getActivity());
        Cursor cur=db.getDBInstance().rawQuery("SELECT * FROM motives",null);
        m=db.mapCursorToObjectList(cur,motives.class);
        Tag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (_inter!=null){
                    boolean contains=false;
                    for (motives tag:m) {
                        if (tag.get_motive().matches(Pattern.quote(editable.toString())))contains=true;
                    }
                    _inter.onAvailableTag(!contains);
                }
            }
        });
        return ll;
    }
    public String get_Tag(){
        return Tag.getEditableText().toString();
    }
    public createEditMotives setOnValidatorTag(validatorTag inter){
        _inter=inter;
        return this;
    }
    public List<motives> saveTag(Context context){
        if (!(get_Tag().matches(""))){
            if (db==null)db=DBSmartWallet.getInstance(context);
            if(categoria!=null){
                final String nombreAnterior=categoria.get_motive();
                categoria.set_motive(get_Tag());
                categoria.update(db,"motive_='"+nombreAnterior+"'");
                Cursor cur=db.getDBInstance().rawQuery("SELECT * FROM motives ORDER BY motive_",null);
                m=db.mapCursorToObjectList(cur,motives.class);

            }else{
                motives tag=new motives();
                tag.set_motive(get_Tag());
                tag.set_enabled(true);
                tag.savein(db);
                m.add(tag);
            }

            return m;
        }else{
           return null;
        }
    }

    public createEditMotives setCategoria(motives categoria) {
        this.categoria = categoria;
        return this;
    }

    public interface validatorTag{
        void onAvailableTag(boolean isvalid);
    }
    public static class DialogFragment extends android.support.v4.app.DialogFragment{
        motives motiveToEdit;
        createEditMotives writeMotives;
        onResult _listener;
        public motives Motivo;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder build=new AlertDialog.Builder(getActivity());
            build.setPositiveButton(R.string.dialogfragment_acept,null);
            build.setNegativeButton(R.string.dialogfragment_cancel, null);
            if(motiveToEdit==null){
                build.setTitle(R.string.nueva_categoria);
            }else{
                build.setTitle(R.string.editar_categoria);
            }

            writeMotives =new createEditMotives().setCategoria(motiveToEdit);
            build.setView(writeMotives.onCreateView(getActivity().getLayoutInflater(),null,null));
            return build.create();
        }
        public void onStart()
        {
            super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
            final AlertDialog d = (AlertDialog)getDialog();
            if(d != null) {
                final Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                        List motives= writeMotives.saveTag(getActivity());
                        if(_listener!=null)_listener.onResult(motives, writeMotives.get_Tag());
                    }
                });
                writeMotives.setOnValidatorTag(new validatorTag() {
                    @Override
                    public void onAvailableTag(boolean isvalid) {
                        if (isvalid)positiveButton.setEnabled(true);
                        else positiveButton.setEnabled(false);
                    }
                });
            }
        }
        public DialogFragment onResultlistener(onResult listener){
            _listener=listener;
            return this;
        }

        public void editMotive(motives motive) {
            motiveToEdit=motive;
        }

        public interface onResult{
            public void onResult(List<motives> motivos,String nuevoTag);
        }
    }

}
