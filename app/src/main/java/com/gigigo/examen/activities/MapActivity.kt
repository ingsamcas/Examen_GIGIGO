package com.gigigo.examen.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.gigigo.examen.R
import com.gigigo.examen.fragments.MapFrag
import com.gigigo.examen.models.Places
import com.google.android.gms.maps.MapFragment

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        var data: Places.Place= intent.getSerializableExtra("Data") as Places.Place;
        setTitle(data.name)
        var FT=supportFragmentManager.beginTransaction();
        FT.replace(R.id.map_container, MapFrag(data), MapFrag.name)
        FT.commit()

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}