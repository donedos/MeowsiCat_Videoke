package com.outofthebox.meowsicattv

import android.Manifest
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.meowsicat.android.storage.BehaviorSettings
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.outofthebox.meowsicattv.ui.App
import com.outofthebox.meowsicattv.ui.theme.MeowsiCatTheme

class MainActivity : ComponentActivity() {

    private val isInPipModeState = mutableStateOf(false)
    private var isInPlayerScreenState = false

    // Picture-in-picture is only allowed while the Player screen is on top. Leaving while a song
    // plays on any other screen must not drop the app into PIP.
    private val canEnterPip: Boolean
        get() = isInPlayerScreenState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            val isInPipMode by isInPipModeState
            val context = LocalContext.current
            var appTheme by remember { mutableIntStateOf(BehaviorSettings.getAppTheme(context)) }

            MeowsiCatTheme(appTheme = appTheme) {
                AppGateway(
                    appName = getString(R.string.app_name),
                    onRequestPermission = { promptStoragePermission() },
                    hasAccess = { hasStorageAccess() },
                    isInPipMode = isInPipMode,
                    isPipSupported = isPipSupported() && isPipAllowed(),
                    onEnterPip = { enterPip() },
                    onPlayerStateChanged = { _, inPlayerScreen ->
                        isInPlayerScreenState = inPlayerScreen
                        updatePipParams()
                    },
                    onThemeChanged = { newTheme ->
                        appTheme = newTheme
                    }
                )
            }
        }

        requestAllRuntimePermissions()
    }

    /**
     * Prompts for every grantable runtime permission in one shot at startup,
     * so the user never has to hunt through system settings. ("All files
     * access" is excluded — Android only allows granting that via Settings.)
     */
    private fun requestAllRuntimePermissions() {
        val wanted = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wanted += Manifest.permission.POST_NOTIFICATIONS
            wanted += Manifest.permission.READ_MEDIA_AUDIO
        } else {
            wanted += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val missing = wanted.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), RC_RUNTIME)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (canEnterPip && isPipSupported() && isPipAllowed()) {
            enterPip()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeState.value = isInPictureInPictureMode
    }

    private fun isPipSupported(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    private fun isPipAllowed(): Boolean {
        if (!isPipSupported()) return false
        val appOps = getSystemService(APP_OPS_SERVICE) as? AppOpsManager
        return if (appOps != null) {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    Process.myUid(),
                    packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    Process.myUid(),
                    packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } else {
            true
        }
    }

    private fun enterPip() {
        if (canEnterPip && isPipSupported() && isPipAllowed()) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePipParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isPipSupported()) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAutoEnterEnabled(canEnterPip)
                    .setAspectRatio(Rational(16, 9))
                    .build()
                setPictureInPictureParams(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun promptStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName")
            )
            try {
                startActivity(intent)
            } catch (_: Exception) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RC_STORAGE
            )
        }
    }

    private fun hasStorageAccess(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    private companion object {
        const val RC_STORAGE = 2001
        const val RC_RUNTIME = 2003
    }
}

@Composable
private fun AppGateway(
    appName: String,
    onRequestPermission: () -> Unit,
    hasAccess: () -> Boolean,
    isInPipMode: Boolean,
    isPipSupported: Boolean,
    onEnterPip: () -> Unit,
    onPlayerStateChanged: (playing: Boolean, inPlayerScreen: Boolean) -> Unit,
    onThemeChanged: (Int) -> Unit = {}
) {
    var granted by remember { mutableStateOf(hasAccess()) }
    var showSplash by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = hasAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!granted) {
        PermissionGate(
            appName = appName,
            onGrant = {
                onRequestPermission()
                granted = hasAccess()
            }
        )
    } else {
        AnimatedContent(
            targetState = showSplash,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
            label = "splashTransition"
        ) { isSplash ->
            if (isSplash) {
                AnimatedSplashScreen(
                    onSplashFinished = { showSplash = false }
                )
            } else {
                App(
                    modifier = Modifier,
                    isInPipMode = isInPipMode,
                    isPipSupported = isPipSupported,
                    onEnterPip = onEnterPip,
                    onPlayerStateChanged = onPlayerStateChanged,
                    onThemeChanged = onThemeChanged
                )
            }
        }
    }
}

@Composable
private fun AnimatedSplashScreen(
    onSplashFinished: () -> Unit
) {
    var frameIndex by remember { mutableIntStateOf(0) }
    val frames = remember {
        intArrayOf(
            R.drawable.cat_01, R.drawable.cat_01, R.drawable.cat_02, R.drawable.cat_03,
            R.drawable.cat_04, R.drawable.cat_05, R.drawable.cat_06, R.drawable.cat_07,
            R.drawable.cat_08, R.drawable.cat_09, R.drawable.cat_10, R.drawable.cat_11,
            R.drawable.cat_12, R.drawable.cat_13, R.drawable.cat_14, R.drawable.cat_15,
            R.drawable.cat_16, R.drawable.cat_17, R.drawable.cat_18, R.drawable.cat_19,
            R.drawable.cat_20
        )
    }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 3200L) {
            delay(140L)
            frameIndex = (frameIndex + 1) % frames.size
        }
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D9488),
                        Color(0xFF0B0F14)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF022C28).copy(alpha = 0.85f))
                    .border(3.dp, Color(0xFF2DD4BF), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(frames[frameIndex]),
                    contentDescription = "MeowsiCat",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "MeowsiCat",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Karaoke for Everyone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2DD4BF)
                )
            }
        }
    }

@Composable
private fun PermissionGate(appName: String, onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "This app reads your karaoke content (MeowsiCat folder) from a shared location.\n" +
                "Grant “All files access” so it can open the library.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGrant) {
            Text("Grant access & continue")
        }
    }
}