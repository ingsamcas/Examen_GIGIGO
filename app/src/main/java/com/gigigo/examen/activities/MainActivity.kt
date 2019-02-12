package com.gigigo.examen.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import com.gigigo.examen.R
import com.gigigo.examen.R.id.main_recyclerView
import com.gigigo.examen.adapters.ListViewPlacesAdapter
import com.gigigo.examen.models.Places
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myPlaces=Places(this)
        myPlaces.getPlaces{loadView(it)}
        setTitle("Examen")
    }
    fun loadView(places: (ArrayList<Places.Place>)){
        main_recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        main_recyclerView.adapter = ListViewPlacesAdapter(places)
    }
}
