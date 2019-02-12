package com.gigigo.examen.adapters

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigigo.examen.R
import com.gigigo.examen.activities.MainActivity
import com.gigigo.examen.activities.MapActivity
import com.gigigo.examen.customviews.ListViewPlaces
import com.gigigo.examen.models.Places
import android.util.Pair as UtilPair

class ListViewPlacesAdapter(var listOfPlaces: ArrayList<Places.Place>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view =  LayoutInflater.from(parent.context).inflate(R.layout.b_place_list_item, parent, false)
        return ListViewPlaces.PlacesViewHolder(view);
    }

    override fun getItemCount(): Int = listOfPlaces.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val placeViewHolder = viewHolder as ListViewPlaces.PlacesViewHolder
        placeViewHolder.bindView(listOfPlaces[position])
        viewHolder.itemView.setOnClickListener(View.OnClickListener {


            var mainActivity=(viewHolder.itemView.parent as ViewGroup).context as MainActivity;
            var intent=Intent(mainActivity,MapActivity::class.java)
            intent.putExtra("Data",listOfPlaces.get(position))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(mainActivity,
                        UtilPair.create(it.findViewById(R.id.b_image), "shared_image"),
                        UtilPair.create(it.findViewById(R.id.b_name), "shared_name"),
                        UtilPair.create(it.findViewById(R.id.b_vicinity), "shared_adress"))
                mainActivity.startActivity(intent,options.toBundle())
            } else {

                mainActivity.startActivity(intent)
            }



        })
    }

}