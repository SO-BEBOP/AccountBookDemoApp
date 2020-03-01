/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.accountbook.R.layout.activity_edit
import com.example.accountbook.adapter.CustomListViewAdapter
import com.example.accountbook.common.ChartCreation
import com.example.accountbook.common.CommonProc
import com.example.accountbook.realmdata.AccountRegist
import com.example.accountbook.realmdata.CategoryRegist
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.CombinedData
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_edit.*
import java.text.SimpleDateFormat
import java.util.*


class EditActivity : AppCompatActivity() {

    private val tagDebug = "<<<<<- DEBUG ->>>>>"
    private val realm = Realm.getDefaultInstance()
    // 登録タイプ初期化(初期値 = 出金)
    private var registType = 1L

    // 登録情報取得
    private val records: RealmResults<AccountRegist> =
        realm.where<AccountRegist>()
            .findAll()
            .sort("id", Sort.ASCENDING)

    // カテゴリー情報全量取得
    private val categoryItems: RealmResults<CategoryRegist> =
        realm.where<CategoryRegist>()
            .findAll().sort("itemId", Sort.ASCENDING)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_edit)

        // LogCheck
        val resultLog = realm.where<AccountRegist>().findAll()
        Log.d(tagDebug, "会計情報全量:${resultLog}")
        val categoryLog = realm.where<CategoryRegist>().findAll()
        Log.d(tagDebug, "カテゴリ全量:${categoryLog}")

        // 既存登録タイプ情報取得
        registType = intent.getLongExtra("registType", 1L)
        //ListView設定処理
        val listView = findViewById<ListView>(R.id.categoryList)
        listView.adapter = CustomListViewAdapter(
            categoryItems.where().equalTo(
                "categoryType", registType
            ).findAll().sort("itemId")
        )
        // カテゴリーリスト登録タイプ設定
        changeCategoryList()
        addWithdrawalBtn.setOnClickListener {
            registType = 1L
            changeCategoryList()
        }
        addDepositBtn.setOnClickListener {
            registType = 2L
            changeCategoryList()
        }
        addFixedCostBtn.setOnClickListener {
            registType = 3L
            changeCategoryList()
        }
        // 既存会計登録情報取得
        val bpId = intent.getLongExtra("id", 0L)
        if (bpId > 0L) {
            val accountRegist = realm.where<AccountRegist>()
                .equalTo("id", bpId).findFirst()
            registNumber.setText(accountRegist?.registNum.toString())
            deleteBtn.isEnabled = true
        } else {
            deleteBtn.isEnabled = false
        }
        // チャート設定
        if (records.count() != 0) {
            setCombineChart()
        }
        // チャートの値設定
        setRecodeValues()

        // ボタン押下処理(登録/削除/履歴)
        saveBtn.setOnClickListener {
            registButtonAction(bpId)
            // 画面更新
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
        deleteBtn.setOnClickListener {
            deleteButtonAction(bpId)
        }
        listBtn.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        // カテゴリー新規追加処理
        addCategoryBtn.setOnClickListener {
            val position: Int? = null
            addCategoryDialog(position)
        }
        categoryList.setOnItemLongClickListener { _, _, position, _ ->
            Log.d(tagDebug, "リスト位置:${position}")
            addCategoryDialog(position)
        }
    }

    // 登録ボタン処理
    private fun registButtonAction(bpId: Long) {
        var number = 0L

        // リストポジションからカテゴリーId取得
        val listPosition: Int = categoryList?.checkedItemPosition ?: -1
        Log.d(tagDebug, "リスト位置:${listPosition}")
        val selectItemId = {
            when (listPosition) {
                -1 -> 0L
                else ->
                    categoryItems
                        .where().equalTo(
                            "categoryType", registType
                        ).findAll()[listPosition]?.itemId
            }
        }
        // 金額入力値設定
        if (!registNumber.text.isNullOrEmpty()) {
            number = registNumber.text.toString().toLong()
        }
        // 会計登録処理
        when (bpId) {
            0L -> {
                realm.executeTransaction {
                    // 登録情報id連番登録処理
                    val maxId = realm.where<AccountRegist>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    val accountRegist = realm.createObject<AccountRegist>(nextId)
                    // 登録年月日
                    accountRegist.dateTime = Date()
                    accountRegist.numDateTime = SimpleDateFormat("yyyyMMdd")
                        .format(Calendar.getInstance().time).toLong()
                    accountRegist.numYear = SimpleDateFormat("yyyy")
                        .format(Calendar.getInstance().time).toString()
                    accountRegist.numMonth = SimpleDateFormat("MM")
                        .format(Calendar.getInstance().time).toString()
                    accountRegist.numDay = SimpleDateFormat("dd")
                        .format(Calendar.getInstance().time).toString()
                    // 金額登録
                    accountRegist.registNum = number
                    // カテゴリーid登録
                    accountRegist.categoryId = selectItemId()!!
                    // 登録タイプ
                    accountRegist.registType = run {
                        when (listPosition) {
                            -1 -> 0L
                            else -> registType
                        }
                    }
                }
            }
            // 修正処理
            else -> {
                realm.executeTransaction {
                    val accountRegist = realm.where<AccountRegist>()
                        .equalTo("id", bpId).findFirst()
                    when (listPosition) {
                        -1 -> {
                            accountRegist?.categoryId = intent.getLongExtra("categoryId", 0L)
                            accountRegist?.registType = intent.getLongExtra("registType", 0L)
                        }
                        else -> {
                            accountRegist?.categoryId = selectItemId()!!
                            accountRegist?.registType = registType
                        }
                    }
                    accountRegist?.registNum = number
                }
            }
        }
        Toast.makeText(applicationContext, "登録しました", Toast.LENGTH_SHORT).show()
        finish()
    }

    // 削除ボタン処理
    private fun deleteButtonAction(bpId: Long) {
        realm.executeTransaction {
            realm.where<AccountRegist>().equalTo("id", bpId)?.findFirst()?.deleteFromRealm()
            val resultLog = realm.where<AccountRegist>().findAll()
            Log.d(tagDebug, "削除:${resultLog}")
        }
        Toast.makeText(applicationContext, "削除しました", Toast.LENGTH_SHORT).show()
        finish()

        val intent = Intent(this, ListActivity::class.java)
        startActivity(intent)
    }

    // カテゴリー更新ダイアログ処理
    private fun addCategoryDialog(position: Int?): Boolean {
        val myEdit = EditText(this)
        val dialog = AlertDialog.Builder(this)
        dialog.setView(myEdit)

        Log.d(tagDebug, "position確認：${position}")
        when (position) {
            null -> {  // 新規登録処理
                dialog.setTitle("登録名を入力して下さい。")
                dialog.setPositiveButton("登録") { _, _ ->
                    realm.executeTransaction {

                        // カテゴリー情報id連番登録処理
                        val maxId = realm.where<CategoryRegist>().max("itemId")
                        val nextId = (maxId?.toLong() ?: 0L) + 1L
                        val categoryRegist = realm.createObject<CategoryRegist>(nextId)
                        // 登録タイプ設定
                        categoryRegist.categoryType = registType
                        // 入力文字列設定
                        categoryRegist.itemName = myEdit.text.toString()

                        Log.d(tagDebug, "登録文字列：${categoryRegist.itemName}")
                        Log.d(tagDebug, "登録タイプ：${categoryRegist.categoryType}")

                        Toast.makeText(
                            this, "「${categoryRegist.itemName}」に登録しました",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> { // 更新処理
                dialog.setTitle("変更名を入力して下さい。")
                dialog.setPositiveButton("更新") { _, _ ->
                    realm.executeTransaction {

                        val userText = myEdit.text.toString()
                        val selectItemId = categoryItems
                            .where().equalTo(
                                "categoryType", registType
                            ).findAll()[position]?.itemId
                        val listUpdate = realm.where<CategoryRegist>()
                            .equalTo("itemId", selectItemId)?.findFirst()
                        listUpdate?.itemName = userText

                        Log.d(tagDebug, "登録文字列：${userText}")
                        Toast.makeText(this, "「${userText}」に更新しました", Toast.LENGTH_SHORT).show()
                    }
                }
                // 削除処理
                dialog.setNegativeButton("削除") { _, _ ->
                    val item = categoryItems
                        .where().equalTo(
                            "categoryType", registType
                        ).findAll()[position]?.itemId

                    // ListView設定処理
                    val listView = findViewById<ListView>(R.id.categoryList)
                    listView.adapter =
                        CustomListViewAdapter(
                            categoryItems.where().equalTo(
                                "categoryType", registType
                            ).findAll().sort("itemId")
                        )
                    realm.executeTransaction {
                        realm.where<CategoryRegist>().equalTo("itemId", item)
                            ?.findFirst()?.deleteFromRealm()

                        Log.d(tagDebug, "登録情報_削除：${categoryItems}")

                        Toast.makeText(applicationContext, "削除しました", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // 処理キャンセル
        dialog.setNeutralButton("キャンセル", null)
        dialog.show()
        return true
    }

    // ListViewのTab表示切替
    private fun changeCategoryList() {
        addWithdrawalBtn.isEnabled = true
        addDepositBtn.isEnabled = true
        addFixedCostBtn.isEnabled = true

        when (this.registType) {
            1L -> addWithdrawalBtn.isEnabled = false
            2L -> addDepositBtn.isEnabled = false
            3L -> addFixedCostBtn.isEnabled = false
        }
        val reload: RealmResults<CategoryRegist> =
            realm.where<CategoryRegist>()
                .equalTo("categoryType", registType)
                .findAll().sort("itemId", Sort.ASCENDING)

        val listView = findViewById<ListView>(R.id.categoryList)
        listView.adapter =
            CustomListViewAdapter(reload)

        val adapter =
            CustomListViewAdapter(reload)
        adapter.notifyDataSetChanged()
    }

    // チャート項目値設定
    private fun setRecodeValues() {
        // 集計処理情報設定_集計単位：年/月/日(plus/minus)
        totalYear.text = CommonProc().sumYear(realm, "total").toString()
        plusYear.text = CommonProc().sumYear(realm, "plus").toString()
        minusYear.text = CommonProc().sumYear(realm, "minus").toString()

        totalMonth.text = CommonProc().sumMonth(realm, "total").toString()
        plusMonth.text = CommonProc().sumMonth(realm, "plus").toString()
        minusMonth.text = CommonProc().sumMonth(realm, "minus").toString()

        totalDay.text = CommonProc().sumDays(realm, "total").toString()
        plusDay.text = CommonProc().sumDays(realm, "plus").toString()
        minusDay.text = CommonProc().sumDays(realm, "minus").toString()
        totalFC.text = CommonProc().sumFixedCost(realm).toString()
    }

    private fun setCombineChart() {
        // ChartTypeCombine
        val data = CombinedData()
        data.setData(ChartCreation(realm).configBarChart())
        data.setData(ChartCreation(realm).configLineChart())

        lineChart.data = data
        // スタイルとフォントファミリーの設定
        val mTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)

            lineChart.axisLeft.apply {
                setDrawTopYLabelEntry(false)
                setValueFormatter { value, _ -> "" + value.toInt() }
            }
            //データラベルの表示
            legend.apply {
                form = Legend.LegendForm.LINE
                typeface = mTypeface
                textSize = 12f
                textColor = Color.BLACK
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
            //y軸右側の表示
            axisRight.isEnabled = false

            //X軸表示
            xAxis.apply {
                typeface = mTypeface
                setDrawLabels(true)
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = CommonProc().getMaxDay()
//                setGranularity()
            }
            //y軸左側の表示
            axisLeft.apply {
                typeface = mTypeface
                textColor = Color.BLACK
                setDrawGridLines(true)
                setDrawZeroLine(false)
                setDrawLabels(true)
                setDrawTopYLabelEntry(true)
                granularity = 5000f

                setValueFormatter { value, _ -> "" + value.toInt() }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}