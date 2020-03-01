/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.adapter

import android.content.Intent
import android.graphics.Color
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbook.EditActivity
import com.example.accountbook.R
import com.example.accountbook.common.ViewHolder
import com.example.accountbook.realmdata.AccountRegist
import com.example.accountbook.realmdata.CategoryRegist
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where

class CustomRecyclerViewAdapter(realmResults: RealmResults<AccountRegist>) :
    RecyclerView.Adapter<ViewHolder>() {

    private val tagDebug = "<<<<<- DEBUG ->>>>>"
    private val rResults: RealmResults<AccountRegist> = realmResults
    private val realm = Realm.getDefaultInstance()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.one_result, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val accountRegist: AccountRegist? = rResults[position]
        val categoryRegist: CategoryRegist? = realm.where<CategoryRegist>()
            .equalTo("itemId", accountRegist?.categoryId)
            .findFirst()

        Log.d(tagDebug, "CategoryResult:${categoryRegist}")

        holder.dateText?.text = DateFormat.format("yyyy/MM/dd kk:mm", accountRegist?.dateTime)
        holder.numberText?.text = accountRegist?.registNum.toString()
        holder.categoryName?.text = when (categoryRegist) {
            null -> "未設定"
            else -> categoryRegist.itemName
        }
        holder.itemView.setBackgroundColor(if (position % 2 == 0) Color.LTGRAY else Color.WHITE)
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, EditActivity::class.java)
            intent.putExtra("id", accountRegist?.id)
            intent.putExtra("categoryId", accountRegist?.categoryId)
            intent.putExtra("registType", accountRegist?.registType)
            it.context.startActivity(intent)
        }
    }
}