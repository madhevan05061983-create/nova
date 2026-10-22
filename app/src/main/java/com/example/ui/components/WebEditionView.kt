package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.NovaViewModel
import com.example.util.AppLauncher

class NovaWebBridge(
    private val context: Context,
    private val viewModel: NovaViewModel,
    private val webViewProvider: () -> WebView?
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun speak(text: String) {
        mainHandler.post {
            viewModel.speakText(text)
        }
    }

    @JavascriptInterface
    fun stopSpeaking() {
        mainHandler.post {
            viewModel.stopSpeaking()
        }
    }

    @JavascriptInterface
    fun toggleListening() {
        mainHandler.post {
            viewModel.toggleListening()
        }
    }

    @JavascriptInterface
    fun launchApp(appName: String): Boolean {
        return AppLauncher.launchAppByName(context, appName).success
    }

    @JavascriptInterface
    fun getBatteryLevel(): Int {
        return AppLauncher.getBatteryLevel(context)
    }

    @JavascriptInterface
    fun signOut() {
        mainHandler.post {
            viewModel.signOut()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebEditionView(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef = remember { arrayOfNulls<WebView>(1) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0x00000000)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.grant(request.resources)
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url != null && !url.startsWith("file:///android_asset/")) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    return true
                                } catch (_: Exception) {}
                            }
                            return false
                        }
                    }

                    val bridge = NovaWebBridge(ctx, viewModel) { webViewRef[0] }
                    addJavascriptInterface(bridge, "NovaAndroid")

                    loadUrl("file:///android_asset/web/index.html")
                    webViewRef[0] = this
                }
            },
            update = { view ->
                webViewRef[0] = view
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef[0]?.destroy()
            webViewRef[0] = null
        }
    }
}
