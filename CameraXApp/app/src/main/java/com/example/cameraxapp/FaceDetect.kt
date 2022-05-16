package com.example.cameraxapp

// package com.example.facedetect


import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException


class FaceDetect : AppCompatActivity() {
    var imageview1: ImageView? = null
    var textview1: TextView? = null
    var options: FaceDetectorOptions? = null
    var imagebitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button1 = findViewById<Button>(R.id.button1)
        imageview1 = findViewById(R.id.imageView)
        textview1 = findViewById(R.id.textView1)
        button1.setOnClickListener { mGetContent.launch("image/*") }
        options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    }

    var mGetContent = registerForActivityResult(
        GetContent()
    ) { uri ->
        try {
            imagebitmap = getBitmapFromUri(uri)
            val myimage = InputImage.fromFilePath(applicationContext, uri)
            processImage(myimage)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun processImage(image: InputImage?) {
/*        val detector = options?.let { FaceDetection.getClient(it) }
        val result = image?.let {
            detector?.process(it)
                ?.addOnSuccessListener { faces -> // Task completed successfully
                    displayFaces(faces)
                }
                ?.addOnFailureListener {
                    // Task failed with an exception
                }
        } */
    }

    fun displayFaces(faces: List<Face>) {
        val bitmap =
            Bitmap.createBitmap(imagebitmap!!.width, imagebitmap!!.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(imagebitmap!!, 0f, 0f, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.0f
        for (face in faces) {
            val bounds = face.boundingBox
            canvas.drawRect(bounds, paint)
        }
        imageview1!!.setImageBitmap(bitmap)
        textview1!!.text = "Total faces: " + faces.size
    }
}