package com.gigigo.examen.customviews

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.gigigo.examen.models.Places
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.b_place_list_item.view.*
import java.util.*

class ListViewPlaces : RecyclerView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       
        private val mRandom = Random()
        fun bindView(place: Places.Place) {
            itemView.b_name.text = place.name
            itemView.b_vicinity.text = place.vicinity
            itemView.b_icon.layoutParams.height = getRandomIntInRange(250, 150)
            ImageLoader.getInstance().displayImage(place.icon,itemView.b_icon)
            ImageLoader.getInstance().displayImage(place.photo,itemView.b_image)
        }

        private fun getRandomIntInRange(max: Int, min: Int): Int {
            return mRandom.nextInt(max - min + min) + min
        }
    }
}