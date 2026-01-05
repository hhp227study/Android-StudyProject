package com.hhp227.runningtracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var timestamp: Long = 0L, // 운동 시작 시간
    var averageSpeed: Float = 0f, // 평균 속도 (km/h)
    var distanceInMeters: Int = 0, // 이동 거리 (m)
    var timeInMillis: Long = 0L, // 운동 시간 (ms)
    var caloriesBurned: Int = 0 // 소모 칼로리
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}