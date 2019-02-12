package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

public class Tcredito extends tarjeta {
	final static public String sqlCreateTABLETcredito =
	"CREATE TABLE Tcredito (idTarjeta INTEGER,diapago INTEGER,interesnomoratorio REAL"+
	" interesnomoratorio REAL,comisionfaltadepago REAL)";
	private int diapago;//En caso de ser de credito
	private float interesnomoratorio;//
	private float interesmoratorio;
	private float comisionfaltadepago;
	
	public Tcredito() {
		super();
		setTipo("Credito");
	}
	
	public void setDiaPago(int DiaPago){diapago=DiaPago;}
	public int getDiaPag(){return diapago;}
	
	public void setInteresMoratorio(float DiaPago){interesmoratorio=DiaPago;}
	public float getInteresMoratorio(){return interesmoratorio;}
	
	public void setInteresNoMoratorio(float DiaPago){interesnomoratorio=DiaPago;}
	public float getInteresNoMoratorio(){return interesnomoratorio;}
	
	public void setComisionFaltaDePago(float DiaPago){comisionfaltadepago=DiaPago;}
	public float getComisionFaltaDePago(){return comisionfaltadepago;}

	@Override
	protected ContentValues ColumnsNameType() {
		return null;
	}
}
