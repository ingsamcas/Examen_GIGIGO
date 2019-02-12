package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;



public class Bancos extends ObjectBackendless {

final static public String sqlCreateTABLEBancos = "CREATE TABLE Bancos (idBanco INTEGER,String Banco)";
private int idBanco;
private String Banco;


public int getIdBanco(){return idBanco;}
public void setIdBanco(int IDBanco){idBanco=IDBanco;}

public String getBanco(){return Banco;}
public void setBanco(String BANCO){Banco=BANCO;}


    @Override
    protected ContentValues ColumnsNameType() {
        return null;
    }
}
