package eclipseapps.mobility.trackergps.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import eclipseapps.library.databases.databaseHelper;
import eclipseapps.mobility.trackergps.R;

/**
 * Created by usuario on 10/12/16.
 */
public class trackerDB extends databaseHelper {
    static trackerDB insulinDB;
    public trackerDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Cont=context;
    }
    public static trackerDB getInstance(Context context){
        if (insulinDB ==null) insulinDB =new trackerDB(context, context.getResources().getString(R.string.databasename), null,context.getResources().getInteger(R.integer.version_localtracker));
        return insulinDB;
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newversion) {
        switch(oldVersion) {
            case 1:
                //sqLiteDatabase.execSQL("DROP TABLE IF EXIST Pedido");
                //sqLiteDatabase.execSQL("DROP TABLE IF EXIST User");
                //sqLiteDatabase.execSQL(new Pedido().retriveCreateSentence());
                //sqLiteDatabase.execSQL(new User().getCreateSentence());
                //upgrade logic from version 1 to 2
            case 2:
                //sqLiteDatabase.execSQL(new product().retriveCreateSentence());
                //sqLiteDatabase.execSQL("ALTER TABLE NFGBackendlessUser ADD COLUMN socialAccount varchar(50);");
                //upgrade logic from version 2 to 3
            case 3:
                //upgrade logic from version 3 to 4
                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown oldVersion " + oldVersion);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       // db.execSQL(new Pedido().retriveCreateSentence());
        //db.execSQL(new consultas().retriveCreateSentence());
        //db.execSQL(new User().getCreateSentence());
        //db.execSQL(new product().retriveCreateSentence());
    }
}
