package com.redhorse.accountbank.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "year_table")
data class YearTable(
    @PrimaryKey val year: Int
)
