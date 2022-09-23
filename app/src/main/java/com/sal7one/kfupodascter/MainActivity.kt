package com.sal7one.kfupodascter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.huawei.hms.api.bean.HwAudioPlayItem
import com.huawei.hms.audiokit.player.callback.HwAudioConfigCallBack
import com.huawei.hms.audiokit.player.manager.*
import com.sal7one.kfupodascter.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var mHwAudioPlayerManager: HwAudioPlayerManager? = null
    private var mHwAudioManager: HwAudioManager? = null
    private var playItemList: MutableList<HwAudioPlayItem>? = mutableListOf()
    private var currentAudio: HwAudioPlayItem? = null
    private lateinit var playPause: ImageView

    private val listOfPodcasts = listOf(
        "http://traffic.libsyn.com/secure/adbackstage/ADB188_Android13_FINAL.mp3",
        "http://traffic.libsyn.com/secure/adbackstage/ADB187_SystemUI_final.mp3",
    )

    private var playTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        playTitle = binding.tvsongname
        playPause = binding.playPause

            val hwAudioPlayerConfig = HwAudioPlayerConfig(this)
        lifecycleScope.launch {
            createHwAudioManager(hwAudioPlayerConfig, getOnlinePlaylist())
        }

        binding.prev.setOnClickListener {
            audioButtonHandler(it)
        }

        binding.playPause.setOnClickListener {
            audioButtonHandler(it)
        }
        binding.next.setOnClickListener {
            audioButtonHandler(it)
        }
    }

    private fun audioButtonHandler(view: View) {
        if (mHwAudioPlayerManager == null) {
            Toast.makeText(this, "Please Initialize Audio first", Toast.LENGTH_SHORT).show()
            return
        }
        when (view.id) {
            R.id.prev -> previousAudio()
            R.id.play_pause -> playPauseAudio()
            R.id.next -> nextAudio()
            else -> Log.d("MainActivity", "no button clicked")
        }
    }

    private fun nextAudio() {
        mHwAudioPlayerManager?.playNext()
        findNext()
        mHwAudioManager?.queueManager?.currentPlayItem?.let { updateSongInfo(it.audioTitle) }
    }

    private fun updateSongInfo(audioTitle: String) {
        playTitle?.text = audioTitle
    }

    open fun updateSongInfo() {
        playTitle!!.text = currentAudio!!.audioTitle
    }

    fun getOnlinePlaylist(): List<HwAudioPlayItem?>? {
        val audioPlayItem = HwAudioPlayItem()

        audioPlayItem.audioId = "android12id"
        audioPlayItem.singer = "The Android Backstage podcast"
        audioPlayItem.onlinePath = listOfPodcasts[0]
        audioPlayItem.setOnline(1)
        audioPlayItem.audioTitle = "KFU podcast | EP 1"

        playItemList?.add(audioPlayItem)

        audioPlayItem.audioId = "systemuiid"
        audioPlayItem.singer = "The Android Backstage podcast"
        audioPlayItem.onlinePath = listOfPodcasts[1]
        audioPlayItem.setOnline(1)
        audioPlayItem.audioTitle = "KFU podcast | EP 2"
        playItemList?.add(audioPlayItem)

        return playItemList
    }

    private fun previousAudio() {
        mHwAudioPlayerManager?.playPre()
        findNext()
        mHwAudioManager?.queueManager?.currentPlayItem?.let { updateSongInfo(it.audioTitle) }
    }

    private fun findNext() {
        var index = 0
        try {
            for (audio in playItemList!!) {
                if (currentAudio!!.audioId === audio.audioId) {
                    currentAudio = playItemList!![index + 1]
                    break
                }
                index++
            }
        } catch (e: Exception) {
            currentAudio = playItemList!![0]
        }
    }

    private fun playPauseAudio() {
        if (mHwAudioPlayerManager != null) {
            if (mHwAudioPlayerManager!!.isPlaying) {
                mHwAudioPlayerManager!!.pause()
                playPause.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.uamp_ic_play_arrow_white_48dp
                    )
                )
            } else {
                playPause.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.uamp_ic_pause_white_48dp
                    )
                )
                mHwAudioPlayerManager!!.play()
            }
        }
    }

    private var mHwAudioConfigManager: HwAudioConfigManager? = null
    private var mHwAudioQueueManager: HwAudioQueueManager? = null
    private var mHwAudioEffectManager: HwAudioEffectManager? = null

    private fun createHwAudioManager(
        hwAudioPlayerConfig: HwAudioPlayerConfig,
        playlist: List<HwAudioPlayItem?>?
    ) {
        hwAudioPlayerConfig.setDebugMode(true).setDebugPath("").playCacheSize = 20
        HwAudioManagerFactory.createHwAudioManager(
            hwAudioPlayerConfig,
            object : HwAudioConfigCallBack {
                override fun onSuccess(hwAudioManager: HwAudioManager) {
                    try {
                        mHwAudioPlayerManager = hwAudioManager.playerManager
                        mHwAudioConfigManager = hwAudioManager.configManager
                        mHwAudioQueueManager = hwAudioManager.queueManager
                        mHwAudioEffectManager = hwAudioManager.effectManager

                        mHwAudioPlayerManager!!.playList(playlist, 0, 0)
                    } catch (e: Exception) {
                        Log.e("TAG123", "init err:$e")
                    }
                }

                override fun onError(errorCode: Int) {
                    Log.e("TAG123", "init err:$errorCode")
                }
            })
    }
}