/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbook.adapter.CustomRecyclerViewAdapter
import com.example.accountbook.common.CommonProc
import com.example.accountbook.realmdata.AccountRegist
import com.example.accountbook.realmdata.CategoryRegist
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.content_list.*


class ListActivity : AppCompatActivity() {

    private val tagDebug = "<<<<<- DEBUG ->>>>>"
    private var selectCategoryId: Long = 0L
    private lateinit var realm: Realm
    private lateinit var adapter: CustomRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        realm = Realm.getDefaultInstance()

        fab.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }

        val categoryRegist: RealmResults<CategoryRegist>? =
            realm.where<CategoryRegist>()
                .distinct("itemName").findAll()
        val results = RealmList<CategoryRegist>()
        results.addAll(categoryRegist!!)

        val spinnerItems: MutableList<String>? = mutableListOf()

        for ((count, resultCategorys) in results.withIndex()) {
            when (count) {
                0 -> {
                    spinnerItems?.add("カテゴリー選択")
                    spinnerItems?.add("未設定")
                    spinnerItems?.add(resultCategorys.itemName)
                }
                else -> spinnerItems?.add(resultCategorys.itemName)
            }
        }
        val adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item, spinnerItems!!
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        // リスナー登録
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテム選択
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {
                val spinnerPosition: Int = position
                Log.d(tagDebug, "spinnerPosition:${spinnerPosition}")
                // default/未設定/Category
                selectCategoryId = when (spinnerPosition) {
                    0 -> -1L
                    1 -> 0L
                    else -> realm.where<CategoryRegist>()
                        .findAll()[spinnerPosition - 2]!!.itemId
                }
                Log.d(tagDebug, "selectCategoryId:${selectCategoryId}")
            }

            //　アイテム未選択
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectCategoryId = -1L
            }
        }
        btnSearch.setOnClickListener {
            dataSearch(realm, selectCategoryId)
        }
    }

    override fun onStart() {
        super.onStart()
        val realmResults =
            realm.where<AccountRegist>()
                .findAll().sort("id", Sort.DESCENDING)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = CustomRecyclerViewAdapter(
            realmResults
        )
        recyclerView.adapter = this.adapter
    }

    // リスト検索処理
    private fun dataSearch(realm: Realm, selectCategoryId: Long?) {

        val year = inputYear.text.toString()
        val month =
            (if (inputMonth.text.length == 1) {
                "0" + inputMonth.text
            } else {
                inputMonth.text
            }).toString()
        val day =
            (if (inputDay.text.length == 1) {
                "0" + inputDay.text
            } else {
                inputDay.text
            }).toString()

        val listSearchCondition: MutableList<String>? = mutableListOf()
        listSearchCondition?.add(year)
        listSearchCondition?.add(month)
        listSearchCondition?.add(day)
        listSearchCondition?.add(selectCategoryId.toString())

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = CustomRecyclerViewAdapter(
            CommonProc().searchRecode(realm, listSearchCondition)
        )
        recyclerView.adapter = this.adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
