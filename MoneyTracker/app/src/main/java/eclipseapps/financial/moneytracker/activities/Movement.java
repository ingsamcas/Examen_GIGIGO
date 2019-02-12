package eclipseapps.financial.moneytracker.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nex3z.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import eclipseapps.android.FragmentN;
import eclipseapps.android.Interfaces.DateTimePickerEvents;
import eclipseapps.android.customviews.DatePickerEditText;
import eclipseapps.android.customviews.SafeInputConnectionEditText;
import eclipseapps.android.customviews.TextViewRoboto;
import eclipseapps.android.customviews.TimePickerEditText;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.cloud.DBSmartWallet;
import eclipseapps.financial.moneytracker.cloud.basics;
import eclipseapps.financial.moneytracker.cloud.cuentas;
import eclipseapps.financial.moneytracker.cloud.facturas;
import eclipseapps.financial.moneytracker.cloud.motives;
import eclipseapps.financial.moneytracker.cloud.rfc;
import eclipseapps.financial.moneytracker.cloud.ventas;
import eclipseapps.financial.moneytracker.customViews.SelectorCheckbox;
import eclipseapps.financial.moneytracker.customViews.tag_item;
import eclipseapps.financial.moneytracker.fragments.CreateOrUpdateCuenta;
import eclipseapps.financial.moneytracker.fragments.attachment_type;
import eclipseapps.financial.moneytracker.fragments.bannerFragment;
import eclipseapps.financial.moneytracker.fragments.baseFragment;
import eclipseapps.financial.moneytracker.fragments.createEditMotives;
import eclipseapps.financial.moneytracker.sync.SyncService;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.Timers;
import eclipseapps.libraries.library.general.functions.general;

///extends ABarBillTapForTap implements ActionBar.TabListener
public class Movement extends trackedActivity implements OnClickListener {
	bannerFragment ads;
	MovementBasicsObjectFragment basics;
	attachment_type attachImages;
	MovementMotiveObjectFragment fragmentMotives;


	basics _movment;
	public cuentas Account;
	//public static ViewPager mViewPager;
	ArrayAdapter<cuentas> arrayadp;
	DBSmartWallet db;
	Spinner nav;
	//MovementCollectionPagerAdapter mAdapter;

	BroadcastReceiver brSms;
	//TabLayout tabLayout;
	RelativeLayout progress;
	Bitmap iconAcepted;
	Timers timer;
	public final static int NewAccount=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadBroadcastReciver();
		setContentView(R.layout.activity_movement);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// back button pressed
				onBackPressed();
			}
		});



		progress=findViewById(R.id.activity_movment_progress);

		if(baseActivity.mIsPremium){
			if(ads!=null && ads.isVisible()){
				getSupportFragmentManager().beginTransaction().remove(ads).commit();
			}
		}else{
			ads=new bannerFragment().setmIsPremium(false);
			getSupportFragmentManager().beginTransaction().add(R.id.adFragment,ads,"BannerFragment").commit();
		}

	   	arrayadp=new ArrayAdapter<cuentas>(this,R.layout.layout_spinner_iten){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView==null){
                    convertView=getLayoutInflater().inflate(R.layout.layout_spinner_iten,null);
                }
				View v=convertView;
				String Cuenta=arrayadp.getItem(position).get_cuenta();
				((TextViewRoboto)v).setText(Cuenta);
				return v;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if(position==getCount()-1){
                    View v=getLayoutInflater().inflate(R.layout.layout_spinner_iten_addaccount,null);
                    v.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        	Movement.this.AcountTracking("New Account Button","");
                            CreateOrUpdateCuenta.DialogFragment dialog=new CreateOrUpdateCuenta.DialogFragment()
                                    .onResultlistener(new CreateOrUpdateCuenta.DialogFragment.onResult() {
                                        @Override
                                        public void onResult(cuentas cuenta) {
											Movement.this.AcountTracking("New Account Created",cuenta.get_cuenta());
                                            loadAccounts();
                                            arrayadp.notifyDataSetChanged();
                                            nav.setSelection(arrayadp.getPosition(cuenta));
                                            Account=cuenta;
                                        }
                                    });
                            dialog.show(getSupportFragmentManager(),"dialog_nuevacuenta");
                        }
                    });
                    return v;
                }else{
                    convertView=getLayoutInflater().inflate(R.layout.layout_spinner_iten,null);
                    View v=convertView;
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    if(params!=null){
                        // Set the height of the Item View
                        params.height = 40;
                        v.setLayoutParams(params);
                    }

                    String Cuenta=arrayadp.getItem(position).get_cuenta();

                    if(Build.VERSION.SDK_INT>=23){
                        ((TextView)v).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark,null));
                    }else{
                        ((TextView)v).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                    ((TextView)v).setText(Cuenta);
                    return v;
                }
			}
		};
      //  arrayadp.setDropDownViewResource(android.R.drawable.ic_menu_more);
	   	//Aqui se agrgan todos los items, por el momento solo efectivo
		db=DBSmartWallet.getInstance(this);
	   	loadAccounts();

		nav= (Spinner) findViewById(R.id.spinner_nav);
		nav.setAdapter(arrayadp);
		nav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
				String cuenta=arrayadp.getItem(position).get_cuenta();
				if (cuenta!=cuentas.NuevaCuenta) {
					Account=arrayadp.getItem(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});


		basics= (MovementBasicsObjectFragment) getSupportFragmentManager().findFragmentByTag("BasicsFragment");
		basics.setOnLifeCycleListener(new FragmentN.LifeCycleObserver() {
			@Override
			public void onActivityCreated(@Nullable Bundle savedInstanceState) {
				FragmentManager fm=basics.getChildFragmentManager();
				final Fragment fragmentImages=fm.findFragmentByTag("Images");
				if(fragmentImages==null){
					attachImages=setAttachImages(getSupportFragmentManager());
				}
				fragmentMotives= (MovementMotiveObjectFragment) fm.findFragmentByTag("Motives");
				if(fragmentMotives==null){
					if (db==null)db=DBSmartWallet.getInstance(getApplicationContext());
					fragmentMotives=new MovementMotiveObjectFragment();
					fragmentMotives.setOnLifeCycleListener(new FragmentN.LifeCycleObserver() {
						@Override
						public void onActivityCreated(@Nullable Bundle savedInstanceState) {

						}

						@Override
						public void onResume() {
							Cursor cur=db.getDBInstance().rawQuery("SELECT * FROM motives WHERE enabled_=1",null);
							List<motives> m=db.mapCursorToObjectList(cur,motives.class);
							fragmentMotives.setPossibleTags(m);
						}
					});

					if(_movment!=null && _movment.getId_()!=0){
						fragmentMotives.loadtags(db.getDBInstance().rawQuery("SELECT * FROM tags WHERE id_="+_movment.getId_(),null));
					}
					fm.beginTransaction().replace(basics.motiveContainer.getId(),fragmentMotives,"Motives").commit();
				}
			}

			@Override
			public void onResume() {

			}
		});
		basics.setPossibleRFCs(db.mapCursorToObjectList(db.getDBInstance().rawQuery("SELECT * FROM rfc",null),rfc.class));
		long mov=getIntent().getLongExtra(SyncService.movimiento,-1);
		if (mov>-1){
			basics[] result=db.getbasicDataMovements("SELECT * FROM basics WHERE id_="+mov,null);
			basics movement=result[0];
			for (int i=0 ;i<arrayadp.getCount();i++){
				if (arrayadp.getItem(i).get_cuenta().matches(movement.getCuenta_()))nav.setSelection(i);
			}
			loadMovement(movement);
			facturas factura=db.selectFirst("SELECT * FROM facturas WHERE id_="+_movment.getId_(),facturas.class);
			String RFC=factura==null?"":factura.getRfc_();
			basics.setValues(db.mapCursorToObjectList(db.getDBInstance().rawQuery("SELECT * FROM basics WHERE id_="+_movment.getId_(),null),basics.class),RFC);
		}else{
			basics basic=db.selectFirst("SELECT * FROM basics WHERE created=(SELECT max(created) FROM basics)",basics.class);
			if(basic==null){
				Account=arrayadp.getItem(0);//Se selecciona la cuenta de efectivo por defecto
			}else{
				for (int i=0;i<arrayadp.getCount();i++) {
					if(basic.getCuenta_()!=null && basic.getCuenta_().matches(arrayadp.getItem(i).get_cuenta())){
						Account=arrayadp.getItem(i);//Se selecciona la cuenta de efectivo por defecto
						nav.setSelection(i);
					}
				}
			}
			loadMovement(null);
		}



	}
	public  attachment_type setAttachImages(FragmentManager fm){
		attachImages=new attachment_type();
		attachImages.setOnLifeCycleListener(new FragmentN.LifeCycleObserver() {
			@Override
			public void onActivityCreated(@Nullable Bundle savedInstanceState) {

				showRFCRequestIfAttachImagesVisible();
				if(basics.RFC==null || basics.RFC.matches("")){
					basics.selectorRFC.setHint("RFC...");
				}else{
					basics.CheckRequest.setChecked(true);
					for(int i=0;i<basics.Posiblerfc.size();i++){
						if(basics.Posiblerfc.get(i)!=null && basics.Posiblerfc.get(i).getRfc_()!=null && basics.Posiblerfc.get(i).getRfc_().matches(basics.RFC)){
							basics.selectorRFC.clear();
							basics.selectorRFC.addSelected(basics.RFC);
							basics.selectorRFC.SetText();
							break;
						}
					}

				}

			}

			@Override
			public void onResume() {//On Resume AttacmentImages

			}
		});
		attachImages.setImages(db.getDBInstance().rawQuery("SELECT * FROM descripimagenes WHERE id_="+_movment.getId_(),null));
		attachImages.registerDataObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				showRFCRequestIfAttachImagesVisible();
			}

		});
		fm.beginTransaction().replace(basics.attachContainer.getId(),attachImages,"Images").commit();
		return attachImages;
	}
	public void showRFCRequestIfAttachImagesVisible(){
		if(Movement.this.countryCodeValue.matches("mx")){
			if(attachImages.adapter.getCount()==0){
				basics.hideRFCRequest();
			}else{
				basics.showRFCRequest();
			}
		}else{
			basics.hideRFCRequest();
		}
	}
	private void loadAccounts(){
		arrayadp.clear();
		List<cuentas> AL=db.getAllAccount();
		for (cuentas row:AL) {
			arrayadp.add(row);
		}
		cuentas nueva=new cuentas();
				nueva.set_cuenta(cuentas.NuevaCuenta);
		arrayadp.add(nueva);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		db.Close();
		super.onStop();
		
	}

	@Override
	public void onBackPressed() {
		if(SyncService.stateService.matches(SyncService.ACTION_PROGRESO)){
			Toast.makeText(this,"Espera. Guardando el movimiento",Toast.LENGTH_LONG).show();
			return;
		}
		//hideKeyboard(this);
		showOkCancelDialog("Desea salir sin guardar cambios?", "Si", "No", true, new Dialogs.DialogsInterface() {
			@Override
			public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
				if(sucess){
					setResult(RESULT_CANCELED);
					Movement.this.finish();
				}
			}
		});
		//hideKeyboard(this);

	}
    public static boolean hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		basics.onActivityResult(requestCode,resultCode,data);
		if(requestCode==dialogActivity.action_RFCDialog_requestCode && resultCode==RESULT_OK){
			basics.setPossibleRFCs(db.mapCursorToObjectList(db.getDBInstance().rawQuery("SELECT * FROM rfc",null),rfc.class));
		}else{
			attachImages.onActivityResult(requestCode, resultCode, data);
		}
	}


	@Override
	public boolean onSupportNavigateUp() {
		return super.onSupportNavigateUp();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(brSms!=null){
			IntentFilter filter = new IntentFilter();
			filter.addAction(SyncService.ACTION_PROGRESO);
			filter.addAction(SyncService.ACTION_FIN);
			filter.addAction(SyncService.ACTION_INTERRUPT);
			registerReceiver(brSms, filter);
		}
		if(iconAcepted==null){
			iconAcepted=BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_48dp);
		}
	}

	@Override
	protected void onPause() {
		if (brSms!=null){
			unregisterReceiver(brSms);
		}
		if(timer!=null)timer.Stop();
		super.onPause();
	}

	void loadBroadcastReciver(){

		brSms=new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(SyncService.ACTION_PROGRESO)) {
					basics.OkButton.setEnabled(false);
					nav.setEnabled(false );
					progress.setVisibility(View.VISIBLE);
				}else if(intent.getAction().equals(SyncService.ACTION_FIN)) {//Finalizo exitoso.thi
					if(getIntent().getLongExtra(SyncService.movimiento,-1)!=-1){
						long timeToUpdate=SystemClock.elapsedRealtime()+1000*60*60*8;
						sp.edit().putLong(ventas.sellsPreferences.TimeToEditEnable, timeToUpdate).apply();
					}
					basics.OkButton.setEnabled(true);
					basics.OkButton.doneLoadingAnimation(Color.rgb(0, 105, 92),iconAcepted);
					nav.setEnabled(true );
					progress.setVisibility(View.INVISIBLE);

					confirmDialog();
				}else if(intent.getAction().equals(SyncService.ACTION_INTERRUPT)) {//Solo si hubo error
					basics.OkButton.setEnabled(true);
					nav.setEnabled(true );
					progress.setVisibility(View.INVISIBLE);
					showOkDialog(SyncService.getFault(),"OK",true,null);
				}

			}
		};

	}



	public void confirmDialog(){
	showOkCancelDialog(getResources().getString(R.string.movement_registred),
		getResources().getString(R.string.Ok),
		getResources().getString(R.string.movement_add_other), false, new Dialogs.DialogsInterface() {
			@Override
			public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
				if(sucess){
					setResult(RESULT_OK);
					Movement.this.finish();
				}else{
					basics.OkButton.revertAnimation();
					progress.setVisibility(View.GONE);
					loadMovement(null);
					basics.clear();
					basics.loadvalues();
					if(attachImages==null)attachImages=setAttachImages(getSupportFragmentManager());
					else attachImages.clear();
					showRFCRequestIfAttachImagesVisible();
					fragmentMotives.clear();
					//fragmentMotives.Motive_Value.add(getResources().getStringArray(R.array.tags)[0]);
					fragmentMotives.refreshMotives();
				}
			}
		});
		Movement.this.sp.edit().putBoolean("FirstMovAdded",true).apply();//Se modifica el registro para que los anuncios se comiencen a presentar
	}

	@Override
	public void onClick(View view) {


			if (basics.Quantity_Value == 0) {
				showOkDialog(getResources().getString(R.string.distinto_cero), getResources().getString(R.string.accept), true, null);
				return;
			} else if (fragmentMotives.Motive_Value.size() == 0) {
				showOkDialog(getResources().getString(R.string.almenos_unacategoria), getResources().getString(R.string.accept), true, null);
				return;
			}
			if(basics.requestRFC() && (basics.RFC==null || basics.RFC.matches(""))){
				showOkDialog(getResources().getString(R.string.seleccionar_rfc), getResources().getString(R.string.accept), true, null);
				return;
			}
			if (attachImages != null) {
				ArrayList<String> paths = new ArrayList<String>();
				paths = attachImages.adapter.getmThumbPaths();
				if (paths != null && paths.size() == 0 && basics.RFC!=null && !basics.RFC.matches("")&& basics.requestRFC()) {
					showOkDialog(getResources().getString(R.string.tomar_unafoto), getResources().getString(R.string.accept), true, null);
					return;
				}
			}
			CircularProgressButton okButton=(CircularProgressButton)view;
			okButton.setEnabled(false);
			okButton.startAnimation();
			timer=new Timers(500, new TimerTask() {
				@Override
				public void run() {
					_movment.setCantidad_(basics.Quantity_Value * basics.Direction);
					_movment.setTiempo_(basics.Time);
					_movment.setDescripcion_(basics.Description_Value);
					_movment.setCuenta_(Account.get_cuenta());
					_movment.setIdcuenta_(Account.get_id());
					//mAdapter._movment.savein(db);//Crea o actualiza un registro

					final Intent msgIntent = new Intent(Movement.this, SyncService.class);
					msgIntent.setAction(SyncService.ACTION_SAVE_LOCAL);
					msgIntent.putExtra(SyncService.movimiento,getIntent().getLongExtra(SyncService.movimiento,-1));
					msgIntent.putExtra(SyncService.RFC,basics.requestRFC()?basics.RFC:"");
					msgIntent.putExtra(SyncService.basics,_movment);
					msgIntent.putExtra(SyncService.cuentas,Account);
					msgIntent.putExtra(SyncService.tags,fragmentMotives.Motive_Value);
					if(attachImages!=null && attachImages.adapter!=null && attachImages.adapter.getmThumbPaths()!=null && attachImages.adapter.getmThumbPaths().size()>0){
						msgIntent.putExtra(SyncService.paths,attachImages.adapter.getmThumbPaths());
					}
					if(fragmentMotives.Motive_Value.size()>0){
						for (String motive:fragmentMotives.Motive_Value) {
							AnalyticsApplication.sendTrack(AnalyticsApplication.Write.Create.getValue(),motive,String.valueOf(_movment.getCantidad_()));
						}
					}
					Movement.this.startService(msgIntent);
				}
			});
			timer.Start();

			//JobIntentService.enqueueWork(this,SyncService.class,100,msgIntent);
	}

	public void loadMovement(basics movment){
		if(movment!=null){
			_movment=movment;
		}else{
			_movment=new basics();
			//_movment.setId_(db.NextId(basics.class.getSimpleName()));//Ya no se necsita esto desde la version 8 de la base de datos
		}
	}


	// Instances of this class are fragments representing a single
	// object in our collection.
	public static class MovementBasicsObjectFragment extends FragmentN{
		CircularProgressButton OkButton;

		public static final String Quantity_="Quantity";
		public static final String Direction_="Direction";
		public static final String Description_="Description";
		public static final String Time_="Time";
		public static final String Rfc_="RFC";

		public double Quantity_Value=0.0;
		public int Direction=-1;//-1=Egreso,1=Ingreso
		public String Description_Value="";
		public String RFC;
		public long Time=System.currentTimeMillis();

		//AppCompatSpinner spinnerRFC;
		SelectorCheckbox selectorRFC;
		TextView outcome_income;
		SafeInputConnectionEditText Et;
		TextWatcher tw=new TextWatcher(){
			CharSequence textBefore;
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				// erUtils.isCreatable
				if (arg0.toString()!=null && arg0.toString().matches("")==false && arg0.toString().matches("\\d+(?:\\.\\d+)?"))
					try{
						Quantity_Value=Double.valueOf(arg0.toString());
						if(Quantity_Value>100000000000000d){
							showOkDialog("La maxima cantidad que puedes introducir es:100000000000000","Ok",true,null);
							if(textBefore!=null && textBefore.toString()!=null && textBefore.toString().matches("")==false){
								Et.setText(textBefore.toString());
							}else{
								Et.setText("100000000000000");
							}
						}
					}catch (NumberFormatException e){
						Quantity_Value=0.0;
						if(textBefore!=null && textBefore.toString()!=null && textBefore.toString().matches("")==false){
							Et.setText(textBefore.toString());
						}else{
							Et.setText("0.0");
						}

					}

				else Quantity_Value=0.0;
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {
				// TODO Auto-generated method stub
				textBefore=arg0;
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1,
									  int arg2, int arg3) {
				// TODO Auto-generated method stub

			}};
		CheckBox checkAlarm;
		SwitchCompat switchcompat;
		TimePickerEditText ETTime;
		DatePickerEditText ETDate;
		EditText descripcion;
		LinearLayout attachContainer;
		private LinearLayout motiveContainer;
		LinearLayout facturaRequest;
		int a=0;
		private List<rfc> Posiblerfc=new ArrayList<rfc>();
		private CheckBox CheckRequest;


		@Override
	    public View onCreateView(final LayoutInflater inflater,
								 ViewGroup container, Bundle savedInstanceState) {
	        // The last two arguments ensure LayoutParams are inflated
	        // properly.
	        View rootView = inflater.inflate(
	                R.layout.fragment_movement_basics, container, false);

	        OkButton=(CircularProgressButton)rootView.findViewById(R.id.movement_guardar);
			OkButton.setOnClickListener((Movement)getActivity());

			//Aqui se recuperan los estados anteiores de la vista ,por ejemplo si la cantidad fue 5 en el edittext, entonces recuperarla
	       Et= (SafeInputConnectionEditText) rootView.findViewById(R.id.editText_quantity);
	       Et.addTextChangedListener(tw);
			outcome_income= (TextView) rootView.findViewById(R.id.switch_income_outcome_textview);
			switchcompat= (SwitchCompat) rootView.findViewById(R.id.switch_income_outcome);
			switchcompat.setTextOff("Retiro");
			switchcompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					if (b){switchcompat.setThumbResource(R.drawable.switch_income);outcome_income.setText("Ingreso");Direction=1;}
					else {switchcompat.setThumbResource(R.drawable.switch_outcome);outcome_income.setText("Retiro");Direction=-1;}
				}
			});
			switchcompat.setChecked(Direction<0?false:true);

			ETTime=(TimePickerEditText) rootView.findViewById(R.id.editTextTime);
			ETDate= (DatePickerEditText) rootView.findViewById(R.id.editTextDate);
			ETDate.setOnDatePickerEvents(new DateTimePickerEvents() {
				@Override
				public void OnClosePicker(int[] date) {
					ETTime.getCalendar().set(Calendar.YEAR,date[2]);
					ETTime.getCalendar().set(Calendar.MONTH,date[1]);
					ETTime.getCalendar().set(Calendar.DAY_OF_MONTH,date[0]);
					Time=ETTime.getCalendar().getTime().getTime();
				}
			});

			ETTime.setOnDatePickerEvents(new DateTimePickerEvents() {
				@Override
				public void OnClosePicker(int[] time) {
					Time=ETTime.getCalendar().getTime().getTime();
				}
			});
			checkAlarm=rootView.findViewById(R.id.check_isrepited);
			checkAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					if(b){
						Intent intent=new Intent(getActivity(),dialogActivity.class);
						intent.setAction(dialogActivity.action_SetAlarm);
						getActivity().startActivityForResult(intent,dialogActivity.action_SetAlarm_requestCode);
					}else{
						compoundButton.setText(getString(R.string.se_repite));
					}

				}
			});

			facturaRequest=rootView.findViewById(R.id.basics_fragment_factura_request_container);
			CheckRequest=(CheckBox)rootView.findViewById(R.id.basics_fragment_factura_request_checkbox);

			CheckRequest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					selectorRFC.setEnabled(b);
					if(!b){
						selectorRFC.clear();
						selectorRFC.invalidate();
					}
				}
			});

			selectorRFC= (SelectorCheckbox) rootView.findViewById(R.id.selector_RFC);
			selectorRFC.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					switch (motionEvent.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if(Posiblerfc.size()==0){
								showOkCancelDialog(getActivity().getString(R.string.fotoyrfc), "Registrar RFC", "Cancelar", true, new Dialogs.DialogsInterface() {
									@Override
									public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
										if(sucess){
											Intent intent=new Intent(getActivity(),dialogActivity.class);
											intent.setAction(dialogActivity.action_RFCDialog);
											getActivity().startActivityForResult(intent, dialogActivity.action_RFCDialog_requestCode);
										}
									}
								});
								return true;
							}
					}
					return false;
				}
			});
			selectorRFC.setOnselected(new SelectorCheckbox.onSelected() {
				@Override
				public void onSelected(List selected) {
					if(selected!=null && selected.size()>0){
						RFC= (String) selected.get(0);
					}
				}
			});

			if(((Movement)getActivity()).countryCodeValue.matches("mx")){
				populateRFCs();
			}else{
				facturaRequest.setVisibility(View.GONE);
			}



			descripcion=(EditText) rootView.findViewById(R.id.editText_textdescription);

			descripcion.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void afterTextChanged(Editable editable) {
					Description_Value=editable.toString();
				}
			});
			attachContainer=rootView.findViewById(R.id.basics_fragment_attach_container);
			motiveContainer=rootView.findViewById(R.id.basics_fragment_tag_container);
			if (SyncService.stateService.matches(SyncService.ACTION_PROGRESO)){
				general.disableViews((ViewGroup) rootView);
			}

			return rootView;
	    }

		private void populateRFCs() {
			List options=new ArrayList();
			for (rfc RFC:Posiblerfc) {
				options.add(RFC.getRfc_());
			}
			selectorRFC.selectMultiple(false).setOptions(options,false,-1).setVisibility(View.VISIBLE);
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putDouble(Quantity_,Quantity_Value);
			outState.putInt(Direction_, Direction);
			outState.putString(Description_,Description_Value);
			outState.putString(Rfc_,RFC);
			outState.putLong(Time_,Time);
			super.onSaveInstanceState(outState);
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (savedInstanceState!=null){
				Quantity_Value=savedInstanceState.getDouble(Quantity_,0.0);
				Direction=savedInstanceState.getInt(Direction_,1);
				Description_Value=savedInstanceState.getString(Description_,"");
				RFC=savedInstanceState.getString(Rfc_,"");
				Time=savedInstanceState.getLong(Time_,System.currentTimeMillis());
			}
		}


		@Override
		public void onStart() {
			super.onStart();
		}

		@Override
		public void onResume() {
			Et.requestFocus();
			super.onResume();
			loadvalues();
		}

		@Override
		public void onPause() {
			super.onPause();

		}
		public boolean requestRFC(){
			return CheckRequest.isChecked();
		}
		public void lostFocus(){
			//Et.setFocusableInTouchMode(false);
			Et.clearFocus();
		}
		public boolean hasFocus(){
			return Et.hasFocus();
		}
		public void clear(){
			Quantity_Value=0.0;
			Direction=-1;
			Description_Value="";
			Time=System.currentTimeMillis();
			RFC="";
			onSaveInstanceState(new Bundle());
		}
		public void loadvalues(){
			Et.setText(Quantity_Value==0?"":String.valueOf(Math.abs(Quantity_Value)));
			switchcompat.setChecked(Direction<0?false:true);
			outcome_income.setText(Direction<0?"Retiro":"Ingreso");
			ETTime.setTime(Time);
			ETDate.setFecha(Time);
			descripcion.setText(Description_Value);
			/*
			selectorRFC.clear();
			if(!RFC.matches("")){
				selectorRFC.addSelected(RFC);
				CheckRequest.setChecked(true);
				facturaRequest.setVisibility(View.VISIBLE);
			}else{
				CheckRequest.setChecked(false);
				facturaRequest.setVisibility(View.GONE);
			}
			selectorRFC.SetText();
			*/



		}


		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if(requestCode==dialogActivity.action_SetAlarm_requestCode){
				if(resultCode==RESULT_OK){
					if(data!=null){
						String freq=data.getStringExtra("freq");
						String timelapse=data.getStringExtra("timeLapse");
						if(timelapse.contains("de cada")){
							if(timelapse.contains("mes")){
								checkAlarm.setText("Se repite el "+freq+" de cada mes");
							}else{
								checkAlarm.setText("Se repite cada "+freq+" de "+timelapse.substring(timelapse.lastIndexOf(" ")+1));
							}
						}else{
							if(freq.matches("1")){
								if(timelapse.matches("días")) checkAlarm.setText("Se repite todos los días");
								else if(timelapse.matches("semanas")) checkAlarm.setText("Se repite cada semana");
								else if(timelapse.matches("meses")) checkAlarm.setText("Se repite cada mes");
								else if(timelapse.matches("años")) checkAlarm.setText("Se repite cada año");
							}else{
								checkAlarm.setText("Se repite cada "+freq+" "+timelapse);
							}
						}
					}
				}else{
					checkAlarm.setChecked(false);
				}
			}

		}

		public void setValues(List<basics> m,String RFC) {
			if(m!=null && m.size()>0){
				Quantity_Value=m.get(0).getCantidad_();
				Direction=m.get(0).getCantidad_()<0?-1:1;
				Description_Value=m.get(0).getDescripcion_();
				Time=m.get(0).getTiempo_();
				this.RFC=RFC;
			}
		}
		public void setPossibleRFCs(List<rfc> m) {
			Posiblerfc=m;
			List options=new ArrayList();
			for (rfc RFC:Posiblerfc) {
				options.add(RFC.getRfc_());
			}
			if(selectorRFC!=null){
				selectorRFC.selectMultiple(false).setOptions(options,false,-1).setVisibility(View.VISIBLE);
				selectorRFC.setHint("RFC...");
			}
		}

		public attachment_type getImageChildFragment() {
			return (attachment_type) getChildFragmentManager().findFragmentByTag("Images");
		}

		public void hideRFCRequest() {
			CheckRequest.setChecked(false);
			selectorRFC.clear();
			facturaRequest.setVisibility(View.GONE);
		}
		public void showRFCRequest() {
			CheckRequest.setChecked(false);
			populateRFCs();
			facturaRequest.setVisibility(View.VISIBLE);
			facturaRequest.invalidate();
		}
	}

	public static class MovementMotiveObjectFragment extends baseFragment implements OnClickListener {
		public ArrayList<String> Motive_Value=new ArrayList<String>();
		FlowLayout containerTags;
		private List<motives> PosibleTags;
        android.support.v7.app.AlertDialog dialog;
		ListView lista;
		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if(savedInstanceState!=null){
				Motive_Value=savedInstanceState.getStringArrayList("Motives");
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putStringArrayList("Motives",Motive_Value);
			super.onSaveInstanceState(outState);
		}

		@Override
	    public View onCreateView(LayoutInflater inflater,
	            ViewGroup container, Bundle savedInstanceState) {
	        // The last two arguments ensure LayoutParams are inflated
	        // properly.
	        View rootView = inflater.inflate(
	        R.layout.fragment_movement_class_outcoming, container, false);
			containerTags= rootView.findViewById(R.id.basics_fragment_tag_container);
			if (SyncService.stateService.matches(SyncService.ACTION_PROGRESO)){
				general.disableViews((ViewGroup) rootView);
			}
	        return rootView;
	    }

		@Override
		public void onResume() {
			super.onResume();
			refreshMotives();
		}

		private void refreshMotives(){
			containerTags.removeAllViews();
			LinearLayout.LayoutParams tagLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			tagLayoutParams.setMargins(0, 20, 0, 0);
			for (final String motive:Motive_Value){
				tag_item tag= new tag_item(getActivity());
				tag.setLayoutParams(tagLayoutParams);
				tag.setText(motive);
				tag.addOnRemoveListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						try{
							Motive_Value.remove(motive);
						}catch (Exception e){

						}
						refreshMotives();
					}
				});
				containerTags.addView(tag,0);
			}
			RelativeLayout plusButton= (RelativeLayout) getLayoutInflater().inflate(R.layout.tag_item_plus_button,containerTags,false);
			plusButton.setOnClickListener(this);
			plusButton.setLayoutParams(tagLayoutParams);
			containerTags.addView(plusButton);
		}

		private void loadtags(Cursor cur){

			if (cur!=null){
				ArrayList<String> tags=new ArrayList<String>();
				if (cur.getCount()>0){
					cur.moveToFirst();
					do{
						if(!tags.contains(cur.getString(cur.getColumnIndex("tag_"))))
							tags.add(cur.getString(cur.getColumnIndex("tag_")));
					}while (cur.moveToNext());
				}
				Motive_Value=tags;

			}
		}


		@Override
		public void onDestroy() {
			super.onDestroy();
			Motive_Value.clear();
		}

		public void setPossibleTags(List<motives> m) {
			PosibleTags=m;
		}
		public void clear(){
			if(Motive_Value!=null && Motive_Value.size()>0)Motive_Value.clear();
		}

		@Override
		public void onClick(View view) {
			if (dialog==null){
				android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());


				lista=new ListView(getActivity());
				lista.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				lista.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.layout_spinner_item_checkbox) {



					public void select(){
						// else if (!b && Selected.contains(compoundButton.getText())) {
						//   Selected.remove(compoundButton.getText());
						//}
					}
					@Override
					public void registerDataSetObserver(DataSetObserver dataSetObserver) {

					}

					@Override
					public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

					}

					@Override
					public int getCount() {

						return PosibleTags!=null?PosibleTags.size():0;
					}

					@Override
					public String getItem(int i) {
						return PosibleTags.get(i).get_motive();
					}

					@Override
					public long getItemId(int i) {
						return i;
					}

					@Override
					public boolean hasStableIds() {
						return false;
					}

					@Override
					public View getView(int i, View view, ViewGroup viewGroup) {
						CheckBox cbTag;
						if (view == null) {
							cbTag = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.layout_spinner_item_checkbox, null);
						} else {
							cbTag = (CheckBox) view;
						}
						cbTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
								if(compoundButton.getText().toString().matches(getResources().getString(R.string.All))){
									if (b){
										Motive_Value.clear();
										for (int j=0;j<PosibleTags.size();j++) {
											Motive_Value.add(PosibleTags.get(j).get_motive());
										}
										lista.invalidateViews();
									}else {
										Motive_Value.clear();
										lista.invalidateViews();
									}

								}else{

									if (isSelected(compoundButton)&& !b){
										remove(compoundButton);
										lista.invalidateViews();
									}else if(!isSelected(compoundButton)&& b ){
										Motive_Value.add(compoundButton.getText().toString());
										lista.invalidateViews();
									}

								}
							}

							private boolean isSelected(CompoundButton compoundButton) {
								return  Motive_Value.contains(compoundButton.getText());
							}
							public void remove(CompoundButton compoundButton) {
								try{
									Motive_Value.remove(compoundButton.getText());
								}catch (Exception e){

								}

							}
						});

						cbTag.setText(PosibleTags.get(i).get_motive());
						cbTag.setChecked(Motive_Value.contains(PosibleTags.get(i).get_motive()));
						//if (Motive_Value.contains(getResources().getString(R.string.All)))cbAll=cbTag;
						return cbTag;

					}

					@Override
					public int getItemViewType(int i) {
						return 0;
					}

					@Override
					public int getViewTypeCount() {
						return 1;
					}

					@Override
					public boolean isEmpty() {
						return false;
					}
				});
				builder.setView(lista);
				builder.setPositiveButton(R.string.Ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								MovementMotiveObjectFragment.this.refreshMotives();
								//if (onselected!=null)onselected.onSelected(getSelected());
							}
						});
				builder.setNegativeButton("Crear nueva categoria", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						final createEditMotives.DialogFragment dialogMotive=new createEditMotives.DialogFragment()
								.onResultlistener(new createEditMotives.DialogFragment.onResult() {
									@Override
									public void onResult(List<motives> motivos,String nuevoTag) {
										PosibleTags=motivos;
										Motive_Value.add(nuevoTag);
										AnalyticsApplication.sendTrack(AnalyticsApplication.Write.Create.getValue(), AnalyticsApplication.Action.Tag.getValue(),nuevoTag);
										dialog.show();
									}
								});
						dialogMotive.show(getActivity().getSupportFragmentManager(),"dialog_nuevomotive");
					}
				});
				dialog=builder.show();
			}else{
				dialog.show();
			}
		}


	}


}
