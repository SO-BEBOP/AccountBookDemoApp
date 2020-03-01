/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.realmdata

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class AccountRegist : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    // 登録年月日
    var dateTime: Date = Date()
    // 登録年月日yyyyMMdd
    var numDateTime: Long = 0
    // 登録年yyyy
    var numYear: String = ""
    // 登録月MM
    var numMonth: String = ""
    // 登録日dd
    var numDay: String = ""
    // カテゴリーid
    var categoryId: Long = 0
    // 登録タイプ
    var registType: Long = 0
    // 金額
    var registNum: Long = 0
}
