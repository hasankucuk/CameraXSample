package com.cameraxsample.app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cameraxsample.app.utils.FLAGS_FULLSCREEN
import com.cameraxsample.app.utils.KEY_EVENT_ACTION
import com.cameraxsample.app.utils.KEY_EVENT_EXTRA
import java.io.File


private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentContainer = findViewById(R.id.fragmentContainer)
    }

    /**
     * Ses kısma tuşuna basıldığında CameraFragmentteki volumeDownReciver dinleyicisini tetikler.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }


    /***
     * Immersive Mode, belirli durumlarda gezinti çubuğu,
     * durum çubuğu ve diğer panelleri ortadan kaldırmamızı sağlar
     */
    override fun onResume() {
        super.onResume()
        fragmentContainer.postDelayed({
            fragmentContainer.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

    /**
     * CameraXSample adında bir klasör oluşturur.
     */
    companion object {
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

}
