/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.common

import android.util.Log
import com.example.accountbook.realmdata.AccountRegist
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.util.*

class CommonProc {

    private val tagDebug = "<<<<<- DEBUG ->>>>>"

    // 現在月の日数取得
    fun getMaxDay(): Float {
        val calendar = Calendar.getInstance()
        val year = SimpleDateFormat("yyyy")
            .format(Calendar.getInstance().time).toInt()
        val month = SimpleDateFormat("M")
            .format(Calendar.getInstance().time).toInt()
        calendar.set(year, month - 1, 1) // 月の指定は0始まりなので注意
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toFloat()
    }

    // 年集計
    fun sumYear(realm: Realm, resultType: String): Number {
        val spend = realm
            .where<AccountRegist>()
            .equalTo("registType", 1L)
            .greaterThan(
                "numDateTime", SimpleDateFormat("yyyy0000")
                    .format(Calendar.getInstance().time).toLong()
            ).sum("registNum")
        val income = realm
            .where<AccountRegist>()
            .equalTo("registType", 2L)
            .greaterThan(
                "numDateTime", SimpleDateFormat("yyyy0000")
                    .format(Calendar.getInstance().time).toLong()
            ).sum("registNum")

        when (resultType) {
            "total" -> {
                return income.toLong() - spend.toLong()
            }
            "plus" -> {
                return income
            }
            "minus" -> {
                return spend
            }
        }
        return 0L
    }

    // 月集計
    fun sumMonth(realm: Realm, resultType: String): Number {
        val spend = realm
            .where<AccountRegist>()
            .equalTo("registType", 1L)
            .greaterThan(
                "numDateTime", SimpleDateFormat("yyyyMM00")
                    .format(Calendar.getInstance().time).toLong()
            ).sum("registNum")
        val income = realm
            .where<AccountRegist>()
            .equalTo("registType", 2L)
            .greaterThan(
                "numDateTime", SimpleDateFormat("yyyyMM00")
                    .format(Calendar.getInstance().time).toLong()
            ).sum("registNum")

        when (resultType) {
            "total" -> {
                return income.toLong() - spend.toLong()
            }
            "plus" -> {
                return income
            }
            "minus" -> {
                return spend
            }
        }
        return 0L
    }

    // 日集計
    fun sumDays(realm: Realm, resultType: String): Number {
        val spend = realm
            .where<AccountRegist>()
            .equalTo("registType", 1L)
            .equalTo(
                "numDateTime", SimpleDateFormat("yyyyMMdd")
                    .format(Calendar.getInstance().time).toLong()
            ).sum("registNum")
        val income = realm
            .where<AccountRegist>()
            .equalTo("registType", 2L)
            .equalTo(
                "numDateTime", SimpleDateFormat("yyyyMMdd")
                    .format(Calendar.getInstance().time).toLong()
            ).sum("registNum")

        when (resultType) {
            "total" -> {
                return income.toLong() - spend.toLong()
            }
            "plus" -> {
                return income
            }
            "minus" -> {
                return spend
            }
        }
        return 0L
    }

    // 固定費集計
    fun sumFixedCost(realm: Realm): Number {
        return realm
            .where<AccountRegist>()
            .equalTo("registType", 3L)
            .sum("registNum")
    }

    fun totalBalance(
        accountItems: RealmResults<AccountRegist>,
        dayCount: Int,
        sdfM: Long
    ): Long {

        val spend = accountItems
            .where().greaterThan("numDateTime", sdfM)
            .equalTo("registType", 1L)
            .distinct("registNum")
            .sort("dateTime", Sort.ASCENDING)
            .equalTo("numDateTime", sdfM + dayCount)
            .findAll().sum("registNum")

        val income = accountItems
            .where().greaterThan("numDateTime", sdfM)
            .equalTo("registType", 2L)
            .distinct("registNum")
            .sort("dateTime", Sort.ASCENDING)
            .equalTo("numDateTime", sdfM + dayCount)
            .findAll().sum("registNum")

        return income.toLong() - spend.toLong()
    }

    //当月出費集計
    fun thisMonthSpendREC(
        accountItems: RealmResults<AccountRegist>,
        dayCount: Int,
        sdfM: Long
    ): Number {
        val spend = accountItems
            .where().greaterThan("numDateTime", sdfM)
            .equalTo("registType", 1L)
            .distinct("registNum")
            .sort("dateTime", Sort.ASCENDING)
            .equalTo("numDateTime", sdfM + dayCount)
            .findAll().sum("registNum")
        return 0L - spend.toLong()
    }

    //当月収入集計
    fun thisMonthIncomeREC(
        accountItems: RealmResults<AccountRegist>,
        dayCount: Int,
        sdfM: Long
    ): Number {
        return accountItems
            .where().greaterThan("numDateTime", sdfM)
            .equalTo("registType", 2L)
            .distinct("registNum")
            .sort("dateTime", Sort.ASCENDING)
            .equalTo("numDateTime", sdfM + dayCount)
            .findAll().sum("registNum")
    }

    // 先月集計
    fun resultLastMonthREC(
        accountItems: RealmResults<AccountRegist>,
        dayCount: Int,
        sdfM: Long
    ): Number {
        val sdfLastM = SimpleDateFormat("yyyyMM00").format(
            (Calendar.getInstance().run {
                add(Calendar.MONTH, -1)
                time
            })
        ).toLong()
        Log.d(tagDebug, "lastMonthSDF:${sdfLastM}")

        val income = accountItems
            .where().greaterThan("numDateTime", sdfM)
            .equalTo("registType", 1L)
            .distinct("registNum")
            .sort("dateTime", Sort.ASCENDING)
            .equalTo("numDateTime", sdfLastM)
            .equalTo("numDateTime", sdfLastM + dayCount)
            .findAll().sum("registNum")

        val spend = accountItems
            .where().greaterThan("numDateTime", sdfM)
            .equalTo("registType", 2L)
            .distinct("registNum")
            .sort("dateTime", Sort.ASCENDING)
            .equalTo("numDateTime", sdfLastM)
            .equalTo("numDateTime", sdfLastM + dayCount)
            .findAll().sum("registNum")

        return income.toLong() - spend.toLong()
    }

    // 月平均算出
    fun resultAvgResultREC(
        accountItems: RealmResults<AccountRegist>,
        sdfD: Long
    ): () -> Int {
        // 平均値の算出
        val sdfAvgDay = SimpleDateFormat("yyyyMMdd").format(
            (Calendar.getInstance().run {
                add(Calendar.MONTH, -12)
                time
            })
        ).toLong()
        Log.d(tagDebug, "sdfAvgDay:${sdfAvgDay}")

        val rangeTotalIncome = accountItems
            .where().between("numDateTime", sdfAvgDay, sdfD)
            .equalTo("registType", 2L)
            .beginGroup()
            .notEqualTo("registNum", 0L)
            .sort("dateTime", Sort.ASCENDING)
            .endGroup()
            .findAll()
            .sum("registNum").toInt()

        val rangeTotalSpend = accountItems
            .where().between("numDateTime", sdfAvgDay, sdfD)
            .equalTo("registType", 1L)
            .beginGroup()
            .notEqualTo("registNum", 0L)
            .sort("dateTime", Sort.ASCENDING)
            .endGroup()
            .findAll()
            .sum("registNum").toInt()

        val rangeTotal = rangeTotalIncome - rangeTotalSpend

        val countResult = accountItems
            .where().between("numDateTime", sdfAvgDay, sdfD)
            .beginGroup()
            .notEqualTo("registNum", 0L)
            .sort("dateTime", Sort.ASCENDING)
            .endGroup()
            .findAll()
            .count()

        Log.d(tagDebug, "AVG: ${(rangeTotal / countResult)}")
        Log.d(tagDebug, "countResult:${countResult}")

        return {
            when (rangeTotal) {
                0 -> 0
                else ->
                    rangeTotal / countResult
            }
        }
    }

    // 登録履歴検索処理
    fun searchRecode(
        realm: Realm, listSearchCondition: MutableList<String>?
    ): RealmResults<AccountRegist> {

        val yyyy: String? = listSearchCondition?.get(0)
        val mm: String? = listSearchCondition?.get(1)
        val dd: String? = listSearchCondition?.get(2)
        val categoryId: String? = listSearchCondition?.get(3)

        Log.d(tagDebug, "listSearchCondition:${listSearchCondition}")

        val setCondYear: RealmResults<AccountRegist> =
            when (yyyy) {
                "" -> realm.where<AccountRegist>().findAll()
                else -> {
                    realm.where<AccountRegist>().equalTo("numYear", yyyy).findAll()
                }
            }
        val setCondMonth: RealmResults<AccountRegist> =
            when (mm) {
                "" -> setCondYear
                else -> {
                    setCondYear.where().equalTo("numMonth", mm).findAll()
                }
            }
        val setCondDay: RealmResults<AccountRegist> =
            when (dd) {
                "" -> setCondMonth
                else -> {
                    setCondMonth.where().equalTo("numDay", dd).findAll()
                }
            }
        val setCondCategoryId: RealmResults<AccountRegist> =
            when (categoryId?.toLong()) {
                -1L -> setCondDay
                0L -> setCondDay.where().equalTo("registType", 0L).findAll()
                else -> {
                    setCondDay.where().equalTo("categoryId", categoryId!!.toLong()).findAll()
                }
            }
        Log.d(tagDebug, "setCondYear:${setCondYear}\n")
        Log.d(tagDebug, "setCondMonth:${setCondMonth}\n")
        Log.d(tagDebug, "setCondDay:${setCondDay}\n")
        Log.d(tagDebug, "setCondCategoryId:${setCondCategoryId}\n")

        return setCondCategoryId.sort("dateTime", Sort.DESCENDING)
    }
}
