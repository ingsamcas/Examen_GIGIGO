package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

public class Tdebito extends tarjeta{
	final static public String sqlCreateTABLETdebito =
	"CREATE TABLE Tdebito (idTarjeta INTEGER,saldopromediominimo REAL,cuentaCheques VARCHAR)";
	private float saldopromediominimo;//Para debito en caso de pedirlo como por ejemplo "periles"
	private String cuentaCheques;//Cuenta de cheques asociada
	public Tdebito() {
		// TODO Auto-generated constructor stub
		super();
		setTipo("Debito");
	}
	public void setsaldopromediominimo(float Saldopromediominimo){saldopromediominimo=Saldopromediominimo;}
	public float gersaldopromediominimo(){return saldopromediominimo;}
	
	public void setcuentaCheques(String CuentaCheques){cuentaCheques=CuentaCheques;}
	public String gercuentaCheques(){return cuentaCheques;}

	@Override
	protected ContentValues ColumnsNameType() {
		return null;
	}
}
