package eclipseapps.mobility.parkeame.cloud;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import eclipseapps.library.databases.databaseHelper;
import eclipseapps.mobility.parkeame.R;

/**
 * Created by usuario on 26/08/17.
 */
public class DBParkeame extends databaseHelper {
    static private SQLiteDatabase DB;//Objeto que se utiliza para accesar a todas las consultas de las bases de datos
    private Cursor Cur;//Objeto que se utiliza para apuntar a los cursores de las consultas
    static DBParkeame dbparkeame;
    public DBParkeame(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Cont=context;
    }

    public static DBParkeame getInstance(Context context){
        if (dbparkeame==null) dbparkeame=new DBParkeame(context, context.getResources().getString(R.string.databasename), null,context.getResources().getInteger(R.integer.version_localparkeame));
        return dbparkeame;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        DB=db;
        DB.execSQL(new user().getCreateSentence());//Tabla de version 2
        DB.execSQL(new Autos().retriveCreateSentence());
        DB.execSQL(new Precios().retriveCreateSentence());
        DB.execSQL(new parkeos().retriveCreateSentence());
        DB.execSQL(new Tokens().retriveCreateSentence());
        DB.execSQL(new Parkimetros().retriveCreateSentence());

    }

}
