package eclipseapps.financial.moneytracker.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import eclipseapps.android.FragmentN;
import eclipseapps.financial.moneytracker.R;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by usuario on 23/11/17.
 */

public class gallery_activity extends trackedActivity {
    public  final static String imagenes="imagenes";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);
        final FlippableStackView stack = (FlippableStackView) findViewById(R.id.stack);
        stack.initStack(2);
        ArrayList im=getIntent().getStringArrayListExtra(imagenes);
       // stack.setAdapter(); //assuming mStackAdapter contains your initialized adapter
    }
    public String getPath(Uri contentUri) {


        String res = contentUri.getPath();
        String[] proj = { MediaStore.Images.Media.DATA,MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
        int count=cursor.getCount();
        int bucketColumn = cursor.getColumnIndex(
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        if(cursor.moveToFirst()){;
            do{
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                res = cursor.getString(column_index);
                Log.d("MoneyTracker",cursor.getString(bucketColumn)+res);
            }while (cursor.moveToNext()&&!res.contains("17491"));

        }
        cursor.close();
        return res;
    }
    public static class MyAdapter extends FragmentStatePagerAdapter {
        private static ArrayList<Integer> imagenes;
        private ArrayList descriptions;
        private FragmentN.Action action;

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            if(imagenes!=null){return imagenes.size();}
                else return 0;

        }

        @Override
        public Fragment getItem(final int position) {
            GalleryFragment fragment=GalleryFragment.newInstance(imagenes.get(position), (String) descriptions.get(position));
            fragment.setAction(action);
            return fragment;
        }

        public  MyAdapter setImagenes(ArrayList rutas){
            imagenes=rutas;
            return this;
        }

        public MyAdapter setDescriptions(ArrayList descriptions) {
            this.descriptions = descriptions;
            return this;
        }

        public MyAdapter setAction(FragmentN.Action action) {
            this.action = action;
            return this;
        }
    }

    public static class GalleryFragment extends FragmentN {
        String description;
        int imageResource;
        ImageLoader imageLoader = ImageLoader.getInstance();
        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static GalleryFragment newInstance(int imageResource,String description) {
            GalleryFragment f = new GalleryFragment();

            Bundle args = new Bundle();
            args.putInt("imageResource", imageResource);
            args.putString("description",description);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            imageResource = getArguments() != null ? getArguments().getInt("imageResource") : R.drawable.notification_icon;
            description=getArguments() != null ? getArguments().getString("description") : "";
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v=new View(getActivity());
            //Uri myUri = Uri.parse(imagePath);
            v = inflater.inflate(R.layout.fragment_pager_list_gif, container, false);
           GifImageView gif=v.findViewById(R.id.gif);
           gif.setImageResource(imageResource);
            TextView Description=v.findViewById(R.id.fragment_pager_list_description);
            Description.setText(description);
            TextView nextButton=v.findViewById(R.id.fragment_pager_list_button_next);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAction()!=null){
                        getAction().execute(null);
                    }
                }
            });
          // gif.
            //gif.setImageURI( Uri.parse(imagePath));
            /*
            if(imagePath.contains(".gif")){
                v = inflater.inflate(R.layout.fragment_pager_list_gif, container, false);
                GifImageView gif=v.findViewById(R.id.gif);


                final MediaController mc = new MediaController(getActivity());
                mc.setMediaPlayer((GifDrawable) gif.getDrawable());
                mc.setAnchorView(gif);
                gif.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mc.show();
                    }
                });
            }else{
                v = inflater.inflate(R.layout.fragment_pager_list, container, false);
                ImageView image= (ImageView) v.findViewById(R.id.fragment_pager_list_image);
                if(!imageLoader.isInited())imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
                if(imagePath.startsWith(("content://"))||imagePath.startsWith(("drawable://"))){
                    Uri myUri = Uri.parse(imagePath);
                    try{
                        // set bitmap to imageview
                        imageLoader.displayImage(imagePath,image);
                    }
                    catch (Exception e){
                        //handle exception
                        e.printStackTrace();
                    }
                }else{
                    File file=new File(imagePath);
                    if (file.exists()) {
                        imageLoader.displayImage("file://" + imagePath, image);
                    }
                }
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
/*
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                    if(imagePath.startsWith(("content://"))){
                        intent.setDataAndType(Uri.parse(imagePath), "image/*");
                    }else{
                        intent.setDataAndType(Uri.parse("file://" + imagePath), "image/*");
                    }

                    startActivity(intent);



                    //SyncService.sendDB(getActivity());





                        // SyncService.copyFile(getActivity());
                    }
                });

            }*/
            return v;

        }

    }
}
