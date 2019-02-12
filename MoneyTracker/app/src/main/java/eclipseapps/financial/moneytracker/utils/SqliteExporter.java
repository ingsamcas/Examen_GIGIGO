package eclipseapps.financial.moneytracker.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eclipseapps.libraries.library.general.functions.OrderMap;

/**
 * Can export an sqlite databse into a csv file.
 *
 * The file has on the top dbVersion and on top of each table data the TAG_FragmentName of the table
 *
 * Inspired by
 * https://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
 * and some other SO threads as well.
 *
 */
public class SqliteExporter {
    private static final String TAG = SqliteExporter.class.getSimpleName();

    public static final String DB_BACKUP_DB_VERSION_KEY = "dbVersion";
    public static final String DB_BACKUP_TABLE_NAME = "table";

    public static String export(Context context, SQLiteDatabase db) throws IOException {
        if( !FileUtils.isExternalStorageWritable() ){
            throw new IOException("Cannot write to external storage");
        }
        File backupDir = FileUtils.createDirIfNotExist(FileUtils.getAppDir(context) + "/backup");
        String fileName = createBackupFileName();
        File backupFile = new File(backupDir.getAbsolutePath(), fileName);
        boolean success=false;
        try {
            success = backupFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!success){
            throw new IOException("Failed to create the backup file");
        }
        //List<String> tables = getTablesOnDataBase(db);
        List<String> tables =new ArrayList<>();
        tables.add("basics");
        Log.d(TAG, "Started to fill the backup file in " + backupFile.getAbsolutePath());
        long starTime = System.currentTimeMillis();
       OrderMap masks=new OrderMap();
        masks.put("tiempo_","Fecha");
        masks.put("cuenta_","Cuenta");
        masks.put("descripcion_","Descripción");
        masks.put("cantidad_","Cantidad");
        OrderMap map=new OrderMap();
        map.put("basics",masks);
       // writeCsv(backupFile, db, map);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Creating backup took " + (endTime - starTime) + "ms.");

        return backupFile.getAbsolutePath();
    }

    private static String createBackupFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "Denario_" + sdf.format(new Date()) + ".csv";
    }

    /**
     * Get all the table names we have in db
     *
     * @param db
     * @return
     */
    public static List<String> getTablesOnDataBase(SQLiteDatabase db){
        Cursor c = null;
        List<String> tables = new ArrayList<>();
        try{
            c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (c.moveToFirst()) {
                while ( !c.isAfterLast() ) {
                    tables.add(c.getString(0));
                    c.moveToNext();
                }
            }
        }
        catch(Exception throwable){
            Log.e(TAG, "Could not get the table names from db", throwable);
        }
        finally{
            if(c!=null)
                c.close();
        }
        return tables;
    }

    /**
     *
     * @param backupFile
     * @param db
     * @param contentMap NombreDeTabla,OrderMap[columna,mascara] a ser respaldadas

    private static void writeCsv(File backupFile, SQLiteDatabase db, OrderMap<String,OrderMap<String,String>> contentMap){
        CSVWriter csvWrite = null;
        Cursor curCSV = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(backupFile));
            //writeSingleValue(csvWrite, DB_BACKUP_DB_VERSION_KEY + "=" + db.getVersion());
            String table="";
            String queryColumns="";
            for (int J=0;J<contentMap.size();J++){
                table= (String) contentMap.keyAt(J);
                String[] columnsQuery=new String[((OrderMap)contentMap.get(J)).size()];
                for(int a=0;a<((OrderMap)contentMap.get(J)).size();a++){
                    columnsQuery[a]= (String) ((OrderMap)contentMap.get(J)).keyAt(a);
                }
                for (String column:columnsQuery) {
                    queryColumns=queryColumns+","+column;
                }
                queryColumns=queryColumns.substring(1);
                //writeSingleValue(csvWrite, DB_BACKUP_TABLE_NAME + "=" + table);
                curCSV = db.rawQuery("SELECT "+queryColumns+" FROM " + table,null);

                String[] columnsMasks=new String[((OrderMap)contentMap.get(J)).size()];
                for(int a=0;a<((OrderMap)contentMap.get(J)).size();a++){
                    columnsMasks[a]= (String) ((OrderMap)contentMap.get(J)).get(a);
                }
                csvWrite.writeNext(columnsMasks);
                while(curCSV.moveToNext()) {
                    int columns = curCSV.getColumnCount();
                    String[] columnArr = new String[columns];
                    for( int i = 0; i < columns; i++){
                        columnArr[i] = curCSV.getString(i);//URLEncoder.encode(curCSV.getString(i), "UTF-8");
                    }
                    csvWrite.writeNext(columnArr);
                }

            }

        }
        catch(Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }finally {
            if(csvWrite != null){
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if( curCSV != null ){
                curCSV.close();
            }
        }
    }

    private static void writeSingleValue(CSVWriter writer, String value){
        writer.writeNext(new String[]{value});
    }
    */
}