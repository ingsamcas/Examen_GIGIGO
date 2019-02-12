package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

public class tarjeta extends ObjectBackendless{
final static public String sqlCreateTABLETarjeta=
		"CREATE TABLE tarjeta (idTarjeta INTEGER,numero VARCHAR,tipo VARCHAR,clase VARCHAR,"+
"emisor VARCHAR,diacorte INTEGER,frecuenciaUsoMinima INTEGER,descripcion VARCHAR)";
private int idTarjeta;
private String numero;//**** **** **** 0001
private String tipo;// Debito o credito
private String clase;//Visa o mastercard
private String emisor;//Banco emisor
private int diacorte;//Los 13 de cada mes
private int frecuenciaUsoMinima;//Bandera que especifica si una tarjeta debe ser usado como minimo una vez al mes
private String descripcion;//Aqui puede colocar datos extras como si es la tarjeta de nomina o es un monedero electonico
							//O si lo desean el ccv o la clave del cajero
private float iva;//impuesto que paga la tarjeta

//O si lo desean el ccv o la clave del cajero

public void setidTarjeta(int IDTarjeta){idTarjeta=IDTarjeta;}
public int getidTarjeta(){return idTarjeta;}

public void setNumero(String Terminacion){numero=Terminacion;}
public String getNumero(){return numero;}

public void setTipo(String Tipo){tipo=Tipo;}
public String getTipo(){return tipo;}

public void setClase(String Clase){clase=Clase;}
public String getClase(){return clase;}

public void setEmisor(String Emisor){emisor=Emisor;}
public String getEmisor(){return emisor;}

public void setDiaCorte(int DiaCorte){diacorte=DiaCorte;}
public int getDiaCorte(){return diacorte;}

public void setFrecuenciaUsoMinima(int frec){frecuenciaUsoMinima=frec;}
public int getFrecuenciaUsoMinima(){return frecuenciaUsoMinima;}

public void setDescripcion(String Descripcion){descripcion=Descripcion;}
public String gerDescripcion(){return descripcion;}

public void setIva(float Descripcion){iva=Descripcion;}
public float getIva(){return iva;}

	@Override
	protected ContentValues ColumnsNameType() {
		return null;
	}
}
