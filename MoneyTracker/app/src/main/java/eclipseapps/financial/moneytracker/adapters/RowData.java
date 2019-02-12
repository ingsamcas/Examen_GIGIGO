package eclipseapps.financial.moneytracker.adapters;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.basics;
import eclipseapps.financial.moneytracker.cloud.descripfiles;
import eclipseapps.financial.moneytracker.cloud.descripimagenes;
import eclipseapps.libraries.library.general.functions.Providers;

public class RowData implements Cloneable {

    enum Type {Ad,Content};
    Type type;
    Calendar cal;
    int anuario;
    String MesNombre;
    String diasemanal;
    int dia;
    String Hora;

    List<String> Tags=new ArrayList<String>();
    List<String> Files=new ArrayList<String>();
    ArrayList<String> Images=new ArrayList<String>();
    public basics basico;
    public String facturaRFCSolicitado="";
    public String objectIdFactura="";
    public String factura="";

    public Type getType() {
        return type;
    }

    public RowData setType(Type type) {
        this.type = type;
        return this;
    }

    public RowData(basics basic, DBSmartWallet DB){
        super();
        basico=basic;
        cal=Calendar.getInstance();
        cal.setTimeInMillis(basico.getTiempo_());
        dia=cal.get(Calendar.DAY_OF_MONTH);
        diasemanal=cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        Hora=String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d", cal.get(Calendar.MINUTE));
        MesNombre=cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        anuario=cal.get(Calendar.YEAR);



        descripfiles[] F=DB.getAllFilesof(basico.getId_()); //Archivos asociados
        if (F!=null && F.length>0){
            for(int i =0;i<F.length;i++){
                Files.add(F[i].get_file());
            }
        }

        descripimagenes[] I=DB.getAllImagesof(basico.getId_());
        if (I!=null && I.length>0){
            for(int i=0;i<I.length;i++){
                if(isImageAccesible(DB.getcontext(), I[i].getImagen_())) Images.add(I[i].getImagen_());
            }
        }

        String[]  tags=DB.GetStringColumn("SELECT * FROM tags WHERE id_="+basico.getId_(),null,"tag_");
        if (tags!=null && tags.length>0){
            for(int i =0;i<tags.length;i++){
                Tags.add(tags[i]);
            }


        }

    }

    public RowData() {
        super();
        //Constructor vacio para inicilarizar en caso de anuncio
    }

    public String fechaActual(){
        return dia+"."+MesNombre+"."+anuario;
    }
    public boolean isSameDateTo(RowData row){
        if (dia==row.dia && MesNombre.matches(row.MesNombre) && anuario==row.anuario)return true;
        else return false;
    }
    public Object clone() {
        RowData obj = null;
        try {
            obj = (RowData) super.clone();
        }
        catch (CloneNotSupportedException e) {
            // This should never happen
        }
        return obj;
    }
    public boolean isImageAccesible(Context context, String imageUrl){
        if(imageUrl.startsWith(("content://"))){
            Uri myUri = Uri.parse(imageUrl);
            if(Providers.getPath(context,myUri)!=null){
                return true;
            }else{
                return false;
            }
        }else{
            File file=new File(imageUrl);
            if (file.exists()) {
                return true;
            }
            return false;
        }
    }
}