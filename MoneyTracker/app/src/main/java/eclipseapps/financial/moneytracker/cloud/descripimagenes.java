package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;

import java.util.Map;

import eclipseapps.library.backendless.ObjectBackendless;

public class descripimagenes extends ObjectBackendless {
	//final static public String sqlCreateTABLEddescripimagenes="CREATE TABLE descripimagenes(id INTEGER,imagen VARCHAR)";
	
	protected int id_;//id de movimiento al que se encuentra asociada la imagen
	protected String imagen_;
	protected String imagenCloud_;
	
	public int getId_(){return id_;}
	public void setId_(int ID){
		id_ =ID;}

	public String getImagen_(){return imagen_;}
	public void setImagen_(String file){
		imagen_ =file;}

	public String getImagenCloud_() {
		return imagenCloud_;
	}

	public void setImagenCloud_(String imagenCloud_) {
		this.imagenCloud_ = imagenCloud_;
	}

	@Override
	protected ContentValues ColumnsNameType() {

		ContentValues columnas=new ContentValues();
		columnas.put("id_",Types.INTEGER);
		columnas.put("imagen_",Types.VARCHAR);
		columnas.put("imagenCloud_",Types.VARCHAR);

		return columnas;
	}

	@Override
	protected ContentValues ConstraintsRelation() {
		ContentValues relations=super.ConstraintsRelation();
		relations.put(Types.FOREIGN_KEY("id_"),Types.REFERENCES_ONDELETECASCADE("basics","id_"));
		return relations;
	}

	/*
    Actualizacion de version 3 a 4
     */
	@Override
    public Map retriveUpdateColumnName(int oldversion,int newversion) {
		Map columnRelation=super.retriveUpdateColumnName(oldversion,newversion);
		if(oldversion==7){
			columnRelation.put("imagen_","imagen_");
			columnRelation.put("id_","id_");//Se coloca como foregin key
			columnRelation.put("objectId","objectId");
			columnRelation.put("created","created");
			columnRelation.put("updated","updated");
		}

		return columnRelation;
    }

}