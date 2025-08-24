package com.darkant.flowengine

import android.media.MediaPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

class VideoNode(private val engine: Engine) : Node<String, Boolean>(engine) {
    private var playerRef: WeakReference<MediaPlayer>? = null

    override suspend fun start(input: String): Boolean {
        val player = engine.getPlayer()
        playerRef = WeakReference(player)
        player.setDataSource(input)
        player.prepareAsync()
        player.setOnPreparedListener {
            it.start()
        }
        return suspendCancellableCoroutine { continuation ->
            player.setOnCompletionListener {
                continuation.resume(true)
            }
            player.setOnErrorListener { mp, what, extra ->
                continuation.resume(false)
                true
            }
        }
    }

    override suspend fun stop() {
        val player = playerRef?.get() ?: return
        player.setOnPreparedListener(null)
    }
}