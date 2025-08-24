package com.darkant.flowengine

import android.media.MediaPlayer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Engine {

    private val mainScope by lazy { MainScope() }
    private val mediaPlayer by lazy { MediaPlayer() }

    fun execute(node: Node<String, *>) {
        mainScope.launch {
            node.start("https://test-dual-mentor.91jzx.cn/ai-dual-mentor/2025/07/11/aidm-audio-1943671594219515905.wav")
        }
    }

    fun getPlayer(): MediaPlayer {
        return mediaPlayer
    }
}