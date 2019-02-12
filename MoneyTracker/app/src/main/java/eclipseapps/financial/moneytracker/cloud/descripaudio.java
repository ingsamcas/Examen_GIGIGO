package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import eclipseapps.library.backendless.ObjectBackendless;

public class descripaudio extends ObjectBackendless{
	final static public String sqlCreateTABLEddescripaudio="CREATE TABLE descripaudio(id INTEGER,audio VARCHAR)";
	
	private int id;//id de movimiento al que se encuentra asociada el audio
	private String audio;
	
	public int getId(){return id;}
	public void setId(int ID){id=ID;}

	public String getAudio(){return audio;}
	public void setAudio(String file){audio=file;}

	@Override
	protected ContentValues ColumnsNameType() {
		return null;
	}
}
