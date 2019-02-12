package eclipseapps.financial.moneytracker.cloud;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.ajts.androidmads.library.SQLiteToExcel;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.trackedActivity;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.general;
import eclipseapps.library.databases.databaseHelper;


public class DBSmartWallet extends databaseHelper implements Serializable{
	public enum datesFilter {
		Hoy,Ayer,EstaSemana,sietedias,EsteMes,treintadias,mespasado, Hoy_haceUnaSemana, Ayer_haceUnaSemana, SemanaPasadaAlMismoDia, MesPasadoAlMismoDia, total
	}
	static private SQLiteDatabase DB;//Objeto que se utiliza para accesar a todas las consultas de las bases de datos
		private Cursor Cur;//Objeto que se utiliza para apuntar a los cursores de las consultas

		

		static public String sqlGetAllTags="SELECT * FROM motives";
		static private String sqlGetAllActiveAlarmsAtDay="SELECT * FROM alarmas WHERE activa='1' AND dia=? AND mes=? AND anuario=?";
		static private String sqlGetAllInactive="SELECT * FROM alarmas WHERE activa='0' ORDER BY anuario,mes,dia ASC";
		static private String sqlGetAllActive="SELECT * FROM alarmas WHERE activa='1'ORDER BY anuario,mes,dia ASC";
		static private String sqlDeleteAllRecords="DELETE FROM alarmas";

		static public String sqlGetAllAcounts="SELECT * FROM cuentas";
		static private String sqlGetAlarmSettings="SELECT  * FROM config WHERE alarma=?";
	    static DBSmartWallet dbsmartwallet;
		public static DBSmartWallet getInstance(Context context){
			if (dbsmartwallet==null) dbsmartwallet=new DBSmartWallet(context, context.getResources().getString(R.string.databasename), null,context.getResources().getInteger(R.integer.version_localsmartwallet));
			return dbsmartwallet;
		}
		public DBSmartWallet(Context context, String name, CursorFactory factory,
				int version) {
			
			super(context, name, factory, version);
			
			Cont=context;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			Log.w("DBFinanzas","Creando la base de datos");
			//db.op
			DB=db;
			DB.execSQL("PRAGMA foreign_keys=ON");
			DB.execSQL(new basics().retriveCreateSentence());//Tabla de version 1
			DB.execSQL(new tags().retriveCreateSentence());//Tabla de version 1
			DB.execSQL(new motives().retriveCreateSentence());//Tabla de version 1
			DB.execSQL(new descripfiles().retriveCreateSentence());//Tabla de version 1
			DB.execSQL(new cuentas().retriveCreateSentence());//Tabla de version 1
			DB.execSQL(new user().getCreateSentence());//Tabla de version 2
			DB.execSQL(new descripimagenes().retriveCreateSentence());//Tabla de version 2.
			DB.execSQL(new links().retriveCreateSentence());//tabla version 3
			//--->Salto de la version 4
			/* En la version 5(Version de produccion) se cambiaron los nombres de las columnas:_columna se renombro como columna_*/
			DB.execSQL(new rfc().retriveCreateSentence());//Tabla de la version 6
			DB.execSQL(new facturas().retriveCreateSentence());//Tabla de la version 6
			DB.execSQL(new ventas().retriveCreateSentence());//Tabla de la version 7
			String[] m=Cont.getResources().getStringArray(R.array.tags);

			for (String tag:m) {
				motives motivo= new motives();
				motivo.set_motive(tag);
				motivo.set_enabled(true);
				String sqlCreate=motivo.retriveInsertSentence();
				DB.execSQL(sqlCreate);
			}

			cuentas Cuenta=new cuentas();
			Cuenta.set_cantidad(0.0);
			Cuenta.set_id(NextId(cuentas.class.getSimpleName()));
			Cuenta.set_cuenta(Cont.getString(R.string.cash));
			DB.execSQL(Cuenta.retriveInsertSentence());


		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			if (oldVersion==newVersion && db.isOpen())db.close();
			else {
				Log.d("onUpgrade","oldVersion and new Version are diferent");
				switch(oldVersion) {
					case 1:
						db.execSQL(new user().getCreateSentence());
						db.execSQL(new descripimagenes().retriveCreateSentence());//Tabla de version 2
					case 2:
						db.execSQL(new links().retriveCreateSentence());
					case 3:
						basics Basics=new basics();
						renameColumns(db,"basics",Basics.retriveCreateSentence(),Basics.retriveUpdateColumnName(oldVersion,newVersion));

						tags Tags=new tags();
						renameColumns(db,"tags",Tags.retriveCreateSentence(),Tags.retriveUpdateColumnName(oldVersion,newVersion));

						motives Motives=new motives();
						renameColumns(db,"motives",Motives.retriveCreateSentence(),Motives.retriveUpdateColumnName(oldVersion,newVersion));

						descripfiles Descripfiles=new descripfiles();
						renameColumns(db,"descripfiles",Descripfiles.retriveCreateSentence(),Descripfiles.retriveUpdateColumnName(oldVersion,newVersion));

						cuentas Cuentas=new cuentas();
						renameColumns(db,"cuentas",Cuentas.retriveCreateSentence(),Cuentas.retriveUpdateColumnName(oldVersion,newVersion));
					case 5://La version 4 se salto y se paso a la 5(en produccion)
						db.execSQL(new rfc().retriveCreateSentence());
						db.execSQL(new facturas().retriveCreateSentence());
						db.execSQL("ALTER TABLE USERS_NFGBackendless ADD COLUMN socialAccount VARCHAR");
						db.execSQL("ALTER TABLE descripimagenes ADD COLUMN imagenCloud_ VARCHAR");
						db.execSQL("ALTER TABLE cuentas ADD COLUMN cantidadInicial_ REAL");

						Cursor cur=db.rawQuery("SELECT * FROM cuentas",null);
						if (cur!=null && cur.getCount()>0){
							cur.moveToFirst();
							String cuenta="";
							float cantidad=0f;
							do{
								cantidad=0f;
								cuenta=cur.getString(cur.getColumnIndex("cuenta_"));
								if (cuenta!=null && !cuenta.matches("")){
									Cursor movs=db.rawQuery("SELECT * FROM basics WHERE cuenta_='"+cuenta+"'",null);
									if(movs!=null && movs.getCount()>0){
										movs.moveToFirst();
										do{
											cantidad=cantidad-movs.getFloat(movs.getColumnIndex("cantidad_"));
										}while (movs.moveToNext());
										db.execSQL("UPDATE cuentas SET cantidadInicial_="+(cur.getFloat(cur.getColumnIndex("cantidad_"))+cantidad)+" WHERE cuenta_='"+cuenta+"'");
									}
								}
							}while (cur.moveToNext());
						}
					case 6:
						new ventas().retriveCreateSentence();
                    case 7://En la version 4.8 de la aplicacion evoluciono la BD a version 8 para agregar foregin keys y poder borrar más facilmente

                        basics Basics8=new basics();//Se actualiza con id_ como primary key
                        renameColumns(db, "basics", Basics8.retriveCreateSentence(), Basics8.retriveUpdateColumnName(oldVersion, newVersion), new AlterDataBase() {
							@Override
							public void transferFunction(SQLiteDatabase dataBase, String oldTable, String newTable) {
								//Se cambia la columna _cuenta por _idcuenta
								dataBase.execSQL("UPDATE basics SET idcuenta_=(SELECT id_ FROM cuentas WHERE cuentas.cuenta_=basics.cuenta_)");
							}
						});

                        cuentas Cuentas8=new cuentas();//Se actualiza con id_ como primary key
                        renameColumns(db,"cuentas",Cuentas8.retriveCreateSentence(),Cuentas8.retriveUpdateColumnName(oldVersion,newVersion));

                        descripfiles Descripfiles8=new descripfiles();//Se actualiza con id_como foreign key
                        renameColumns(db,"descripfiles",Descripfiles8.retriveCreateSentence(),Descripfiles8.retriveUpdateColumnName(oldVersion,newVersion));

                        descripimagenes DescripImages8=new descripimagenes();//Se actualiza con id_como foreign key
                        renameColumns(db,"descripimagenes",DescripImages8.retriveCreateSentence(),DescripImages8.retriveUpdateColumnName(oldVersion,newVersion));

                        facturas Facturas=new facturas();//Se actualiza con id_como foreign key
                        renameColumns(db,"facturas",Facturas.retriveCreateSentence(),Facturas.retriveUpdateColumnName(oldVersion,newVersion));

                        links Links=new links();//Se actualiza con id_como foreign key
                        renameColumns(db,"links",Links.retriveCreateSentence(),Links.retriveUpdateColumnName(oldVersion,newVersion));

						db.execSQL("ALTER TABLE motives ADD COLUMN enabled_ INTEGER DEFAULT 1");//Se agrega la columna enabled_Si un movimiento es enabled entonces aparece en la lista de opciones al momento de hacer un movimiento
						db.execSQL("ALTER TABLE motives ADD COLUMN default_ INTEGER DEFAULT 0");//Se agrega la columna default_:Si un movimiento es default no es posible borrarlo

                        rfc RFC=new rfc();//Se actualiza con primariKey
                        renameColumns(db,"rfc",RFC.retriveCreateSentence(),RFC.retriveUpdateColumnName(oldVersion,newVersion));

                        tags Tags8=new tags();//Se actualiza con id_como foreign key
                        renameColumns(db,"tags",Tags8.retriveCreateSentence(),Tags8.retriveUpdateColumnName(oldVersion,newVersion));

				}
			}
		}


	public int NextId(String Table){
			 return general.FirstIntMissing(GetAllIds(Table));
		 }
		 public int[] GetAllIds(String Table){
			 return GetIntColumn("SELECT * FROM "+Table, null, "id_");
		 }
		 /*public int[] GetallActiveAlarms(){
			 //this.getReadableDatabase()
			return DataBase.GetIntColumn(this.getReadableDatabase(), sqlGetAllActive, null, "alarma");
		 }*/
		 public int[] GetallIdsDistinctbyDate(){
			return GetIntColumn(basics.retriveQuerys.sqlGetAllIdsDistinctDate, null, "id_");
		 }
		 public int[] GetAllIdsAt(int YEAR,int MONTH,int DAY,int HOUR,int MINUTES,int AMPM){
			// this.getReadableDatabase()
			return GetIntColumn( basics.retriveQuerys.sqlGetAllActiveAtTime0,
					new String[]{String.valueOf(YEAR),String.valueOf(MONTH),String.valueOf(DAY),String.valueOf(HOUR),String.valueOf(MINUTES),
				String.valueOf(AMPM)},"Id");
		 }
		 public int[] GetAllIdsAt(int YEAR,int MONTH,int DAY,int HOUR){
				// this.getReadableDatabase()
				return GetIntColumn(basics.retriveQuerys.sqlGetAllActiveAtTime1,
						new String[]{String.valueOf(YEAR),String.valueOf(MONTH),String.valueOf(DAY),String.valueOf(HOUR)},"Id");
			 }
		 public int[] GetAllIdsAt(int YEAR,int MONTH,int DAY){
				// this.getReadableDatabase()
				return GetIntColumn(basics.retriveQuerys.sqlGetAllActiveAtTime2,
						new String[]{String.valueOf(YEAR),String.valueOf(MONTH),String.valueOf(DAY)},"Id");
			 }
		 public int[] GetAllIdsAt(int YEAR,int MONTH){
				// this.getReadableDatabase()
				return GetIntColumn(basics.retriveQuerys.sqlGetAllActiveAtTime3,
						new String[]{String.valueOf(YEAR),String.valueOf(MONTH)},"Id");
			 }
		 public int[] GetAllIdsAt(int YEAR){
				// this.getReadableDatabase()
				return GetIntColumn(basics.retriveQuerys.sqlGetAllActiveAtTime4,
						new String[]{String.valueOf(YEAR)},"Id");
			 }
		 
		 public boolean SetNewMovement(long TIEMPO,String Description,String Motivo,float cantidad,String cuenta){
			 boolean success=false;
			 SQLiteDatabase db=null;
			 try{
				
                 int NextId=NextId(basics.class.getSimpleName());
                 db=getDBInstance();
			 if (getDBInstance()!=null){ 
				 ContentValues CV=new ContentValues(7);
				 CV.put("id_",NextId);
				 CV.put("tiempo_", TIEMPO);
				 CV.put("cantidad_", cantidad);
				 CV.put("cuenta_", cuenta);
				 CV.put("descripcion_", Description);
				 db.insert("basics_", null, CV);
				 db.close();
				 success=true;
				 
				 BackUpDBinExternalStorage("DBSmartWalet"+String.valueOf(NextId)+".db3");
			 }else{
				 success=false;
			 }
			 }catch(Exception e){
				 success=false;
			 }finally{
				 if (db!=null && db.isOpen())db.close();
				 
			 }
			 
			
		 
		
		 return success;
	 }
		 
		/**
		 * Make a query on basics table and return an array of basics objects, each one is a row of the table descripfiles
		 */
		 public basics[] getbasicDataMovements(String Queryonbasics,String[] Args){
	
			SQLiteDatabase db=getDBInstance();
			basics[] rows = new basics[]{};
			if (db!=null){
				Cursor Cur=db.rawQuery(Queryonbasics, Args);//
				 if (Cur!=null){
					 rows=new basics[Cur.getCount()];
					 for(int i=0;i<Cur.getCount();i++){
						 Cur.moveToPosition(i);
						 rows[i]=new basics();
						 rows[i].setObjectId(Cur.getString(Cur.getColumnIndex("objectId")));
						 Date created=new Date();
						 created.setTime(Cur.getLong(Cur.getColumnIndex("created")));
						 rows[i].setCreated(created);
						 Date updated=new Date();
						 updated.setTime(Cur.getLong(Cur.getColumnIndex("updated")));
						 rows[i].setUpdated(updated);

						 rows[i].setId_(Cur.getInt(Cur.getColumnIndex("id_")));
						 rows[i].setTiempo_(Cur.getLong(Cur.getColumnIndex("tiempo_")));
						 rows[i].setCantidad_(Cur.getDouble(Cur.getColumnIndex("cantidad_")));
						 rows[i].setCuenta_(Cur.getString(Cur.getColumnIndex("cuenta_")));
						 rows[i].setIdcuenta_(Cur.getInt(Cur.getColumnIndex("idcuenta_")));
						 rows[i].setDescripcion_(Cur.getString(Cur.getColumnIndex("descripcion_")));

					 }
					 Cur.close();
				 }else Log.d("Cur","Es null)");
				 Cur.close();//db.close
			}else Log.d("db","Es null");
			 
			 return rows;
		 }

	public descripfiles[] getAllFilesof(int idMovemntent){

		SQLiteDatabase db=getDBInstance();
		descripfiles[] rows = new descripfiles[]{};
		if (db!=null){
			Cursor Cur=db.rawQuery("SELECT * FROM descripfiles WHERE id_="+idMovemntent, null);//
			if (Cur!=null){
				rows=new descripfiles[Cur.getCount()];
				for(int i=0;i<Cur.getCount();i++){
					Cur.moveToPosition(i);
					rows[i]=new descripfiles();
					rows[i].setObjectId(Cur.getString(Cur.getColumnIndex("objectId")));
					Date created=new Date();
					created.setTime(Cur.getLong(Cur.getColumnIndex("created")));
					rows[i].setCreated(created);
					Date updated=new Date();
					updated.setTime(Cur.getLong(Cur.getColumnIndex("updated")));
					rows[i].setUpdated(updated);
					rows[i].set_id(Cur.getInt(Cur.getColumnIndex("id_")));
					rows[i].set_file(Cur.getString(Cur.getColumnIndex("file_")));
				}
				Cur.close();
			}else Log.d("Cur","Es null)");
			Cur.close();//db.close
		}else Log.d("db","Es null");

		return rows;
	}
	public descripimagenes[] getAllImagesof(int idMovement) {
		SQLiteDatabase db=getDBInstance();
		descripimagenes[] rows = new descripimagenes[]{};
		if (db!=null){
			Cursor Cur=db.rawQuery("SELECT * FROM descripimagenes WHERE id_="+idMovement, null);//
			if (Cur!=null){
				rows=new descripimagenes[Cur.getCount()];
				for(int i=0;i<Cur.getCount();i++){
					Cur.moveToPosition(i);
					rows[i]=new descripimagenes();
					rows[i].setObjectId(Cur.getString(Cur.getColumnIndex("objectId")));
					Date created=new Date();
					created.setTime(Cur.getLong(Cur.getColumnIndex("created")));
					rows[i].setCreated(created);
					Date updated=new Date();
					updated.setTime(Cur.getLong(Cur.getColumnIndex("updated")));
					rows[i].setUpdated(updated);
					rows[i].setId_(Cur.getInt(Cur.getColumnIndex("id_")));
					rows[i].setImagen_(Cur.getString(Cur.getColumnIndex("imagen_")));
				}
				Cur.close();
			}else Log.d("Cur","Es null)");
			Cur.close();//db.close
		}else Log.d("db","Es null");

		return rows;
	}
		 public SQLiteDatabase getDBInstance() { 
			 if (DB == null) {
				 DB = getInstance(Cont).getWritableDatabase();
		      } else if (!DB.isOpen()){
		    	  DB = this.getWritableDatabase();
		      }
			 DB.execSQL("PRAGMA foreign_keys=ON");
			 return DB; 
			 }
	public void Close(){
		if (getDBInstance().isOpen())getDBInstance().close();
	}

	/**
	 * Suma todas las cantidades positivas de la tabla basics de una cuenta
	 * @param Account Nombre de la cuenta de la que se sumaran las cantidades positivas
	 * @return	regresa la suma total(desde el principio) de todas las cantidades positivas de una cuenta dada
	 */
	public double getAllIngresoFrom(String Account){
		Cursor cur=getDBInstance().rawQuery("SELECT SUM (cantidad_) FROM basics WHERE cantidad_>0 AND cuenta_='"+Account+"'",null);
		cur.moveToFirst();
		return cur.getDouble(0);
	}
	public double getAllEgresoFrom(String Account){
		Cursor cur=getDBInstance().rawQuery("SELECT SUM (cantidad_) FROM basics WHERE cantidad_<0 AND cuenta_='"+Account+"'",null);
		cur.moveToFirst();
		return cur.getDouble(0);
	}
	public OrderMap<String,Double> getAllEgresoGroupByTagsCombined(String Query){
		OrderMap map=new OrderMap();
		 	Cur=getDBInstance().rawQuery("SELECT stringtags_,SUM(cantidad_) As suma FROM " +
				"(SELECT cantidad_,basics.id_,GROUP_CONCAT(DISTINCT tags.tag_) As stringtags_ " +
				"FROM tags INNER JOIN ("+Query+") As basics " +
				"WHERE tags.id_=basics.id_ AND cantidad_<0 GROUP BY basics.id_) GROUP BY stringtags_",null);
		if(Cur.moveToFirst()){
			do{
				map.put(Cur.getString(Cur.getColumnIndex("stringtags_")),Cur.getDouble(Cur.getColumnIndex("suma")));
			}while (Cur.moveToNext());
		}
		return map;
	}
	public OrderMap<String,Double> getAllEgresoGroupByTagsSplited(String Query){
		OrderMap map=new OrderMap();
		Cur=getDBInstance().rawQuery("SELECT tags.tag_ As tag,SUM(cantidad_) As egreso FROM tags INNER JOIN ("+Query+") As basics " +
				"WHERE cantidad_<0 AND basics.id_=tags.id_ GROUP By tags.tag_ ORDER BY egreso ASC",null);
		if(Cur.moveToFirst()){
			do{
				map.put(Cur.getString(Cur.getColumnIndex("tag")),Cur.getDouble(Cur.getColumnIndex("egreso")));
			}while (Cur.moveToNext());
		}
		return map;
	}
	public OrderMap getAllEgresoGroupByDaySplited(String query) {
		OrderMap<Long,Double> map=new OrderMap();
		if(query==null){
			return map;
		}

		query="SELECT basics.tiempo_ As tiempo, date(basics.tiempo_/1000,'unixepoch','localtime') As dia, SUM(basics.cantidad_) As resultPerDate FROM ("+ query +
				") As basics WHERE basics.cantidad_<0 GROUP BY date(tiempo_/1000,'unixepoch','localtime') ORDER BY basics.tiempo_ DESC";
		Cur=getDBInstance().rawQuery(query,null);
		if(Cur.moveToFirst()){
		    Calendar cal=Calendar.getInstance();
			DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
            do{
            	long milis=Cur.getLong(Cur.getColumnIndex("tiempo"));
                cal.setTimeInMillis(milis);
                String day=symbols.getWeekdays()[cal.get(Calendar.DAY_OF_WEEK)];
                map.put(day,Math.abs(Cur.getDouble(Cur.getColumnIndex("resultPerDate"))));
			}while (Cur.moveToNext());
		}
		return map;
	}
	public double getAllEgresoFromQuery(String Query){
		double[] result= GetDoubleColumn("SELECT SUM(cantidad_) As resultado FROM ("+ Query+") WHERE cantidad_<0",null,"resultado");
		if(result!=null){
			return result[0];
		}else{
			return 0.0d;
		}
	}
	public String getQueryByDate(datesFilter dates){
		Calendar calendar=Calendar.getInstance();
		if(dates== datesFilter.Hoy){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+System.currentTimeMillis()+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.Hoy_haceUnaSemana){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.HOUR_OF_DAY,-24*7);
			calendar.set(Calendar.HOUR_OF_DAY,23);
			calendar.set(Calendar.MINUTE,59);
			calendar.set(Calendar.SECOND,59);
			long limiteSup=calendar.getTimeInMillis();
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.Ayer){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			long limiteSup=calendar.getTimeInMillis();
			calendar.add(Calendar.HOUR_OF_DAY,-24);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.Ayer_haceUnaSemana){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.HOUR_OF_DAY,-24*8);
			calendar.set(Calendar.HOUR_OF_DAY,23);
			calendar.set(Calendar.MINUTE,59);
			calendar.set(Calendar.SECOND,59);
			long limiteSup=calendar.getTimeInMillis();
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.EstaSemana){
			calendar.setTimeInMillis(System.currentTimeMillis());
			long limiteSup=calendar.getTimeInMillis();
			while(calendar.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY){
				calendar.add(Calendar.HOUR_OF_DAY,-24);
			}
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.SemanaPasadaAlMismoDia){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.HOUR_OF_DAY,-24*7);
			calendar.set(Calendar.HOUR_OF_DAY,23);
			calendar.set(Calendar.MINUTE,59);
			calendar.set(Calendar.SECOND,59);
			long limiteSup=calendar.getTimeInMillis();
			while(calendar.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY){
				calendar.add(Calendar.HOUR_OF_DAY,-24);
			}
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.sietedias){
			calendar.setTimeInMillis(System.currentTimeMillis());
			long limiteSup=calendar.getTimeInMillis();
			calendar.add(Calendar.HOUR_OF_DAY,-24*6);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.EsteMes){
			calendar.setTimeInMillis(System.currentTimeMillis());
			long limiteSup=calendar.getTimeInMillis();
			calendar.set(Calendar.DAY_OF_MONTH,1);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.MesPasadoAlMismoDia){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.MONTH,-1);
			calendar.set(Calendar.HOUR_OF_DAY,23);
			calendar.set(Calendar.MINUTE,59);
			calendar.set(Calendar.SECOND,59);
			long limiteSup=calendar.getTimeInMillis();
			calendar.set(Calendar.DAY_OF_MONTH,1);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<="+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.treintadias){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			long limiteSup=calendar.getTimeInMillis();
			calendar.add(Calendar.HOUR_OF_DAY,-24*29);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
		}else if(dates== datesFilter.mespasado){
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.DAY_OF_MONTH,1);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			long limiteSup=calendar.getTimeInMillis();
			calendar.add(Calendar.MONTH,-1);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			return "SELECT * FROM basics WHERE tiempo_>="+calendar.getTimeInMillis()+" AND tiempo_<"+limiteSup+" ORDER BY tiempo_ DESC";
		}else{
			return "SELECT * FROM basics ORDER BY tiempo_ DESC";
		}
	}
	public int updateBasicsTable(ContentValues contentValues,String where){
		 	return getDBInstance().update("basics",contentValues,where,null);
	}
	public int updateBasicsTable(String column,String newValue,String where){
		 	ContentValues cv=new ContentValues();
		 	cv.put(column,newValue);
		return updateBasicsTable(cv,where);
	}
	public List<cuentas> getAllAccount(){
		List<cuentas> filas_cuentas=new ArrayList<cuentas>();
		Cur=getallfrom("cuentas ORDER BY cuenta_");
		if(Cur!=null && Cur.moveToFirst()){
			do{
				cuentas Row=new cuentas();
				Row.set_cuenta(Cur.getString(Cur.getColumnIndex("cuenta_")));
				Row.setCantidadInicial_(Cur.getDouble(Cur.getColumnIndex("cantidadInicial_")));
				Row.set_id(Cur.getInt(Cur.getColumnIndex("id_")));
				Row.set_cantidad(Cur.getDouble(Cur.getColumnIndex("cantidad_")));
				Row.setCreated(new Date(Cur.getInt(Cur.getColumnIndex("created"))));
				Row.setUpdated(new Date(Cur.getInt(Cur.getColumnIndex("updated"))));
				Row.setObjectId(Cur.getString(Cur.getColumnIndex("objectId")));
				filas_cuentas.add(Row);
			}while (Cur.moveToNext());
			Cur.close();
		}
		return filas_cuentas;
	}
	public ArrayList<String> getAllBanksNames(){
		return new ArrayList<String>();
	}
	public void sync(){
		
	   
	    BackendlessUser user = new BackendlessUser();
	    user.setEmail( "samuel@backendless.com" );
	    user.setPassword( "my_super_password_1231" );
 
	    Backendless.UserService.register( user, new BackendlessCallback<BackendlessUser>() 
	    {
	    	@Override
	    	public void handleResponse( BackendlessUser backendlessUser )
	    	{
	    		Log.i( "Registration", backendlessUser.getEmail() + " successfully registered" );
	    	}
	    	
	    	//Metodo necesario cada vez que se llama a BackendlessCallback por que sino se tiene
	    	//se detiene el hilo y el error aparece en el Log
	    	@Override
	    	public void handleFault( BackendlessFault fault )
	    	{
	    	throw new RuntimeException( fault.getMessage() );
	    	}

	    	
	    } );
	}

	public ArrayList<Cursor> getData(String Query){
		//get writable database
		SQLiteDatabase sqlDB = this.getWritableDatabase();
		String[] columns = new String[] { "message" };
		//an array list of cursor to save two cursors one has results from the query
		//other cursor stores error message if any errors are triggered
		ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
		MatrixCursor Cursor2= new MatrixCursor(columns);
		alc.add(null);
		alc.add(null);

		try{
			String maxQuery = Query ;
			//execute the query results will be save in Cursor c
			Cursor c = sqlDB.rawQuery(maxQuery, null);

			//add value to cursor2
			Cursor2.addRow(new Object[] { "Success" });

			alc.set(1,Cursor2);
			if (null != c && c.getCount() > 0) {

				alc.set(0,c);
				c.moveToFirst();

				return alc ;
			}
			return alc;
		} catch(SQLException sqlEx){
			Log.d("printing exception", sqlEx.getMessage());
			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		} catch(Exception ex){
			Log.d("printing exception", ex.getMessage());

			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+ex.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		}
	}

    public int deleteAllFrom(cuentas cuenta) {
		   return  getDBInstance().delete("cuentas","id_='"+cuenta.get_id()+"'",null);
    }

    public OrderMap getEgresoAcumuladoByDate(String query) {
		OrderMap<Long,Double> map=new OrderMap();
		if(query==null){
			return map;
		}

		query="SELECT basics.tiempo_ As tiempo, date(basics.tiempo_/1000,'unixepoch','localtime') As dia, SUM(basics.cantidad_) As resultPerDate FROM ("+ query +
				") As basics WHERE basics.cantidad_<0 GROUP BY date(tiempo_/1000,'unixepoch','localtime') ORDER BY basics.tiempo_ ASC";
		Cur=getDBInstance().rawQuery(query,null);
		if(Cur.moveToFirst()){
			int i=0;
			double cantidad=0;
			Calendar date=Calendar.getInstance();
			do{
				date.setTimeInMillis(Cur.getLong(Cur.getColumnIndex("tiempo")));
				date.set(Calendar.HOUR_OF_DAY,0);
				date.set(Calendar.MINUTE,0);
				date.set(Calendar.SECOND,0);
				date.set(Calendar.MILLISECOND,0);
				cantidad=cantidad+Cur.getDouble(Cur.getColumnIndex("resultPerDate"));
				map.put(i,date.getTimeInMillis(),cantidad);
				i++;
			}while (Cur.moveToNext());
            long lastDay=(long)map.keyAt(map.size()-1);
            long firstday=(long)map.keyAt(0);
			long diff =lastDay-firstday;
			float dayCount = (float) diff / (24 * 60 * 60 * 1000);

			if((int)dayCount>0){
				OrderMap fillResult=new OrderMap();
				i=0;
				date.setTimeInMillis((Long) map.keyAt(0));
				do{
					if(map.containsKey(date.getTimeInMillis())){
						fillResult.put(date.getTimeInMillis(),map.get(date.getTimeInMillis()));
					}else{
						fillResult.put(date.getTimeInMillis(),fillResult.get(i-1));
					}
					date.add(Calendar.HOUR_OF_DAY,24);
					i++;
				}while (dayCount-->0);

				return fillResult;
			}
		}


		return map;
    }

	public OrderMap getEgresoAcumuladoByHour(String query) {
		OrderMap<Long,Double> map=new OrderMap();
		query="SELECT basics.tiempo_ As tiempo, strftime('%H',time(basics.tiempo_/1000,'unixepoch','localtime')) As hour, SUM(basics.cantidad_) As resultPerHour FROM ("+ query +
				") As basics WHERE basics.cantidad_<0 GROUP BY basics.tiempo_ ORDER BY hour ASC";
		Cur=getDBInstance().rawQuery(query,null);
		if(Cur.moveToFirst()){
			int i=0;
			double cantidad=0;
			Calendar date=Calendar.getInstance();
			do{
				date.setTimeInMillis(Cur.getLong(Cur.getColumnIndex("tiempo")));
				//date.set(Calendar.HOUR_OF_DAY,0);
				date.set(Calendar.MINUTE,0);
				date.set(Calendar.SECOND,0);
				date.set(Calendar.MILLISECOND,0);
				cantidad=cantidad+Cur.getDouble(Cur.getColumnIndex("resultPerHour"));
				map.put(date.getTimeInMillis(),cantidad);//PI_DEBUG:Original line:map.put(i,date.getTimeInMillis(),cantidad);i++;
				//i++;
			}while (Cur.moveToNext());
			long lastHour=(long)map.keyAt(map.size()-1);
			long firstHour=(long)map.keyAt(0);
			long diff =lastHour-firstHour;
			float HourCount = (float) diff / (60*60 * 1000);

			if((int)HourCount>1){
				OrderMap fillResult=new OrderMap();
				i=0;
				date.setTimeInMillis((Long) map.keyAt(0));
				do{
					if(map.containsKey(date.getTimeInMillis())){
						fillResult.put(date.getTimeInMillis(),map.get(date.getTimeInMillis()));
					}else{
						fillResult.put(date.getTimeInMillis(),fillResult.get(i-1));
					}
					date.add(Calendar.HOUR_OF_DAY,1);
					i++;
				}while (HourCount-->0);

				return fillResult;
			}
		}


		return map;
	}
	public OrderMap getEgresoAcumuladoByMinutes(String query) {
		OrderMap<Long,Double> map=new OrderMap();
		query="SELECT basics.tiempo_ As tiempo, strftime('%m',time(basics.tiempo_/1000,'unixepoch','localtime')) As minutes, SUM(basics.cantidad_) As resultPerHour FROM ("+ query +
				") As basics WHERE basics.cantidad_<0 GROUP BY basics.tiempo_ ORDER BY minutes ASC";
		Cur=getDBInstance().rawQuery(query,null);
		if(Cur.moveToFirst()){
			int i=0;
			double cantidad=0;
			Calendar date=Calendar.getInstance();
			do{
				date.setTimeInMillis(Cur.getLong(Cur.getColumnIndex("tiempo")));
				date.set(Calendar.MILLISECOND,0);
				date.set(Calendar.SECOND,0);
				cantidad=cantidad+Cur.getDouble(Cur.getColumnIndex("resultPerHour"));
				map.put(date.getTimeInMillis(),cantidad);
				//i++;
			}while (Cur.moveToNext());
			long lastMinute=(long)map.keyAt(map.size()-1);
			long firstMinute=(long)map.keyAt(0);
			long diff =lastMinute-firstMinute;
			float minuteCount = (float) diff / (60*1000);

			if((int)minuteCount>1){
				OrderMap fillResult=new OrderMap();
				i=0;
				date.setTimeInMillis((Long) map.keyAt(0));
				do{
					if(map.containsKey(date.getTimeInMillis())){
						fillResult.put(date.getTimeInMillis(),map.get(date.getTimeInMillis()));
					}else{
						fillResult.put(date.getTimeInMillis(),fillResult.get(i-1));
					}
					date.add(Calendar.MINUTE,1);
					i++;
				}while (minuteCount-->0);

				return fillResult;
			}
		}



		return map;
	}
	public void exportToExcel(final trackedActivity context, String Query,int limit){
		context.wait("Exportando...",false);
		if(getDBTableNames().contains("Movimientos")) getDBInstance().execSQL("DROP TABLE Movimientos");
		String WHERE=Query.contains("INNER JOIN")?Query.substring(Query.indexOf("INNER JOIN")):"";
		if(WHERE.matches(""))WHERE=Query.contains("WHERE")?Query.substring(Query.indexOf("WHERE")):"";
		getDBInstance().execSQL("CREATE TABLE Movimientos AS " +
				"SELECT date(basics.tiempo_/1000,'unixepoch','localtime') AS Fecha,basics.cuenta_ AS Cuenta,basics.descripcion_ AS Descripcion,basics.cantidad_ AS Cantidad" +
				" FROM basics "+WHERE+(limit==0?"":" LIMIT "+ String.valueOf(limit)));
		File file=new File(context.getFilesDir().getAbsolutePath()+"/SQLiteToExcel/");
		if(!file.exists()){
			file.mkdirs();
		}
		SQLiteToExcel sqliteToExcel = new SQLiteToExcel(context, "DBSmartWallet.db3",file.getAbsolutePath()+"/");
		sqliteToExcel.exportSingleTable("Movimientos", "Denario.xls", new SQLiteToExcel.ExportListener() {
			int numMovs;
			@Override
			public void onStart() {
				numMovs=dbsmartwallet.GetIntColumn("SELECT COUNT(*) As result FROM Movimientos",null,"result")[0];

			}
			@Override
			public void onCompleted(String filePath) {
				getDBInstance().execSQL("DROP TABLE Movimientos");
				context.dismisswait();
				if (!filePath.matches("")){
					File filelocation = new File(filePath);
					Uri path= FileProvider.getUriForFile(context,
							"eclipseapps.financial.moneytracker.fileprovider",
							filelocation);
                            /*
                            if(Build.VERSION.SDK_INT>=24){
                                path = FileProvider.getUriForFile(MainActivity.this,
                                        "eclipseapps.financial.moneytracker.fileprovider",
                                        filelocation);
                            }else{
                                path = Uri.fromFile(filelocation);
                            }*/


					Intent emailIntent = new Intent(Intent.ACTION_SEND);
					// set the type to 'email'
					emailIntent .setType("vnd.android.cursor.dir/email");
					// the attachment
					emailIntent .putExtra(Intent.EXTRA_STREAM, path);
					// the mail subject
					emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Denario:Mis movimientos");
					emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					context.startActivity(Intent.createChooser(emailIntent , "Mis moviemientos registrados..."));
					context.ReadMovementsTracking("ExportMovments", String.valueOf(numMovs));//Registrar el numero de movimientos que se exportaron
				}
			}
			@Override
			public void onError(Exception e) {
				trackedActivity.sendLogAsError("ExportToExcelFault:", e.getMessage());
				context.dismisswait();
				context.showOkDialog("Ups! Ha ocurrido un error.Vuelve a intentarlo","OK",true,null);
			}
		});
	}

}
