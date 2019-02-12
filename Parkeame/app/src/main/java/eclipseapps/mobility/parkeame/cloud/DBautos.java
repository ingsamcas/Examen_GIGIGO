package eclipseapps.mobility.parkeame.cloud;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eclipseapps.library.databases.databaseHelper;
import eclipseapps.mobility.parkeame.BuildConfig;
import eclipseapps.mobility.parkeame.R;

/**
 * Created by usuario on 27/05/18.
 */

public class DBautos extends SQLiteOpenHelper {
    protected Context Cont;
    static private SQLiteDatabase DB;//Objeto que se utiliza para accesar a todas las consultas de las bases de datos
    private static String DB_NAME = "autos.db3";
    private static String DB_PATH ="/data/data/"+ BuildConfig.APPLICATION_ID+"/databases/";
    static DBautos dbautos;

    public DBautos(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Cont=context;
        boolean dbexist = checkdatabase();
        if (dbexist) {
            System.out.println("Database exists");
            opendatabase();
        } else {
            System.out.println("Database doesn't exist");
            try {
                createdatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public static DBautos getInstance(Context context){
        if (dbautos==null) dbautos=new DBautos(context, context.getResources().getString(R.string.databasename_autos), null,context.getResources().getInteger(R.integer.version_localparkeame));
        return dbautos;
    }

    public void createdatabase() throws IOException {
        boolean dbexist = checkdatabase();
        if(dbexist) {
            System.out.println(" Database exists.");
        } else {
            this.getReadableDatabase();
            try {
                copydatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
            opendatabase();
        }
    }

    private boolean checkdatabase() {

        boolean checkdb = false;
        try {
            String myPath = DB_PATH + DB_NAME;
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }

    private void copydatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = Cont.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outfilename = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream(outfilename);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }

    public void opendatabase() throws SQLException {
        //Open the database
        String mypath = DB_PATH + DB_NAME;
        DB = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if(DB != null) {
            DB.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }
    public String[] GetStringColumn(String Query,String[] Args,String ColumnName){
        Cursor Cur=DB.rawQuery(Query, Args);
        if(Cur.getCount()>0){
            String[] Columna=new String[Cur.getCount()];
            for(int i=0;i<=Cur.getCount()-1;i++){
                Cur.moveToPosition(i);
                Columna[i]=Cur.getString(Cur.getColumnIndex(ColumnName));
            }
            Cur.close();
            return Columna;
        }
        Cur.close();
        return null;
    }

}
