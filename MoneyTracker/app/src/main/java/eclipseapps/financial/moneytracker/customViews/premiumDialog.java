package eclipseapps.financial.moneytracker.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import eclipseapps.android.customviews.TextViewRoboto;
import eclipseapps.financial.moneytracker.R;

public class premiumDialog extends RelativeLayout {
    ViewGroup mcontainer;
    ImageView promoImage;
    TextViewRoboto titulo;
    TextViewRoboto precio;
    TextViewRoboto okButton;
    TextViewRoboto cancelButton;
    Context context;

    public premiumDialog(Context context) {
        super(context);
        init(context);
    }
    public premiumDialog(Context context,ViewGroup container) {
        super(context);
        mcontainer=container;
        init(context);

    }

    public premiumDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public premiumDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        this.context=context;
        inflate(getContext(),R.layout.premiumdialog,this);
        promoImage=findViewById(R.id.premiumdialog_image);
        titulo=findViewById(R.id.premiumdialog_titulo);
        precio=findViewById(R.id.premiumdialog_precio);
        okButton=findViewById(R.id.premiumdialog_okButton);
        cancelButton=findViewById(R.id.premiumdialog_cancelButton);

    }


    public ImageView getPromoImage() {
        return promoImage;
    }

    public premiumDialog setPromoImage(int drawablePromoImage) {
        if(drawablePromoImage!=0){
            ImageLoader imageLoader = ImageLoader.getInstance();
            if(!imageLoader.isInited()){
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
            }
            imageLoader.displayImage("drawable://" + drawablePromoImage, promoImage);
        }
        return this;
    }

    public TextViewRoboto getTitulo() {
        return titulo;
    }

    public premiumDialog setTitulo(TextViewRoboto titulo) {
        this.titulo = titulo;
        return this;
    }

    public TextViewRoboto getPrecio() {
        return precio;
    }

    public premiumDialog setPrecio(TextViewRoboto precio) {
        this.precio = precio;
        return this;
    }

    public TextViewRoboto getOkButton() {
        return okButton;
    }

    public premiumDialog setOkButton(String okButton,View.OnClickListener onClickListener) {
        this.okButton.setText(okButton);
        this.okButton.setOnClickListener(onClickListener);
        return this;
    }

    public TextViewRoboto getCancelButton() {
        return cancelButton;
    }

    public premiumDialog setCancelButton(TextViewRoboto cancelButton) {
        this.cancelButton = cancelButton;
        return this;
    }
}
