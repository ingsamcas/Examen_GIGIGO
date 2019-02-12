package eclipseapps.financial.moneytracker.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.imagegallery.library.activities.FullScreenImageGalleryActivity;
import com.etiennelawlor.imagegallery.library.activities.ImageGalleryActivity;
import com.etiennelawlor.imagegallery.library.adapters.FullScreenImageGalleryAdapter;
import com.etiennelawlor.imagegallery.library.adapters.ImageGalleryAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eclipseapps.android.customviews.FontFitTextView;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.FacturaViewer_activity;
import eclipseapps.financial.moneytracker.cloud.basics;
import eclipseapps.libraries.library.android.animation.AnimForlistView;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.Providers;

public class movementsListAdpter extends BaseExpandableListAdapter implements ImageGalleryAdapter.ImageThumbnailLoader, FullScreenImageGalleryAdapter.FullScreenImageLoader {

    protected final String AdGroup="AdGroup";
    private RelativeLayout Descripcion;
    private FontFitTextView TvCantidadEntero;
    private TextView TvCantidadDecimal;
    private TextView TvDescripcion;
    private TextView TvCuenta;
    private TextView TvHora;
    private TextView TvFechaRapida;

    private boolean clickOnGroup=false;
    private ImageView Adjuntos;
    ImageView ImagenesAdjuntas;
    ImageLoader imageLoader = ImageLoader.getInstance();
    protected Context context;


    //private Calendar cal;
    private Animation animation;
    private List<RowData> rows=new ArrayList<>();//Lista original
    private Bundle sum;//(dia+"."+MesNombre+"."+anuario,float)
    protected OrderMap<String, List<RowData>> _listDataChild=new OrderMap<>();
    private boolean isTutorialLaunchedYet=false;
    private List Selecteditems=new ArrayList();
    private View.OnLongClickListener onClickListener;

    public List getSelecteditems() {
        return Selecteditems;
    }

    @Override
    public int getChildTypeCount() {
        return 2;
    }//Contenido y anuncio

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return ((List<RowData>)_listDataChild.get(groupPosition)).get(childPosition).type== RowData.Type.Content?0:1;
    }

    @Override
    public int getGroupTypeCount() { return 2; }//Contenido y anuncio

    @Override
    public int getGroupType(int groupPosition) {
        String name=_listDataChild.keyAt(groupPosition).toString();
        return name.matches(AdGroup)?1:0;
    }

    public boolean hasAd(){
        return false;
    }
    public movementsListAdpter(Context context) {
       super();
        this.context=context;

    }


    public movementsListAdpter setData(List<RowData> _rows, Bundle _sum){
        rows=_rows;
        sum=_sum;
        _listDataChild=new OrderMap<>();
        List<RowData> dataDay=new ArrayList<>();
        int position=0;
        if(_rows!=null && _rows.size()>0){
            do{
                if((position>0 && !rows.get(position).isSameDateTo(rows.get(position-1)))){
                    _listDataChild.put(rows.get(position-1).fechaActual(),dataDay);
                    dataDay=new ArrayList<>();
                    dataDay.add(rows.get(position).setType(RowData.Type.Content));
                }else{
                    dataDay.add(rows.get(position).setType(RowData.Type.Content));
                }
                if (position==rows.size()-1){
                    _listDataChild.put(rows.get(position).fechaActual(),dataDay);
                }
                position++;
            }while (position<rows.size());

        }
        return this;
    }

    private void colorIfSelected(int iParent,int iChild,LinearLayout element){
        TextView description=element.findViewById(R.id.movementlist_descripcion_text);
        if (Selecteditems.contains(getChildId(iParent,iChild))){
            if(android.os.Build.VERSION.SDK_INT >= 23) {
                element.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark,null));
                description.setTextColor(Color.WHITE);
            }
            else {
                element.setBackgroundResource(R.color.colorPrimaryDark);
                description.setTextColor(Color.WHITE);
            }
        }else{
            if(android.os.Build.VERSION.SDK_INT >= 23) {
                element.setBackgroundColor(context.getResources().getColor(android.R.color.transparent,null));
                description.setTextColor(context.getResources().getColor(R.color.colorPrimary,null));
            }
            else {
                element.setBackgroundResource(android.R.color.transparent);
                description.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            }
        }

    }
    public void setMonthIfCase(View row, String mes){
        TextView TvMes=((TextView)row.findViewById(R.id.nativeads2_fecha_mes));
        TvMes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    return true;
                }
                return false;
            }
        });
        if (!mes.matches("")){
            TvMes.setText(mes);
            TvMes.setVisibility(View.VISIBLE);
        }else{
            TvMes.setText("");
            TvMes.setVisibility(View.GONE);
        }
    }
    public void setYearIfCase(View row,int anuario){
        TextView TvYear=((TextView)row.findViewById(R.id.nativeads2_fecha_anuario));
        TvYear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    return true;
                }
                return false;
            }
        });
        if (anuario!=0){
            TvYear.setText(String.valueOf(anuario));
            TvYear.setVisibility(View.VISIBLE);
        }else{
            TvYear.setText("");
            TvYear.setVisibility(View.GONE);
        }
    }

    public String getPath(Uri contentUri) {
        return Providers.getPath(context,contentUri);
        /*
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA  };
        try{
            Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if(cursor.moveToFirst()){;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                res = cursor.getString(column_index);
            }
            cursor.close();
            return res;
        }catch (SecurityException ex){
            AnalyticsApplication.sendLogAsError("ReadImageFromUri",ex.getMessage());
            return null;
        }*/

    }

    @Override
    public int getGroupCount() {
        return _listDataChild.size();
    }

    @Override
    public int getChildrenCount(int i) {
        List rows= (List) _listDataChild.get(i);
        return rows.size();
    }

    @Override
    public Object getGroup(int iParent) {//Return List<RowData>
        return _listDataChild.get(iParent);
    }

    @Override
    public Object getChild(int iParent, int iChild) {//Return RowData
        List rows= (List) _listDataChild.get(iParent);
        return rows.get(iChild);
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int iParent, int iChild) {
        List rows= (List) _listDataChild.get(iParent);
        return ((RowData)rows.get(iChild)).basico.getId_();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int iParent, boolean isExpanded, View view, ViewGroup viewGroup) {
        if(getGroupType(iParent)==0){
            View row = view;

            if(row==null){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row=inflater.inflate(R.layout.fragment_movementslistgroupelement, viewGroup, false);
            }
            //sombra=(View)row.findViewById(R.id.sombra_izquierda);
            TvFechaRapida=((TextView)row.findViewById(R.id.nativeads2_fecha_rapida));
            final FontFitTextView TvTotalDiario= (FontFitTextView) row.findViewById(R.id.nativeads2_total_diario);


            TvFechaRapida.setText(String.valueOf(((RowData)getChild(iParent,0)).dia+"\n"+((RowData)getChild(iParent,0)).diasemanal));
            String total=String.format("%.2f", sum.getDouble(((RowData)getChild(iParent,0)).fechaActual()));
            if (total.contains("-"))total=total.replace("-","-$ ");
            else{total="$ "+total;}
            TvTotalDiario.setText(total);
            //divisor.setVisibility(View.VISIBLE);

            if(iParent==0){
                setMonthIfCase(row,((RowData)getChild(iParent,0)).MesNombre);
                setYearIfCase(row,((RowData)getChild(iParent,0)).anuario);
            }else{
                if(((RowData)getChild(iParent,0)).MesNombre.matches(((RowData)getChild(iParent-1,0)).MesNombre)){
                    setMonthIfCase(row,"");
                }else{
                    setMonthIfCase(row,((RowData)getChild(iParent,0)).MesNombre);
                }
                if(((RowData)getChild(iParent,0)).anuario==((RowData)getChild(iParent-1,0)).anuario){
                    setYearIfCase(row,0);
                }else{
                    setYearIfCase(row,((RowData)getChild(iParent,0)).anuario);
                }
            }
            final CardView card= (CardView) row.findViewById(R.id.nativeads2_list_group_card);
            card.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    clickOnGroup=true;
                    return false;
                }
            });
            final LinearLayout backFecha=row.findViewById(R.id.nativeads2_fecha_rapida_background);

            animation= AnimForlistView.getAnimation((Activity) context,
                    AnimForlistView.TranslateAnimation1, 500);//AnimatedlistView.fade_in, 500);


            row.startAnimation(animation);
            animation = null;
            row.setTag(getChildId(iParent,0));
            row.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (!clickOnGroup && motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        return true;
                    }
                    clickOnGroup=false;
                    return false;
                }
            });
            if (isExpanded){
                if (Build.VERSION.SDK_INT>=23){
                    card.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary,null));
                    TvTotalDiario.setTextColor(context.getResources().getColor(android.R.color.white,null));
                    TvFechaRapida.setTextColor(context.getResources().getColor(R.color.colorAccent,null));
                    backFecha.setBackground(context.getResources().getDrawable(R.drawable.gradient_circular_drawable_blanco,null));
                }else{
                    card.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                    TvTotalDiario.setTextColor(context.getResources().getColor(android.R.color.white));
                    TvFechaRapida.setTextColor(context.getResources().getColor(R.color.colorAccent));
                    backFecha.setBackground(context.getResources().getDrawable(R.drawable.gradient_circular_drawable_blanco));
                }
            }else{
                if (Build.VERSION.SDK_INT>=23){
                    card.setCardBackgroundColor(context.getResources().getColor(android.R.color.white,null));
                    TvTotalDiario.setTextColor(context.getResources().getColor(android.R.color.darker_gray,null));
                    TvFechaRapida.setTextColor(context.getResources().getColor(android.R.color.white,null));
                    backFecha.setBackground(context.getResources().getDrawable(R.drawable.gradient_circular_drawable_verde,null));
                }else{
                    card.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                    TvTotalDiario.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    TvFechaRapida.setTextColor(context.getResources().getColor(android.R.color.white));
                    backFecha.setBackground(context.getResources().getDrawable(R.drawable.gradient_circular_drawable_verde));
                }
            }
            return row;
        }else{
            return getGroupNativeAdView(context,view,viewGroup,viewGroup.getWidth());
        }

    }

    protected View getGroupNativeAdView(Context context, View view, ViewGroup viewGroup, int width) {
        if(view==null){
            view=new View(context);//dummy view
        }
        return view;
    }

    @Override
    public View getChildView(final int iParent, final int iChild, boolean isLastChild, View convertView, ViewGroup viewGroup) {
        if(getChildType(iParent,iChild)==0){
            View row = convertView;

            if(row==null){
                LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row=inflater.inflate(R.layout.fragment_movementslistelement, viewGroup, false);
            }
            final View Row=row;
            final LinearLayout infoContainer=(LinearLayout) Row.findViewById(R.id.fragment_movementslistelemnt_elemnt);

            final View.OnLongClickListener onlongclick=new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!Selecteditems.contains(getChildId(iParent,iChild))) {
                        Selecteditems.add(getChildId(iParent,iChild));
                    } else {
                        Selecteditems.remove(getChildId(iParent,iChild));
                    }
                    colorIfSelected(iParent,iChild, infoContainer);
                    if (onClickListener != null) onClickListener.onLongClick(view);
                    return false;
                }
            };
            row.setOnLongClickListener(onlongclick);
            colorIfSelected(iParent,iChild,infoContainer);
            if (rows!=null && rows.size()>0){

                TvCantidadEntero= (FontFitTextView) row.findViewById(R.id.cantidad_enteros);
                basics basic=((RowData)getChild(iParent,iChild)).basico;
                long cant= (long) Math.abs(basic.getCantidad_());
                TvCantidadEntero.setText((basic.getCantidad_()<0?"-$":" $")+String.valueOf(cant));

                TvCantidadDecimal= (TextView) row.findViewById(R.id.cantidad_decimales);
                String decimals=String.format("%.2f", Math.abs(((RowData)getChild(iParent,iChild)).basico.getCantidad_()));
                TvCantidadDecimal.setText(decimals.substring(decimals.indexOf(".")+1));

                Descripcion =((RelativeLayout)row.findViewById(R.id.movementlist_descripcion_container));
                TvDescripcion=((TextView)row.findViewById(R.id.movementlist_descripcion_text));



                if (!(((RowData)getChild(iParent,iChild)).basico.getDescripcion_()==null) && !((RowData)getChild(iParent,iChild)).basico.getDescripcion_().matches("")) {
                    TvDescripcion.setText(((RowData)getChild(iParent,iChild)).basico.getDescripcion_());
                    Descripcion.setVisibility(View.VISIBLE);
                    Descripcion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(context);
                            builder.setMessage(((RowData)getChild(iParent,iChild)).basico.getDescripcion_());
                            builder.setPositiveButton("ok",null);
                            builder.show();
                        }
                    });
                    // DescripcionCompleta.setVisibility(isBackOfCardShowing?View.VISIBLE:View.GONE);
                }else {
                    TvDescripcion.setText("");
                    Descripcion.setVisibility(View.GONE);
                    // DescripcionCompleta.setVisibility(isBackOfCardShowing?View.VISIBLE:View.GONE);
                }


                Adjuntos=(ImageView)row.findViewById(R.id.movementlist_attachment_icon);
                if(((RowData)getChild(iParent,iChild)).Files!=null && ((RowData)getChild(iParent,iChild)).Files.size()>0){
                    Adjuntos.setVisibility(View.VISIBLE);
                }else{
                    Adjuntos.setVisibility(View.GONE);
                }
                FrameLayout fotoscontenedor= (FrameLayout) row.findViewById(R.id.movementlist_fotoscontenedor);
                ImagenesAdjuntas=(ImageView)row.findViewById(R.id.movementlist_images);
                ImagenesAdjuntas.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> images=((RowData)getChild(iParent,iChild)).Images;


                        ImageGalleryActivity.setImageThumbnailLoader(movementsListAdpter.this);
                        FullScreenImageGalleryActivity.setFullScreenImageLoader(movementsListAdpter.this);

                        Intent intent = new Intent(context, ImageGalleryActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList(ImageGalleryActivity.KEY_IMAGES, images);
                        bundle.putString(ImageGalleryActivity.KEY_TITLE, "Movimiento");
                        intent.putExtras(bundle);

                        context.startActivity(intent);
                    }
                });
                if(((RowData)getChild(iParent,iChild)).Images!=null && ((RowData)getChild(iParent,iChild)).Images.size()>0){
                    fotoscontenedor.setVisibility(View.VISIBLE);
                    if(!imageLoader.isInited())imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                    if(((RowData)getChild(iParent,iChild)).Images.get(0).startsWith(("content://"))){
                        Log.d("MoneyImage-Content",((RowData)getChild(iParent,iChild)).Images.get(0));
                        try{
                            // set bitmap to imageview
                            imageLoader.displayImage(((RowData)getChild(iParent,iChild)).Images.get(0),ImagenesAdjuntas);
                        }
                        catch (SecurityException e){
                            //handle exception
                            e.printStackTrace();
                        }


                    }else{
                        Log.d("MoneyImage-File",((RowData)getChild(iParent,iChild)).Images.get(0));
                        File file=new File(((RowData)getChild(iParent,iChild)).Images.get(0));
                        if (file.exists()) {
                            imageLoader.displayImage("file://" + ((RowData)getChild(iParent,iChild)).Images.get(0), ImagenesAdjuntas);
                        }
                    }
                }else{
                    fotoscontenedor.setVisibility(View.GONE);
                }

                final HorizontalScrollView scrolltags= (HorizontalScrollView) row.findViewById(R.id.contenedor_tags_scroll);

                GestureDetector.OnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e)
                    {
                        onlongclick.onLongClick(scrolltags);
                    }
                };

                final GestureDetector gestureDetector = new GestureDetector(context, listener);

                scrolltags.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        return gestureDetector.onTouchEvent(event);
                    }
                });

                LinearLayout contenedor= (LinearLayout) row.findViewById(R.id.contenedor_tags);
                for(int j=0;j<contenedor.getChildCount();j++){
                    TextView tag0= (TextView) contenedor.getChildAt(j);
                    tag0.setVisibility(View.GONE);
                }

                for (int i = 0; i<((RowData)getChild(iParent,iChild)).Tags.size(); i++) {
                    if (i<contenedor.getChildCount()){
                        TextView tag= (TextView) contenedor.getChildAt(i);
                        tag.setText("  "+((RowData)getChild(iParent,iChild)).Tags.get(i)+"  ");
                        tag.setVisibility(View.VISIBLE);
                    }else{
                        TextView Tag= (TextView) ((Activity)context).getLayoutInflater().inflate(R.layout.layout_tag,null);
                        contenedor.addView(Tag, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                        Tag.setText("  "+((RowData)getChild(iParent,iChild)).Tags.get(i)+"  ");
                        Tag.setVisibility(View.VISIBLE);
                    }
                }
                String RFCSolicitado=((RowData)getChild(iParent,iChild)).facturaRFCSolicitado;
                final String factura=((RowData)getChild(iParent,iChild)).factura;
                TextView facturaSolicitada=row.findViewById(R.id.movementlistelement_facturasolicitada);
                //facturaSolicitada.setVisibility(View.INVISIBLE);
                FrameLayout facturaConetnida=row.findViewById(R.id.movementlist_facturacontenedor);

                if(!RFCSolicitado.matches("") && factura.matches("")){
                    //Se ha solicitado factura pero aun no se cuenta con ella
                    facturaConetnida.setVisibility(View.INVISIBLE);
                    facturaSolicitada.setText("     Facturar a "+RFCSolicitado);
                    facturaSolicitada.setVisibility(View.VISIBLE);
                }else{
                    if(!factura.matches("")){
                        facturaConetnida.setVisibility(View.VISIBLE);
                        facturaConetnida.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(context, FacturaViewer_activity.class);
                                intent.putExtra(FacturaViewer_activity.objectId_Fatura,((RowData)getChild(iParent,iChild)).objectIdFactura);
                                context.startActivity(intent);
                            }
                        });
                    }else{
                        facturaConetnida.setVisibility(View.GONE);
                    }

                    facturaSolicitada.setVisibility(View.GONE);
                }

                //fondoCantidad=(ImageView)row.findViewById(R.id.fragment_movementslistelemnt_fondo_cantidad);
                //Picasso.with(getActivity()).load(R.drawable.fondo_cantidad).fit().into(fondoCantidad);

                TvCuenta=((TextView)row.findViewById(R.id.fragment_movementslistelemnt_cuenta));
                TvCuenta.setText(((RowData)getChild(iParent,iChild)).basico.getCuenta_());

                TvHora =((TextView)row.findViewById(R.id.movementlist_time_text));
                TvHora.setText(((RowData)getChild(iParent,iChild)).Hora);


                RelativeLayout trendingCanvasIndicator=row.findViewById(R.id.trending_canvas);
                if(((RowData)getChild(iParent,iChild)).basico.getCantidad_()<0){
                    trendingCanvasIndicator.setBackgroundResource(R.drawable.ic_trending_down_black_240dp);
                }else{
                    trendingCanvasIndicator.setBackgroundResource(R.drawable.ic_trending_up_black_240dp);
                }
            }
            return row;
        }else{
            return getChildNativeAdView(context,convertView,viewGroup,viewGroup.getWidth());
        }

    }



    public View getChildNativeAdView(Context context, View convertView, ViewGroup parent, long parentWitdth) {
        if(convertView==null){
            convertView=new View(context);//dummy view
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


    public movementsListAdpter setOnClickListener(View.OnLongClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }
    public void onResume(){ }
    public void onPause(){ }
    public void onDestroy(){}

    @Override
    public void loadFullScreenImage(ImageView iv, String imageUrl, int width, LinearLayout bglinearLayout) {
        showImageOnImageView(imageUrl,iv);
    }

    @Override
    public void loadImageThumbnail(ImageView iv, String imageUrl, int dimension) {
        showImageOnImageView(imageUrl,iv);
    }
    public void showImageOnImageView(String imageUrl,ImageView imageView){
        if(imageUrl.startsWith(("content://"))){
            imageLoader.displayImage(imageUrl, imageView);
        }else{
            imageLoader.displayImage("file://" + imageUrl, imageView);
        }
    }

}

