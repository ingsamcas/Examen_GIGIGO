package com.gigigo.examen.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gigigo.examen.R
import android.widget.Toast
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import com.gigigo.examen.adapters.ImageAdapter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.e_gallery.*


class GalleryActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.e_gallery)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        val images=intent.getStringArrayListExtra("Images")
        val title=intent.getStringExtra("Title")
        if(images!=null&&title!=null){
            e_gallery.adapter= ImageAdapter(this,images)
            e_gallery.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>, v: View,
                                         position: Int, id: Long) {
                    //---display the images selected---
                    val imageView = findViewById(R.id.e_image) as ImageView
                    ImageLoader.getInstance().displayImage(images[position],imageView)
                }
            })
            ImageLoader.getInstance().displayImage(images[0],e_image)
            setTitle(title)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}