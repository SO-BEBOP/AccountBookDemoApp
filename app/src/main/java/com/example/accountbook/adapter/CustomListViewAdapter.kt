/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.accountbook.realmdata.CategoryRegist
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class CustomListViewAdapter(data: OrderedRealmCollection<CategoryRegist>?) :
    RealmBaseAdapter<CategoryRegist>(data) {
    inner class ViewHolder(cell: View) {
        var itemName: TextView = cell.findViewById(android.R.id.text1)
        var itemId = cell.id.toLong()
    }

    private val tagDebug = "<<<<<- DEBUG ->>>>>"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        when (convertView) {
            null -> {
                val inflater = LayoutInflater.from(parent?.context)
                view = inflater.inflate(android.R.layout.simple_list_item_checked, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            }
            else -> {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
        }
        adapterData?.run {
            val realmData = get(position)
            viewHolder.itemName.text = realmData.itemName
            viewHolder.itemId = realmData.itemId

            Log.d(
                tagDebug,
                "ListViewAdapterData(Name/itemId):${viewHolder.itemName.text}/${viewHolder.itemId}"
            )
        }
        return view
    }
}