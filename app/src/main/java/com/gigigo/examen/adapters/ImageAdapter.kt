package com.gigigo.examen.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Gallery
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.gigigo.examen.R
import com.nostra13.universalimageloader.core.ImageLoader


class ImageAdapter(val context: Context,val images:ArrayList<String>):BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView
        if (convertView == null) {
            imageView = ImageView(context)
            imageView.setLayoutParams(Gallery.LayoutParams(300, 280))
            imageView.setPadding(20,20,20,20)
            imageView.setBackgroundResource(android.R.color.white);
        } else {
            imageView = convertView as ImageView
        }
        ImageLoader.getInstance().displayImage(images[position],imageView)

        return imageView
    }

    override fun getItem(position: Int): Any {
        return images.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return images.size
    }
}