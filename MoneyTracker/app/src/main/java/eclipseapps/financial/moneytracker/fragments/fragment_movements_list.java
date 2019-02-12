package eclipseapps.financial.moneytracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eclipseapps.android.customviews.ImageViewSquare;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.TutorialAcitvity;
import eclipseapps.financial.moneytracker.activities.baseActivity;
import eclipseapps.financial.moneytracker.activities.trackedActivity;
import eclipseapps.financial.moneytracker.adapters.RowData;
import eclipseapps.financial.moneytracker.adapters.movementsListAdpter;
import eclipseapps.financial.moneytracker.adapters.yahoo_nativeAd_Adapter;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.basics;
import eclipseapps.financial.moneytracker.cloud.facturas;


public class fragment_movements_list extends baseFragment {




	private ExpandableListView LV;
	private movementsListAdpter adapter;
	private DBSmartWallet DB;

	public String Queryonbasics;
	private String[] Args;
	public int anim=1;

	View.OnLongClickListener onClickListener;
	Action onRefreshListener;
	Bundle sumsPerDay;
	ImageViewSquare imagevacumm;
	RelativeLayout vacummessage;

	public final static String result_ingreso="ingreso";
	public final static String result_egreso="egreso";
	public final static String result_total="total";

    public fragment_movements_list setOnClickListener(View.OnLongClickListener Listener) {
		onClickListener =Listener;
		return this;

	}
	public fragment_movements_list setOnRefreshDataListener(Action Listener) {
		onRefreshListener =Listener;
		return this;

	}

	public List getSelecteditems() {
		return adapter.getSelecteditems();
	}

	public fragment_movements_list LoadQuery(String queryonbasics, String[] args){
		setQuery(queryonbasics,args);
		return this;
	}


	public fragment_movements_list setQuery(String queryonbasics, String[] args){

		Queryonbasics=queryonbasics;
		Args=args;
		return this;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(adapter!=null)adapter.onResume();
		refreshData();
		if(trackedActivity.sp!=null){
			if (!trackedActivity.sp.getBoolean(TutorialAcitvity.action_HOW_ADD,false)){
				if(trackedActivity.sp.getBoolean(TutorialAcitvity.Preference_tutoEntendido,false)){//Ya lo habia entendido con una version antigua
					trackedActivity.sp.edit().putBoolean(TutorialAcitvity.action_HOW_ADD,true).apply();//Simplemente actualiza para que utilize las nuevas constantes a partir de version 3.9
					return;
				}
				Intent intent=new Intent(getActivity(),TutorialAcitvity.class);
				intent.setAction(TutorialAcitvity.action_HOW_ADD);
				startActivity(intent);
			}else if(!trackedActivity.sp.getBoolean(TutorialAcitvity.action_HOW_EDIT,false)&& trackedActivity.sp.getBoolean("FirstMovAdded",false)){
				Intent intent=new Intent(getActivity(),TutorialAcitvity.class);
				intent.setAction(TutorialAcitvity.action_HOW_EDIT);
				startActivity(intent);
			}else{
				if(trackedActivity.sp.getLong("Sessions",0)%7==0 && !trackedActivity.sp.getBoolean("hasRated",false)
						&&trackedActivity.sp.getBoolean(TutorialAcitvity.action_HOW_EDIT,false)
						&&trackedActivity.sp.getBoolean(TutorialAcitvity.action_HOW_ADD,false)
						&&!baseActivity.mIsPremium){
					Intent intent=new Intent();
					intent.setAction("RatedFalse");
					if(getAction()!=null)getAction().execute(intent);
				}
			}
		}
	}

	@Override
	public void onPause() {
		if(adapter!=null)adapter.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if(adapter!=null)adapter.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("Queryselected",Queryonbasics);

	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null){
			Queryonbasics=savedInstanceState.getString("Queryselected");
		}
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		FrameLayout LL= (FrameLayout) inflater.inflate(R.layout.fragment_movementslist, container,false);
		LV= (ExpandableListView) LL.findViewById(R.id.listviewmovements);
		if(baseActivity.mIsPremium){
			adapter= new movementsListAdpter(getActivity());
		}else{
			adapter= new yahoo_nativeAd_Adapter(getActivity());
		}

        adapter.setOnClickListener(onClickListener);
		imagevacumm= (ImageViewSquare) LL.findViewById(R.id.listviewmovements_imagevacum);
		vacummessage=LL.findViewById(R.id.listviewmovements_vacum);


//		refreshData();

		return LL;
	}

	public Bundle getSumPerDay(){
		return sumsPerDay;
	}
	public float getTotal(){
		float total=0.0f;
		Set<String> keySet = sumsPerDay.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()){
			total=total+sumsPerDay.getFloat(iterator.next(),0f);
		}
		return total;
	}
	public void refreshData(){
		if(Queryonbasics==null || Queryonbasics.matches("")){
			return;
		}
		if(DB==null)DB=DBSmartWallet.getInstance(getActivity());
		basics[] databasic=DB.getbasicDataMovements(Queryonbasics, Args);
		List<RowData> info=new ArrayList<RowData>();
		sumsPerDay=new Bundle();
		Double total=0.0;
		Double ingreso=0.0;
		Double egreso=0.0;
		final List<facturas> Facturas=DB.select("Select * FROM facturas",facturas.class);
		if (databasic!=null && databasic.length>0){
			for (int i=0;i<databasic.length;i++){
				RowData row=new RowData(databasic[i],DB);
				if(Facturas!=null && Facturas.size()>0){
					for(int j=0;j<Facturas.size();j++){
						if(row.basico.getId_()==Facturas.get(j).getId_()){
							row.facturaRFCSolicitado=(Facturas.get(j).getRfc_()==null?"":Facturas.get(j).getRfc_());
							row.factura=(Facturas.get(j).getPdf_()==null?"":Facturas.get(j).getPdf_());
							row.objectIdFactura=(Facturas.get(j).getObjectId()==null?"":Facturas.get(j).getObjectId());
						}
					}
				}
				if(row.basico.getCantidad_()<0){
					egreso=egreso+row.basico.getCantidad_();
				}else{
					ingreso=ingreso+row.basico.getCantidad_();
				}
				info.add(row);
				sumsPerDay.putDouble(row.fechaActual(), (sumsPerDay.getDouble(row.fechaActual(),0.0)+row.basico.getCantidad_()));
			}
		}

		adapter.setData(info,sumsPerDay);
		LV.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		if(info.size()==0){
			vacummessage.setVisibility(View.VISIBLE);
			ImageLoader imageLoader = ImageLoader.getInstance();
			if(!imageLoader.isInited())imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
			imageLoader.displayImage("drawable://" + R.drawable.iconosindatos2,imagevacumm);
		}else{
			vacummessage.setVisibility(View.INVISIBLE);
		}
		if(onRefreshListener!=null){
			Intent results=new Intent();
			results.putExtra(result_ingreso,ingreso);
			results.putExtra(result_egreso,egreso);
			results.putExtra(result_total,ingreso+egreso);
			onRefreshListener.execute(results);
		}
	}


	public movementsListAdpter getAdapter(){
		return adapter;
	}




}
