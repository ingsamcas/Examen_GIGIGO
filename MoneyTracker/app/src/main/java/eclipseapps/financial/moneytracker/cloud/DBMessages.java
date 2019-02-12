package eclipseapps.financial.moneytracker.cloud;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;
import java.util.List;

import eclipseapps.financial.moneytracker.R;
import eclipseapps.library.databases.databaseHelper;


public class DBMessages extends databaseHelper{
	    private static List<String> filas_comentarios;
		static private SQLiteDatabase DB;//Objeto que se utiliza para accesar a todas las consultas de las bases de datos
		private Cursor Cur;//Objeto que se utiliza para apuntar a los cursores de las consultas
		static public String sqlGetAllMessages="SELECT * FROM comentarios";
	    static DBMessages dbsMessages;
		public static DBMessages getInstance(Context context){
			if (dbsMessages ==null) dbsMessages =new DBMessages(context, context.getResources().getString(R.string.databasename_messages), null,context.getResources().getInteger(R.integer.version_localMessages));
			return dbsMessages;
		}
		public DBMessages(Context context, String name, CursorFactory factory,
                          int version) {
			
			super(context, name, factory, version);
			
			Cont=context;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}


	public void Close(){
		if (getDBInstance().isOpen())getDBInstance().close();
	}
    public int getVersion() throws SQLiteException{
			int result=0;
			try{
				result=GetIntColumn("SELECT * FROM metaData",null,"version")[0];
			}catch (RuntimeException e){
				result=0;
			}
        return result;
    }
	public List<String> getAllMessages(){

		filas_comentarios=new ArrayList<String>();
		Cur=getallfrom("comentarios ORDER BY Id");
		Cur.moveToFirst();
				do{
					filas_comentarios.add(Cur.getString(Cur.getColumnIndex("Comentario")));
				}while (Cur.moveToNext());
		Cur.close();
		return filas_comentarios;
	}
	public int getAllComentariosCount(){
		if(filas_comentarios==null){
			getAllMessages();
		}
		return filas_comentarios.size();
	}
	public String getMessage(int Id){
		return GetStringScalar("SELECT * FROM comentarios WHERE Id="+String.valueOf(Id),null,"Comentario");
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



}
