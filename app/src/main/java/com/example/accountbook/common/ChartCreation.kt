/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.common

import android.graphics.Color
import com.example.accountbook.realmdata.AccountRegist
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.util.*


class ChartCreation(realm: Realm) {

    // 会計登録情報全取得
    private val accountItems: RealmResults<AccountRegist> =
        realm.where<AccountRegist>().findAll()
            .sort("id", Sort.ASCENDING)

    // 当月収入集計
    private val thisMonthIncomeREC = mutableListOf<Entry>()
    // 当月出費集計
    private val thisMonthSpendREC = mutableListOf<Entry>()
    // 当月収支合計
    private val thisMonthTotalBalance = mutableListOf<Entry>()
    // 先月集計
    private val lastMonthREC = mutableListOf<Entry>()
    // 平均集計
    private val avgResultREC = mutableListOf<Entry>()
    // BarChart用当月収支
    private val income: ArrayList<BarEntry> = ArrayList()
    private val spend: ArrayList<BarEntry> = ArrayList()

    private fun setChartValues() {
        // 日付フォーマット最適化
        val sdfM: Long = SimpleDateFormat("yyyyMM00")
            .format(Calendar.getInstance().time).toLong()
        val sdfD: Long = SimpleDateFormat("yyyyMMdd")
            .format(Calendar.getInstance().time).toLong()

        var dayCount = 0

        (sdfM..sdfD).forEach { _: Long ->

            val totalBalance = CommonProc().totalBalance(accountItems, dayCount, sdfM)
            val thisMonthDailyIncome = CommonProc().thisMonthIncomeREC(accountItems, dayCount, sdfM)
            val thisMonthDailySpend = CommonProc().thisMonthSpendREC(accountItems, dayCount, sdfM)
            val lastMonthDaily = CommonProc().resultLastMonthREC(accountItems, dayCount, sdfM)
            val averageRec = CommonProc().resultAvgResultREC(accountItems, sdfD)

            thisMonthTotalBalance.add(Entry(dayCount.toFloat(), totalBalance.toFloat()))
            thisMonthIncomeREC.add(Entry(dayCount.toFloat(), thisMonthDailyIncome.toFloat()))
            thisMonthSpendREC.add(Entry(dayCount.toFloat(), thisMonthDailySpend.toFloat()))
            lastMonthREC.add(Entry(dayCount.toFloat(), lastMonthDaily.toFloat()))
            avgResultREC.add(Entry(dayCount.toFloat(), averageRec().toFloat()))
            income.add(BarEntry(dayCount.toFloat(), thisMonthDailyIncome.toFloat()))
            spend.add(BarEntry(dayCount.toFloat(), thisMonthDailySpend.toFloat()))

            dayCount++
        }
    }

    fun configLineChart(): LineData {
        // 登録情報取得
        setChartValues()
        // 各ライン設定
        val totalBalanceLine = LineDataSet(thisMonthTotalBalance, "Total").apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = Color.parseColor("#FFB300")
            highLightColor = Color.BLACK
            setDrawCircles(true)
            setDrawCircleHole(false)
            setDrawValues(false)
            lineWidth = 3f
        }
        val lastMonthTotalSpendLine = LineDataSet(lastMonthREC, "LastMonth").apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = Color.CYAN
            highLightColor = Color.TRANSPARENT
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawValues(false)
            lineWidth = 1f
        }
        val averageRecordsLine = LineDataSet(avgResultREC, "AnnualAvg").apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = Color.GREEN
            highLightColor = Color.BLACK
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawValues(false)
            lineWidth = 1f
        }
        val data = LineData(
            totalBalanceLine,
            lastMonthTotalSpendLine,
            averageRecordsLine
        )
        data.setValueTextColor(Color.BLACK)
        data.setValueTextSize(10f)
        return data
    }

    fun configBarChart(): BarData {
        // 登録情報取得
        setChartValues()
        // 日計収入
        val dailyIncome: BarDataSet = BarDataSet(income, "IN").apply {
            //整数で表示
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toInt() }
            //ハイライトさせない
            isHighlightEnabled = false
            //Barの色をセット
            color = Color.RED
        }
        val dailySpend: BarDataSet = BarDataSet(spend, "OUT").apply {
            //整数で表示
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toInt() }
            //ハイライトさせない
            isHighlightEnabled = false
            //Barの色をセット
            color = Color.BLUE
        }
        val data = BarData(
            dailyIncome,
            dailySpend
        )
        // 数値表示設定
        data.setDrawValues(false)
        return data
    }
}


