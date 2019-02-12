package eclipseapps.mobility.parkeame.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.cloud.Precios;

/**
 * Created by usuario on 27/08/17.
 */
public class PricesTabs extends Fragment {
    private List<Precios> precios;
    private onSelectedTabListener listener;

    public PricesTabs setPrecios(List<Precios> precios, onSelectedTabListener listener) {
        this.precios = precios;
        this.listener=listener;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.e_fixtures_new_tabs,container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) view.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(viewPager);


        return view;

    }

    void updatePrices(){
        ViewPager viewPager = (ViewPager) getView().findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.getAdapter().notifyDataSetChanged();
    }
    private Adapter onAdapterReady(){
        Adapter adapter = new Adapter(getChildFragmentManager());
        if(precios==null){

        }
        for (int i=0;i<precios.size();i++){
            final int tiempo=precios.get(i).getTiempoEstacionamiento_();
            String[] formated = Precios.retriveTimeFormated(precios.get(i).getTiempoEstacionamiento_());
            String Titulo;
            if(formated[0].matches("0 hr")){
                Titulo=formated[1];
            }else if(formated[1].matches("0 min")){
                Titulo=formated[0];
            }else{
                Titulo=formated[0]+"\n"+formated[1];
            }
            final float precio=precios.get(i).getPrecio_();
            adapter.addFragment(new PriceTimeFragment().setPrice(precio).setListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTabClick(precio,tiempo);
                }
            }), Titulo);
        }
        return adapter;
    }
    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        viewPager.setAdapter(onAdapterReady());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                listener.onTabselected(precios,position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        listener.onTabselected(precios,0);
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);

        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public static class PriceTimeFragment extends Fragment{
        private float price;
        View.OnClickListener listener;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            RelativeLayout rootView= (RelativeLayout) inflater.inflate(R.layout.e_e_fragment_element,container,false);
            TextView Integer= (TextView) rootView.findViewById(R.id.e_e_integers);
            TextView Decimals= (TextView) rootView.findViewById(R.id.e_e_decimals);
            int integer = (int)this.price;
            int decimal = (int) (Math.round(100 * this.price - 100 * integer));
            Integer.setText("Parkear por $"+String.valueOf(integer));
            Decimals.setText(String.format("%02d", decimal));
            rootView.setOnClickListener(listener);
                return rootView;
        }
        public PriceTimeFragment setPrice(float Price){
            this.price=Price;
            return this;
        }

        public PriceTimeFragment setListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }
    }

    public interface onSelectedTabListener{
        void onTabselected(List<Precios> precios,int timeInMin);
        void onTabClick(float price,int timeInMin);
    }


}
