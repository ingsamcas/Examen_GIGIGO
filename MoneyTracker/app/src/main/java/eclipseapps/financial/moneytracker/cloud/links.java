package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

public class links extends ObjectBackendless {
	//final static public String sqlCreateTABLEddescripimagenes="CREATE TABLE descripimagenes(id INTEGER,imagen VARCHAR)";
	
	private int id_;//id de movimiento al que se encuentra asociada la imagen
	private String link_;
	
	public int getId(){return id_;}
	public void setId(int ID){
		id_ =ID;}

	public String getLink(){return link_;}
	public void setLink(String Link){
		link_ =Link;}

	@Override
	protected ContentValues ColumnsNameType() {

		ContentValues columnas=new ContentValues();
		columnas.put("id_",Types.INTEGER);
		columnas.put("link_",Types.VARCHAR);
		columnas.put(Types.FOREIGN_KEY("id_"),Types.REFERENCES_ONDELETECASCADE("basics","id_"));
		return columnas;
	}
	/*
	Actualizacion de version 3 a 4
	 */
	@Override
    public Map retriveUpdateColumnName(int olversion,int newversion) {
		Map columnRelation=super.retriveUpdateColumnName(olversion,newversion);
		if(olversion==3){
			columnRelation.put("link","link_");
			columnRelation.put("_id","id_");
			columnRelation.put("_objectId","objectId");
			columnRelation.put("_created","created");
			columnRelation.put("_updated","updated");
		}else if(olversion==7){
			columnRelation.put("link_","link_");
			columnRelation.put("id_","id_");//Se modifica como foreign key
			columnRelation.put("objectId","objectId");
			columnRelation.put("created","created");
			columnRelation.put("updated","updated");
		}

		return columnRelation;
    }
}