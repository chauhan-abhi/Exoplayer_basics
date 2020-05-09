package com.example.exoplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerActivity : AppCompatActivity() {
    lateinit var playerView:PlayerView
    var mPlayer: SimpleExoPlayer? = null
    var playWhenReady = true
    var currentWindow: Int = 0
    var playBackPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.video_view)
    }

    private fun buildMediaSourceForConcatenatingMedia(mp4Uri: Uri, mp3Uri: Uri) : MediaSource {
        val ds: DataSource.Factory = DefaultDataSourceFactory(this, "exoplayer-codelab")
        val mediaSourceFactory = ProgressiveMediaSource.Factory(ds)
        val videoMediaSource =  mediaSourceFactory.createMediaSource(mp4Uri)
        val audioMediaSource = mediaSourceFactory.createMediaSource(mp3Uri)
        return ConcatenatingMediaSource(videoMediaSource, audioMediaSource)

    }

    private fun buildDashMediaSource(uri: Uri): MediaSource {
        val ds: DataSource.Factory = DefaultDataSourceFactory(this, "exoplayer-codelab")
        val dashMediaSourceFactory = DashMediaSource.Factory(ds)
        return dashMediaSourceFactory.createMediaSource(uri)
    }

    private fun initializePlayer() {
        if (mPlayer == null) {
            val trackSelector = DefaultTrackSelector(this)
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd())
            mPlayer = SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build()

        }

        //mPlayer = SimpleExoPlayer.Builder(this).build()
        //val mp4Uri = Uri.parse(getString(R.string.media_url_mp4))
        //val mp3Uri = Uri.parse(getString(R.string.media_url_mp3))
        val dashUri = Uri.parse(getString(R.string.media_url_dash))
        playerView.player = mPlayer

        //val mediaSource = buildMediaSourceForConcatenatingMedia(mp4Uri, mp3Uri)
        val dashMediaSource = buildDashMediaSource(dashUri)

        mPlayer?.playWhenReady = playWhenReady
        mPlayer?.seekTo(currentWindow, playBackPosition)
        //mPlayer?.prepare(mediaSource, false, false)
        mPlayer?.prepare(dashMediaSource, false, false)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24 )
            initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24)
            releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT < 24 && mPlayer == null) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >=24)
            releasePlayer()
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            playWhenReady = mPlayer!!.playWhenReady
            playBackPosition = mPlayer!!.currentPosition
            currentWindow = mPlayer!!.currentWindowIndex
            mPlayer!!.release()
            mPlayer = null
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}