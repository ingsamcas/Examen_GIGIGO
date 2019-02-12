package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentValues;

import java.util.Date;

import eclipseapps.library.backendless.ObjectBackendless;

/**
 * Created by usuario on 12/10/17.
 */

public class Tokens extends ObjectBackendless {
    private boolean Selected_;
    private String Token_;
    private String CardNumber_;
    private String HolderName_;
    private String Brand_;
    private long creationDate_;

    public String getToken_() {
        return Token_;
    }

    public void setToken_(String token_) {
        Token_ = token_;
    }

    public String getCardNumber_() {
        return CardNumber_;
    }

    public void setCardNumber_(String cardNumber_) {
        CardNumber_ = cardNumber_;
    }

    public String getBrand_() {
        return Brand_;
    }

    public void setBrand_(String brand_) {
        Brand_ = brand_;
    }

    public long getCreationDate_() {
        return creationDate_;
    }

    public void setCreationDate_(long creationDate_) {
        this.creationDate_ = creationDate_;
    }

    public Boolean getSelected_() {
        return Selected_;
    }

    public void setSelected_(boolean selected_) {
        Selected_ = selected_;
    }

    public String getHolderName_() {
        return HolderName_;
    }

    public void setHolderName_(String holderName_) {
        HolderName_ = holderName_;
    }

    @Override
    protected ContentValues ColumnsNameType() {
        ContentValues columnas = new ContentValues();
        columnas.put("Token_", Types.VARCHAR);//Token de tarjeta
        columnas.put("HolderName_", Types.VARCHAR);//Nombre de la tarjeta
        columnas.put("CardNumber_", Types.VARCHAR);//4242xxxxxxxxxx4242
        columnas.put("Brand_", Types.VARCHAR);//Visa o Mastercard
        columnas.put("creationDate_", Types.INTEGER);//tiempo
        columnas.put("Selected_", Types.INTEGER);//tiempo
        return columnas;
    }
}
