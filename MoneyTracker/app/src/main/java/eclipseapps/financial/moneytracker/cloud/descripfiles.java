package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

public class descripfiles extends ObjectBackendless {
	
	private int _id;//id de movimiento al que se encuentra asociada la imagen
	private String _file;
	
	public int get_id(){return _id;}
	public void set_id(int ID){_id=ID;}

	public String get_file(){return _file;}
	public void set_file(String file){_file=file;}

	@Override
	protected ContentValues ColumnsNameType() {
		ContentValues cv=new ContentValues();
		cv.put("id_",Types.INTEGER);
		cv.put("file_",Types.VARCHAR);
		cv.put(Types.FOREIGN_KEY("id_"),Types.REFERENCES_ONDELETECASCADE("basics","id_"));
		return cv;
	}
	@Override
    public Map retriveUpdateColumnName(int oldversion,int newversion) {
		Map columnRelation=super.retriveUpdateColumnName(oldversion,newversion);
		if(oldversion==3){
			columnRelation.put("_file","file_");
			columnRelation.put("_id","id_");
			columnRelation.put("_objectId","objectId");
			columnRelation.put("_created","created");
			columnRelation.put("_updated","updated");
		}else if(oldversion==7){
			columnRelation.put("file_","file_");
			columnRelation.put("id_","id_");//Esta columna se actualizo como foreign key
			columnRelation.put("objectId","objectId");
			columnRelation.put("created","created");
			columnRelation.put("updated","updated");
		}
		return columnRelation;
    }
}