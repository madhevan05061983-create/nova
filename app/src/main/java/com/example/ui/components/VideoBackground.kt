package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaDeepSpace

const val NOVA_VIDEO_BACKGROUND_URL = "https://res.cloudinary.com/lirx7iwb/video/upload/v1792684712/a_edit_1.mp4"

@Composable
fun VideoBackground(
    modifier: Modifier = Modifier,
    videoUri: String = NOVA_VIDEO_BACKGROUND_URL,
    isPlaying: Boolean = true,
    overlayAlpha: Float = 0.55f
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isVideoPrepared by remember { mutableStateOf(false) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Pause/Resume video with Android Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        mediaPlayerRef?.pause()
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying) {
                        try {
                            mediaPlayerRef?.start()
                        } catch (_: Exception) {}
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Toggle playback based on isPlaying param
    LaunchedEffect(isPlaying, mediaPlayerRef) {
        val player = mediaPlayerRef ?: return@LaunchedEffect
        try {
            if (isPlaying && isVideoPrepared && !player.isPlaying) {
                player.start()
            } else if (!isPlaying && player.isPlaying) {
                player.pause()
            }
        } catch (_: Exception) {}
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fallback / ambient base while loading or if video stream is buffering
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NovaCyan.copy(alpha = 0.18f),
                            NovaDeepSpace.copy(alpha = 0.85f),
                            NovaDeepSpace
                        )
                    )
                )
        )

        // Hardware-accelerated TextureView with MediaPlayer
        AndroidView(
            factory = { ctx ->
                val textureView = TextureView(ctx)
                textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        val surface = Surface(surfaceTexture)
                        val mp = MediaPlayer().apply {
                            setSurface(surface)
                            isLooping = true
                            setVolume(0f, 0f) // Silent background video
                            setOnPreparedListener { preparedMp ->
                                isVideoPrepared = true
                                adjustAspectRatio(textureView, width, height, preparedMp.videoWidth, preparedMp.videoHeight)
                                if (isPlaying) {
                                    preparedMp.start()
                                }
                            }
                            setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                                adjustAspectRatio(textureView, width, height, vWidth, vHeight)
                            }
                            setOnErrorListener { _, _, _ ->
                                isVideoPrepared = false
                                true
                            }
                            try {
                                setDataSource(ctx, Uri.parse(videoUri))
                                prepareAsync()
                            } catch (_: Exception) {
                                isVideoPrepared = false
                            }
                        }
                        mediaPlayerRef = mp
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        val mp = mediaPlayerRef ?: return
                        if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                            adjustAspectRatio(textureView, width, height, mp.videoWidth, mp.videoHeight)
                        }
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        try {
                            mediaPlayerRef?.stop()
                            mediaPlayerRef?.release()
                        } catch (_: Exception) {}
                        mediaPlayerRef = null
                        isVideoPrepared = false
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                }
                textureView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Smooth fade-in overlay for cinematic high-contrast text legibility
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NovaDeepSpace.copy(alpha = overlayAlpha.coerceIn(0.2f, 0.85f)),
                                NovaDeepSpace.copy(alpha = (overlayAlpha + 0.15f).coerceIn(0.3f, 0.92f)),
                                NovaDeepSpace.copy(alpha = (overlayAlpha + 0.25f).coerceIn(0.4f, 0.97f))
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Adjusts the TextureView matrix to achieve Center-Crop scaling
 * so that the video fills the whole display without stretching or letterboxing.
 */
private fun adjustAspectRatio(
    textureView: TextureView,
    viewWidth: Int,
    viewHeight: Int,
    videoWidth: Int,
    videoHeight: Int
) {
    if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) return

    val viewAspect = viewWidth.toDouble() / viewHeight
    val videoAspect = videoWidth.toDouble() / videoHeight

    var scaleX = 1f
    var scaleY = 1f

    if (viewAspect > videoAspect) {
        // View is wider than video: scale Y to crop top/bottom
        scaleY = (viewWidth.toFloat() / videoWidth * videoHeight) / viewHeight
    } else {
        // View is taller than video: scale X to crop sides
        scaleX = (viewHeight.toFloat() / videoHeight * videoWidth) / viewWidth
    }

    val pivotX = viewWidth / 2f
    val pivotY = viewHeight / 2f

    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, pivotX, pivotY)
    textureView.setTransform(matrix)
}
