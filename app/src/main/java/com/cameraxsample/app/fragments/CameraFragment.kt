package com.cameraxsample.app.fragments


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.cameraxsample.app.MainActivity
import com.cameraxsample.app.R
import com.cameraxsample.app.utils.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import kotlin.coroutines.CoroutineContext





private const val TAG = "CameraXSample"
private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val PHOTO_EXTENSION = ".jpg"


class CameraFragment : Fragment(), CoroutineScope {

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: TextureView
    private lateinit var outputDirectory: File

    private var lensFacing = CameraX.LensFacing.BACK
    private var imageCapture: ImageCapture? = null

    //Coroutine asenkron bir şekilde kod yazmamızı sağlar
    private val job= Job()
    override val coroutineContext: CoroutineContext
    get() = Dispatchers.Default +job

    /**
     * Ses kısma tuşuna basıldığında tetiklenecek olan reciver.
     * Burada görüntü almamızı sağlayacak.
     */
    private val volumeDownReceiver =object  :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            val keyCode= intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)
            when(keyCode){
                KeyEvent.KEYCODE_VOLUME_DOWN ->{
                    val shutter =container
                        .findViewById<ImageButton>(R.id.ivBtnCameraCapture)
                    shutter.simulateClick()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance=true //Fragment örneğini korur. Yani ekran değişimlerinde her seferinde onCreate olmaz
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container= view as ConstraintLayout
        viewFinder = container.findViewById(R.id.viewFinder)

        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        LocalBroadcastManager.getInstance(context!!).registerReceiver(volumeDownReceiver,filter)

        outputDirectory=MainActivity.getOutputDirectory(requireContext())

        viewFinder.post {
            updateCameraUi()
            bindCameraUseCases()

            //Çekilmiş son fotoğrafı arkaplanda yükler
            launch(coroutineContext) {
                outputDirectory.listFiles { file ->
                    EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
                }.sorted().reversed().firstOrNull()?.let { setGalleryThumbnail(it) }
            }}
    }

    private fun setGalleryThumbnail(file: File){

        val thumbnail=container.findViewById<ImageButton>(R.id.ivBtnGallery)


        launch(coroutineContext) {
            val bitmap = ImageUtils.decodeBitmap(file)

            val thumbnailBitmap = ImageUtils.cropCircularThumbnail(bitmap)

            withContext(Dispatchers.Main){
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    thumbnail.foreground=BitmapDrawable(resources, thumbnailBitmap)
                }else{
                    Glide.with(requireContext()).load(thumbnailBitmap).into(thumbnail)
                }
            }
        }
    }

    /**
     * Fotograf yakalandığında bize bir file döner.
     * Dönen file mediaScanner ile tarayarak galerimizde görünmesini sağlarız
     */
    private val imageSavedListener= object : ImageCapture.OnImageSavedListener{
        override fun onImageSaved(file: File) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                setGalleryThumbnail(file)
            }else if(Build.VERSION.SDK_INT< Build.VERSION_CODES.N) {
                requireActivity().sendBroadcast(Intent(Camera.ACTION_NEW_PICTURE).setData(Uri.fromFile(file)))
            }

            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(file.extension)
            MediaScannerConnection.scanFile(
                context, arrayOf(file.absolutePath), arrayOf(mimeType), null)
        }

        override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
            Toast.makeText(context,"Fotoğraf yakalanırken bir hata oluştu",Toast.LENGTH_LONG).show()
        }

    }


    private fun bindCameraUseCases() {

        // Make sure that there are no other use cases bound to CameraX
        CameraX.unbindAll()

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        Log.d(javaClass.simpleName, "Metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val viewFinderConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        val preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder)

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)

            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        imageCapture = ImageCapture(imageCaptureConfig)


        CameraX.bindToLifecycle(
            this, preview, imageCapture)
    }

    @SuppressLint("RestrictedApi")
    private fun updateCameraUi() {

        // Remove previous UI if any
        container.findViewById<ConstraintLayout>(R.id.cameraViewContainer)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(requireContext(), R.layout.camera_view_container, container)

        controls.findViewById<ImageButton>(R.id.ivBtnCameraCapture).setOnClickListener {
            val photoFile = ImageUtils.createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            val metadata = ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT
            }

            imageCapture?.takePicture(photoFile, imageSavedListener, metadata)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                container.postDelayed({
                    container.foreground = ColorDrawable(Color.WHITE)
                    container.postDelayed({ container.foreground = null }, ANIMATION_FAST_MILLIS)
                }, ANIMATION_SLOW_MILLIS)
            }
        }

        controls.findViewById<ImageButton>(R.id.ivBtnCameraSwitch).setOnClickListener {
            lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                CameraX.LensFacing.BACK
            } else {
                CameraX.LensFacing.FRONT
            }
            try {
                CameraX.getCameraWithLensFacing(lensFacing)
                bindCameraUseCases()
            } catch (exc: Exception) {
            }
        }

        controls.findViewById<ImageButton>(R.id.ivBtnGallery).setOnClickListener {
            val arguments = Bundle().apply {
                putInt(SELECTED_POSITION,2)
                putString(KEY_ROOT_DIRECTORY, outputDirectory.absolutePath)
                 }
            Navigation.findNavController(requireActivity(), R.id.fragmentContainer)
                .navigate(R.id.action_camera_to_gallery, arguments)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(volumeDownReceiver)
        CameraX.unbindAll()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }


}
