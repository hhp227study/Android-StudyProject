package com.hhp227.runningtracker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.hhp227.runningtracker.services.TrackingService
import com.hhp227.runningtracker.services.TrackingService.Companion.pathPoints
import com.hhp227.runningtracker.services.TrackingService.Companion.timeRunInMillis
import com.hhp227.runningtracker.ui.theme.RunningTrackerTheme
import com.hhp227.runningtracker.util.Constants.MAP_ZOOM
import com.hhp227.runningtracker.util.Constants.POLYLINE_COLOR
import com.hhp227.runningtracker.util.Constants.POLYLINE_WIDTH
import com.hhp227.runningtracker.util.TrackingUtility
import timber.log.Timber

// 1. 필요한 권한들 정의
private val requiredPermissions = mutableListOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
).apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        add(android.Manifest.permission.FOREGROUND_SERVICE_LOCATION)
    }
}.toTypedArray()

class MainActivity : ComponentActivity() {

    // 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }

        if (granted) {
            Timber.d("All permissions granted")
        } else {
            Toast.makeText(this, "앱 사용을 위해 위치 및 알림 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            // finish() // ⬅️ 이 부분을 제거하여 앱의 강제 종료를 막습니다.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Timber.plant(Timber.DebugTree()) // Timber 초기화

        // 권한 요청
        requestPermissions()

        setContent {
            RunningTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RunningScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun requestPermissions() {
        if (!TrackingUtility.hasLocationPermissions(this) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    // 포그라운드 서비스에게 명령을 전달하는 함수
    fun sendCommandToService(action: String) = Intent(
        this,
        TrackingService::class.java
    ).also {
        it.action = action
        startService(it)
    }

    // 화면 유지(Wake Lock)를 처리하는 함수
    fun keepScreenOn(keep: Boolean) {
        if (keep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // GPS 켜짐 상태를 확인하고 요청하는 함수
    fun checkGpsAndPrompt() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS가 꺼져 있습니다. 설정에서 켜주세요.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}

// ---------------------- Compose UI ----------------------

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    @SuppressLint("ContextCastToActivity") activity: MainActivity = LocalContext.current as MainActivity
) {
    val isTracking by TrackingService.isTracking.observeAsState(initial = false)
    val pathPointsList by pathPoints.observeAsState(initial = mutableListOf())
    val timeRun by timeRunInMillis.observeAsState(initial = 0L)

    var showFinishDialog by remember { mutableStateOf(false) }

    // 맵 카메라 상태
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), MAP_ZOOM) // 초기 서울 시청
    }

    // isTracking 상태에 따라 화면 유지(Wake Lock) 처리
    LaunchedEffect(isTracking) {
        activity.keepScreenOn(isTracking)
    }

    // 경로 업데이트 시 카메라 이동
    LaunchedEffect(pathPointsList.size) {
        if (pathPointsList.isNotEmpty() && pathPointsList.last().isNotEmpty()) {
            val lastLatLng = pathPointsList.last().last()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(lastLatLng, MAP_ZOOM)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. 지도 표시
        MapContent(
            pathPoints = pathPointsList,
            cameraPositionState = cameraPositionState
        )

        // 2. 시간 표시 (상단)
        Text(
            text = TrackingUtility.getFormattedStopWatchTime(timeRun),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            style = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, color = Color.Black) // 크기 및 색상 조정
        )

        // 3. 컨트롤 버튼 (하단)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            if (!isTracking) { // 시작 또는 재시작 버튼
                Button(onClick = {
                    activity.checkGpsAndPrompt() // GPS 켜짐 확인
                    activity.sendCommandToService(TrackingService.ACTION_START_OR_RESUME_SERVICE)
                }) {
                    Text(if (timeRun == 0L) "시작" else "재시작")
                }
            } else { // 일시정지 버튼
                Button(onClick = {
                    activity.sendCommandToService(TrackingService.ACTION_PAUSE_SERVICE)
                }) {
                    Text("일시정지")
                }

                // 종료 버튼 (일시정지 상태에서만 노출 고려)
                if (timeRun > 0L) {
                    Button(onClick = {
                        showFinishDialog = true
                    }, modifier = Modifier.padding(start = 16.dp)) {
                        Text("종료")
                    }
                }
            }
        }
    }

    // 운동 종료 다이얼로그 (생략)
    if (showFinishDialog) {
        // TODO: 종료 다이얼로그 구현 및 종료 시 로컬 DB 저장 로직 호출
        // activity.sendCommandToService(TrackingService.ACTION_STOP_SERVICE) 호출 및 DB 저장
    }
}

@Composable
fun MapContent(
    pathPoints: List<List<LatLng>>,
    cameraPositionState: CameraPositionState
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        pathPoints.forEach { polyline ->
            Polyline(
                points = polyline,
                color = Color(POLYLINE_COLOR),
                width = POLYLINE_WIDTH
            )
        }
    }
}