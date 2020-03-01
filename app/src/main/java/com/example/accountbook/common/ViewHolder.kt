/*
 * Copyright (c) 2020. SO-BEBOP. All Rights Reserved.
 */

package com.example.accountbook.common

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.one_result.view.*

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var dateText: TextView? = null
    var categoryName: TextView? = null
    var numberText: TextView? = null

    init {
        dateText = itemView.dateText
        categoryName = itemView.categoryName
        numberText = itemView.numberText
    }
}