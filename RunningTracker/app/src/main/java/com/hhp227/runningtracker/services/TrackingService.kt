package com.hhp227.runningtracker.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.hhp227.runningtracker.MainActivity
import com.hhp227.runningtracker.R
import com.hhp227.runningtracker.services.TrackingService.Companion.ACTION_STOP_SERVICE
import com.hhp227.runningtracker.util.Constants.FASTEST_LOCATION_INTERVAL
import com.hhp227.runningtracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.hhp227.runningtracker.util.Constants.BATTERY_CRITICAL_THRESHOLD_STOP
import com.hhp227.runningtracker.util.TrackingUtility
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import android.content.BroadcastReceiver

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isFirstRun = true
    private var serviceKilled = false // 데이터 유실 방지 및 서비스 종료 상태 관리

    private var timeRun = 0L
    private var lastSecondTimestamp = 0L

    private val batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryLevel = TrackingUtility.getBatteryLevel(applicationContext)
                if (batteryLevel <= BATTERY_CRITICAL_THRESHOLD_STOP) {
                    Timber.d("Critical battery level detected: $batteryLevel%. Stopping service.")
                    killService() // 강제 종료 및 저장
                }
            }
        }
    }

    // 타이머 구현 (간단한 예시)
    private fun startTimer() {
        timeRun = 0L
        isTracking.postValue(true)
        val timerJob = lifecycle.coroutineScope.launch {
            while (isTracking.value == true && !serviceKilled) {
                // 현재 시간 - 운동 시작 시간
                val timeDifference = System.currentTimeMillis() - lastSecondTimestamp

                if (timeDifference >= 1000L) {
                    timeRun += timeDifference
                    timeRunInMillis.postValue(timeRun)
                    lastSecondTimestamp += 1000L
                }
                delay(50L) // 50ms마다 체크
            }
            timeRun = 0L
        }
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        postInitialValues()

        // isTracking 관찰: 위치 업데이트 요청/제거
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it) // 알림 업데이트
        }

        // 배터리 리시버 등록
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        Timber.d("ACTION_START_OR_RESUME_SERVICE (Start)")
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("ACTION_START_OR_RESUME_SERVICE (Resume)")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("ACTION_PAUSE_SERVICE")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("ACTION_STOP_SERVICE")
                    killService()
                }
            }
        }
        // 서비스가 강제 종료되어도 다시 시작하도록 설정 (데이터 유실 방지 전략의 일환)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                    .build()

                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value == true) {
                result.locations.forEach { location ->
                    addPathPoint(location)
                    Timber.d("New Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun startForegroundService() {
        // 첫 시작 시 빈 Polyline 추가
        addEmptyPolyline()

        startTimer() // 타이머 시작

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        // 초기 알림 빌더 생성
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // R.drawable.ic_launcher_foreground 대신 사용할 아이콘 (ic_run 등을 추가해야 합니다)
            .setContentTitle("Running Tracker")
            .setContentText("운동 중")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        // isTracking 관찰하며 알림의 액션을 동적으로 변경
        isTracking.observe(this) {
            // 알림 업데이트 (일시정지/재시작 버튼)
            val pendingIntent: PendingIntent? = if (it) {
                val pauseIntent = Intent(this, TrackingService::class.java).apply {
                    action = ACTION_PAUSE_SERVICE
                }
                PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            } else {
                val resumeIntent = Intent(this, TrackingService::class.java).apply {
                    action = ACTION_START_OR_RESUME_SERVICE
                }
                PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            }

            val notificationActionText = if (it) "일시정지" else "재시작"

            // 알림 업데이트 시 Builder를 복사하여 사용
            val newNotificationBuilder = notificationBuilder
                .clearActions()
                .addAction(
                    R.drawable.ic_pause, // ic_pause 아이콘 추가 필요
                    notificationActionText,
                    pendingIntent
                )

            notificationManager.notify(NOTIFICATION_ID, newNotificationBuilder.build())
        }

        // timeRunInMillis 관찰하며 시간 업데이트
        timeRunInMillis.observe(this) {
            if (!serviceKilled) {
                val formattedTime = TrackingUtility.getFormattedStopWatchTime(it)
                val updatedNotification = notificationBuilder.setContentText(formattedTime)
                notificationManager.notify(NOTIFICATION_ID, updatedNotification.build())
            }
        }
    }

    // 알림 업데이트 로직 분리 (필요 시)
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 알림 업데이트 로직 (위의 startForegroundService 내부에 포함됨)
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
        const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

        const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Tracking Channel"
        const val NOTIFICATION_ID = 1

        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>() // 현재 달리는 경로 (경로의 경로들)

        // 타이머 관련 LiveData
        val timeRunInMillis = MutableLiveData<Long>()
    }
}