package eclipseapps.mobility.parkeame.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;

import java.util.TimerTask;

import eclipseapps.mobility.parkeame.R;
import eclipseapps.payments.UI.Fragments.AccountManager;

/**
 * Created by usuario on 04/11/17.
 */

public class listElementCar extends LinearLayout {
    private boolean selected;


    private String tipo;//Sedan camioeta etc...
    private String modelo;//fusion...
    private String placas;
    private ImageView imageTipo;
    private TextView modeloView;
    private TextView placasView;
    private ImageView selectedimage;
    ImageLoader imLoader;
    private void initview(Context context){
        View view= LayoutInflater.from(context).inflate(R.layout.m_rowcarro_customview, this, false);
        addView(view);
        imageTipo =view.findViewById(R.id.m_tipo);
        modeloView =view.findViewById(R.id.m_modeloview);
        placasView =view.findViewById(R.id.m_placasview);
        selectedimage=view.findViewById(R.id.m_selected);
        imLoader=ImageLoader.getInstance();
        imLoader.init(ImageLoaderConfiguration.createDefault(context));
    }

    public listElementCar(Context context) {
        super(context);
        initview(context);
    }

    public listElementCar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initview(context);
    }

    public listElementCar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initview(context);
    }

    public String getModelo() {
        return modelo;
    }

    public listElementCar setModelo(String card) {
        this.modelo = card;
        refresh();
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public listElementCar setTipo(String brand) {
        this.tipo = brand;
        refresh();
        return this;
    }


    public boolean isSelected() {
        return selected;
    }


    public listElementCar Selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public String getPlacas() {
        return placas;
    }

    public listElementCar setPlacas(String placas) {
        this.placas = placas;
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        refresh();
    }

    public listElementCar refresh(){
        if (imageTipo !=null){
            imageTipo.post(new TimerTask() {
                @Override
                public void run() {
                    if (tipo !=null){
                        final String[] tipos=getResources().getStringArray(R.array.Tipo);
                        if (tipo.matches(tipos[0])){
                            Picasso.with(getContext()).load(R.drawable.ic_directions_car_white_48dp).fit().into(imageTipo);
                        }else if (tipo.matches(tipos[1])){
                            Picasso.with(getContext()).load(R.drawable.ic_local_shipping_white_48dp).fit().into(imageTipo);
                        }else if (tipo.matches(tipos[2])){
                            Picasso.with(getContext()).load(R.drawable.ic_airport_shuttle_white_48dp).fit().into(imageTipo);
                        }else if (tipo.matches(tipos[3])){
                            Picasso.with(getContext()).load(R.drawable.ic_motorcycle_white_48dp).fit().into(imageTipo);
                        }
                    }
                }
            });
        }
        if (modelo !=null){
            modeloView.post(new TimerTask() {
                @Override
                public void run() {
                    if (modeloView !=null){
                        modeloView.setText(modelo);
                    }
                }
            });
        }
        if (placas !=null){
            placasView.post(new TimerTask() {
                @Override
                public void run() {
                    if (placasView !=null){
                        placasView.setText(placas);
                    }
                }
            });
        }
        if (selected){
            selectedimage.setVisibility(VISIBLE);
            imLoader.displayImage("drawable://"+eclipseapps.payments.R.drawable.ic_check_circle_black_48dp,selectedimage);
        }else{
            selectedimage.setVisibility(INVISIBLE);
        }
        return this;
    }
}
