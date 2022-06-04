package com.example.cameraxapp


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraxapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates


typealias LumaListener = (luma: Double) -> Unit


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    //private var videoCapture: VideoCapture<Recorder>? = null
    //private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var frontcamera = false

    private var countImageCapture = 0

    //Face detector code:
    private var options: FaceDetectorOptions? = null

    //private var count = 0

    //private var countFace = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.changeCameraButton.setOnClickListener { startCamera() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback(){
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    Log.i(TAG, "Success")
                    countImageCapture++
                    if (countImageCapture == 1) {
                        DataRepositorySingleton.realImage_1 = imageProxyToBitmap(image)
                        val inputImage_1 = InputImage.fromBitmap(DataRepositorySingleton.realImage_1, 0)
                        val msg = "Please wait!!"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        processImage(inputImage_1)
                    } else {
                        DataRepositorySingleton.realImage_2 = imageProxyToBitmap(image)
                        val inputImage_2 = InputImage.fromBitmap(DataRepositorySingleton.realImage_2, 0)
                        val msg = "Please wait!!"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        processImage(inputImage_2)
                    }
                    //countImageCapture(image)
                }
                override fun onError(exception: ImageCaptureException) {
                    //super.onError(exception)
                    Log.e(TAG, "Photo capture failed:")
                }
            }
        )
    }

    private fun intentCall() {
        val intent = Intent(this, ImagePreview::class.java)
        startActivity(intent)
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer[bytes]
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }

    private fun processImage(image: InputImage?) {
        val detector = options?.let { FaceDetection.getClient(it) }
        val result = image?.let {
            detector?.process(it)
                ?.addOnSuccessListener { faces -> // Task completed successfully
                    if (countImageCapture == 1) {
                        if (faces.count() != 0) {
                            val msg = "Face Detected!!"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            viewBinding.imageCaptureButton.text = "TAKE SECOND PHOTO"
                            for (face in faces) {
                                DataRepositorySingleton.faceCrop_1 = face.boundingBox
                            }
                        } else {
                            val msg = "Captured images should contain a face!!"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        if (faces.count() != 0) {
                            val msg = "Face Detected!!"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            for (face in faces) {
                                DataRepositorySingleton.faceCrop_2 = face.boundingBox
                            }
                            intentCall()
                        } else {
                            val msg = "Captured images should contain a face!!"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
        }
                ?.addOnFailureListener {
                    // Task failed with an exception
                    Log.i(TAG, "Failed")
                }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            //val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            if(frontcamera) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                frontcamera = false
            } else {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                frontcamera = true

            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        //private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        var countFace = 0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}