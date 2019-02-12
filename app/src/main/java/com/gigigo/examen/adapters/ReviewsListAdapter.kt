package com.gigigo.examen.adapters

import android.content.Context
import android.widget.TextView
import android.icu.util.ULocale.getVariant
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter
import android.widget.RatingBar
import com.gigigo.examen.R
import com.gigigo.examen.models.Places


class ReviewsListAdapter(context: Context, private val listData: ArrayList<Places.Review>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater

    init {
        layoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return listData.size
    }

    override fun getItem(position: Int): Any {
        return listData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.d_review_list_item, null)
            holder = ViewHolder()
            holder.comment = convertView!!.findViewById(R.id.d_comment)
            holder.influencer = convertView!!.findViewById(R.id.d_influencer)
            holder.rate = convertView!!.findViewById(R.id.d_rate)
            convertView!!.setTag(holder)
        } else {
            holder = convertView!!.getTag() as ViewHolder
        }

        holder.comment!!.setText(listData.get(position).comment)
        holder.influencer!!.setText(listData.get(position).influencer)
        holder.rate!!.rating=listData.get(position).rate

        return convertView
    }

    internal class ViewHolder {
        var comment: TextView? = null
        var influencer: TextView? = null
        var rate:RatingBar?=null
    }

}