package com.example.cameraxapp

import android.content.ContentValues
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class EditImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)
        preview()

        val seekBar:SeekBar = findViewById(R.id.seekBar2)
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        val saveButton: Button = findViewById(R.id.save_button2)
        saveButton.setOnClickListener{ saveImage() }
    }

    private fun preview() {
        val imageview: ImageView = findViewById(R.id.imageView3)
        imageview.setImageBitmap(DataRepositorySingleton.dataToDo)
    }

    private var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // updated continuously as the user slides the thumb
            val bitmap1: Bitmap = changeBitmapContrastBrightness(DataRepositorySingleton.dataToDo,50.0f,progress.toFloat())
            val imageview3: ImageView = findViewById(com.example.cameraxapp.R.id.imageView3)
            DataRepositorySingleton.dataToDo = bitmap1
            imageview3.setImageBitmap(bitmap1)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // called when the user first touches the SeekBar
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // called after the user finishes moving the SeekBar
        }
    }


    fun changeBitmapContrastBrightness(bmp: Bitmap, contrast: Float, brightness: Float): Bitmap {
        val cm = ColorMatrix(
            floatArrayOf(
                contrast,
                0f,
                0f,
                0f,
                brightness,
                0f,
                contrast,
                0f,
                0f,
                brightness,
                0f,
                0f,
                contrast,
                0f,
                brightness,
                0f,
                0f,
                0f,
                1f,
                0f
            )
        )
        val ret = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)
        val canvas = Canvas(ret)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bmp, 0.0f, 0.0f, paint)
        return ret
    }

    private fun saveImage() {
        saveMediaToStorage(DataRepositorySingleton.dataToDo)
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            val msg = "Photo saved in Gallery"
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

}