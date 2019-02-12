package eclipseapps.mobility.parkeame.customviews;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import eclipseapps.library.backendless.interfaces.RegisterForm;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.user;

/**
 * Created by usuario on 19/09/17.
 */

public class NameRequest extends RegisterForm {
    EditText name;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout GeneralView= (RelativeLayout) inflater.inflate(R.layout.k_name_request,container,false);
        name=GeneralView.findViewById(R.id.name_request_input_name);
        return GeneralView;
    }

    @Override
    public Bundle getFormValues() {
        Bundle resultado=new Bundle();
        resultado.putString(new user(DBParkeame.getInstance(getActivity())).name,name.getText().toString());
        return resultado;
    }

    @Override
    public boolean checkAllCorrect() {
        if (name.getText()==null || name.getText().toString().matches("")){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public String getError() {
        if (name.getText()==null || name.getText().toString().matches("")){
            return "Introduce tu nombre porfavor";
        }else return null;
    }
}
