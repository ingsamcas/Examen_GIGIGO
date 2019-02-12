package eclipseapps.financial.moneytracker.fragments;

import android.content.ContentValues;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yarolegovich.slidingrootnav.SlidingRootNav;

import java.util.Arrays;
import java.util.List;

import eclipseapps.android.customviews.FontFitTextView;
import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.financial.moneytracker.AnalyticsApplication;
import eclipseapps.financial.moneytracker.R;
import eclipseapps.financial.moneytracker.activities.MainActivity;
import eclipseapps.financial.moneytracker.cloud.motives;
import eclipseapps.financial.moneytracker.customViews.SelectorCheckbox;
import eclipseapps.libraries.library.general.functions.OrderMap;

public class categoryManager extends baseFragment implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener {
    ArrayAdapter<motives> adapter;
    public static String name="categoryManager";
    List<motives> tags;
    ListView lv;
    TextView addTag;
    FontFitTextView tagToEdit;
    RelativeLayout delete_icon;
    TextView editTag;
    RelativeLayout edit_container;
    RelativeLayout list_container;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout LL= (RelativeLayout) inflater.inflate(R.layout.j_categorymanager,container,false);
        lv=LL.findViewById(R.id.j_listview);
        addTag=LL.findViewById(R.id.j_pluscategory);
        tagToEdit=LL.findViewById(R.id.j_edit_categoryname);
        delete_icon =LL.findViewById(R.id.j_delete_icon);
        editTag =LL.findViewById(R.id.j_edit_editar);
        edit_container=LL.findViewById(R.id.j_edit_container);
        list_container=LL.findViewById(R.id.j_list_container);

        list_container.setVisibility(View.VISIBLE);
        edit_container.setVisibility(View.GONE);


        return LL;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tags=db.mapCursorToObjectList(db.getallfrom("motives ORDER BY motive_"),motives.class);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter=new ArrayAdapter<motives>(getActivity(),R.layout.j_item,tags){
            @Override
            public boolean areAllItemsEnabled() {
                return true;
            }

            @Override
            public boolean isEnabled(int i) {
                return true;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(final int i, View view, ViewGroup viewGroup) {
                RelativeLayout LL= (RelativeLayout) getLayoutInflater().inflate(R.layout.j_item,viewGroup,false);
                final TextView name=LL.findViewById(R.id.j_name);
                final SwitchCompat enabled=LL.findViewById(R.id.j_enable);

                name.setTag(i);
                name.setOnClickListener(categoryManager.this);
                String tempString = getItem(i).get_motive();
                SpannableString content = new SpannableString(tempString);
                content.setSpan(new UnderlineSpan(), 0, tempString.length(), 0);
                name.setText(content);
                enabled.setChecked(tags.get(i).get_enabled_());
                enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            Toast.makeText(getActivity(),"Categoria activada",Toast.LENGTH_LONG).show();
                        }else{
                            boolean lock=true;
                            for (motives motive:tags) {
                                if(motive.get_enabled_() && !motive.get_motive().matches(tags.get(i).get_motive())){
                                    lock=false;//Si puede desactivarla
                                }
                            }
                            if(lock){
                                enabled.setChecked(true);
                                Toast.makeText(getActivity(),"Debes tener al menos una categoría activa",Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(getActivity(),"Categoria desactivada:\nNo aparecera en la lista al momento de realizar un movimiento",Toast.LENGTH_LONG).show();
                        }
                        tags.get(i).set_enabled(b);
                        tags.get(i).update(db,"motive_='"+tags.get(i).get_motive()+"'");
                    }
                });
                LL.setVisibility(View.VISIBLE);
                return LL;
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
            public int getCount() {
                return super.getCount();
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public void remove(@Nullable motives object) {
            super.remove(object);

             getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        lv.invalidateViews();
                    }
                 });
            }
        };
        lv.setAdapter(adapter);
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final createEditMotives.DialogFragment dialogMotive=new createEditMotives.DialogFragment()
                        .onResultlistener(new createEditMotives.DialogFragment.onResult() {
                            @Override
                            public void onResult(List<motives> motivos,String nuevoTag) {
                                adapter.clear();
                                adapter.addAll(motivos);
                                adapter.notifyDataSetChanged();
                                lv.invalidateViews();
                                AnalyticsApplication.writeMovementsTracking(AnalyticsApplication.Write.Create, AnalyticsApplication.Action.Tag,nuevoTag);
                            }
                        });
                dialogMotive.show(getActivity().getSupportFragmentManager(),"dialog_nuevomotive");
            }
        });
        delete_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAdapter((Integer) delete_icon.getTag());
            }
        });
        editTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.mIsPremium){
                    final createEditMotives.DialogFragment dialogMotive=new createEditMotives.DialogFragment()
                            .onResultlistener(new createEditMotives.DialogFragment.onResult() {
                                @Override
                                public void onResult(List<motives> motivos,String nuevoTag) {
                                    if(motivos!=null){//si hubo edicion
                                        ContentValues cv=new ContentValues();
                                        cv.put("tag_",nuevoTag);
                                        String where="tag_='"+tagToEdit.getText()+"'";
                                        db.getDBInstance().update("tags",cv,where,null);
                                        adapter.clear();
                                        adapter.addAll(motivos);
                                        adapter.notifyDataSetChanged();
                                        tagToEdit.setText(nuevoTag);
                                        for (int i=0;i<motivos.size();i++) {
                                            if(motivos.get(i).get_motive().matches(nuevoTag)){
                                                editTag.setTag(i);
                                                break;
                                            }
                                        }
                                        lv.invalidateViews();
                                        AnalyticsApplication.writeMovementsTracking(AnalyticsApplication.Write.Update, AnalyticsApplication.Action.Tag,nuevoTag);
                                    }
                                }
                            });
                    dialogMotive.editMotive(tags.get((Integer) editTag.getTag()));
                    dialogMotive.show(getActivity().getSupportFragmentManager(),"dialog_nuevomotive");
                }else{
                    showOkDialog("Esta función solo esta disponible en versión Premium", "Premium", true, new Dialogs.DialogsInterface() {
                        @Override
                        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                            if(sucess){
                                ((MainActivity)getActivity()).askForPremium("ForEditTag");
                            }
                        }
                    });
                }
            }
        });
    }
    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;

    @Override
    public void onClick(View view) {
        if(view!=null && view instanceof TextView){
            tagToEdit.setText(tags.get((int) ((TextView)view).getTag()).get_motive());
            delete_icon.setTag((int) ((TextView)view).getTag());
            editTag.setTag((int) ((TextView)view).getTag());
            list_container.setVisibility(View.GONE);
            edit_container.setVisibility(View.VISIBLE);
            final SlidingRootNav rootNav=((MainActivity)getActivity()).getSlidingRootNav();
            rootNav.setMenuLocked(true);
            ((MainActivity)getActivity()).getToolBar().setTitle("Modificar Categoría");
            ((MainActivity)getActivity()).getToolBar().setNavigationIcon(R.drawable.baseline_arrow_back_white_36);
            ((MainActivity)getActivity()).getToolBar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            /*
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // back button pressed
                    onBackPressed();
                }
            });*/
        }
    }
    public boolean onBackPressed(){
        if(edit_container.getVisibility()==View.VISIBLE && list_container.getVisibility()==View.GONE){
            list_container.setVisibility(View.VISIBLE);
            edit_container.setVisibility(View.GONE);
            ((MainActivity)getActivity()).refreshToolbar(null);
            ((MainActivity)getActivity()).getToolBar().setTitle("Categorías");
            //((MainActivity)getActivity()).getToolBar().setNavigationIcon(R.drawable.);
            //((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            return true;//Consume el evento
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }


    enum Direction {LEFT, RIGHT;}
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO Auto-generated method stub
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                historicX = motionEvent.getX();
                historicY = motionEvent.getY();
                return false;

            case MotionEvent.ACTION_UP:
                if (lv.getChildAt(0) != null) {
                    int heightOfEachItem = lv.getChildAt(0).getHeight();
                    int heightOfFirstItem = -lv.getChildAt(0).getTop() + lv.getFirstVisiblePosition()*heightOfEachItem;
                    //IF YOU HAVE CHILDS IN LIST VIEW YOU START COUNTING
                    //listView.getChildAt(0).getTop() will see top of child showed in screen
                    //Dividing by height of view, you get how many views are not in the screen
                    //It needs to be Math.ceil in this case because it sometimes only shows part of last view
                    final int firstPosition = (int) Math.ceil(heightOfFirstItem / heightOfEachItem); // This is the same as child #0

                    //Here you get your List position, use historic Y to get where the user went first
                    final int wantedPosition = (int) Math.floor((historicY - lv.getChildAt(0).getTop()) / heightOfEachItem) + firstPosition;
                    //Here you get the actually position in the screen
                    final int wantedChild = wantedPosition - firstPosition;
                    //Depending on delta, go right or left
                    Animation.AnimationListener listener=new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            lv.getChildAt(wantedChild).setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    };
                    if (motionEvent.getX() - historicX < -DELTA) {
                        //If something went wrong, we stop it now
                        if (wantedChild < 0 || wantedChild >= tags.size()|| wantedChild >= lv.getChildCount()) {

                            return true;
                        }
                        //Start animation with 500 miliseconds of time
                        Animation animation=outToLeftAnimation(500);
                        animation.setAnimationListener(listener);
                        lv.getChildAt(wantedChild).startAnimation(animation);
                        //after 500 miliseconds remove from List the item and update the adapter.
                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        updateAdapter(wantedPosition);
                                    }
                                },
                                500
                        );
                        return true;

                    } else if (motionEvent.getX() - historicX > DELTA) {
                        //If something went wrong, we stop it now
                        if (wantedChild < 0 || wantedChild >= tags.size() || wantedChild >= lv.getChildCount()) {

                            return true;
                        }
                        //Start animation with 500 miliseconds of time
                        Animation animation=outToRightAnimation(500);
                        animation.setAnimationListener(listener);
                        lv.getChildAt(wantedChild).startAnimation(animation);
                        //after 500 miliseconds remove from List the item and update the adapter.
                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        updateAdapter(wantedPosition);
                                    }
                                },
                                500
                        );
                        return true;

                    }
                }
                return true;
            default:
                return false;
        }
    }

    private void updateAdapter(final int position) {
        if(!MainActivity.mIsPremium){
            showOkDialog("Esta función solo esta disponible en versión Premium", "Premium", true, new Dialogs.DialogsInterface() {
                @Override
                public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                    if(sucess){
                        ((MainActivity)getActivity()).askForPremium("ForDeleteTag");
                    }
                }
            });
            return;
        }
        if(adapter.getCount()==1){
            showOkDialog("Necesitas conservar al menos una categoría","Aceptar",true,null);
            lv.invalidateViews();
        }else{
            showOkCancelDialog("Deseas eliminar esta categoría?\nLos movimientos asociados no se borraran pero dejaran de pertencer a la categoria",
                    "Si", "No", true,
                    new Dialogs.DialogsInterface() {

                        @Override
                        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                            if(sucess){
                                long tagsNumber=db.GetLongColumn("SELECT COUNT(tag_) AS TagsNumber FROM tags WHERE tags.id_ IN (SELECT tags.id_ FROM tags INNER JOIN basics WHERE basics.id_=tags.id_ AND tags.tag_=?)",new String[]{adapter.getItem(position).get_motive()},"TagsNumber")[0];
                                if(tagsNumber>0){//Hay movimientos que solo estan asociados a este unico tag, entonces le pregunta al usuario con que tag lo desea reemplazar
                                    String[] result=db.GetStringColumn("SELECT * FROM motives WHERE enabled_=1 AND motive_<>?",new String[]{adapter.getItem(position).get_motive()},"motive_");

                                    final SelectorCheckbox selector=new SelectorCheckbox(getActivity());
                                    selector.setHint("Si borras esta categoría habrá " + String.valueOf(tagsNumber) + " movimientos que ya no tendran ninguna categoría asociada. Selecciona a que categoría perteneceran");
                                    selector.setOptions(Arrays.asList(result),false,-1);
                                    selector.setOnselected(new SelectorCheckbox.onSelected() {
                                        @Override
                                        public void onSelected(List selected) {
                                            if(selected!=null && selected.size()>0){
                                                deleteTag(position);
                                                String values="";
                                                for (Object tag:selected) {
                                                    if(values!="")values=values+" UNION ALL ";
                                                    values=values+
                                                            "SELECT ('"+tag+"'),id_,'"+System.currentTimeMillis()+"','"+System.currentTimeMillis()+"' FROM basics " +
                                                            "WHERE NOT EXISTS (SELECT tags.id_ FROM tags WHERE basics.id_=tags.id_)";
                                                }
                                               values="INSERT INTO tags (tag_,id_,updated,created) "+values;
                                                db.getDBInstance().execSQL(values);

                                                adapter.remove(adapter.getItem(position));
                                                onBackPressed();
                                            }

                                        }
                                    });
                                    showOkDialogFromView(selector, "Seleccionar", true, new Dialogs.DialogsInterface() {
                                        @Override
                                        public void DialogFinish(String Tag, int Dialogkind, boolean sucess, OrderMap MapTag) {
                                           if(sucess){
                                               selector.showDialog();
                                           }
                                        }
                                    });
                                    return;
                                }
                                deleteTag(position);
                                /*
                                db.getDBInstance().execSQL("INSERT INTO tags (tag_,id_,updated,created) " +
                                        "VALUES ('Gasto Corriente'," +
                                        "(SELECT id_ FROM basics WHERE NOT EXISTS (SELECT tags.id_ FROM tags WHERE basics.id_=tags.id_))," +
                                        "'"+System.currentTimeMillis()+"','"+System.currentTimeMillis()+"')");*/
                                adapter.remove(adapter.getItem(position));
                                onBackPressed();
                            }else{
                                lv.invalidateViews();
                            }
                        }
                    });

        }

    }
    private void deleteTag(int position){
        AnalyticsApplication.writeMovementsTracking(AnalyticsApplication.Write.Delete, AnalyticsApplication.Action.Tag,adapter.getItem(position).get_motive());
        db.getDBInstance().delete("tags","tag_='"+adapter.getItem(position).get_motive()+"'",null);
        db.getDBInstance().delete("motives","motive_='"+adapter.getItem(position).get_motive()+"'",null);
    }
    private Animation outToLeftAnimation(int duration) {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(duration);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    private Animation outToRightAnimation(int duration) {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(duration);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }

}
