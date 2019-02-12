package eclipseapps.mobility.trackergps.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import eclipseapps.android.FragmentN;
import eclipseapps.mobility.trackergps.R;


/**
 * Created by usuario on 15/04/18.
 */

public class edit_user_account extends FragmentN {
    private String imageDir;
    private String email="";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout LL= (LinearLayout) inflater.inflate(R.layout.edit_user_account,container,false);
        ImageView imageView=LL.findViewById(R.id.edit_user_account_imageUser);
        TextView id=LL.findViewById(R.id.edit_user_account_idUser);
        if(!email.matches(""))id.setText(email);
        // if(!imageDir.matches(""))imageView.
        return LL;
    }
    public edit_user_account setEmail(String email){
        this.email=email;
        return this;
    }
}
