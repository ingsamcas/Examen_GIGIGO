package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eclipseapps.financial.moneytracker.R;

/**
 * Created by usuario on 18/04/17.
 */
public class spinnerCheckbox extends TextView implements View.OnClickListener,DialogInterface.OnCancelListener ,DialogInterface.OnMultiChoiceClickListener{
    boolean[] Selected;
    List<String> _options;
    Context _context;
    ListView lista;
    CheckBox cbAll;
    AlertDialog dialog;
    public spinnerCheckbox(Context context) {
        super(context);
        _context = context;
    }
    public spinnerCheckbox(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        _context = context;
    }

    public spinnerCheckbox setOptions(final List<String> options) {
        _options = options;
        _options.add(0,getResources().getString(R.string.All));
        if(_options.size()>0){
            Selected=new boolean[_options.size()];
            for (int j=0;j<Selected.length;j++) {
                Selected[j]=true;
            }
        }else{
            Selected=new boolean[0];
        }
        SetText();
        setOnClickListener(this);
        return this;
    }
    private void SetText(){
        if (isSelected(getResources().getString(R.string.All))){setText(getResources().getString(R.string.All));}
        else{
            List<String> selected=getSelected();
            if (selected.size()==0){setText(getResources().getString(R.string.Ninguna));}
            else{
                String S="";
                for (String s:selected) {
                    S=S+","+s;
                }
                S=S.substring(1);
                setText(S);
            }
        }
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < _options.size(); i++) {
           // if (selected[i] == true) {
             //   spinnerBuffer.append(_options.get(i));
               // spinnerBuffer.append(", ");
           // } else {
             //   someUnselected = true;
            //}
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        } else {
           // spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { "spinnerText" });
        //listener.onItemsSelected(selected);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i, boolean b) {

    }

    public List getSelected() {
        List selected=new ArrayList<String>();
        for (int j=0;j<_options.size();j++){
            if (Selected[j] && !_options.get(j).matches(getResources().getString(R.string.All)))selected.add(_options.get(j));
        }
        return selected;
    }
    public boolean isSelected(CompoundButton compoundButton){
        return Selected[_options.indexOf(compoundButton.getText().toString())];
    }
    public boolean isSelected(String option){
        return Selected[_options.indexOf(option)];
    }
    public void addSelected(String selected){
        Selected[_options.indexOf(selected)]=true;
    }
    public void removeSelected(String selected){
        Selected[_options.indexOf(selected)]=false;
    }

    @Override
    public void onClick(View view) {
        if (dialog==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


            lista=new ListView(_context);
            lista.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            lista.setAdapter(new ArrayAdapter<String>(_context,R.layout.layout_spinner_item_checkbox) {



                public void select(){
                    // else if (!b && Selected.contains(compoundButton.getText())) {
                    //   Selected.remove(compoundButton.getText());
                    //}
                }
                @Override
                public void registerDataSetObserver(DataSetObserver dataSetObserver) {

                }

                @Override
                public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

                }

                @Override
                public int getCount() {

                    return _options.size();
                }

                @Override
                public String getItem(int i) {
                    return _options.get(i);
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
                    CheckBox cbTag;
                    if (view == null) {
                        cbTag = (CheckBox) LayoutInflater.from(_context).inflate(R.layout.layout_spinner_item_checkbox, null);
                    } else {
                        cbTag = (CheckBox) view;
                    }
                    cbTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if(compoundButton.getText().toString().matches(getResources().getString(R.string.All))){
                                if (b){
                                    for (int j=0;j<Selected.length;j++) {
                                        Selected[j]=true;
                                    }
                                    lista.invalidateViews();
                                }else {
                                    boolean refresh=true;
                                    for (int j=0;j<Selected.length;j++) {
                                        if (Selected[j]==false)refresh=false;
                                    }
                                    if(refresh){
                                        for (int j=0;j<Selected.length;j++) {
                                            Selected[j]=false;
                                        }
                                        lista.invalidateViews();
                                    }
                                }

                            }else{

                                if (isSelected(compoundButton)&& !b){
                                    removeSelected(compoundButton.getText().toString());
                                    if (isSelected(getResources().getString(R.string.All))){
                                        removeSelected(getResources().getString(R.string.All));
                                        lista.invalidateViews();
                                    }
                                }else if(!isSelected(compoundButton)&& b){
                                    addSelected(compoundButton.getText().toString());
                                }

                            }
                        }
                    });

                    cbTag.setText((CharSequence) _options.get(i));
                    cbTag.setChecked(Selected[i]);
                    if (_options.get(i).contains(getResources().getString(R.string.All)))cbAll=cbTag;
                    return cbTag;

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
            builder.setView(lista);
            builder.setPositiveButton(R.string.Ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            SetText();
                        }
                    });
            builder.setOnCancelListener(this);
            dialog=builder.show();
        }else{
            dialog.show();
        }
    }
}

