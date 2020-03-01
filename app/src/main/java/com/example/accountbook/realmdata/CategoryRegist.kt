/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.realmdata

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CategoryRegist : RealmObject() {
    @PrimaryKey
    var itemId: Long = 0
    // カテゴリータイプ(0:未登録/1:出金/2:入金/3:固定費)
    var categoryType: Long = 0
    // 項目名称
    var itemName: String = "未登録"
}