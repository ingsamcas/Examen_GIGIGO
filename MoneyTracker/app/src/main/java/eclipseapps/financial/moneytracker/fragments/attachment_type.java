package eclipseapps.financial.moneytracker.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.sync.SyncService;
import eclipseapps.libraries.library.general.functions.general;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;


/**
 * Created by usuario on 21/07/17.
 */
public class attachment_type extends baseFragment {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_LOAD =2 ;
    public  String mCurrentPhotoFilePath;
    public final static String imagePath="mCurrentPhotoFilePath";
    RelativeLayout view;
    GridView attachemnt_images;
    public ImageAdapter adapter;
    TextView TxtImages;
    private DataSetObserver observer;

    //public static List<String> images=new ArrayList();
    public attachment_type(){
        super();
        adapter=new ImageAdapter(getActivity());
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //adapter=new ImageAdapter(getActivity());
        if (savedInstanceState!=null){
            adapter.setmThumbPaths(savedInstanceState.getStringArrayList("paths"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("paths",adapter.getmThumbPaths());
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= (RelativeLayout) inflater.inflate(R.layout.fragment_attachment_type,container,false);
        RelativeLayout photo= (RelativeLayout) view.findViewById(R.id.attachment_type_icon_camera);
        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogSelectType dialog=new DialogSelectType().setParent(attachment_type.this);
                dialog.show(getChildFragmentManager(),"ImageType");
            }
        };
        photo.setOnClickListener(listener);
        TxtImages=view.findViewById(R.id.textView_Imagenes);
        TxtImages.setOnClickListener(listener);
        labelGoneIfHasImages();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                labelGoneIfHasImages();
                if(observer!=null)observer.onChanged();
            }
        });
        attachemnt_images= (GridView) view.findViewById(R.id.attachment_type_imageContainer);
        attachemnt_images.setAdapter(adapter);

        if (SyncService.stateService.matches(SyncService.ACTION_PROGRESO)){
            general.disableViews((ViewGroup) view);
        }
        return view;
    }
    private void labelGoneIfHasImages(){
        if(adapter.getCount()>0){
            TxtImages.setVisibility(View.GONE);
        }else{
            TxtImages.setVisibility(View.VISIBLE);
        }
    }
    public void registerDataObserver(DataSetObserver observer){
        this.observer=observer;
    }
    public void onGalleryImage(Uri selectedImage, CircleImageView view ){

        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        view.setImageBitmap(BitmapFactory.decodeFile(picturePath));
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

                // Error occurred while creating the File
                ///...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "eclipseapps.financial.moneytracker.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra(imagePath,mCurrentPhotoFilePath);
                List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getActivity().grantUriPermission(packageName, photoURI, FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);
                }
                getActivity().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    public  File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String imageFileName =  timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoFilePath = image.getAbsolutePath();
        return image;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == getActivity().RESULT_OK){
                if(requestCode==REQUEST_IMAGE_LOAD){
                    ArrayList<String> imagesPathList = new ArrayList<String>();
                    ClipData images=data.getClipData();
                    if (images==null){
                        addImageToAdapter(data.getData(),data.getFlags() & FLAG_GRANT_READ_URI_PERMISSION);
                    }else{
                        for (int i=0;i<images.getItemCount();i++) {
                            addImageToAdapter(images.getItemAt(i).getUri(),data.getFlags() & FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                   AnalyticsApplication.writeMovementsTracking(AnalyticsApplication.Write.Create, AnalyticsApplication.Action.Image, AnalyticsApplication.Tag.FromGallery.getValue());
                    adapter.notifyDataSetChanged();
                    attachemnt_images.invalidate();
                }else{
                    AnalyticsApplication.writeMovementsTracking(AnalyticsApplication.Write.Create, AnalyticsApplication.Action.Image, AnalyticsApplication.Tag.FromCamera.getValue());
                    //Si no fue Image Load Fue por IMage Capture con lo cual la variable mCurrentPhotoFilePath ya tiene la ruta
                    if(mCurrentPhotoFilePath!=null){
                        adapter.add(mCurrentPhotoFilePath);
                        adapter.notifyDataSetChanged();
                        attachemnt_images.invalidate();
                    }
                }
            }
    }
    public void addImageToAdapter(Uri selectedImageUri,final int takeFlags){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getActivity().getContentResolver().takePersistableUriPermission (selectedImageUri, takeFlags);
        }

        if (selectedImageUri.toString().startsWith("content://")){
            mCurrentPhotoFilePath = selectedImageUri.toString();
        }else{
            mCurrentPhotoFilePath = getPath(selectedImageUri);
        }
        adapter.add(mCurrentPhotoFilePath);
    }
    public String getPath(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void loadImages(){
        if (adapter.size()>0){
           adapter.notifyDataSetChanged();
        }
    }
    public void clear(){
        adapter=new ImageAdapter(getActivity());
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                labelGoneIfHasImages();
                if(observer!=null)observer.onChanged();
            }
        });
        attachemnt_images.setAdapter(adapter);
        attachemnt_images.invalidateViews();
        labelGoneIfHasImages();
    }
    public void setImages(Cursor cur){
        if (cur!=null){

            ArrayList images=new ArrayList<String>();
            if (cur.getCount()>0){
                cur.moveToFirst();
                do{
                    if(!images.contains(cur.getString(cur.getColumnIndex("imagen_"))))
                        images.add(cur.getString(cur.getColumnIndex("imagen_")));
                }while (cur.moveToNext());
            }
            if(adapter==null) {
                adapter = new ImageAdapter(getActivity());
            }
            adapter.setmThumbPaths(images);

        }
    }
    public void fillThump(final ImageView image){
        fillThump(mCurrentPhotoFilePath,image);
    }
    public synchronized void fillThump(final String Path,final ImageView image){
        //imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
        //imageLoader.displayImage("file://" + ((fragment_movements_list.RowData)getChild(iParent,iChild)).Images.get(0), ImagenesAdjuntas);
        //Picasso.with(getActivity()).load(Path).into(circleimage);
        image.post(new Runnable() {
            @Override
            public void run() {

                int targetW = image.getWidth();
                int targetH = image.getHeight();


                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(Path, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                Bitmap realImage = BitmapFactory.decodeFile(Path, bmOptions);
              //  Bitmap bitmap = imageOreintationValidator(realImage, Path);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + Path), "image/*");
                        startActivity(intent);
                    }
                });
              //  circleimage.setImageBitmap(bitmap);
                image.invalidate();

                //Picasso.with(getActivity()).load(R.drawable.premiumversionbenefits).into(circleimage);
                //circleimage.invalidate();
            }
        });

    }

    private int dpsToPixel(int dps){
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static class DialogSelectType extends DialogFragment{
        attachment_type parent;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            CardView general= (CardView) inflater.inflate(R.layout.fragment_attachment_type_dialog,container,false);
            LinearLayout gallery= (LinearLayout) general.findViewById(R.id.attachment_type_dialog_gallery);
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT>=23){
                        if(getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                            // Should we show an explanation?
                            if (shouldShowRequestPermissionRationale(
                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // Explain to the user why we need to read the contacts
                            }

                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE));

                            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                            // app-defined int constant that should be quite unique

                            return;
                        }
                    }

                    DialogSelectType.this.dismiss();
                    launchPickerImage();

                  /*
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Selecciona una imagen");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {getIntent});

                    parent.startActivityForResult(getIntent, REQUEST_IMAGE_LOAD);
                    */
                    //Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    //startActivityForResult(i, REQUEST_IMAGE_LOAD);
                }
            });
            LinearLayout photo= (LinearLayout) general.findViewById(R.id.attachment_type_dialog_camera);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DialogSelectType.this.dismiss();
                    parent.dispatchTakePictureIntent();
                }
            });
            return general;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog= super.onCreateDialog(savedInstanceState);
            dialog.setTitle("Seleccionar desde...");
            return dialog;
        }

        public DialogSelectType setParent(attachment_type parent) {
            this.parent = parent;
            return this;
        }
        public void launchPickerImage(){
            Intent intent;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }else{
                intent = new Intent(Intent.ACTION_GET_CONTENT);
            }
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            Log.d("Movement","Launching ImageRequest with REQUEST IMAGE LOAD");
            getActivity().startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), REQUEST_IMAGE_LOAD);
        }
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode==getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)   && grantResults[0]==0){
               launchPickerImage();
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        ImageLoader imageLoader = ImageLoader.getInstance();
        // references to our images
        private ArrayList<String> mThumbPaths=new ArrayList<String>();

        public ArrayList<String> getmThumbPaths() {
            return mThumbPaths;
        }

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbPaths.size();
        }

        public Object getItem(int position) {
            return mThumbPaths.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {
            FrameLayout view;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                view= (FrameLayout) getLayoutInflater().inflate(R.layout.circleimage_element,null);
                if(Build.VERSION.SDK_INT<=19){
                    AbsListView.LayoutParams params=new AbsListView.LayoutParams(dpsToPixel(40),dpsToPixel(40));
                    view.setLayoutParams(params);
                }else{
                    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(dpsToPixel(40),dpsToPixel(40));
                    params.setMargins(dpsToPixel(5),dpsToPixel(5),dpsToPixel(5),dpsToPixel(5));
                    view.setLayoutParams(params);
                }
            } else {
                view = (FrameLayout) convertView;
            }
            ImageView delete=view.findViewById(R.id.circleimage_element_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mThumbPaths.get(position).startsWith(("file://"))){
                        //Borra la foto de la memoria
                    }
                    mThumbPaths.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
            CircleImageView imageView=view.findViewById(R.id.circleimage_element_circleimage);

                boolean loaded=false;
                if(!imageLoader.isInited()){
                    imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
                }
                if(mThumbPaths.get(position).startsWith(("content://"))){
                    Uri myUri = Uri.parse(mThumbPaths.get(position));
                    if(getPath(myUri)!=null){
                        imageLoader.displayImage(mThumbPaths.get(position), imageView);
                        loaded=true;
                    }else{
                        try{
                            // set bitmap to imageview
                            imageLoader.displayImage(mThumbPaths.get(position),imageView);
                            loaded=true;
                        }
                        catch (Exception e){
                            //handle exception
                            e.printStackTrace();
                        }
                    }
                }else{
                    File file=new File(mThumbPaths.get(position));
                    if (file.exists()) {
                        imageLoader.displayImage("file://" + mThumbPaths.get(position), imageView);
                        loaded=true;
                    }
                }
                if (loaded){
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            if(mThumbPaths.get(position).startsWith(("content://"))){
                                intent.setDataAndType(Uri.parse(mThumbPaths.get(position)), "image/*");
                            }else{
                                File photoFile=new File(mThumbPaths.get(position));
                                if(photoFile.exists()){
                                    Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                            "eclipseapps.financial.moneytracker.fileprovider",
                                            photoFile);
                                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                                }

                            }

                            startActivity(Intent.createChooser(intent, "Share Image"));

                        }
                    });
                }
            return view;
        }

        public ImageAdapter setmThumbPaths(ArrayList<String> mThumbPaths) {
            this.mThumbPaths = mThumbPaths;
            return this;
        }

        public void add(String mCurrentPhotoFilePath) {
            mThumbPaths.add(mCurrentPhotoFilePath);
        }

        public int size() {
            return mThumbPaths.size();
        }
    }
}
