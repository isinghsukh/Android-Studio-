package com.example.cameraxapp

import android.graphics.Bitmap
import android.graphics.Rect
import android.media.FaceDetector
import android.view.DisplayCutout
import com.google.mlkit.vision.face.Face
import kotlin.properties.Delegates


object DataRepositorySingleton {

        lateinit var realImage_1: Bitmap
        lateinit var realImage_2: Bitmap
        var faceCrop_1: Rect? = null
        var faceCrop_2: Rect? = null
        lateinit var faceBitmap_1: Bitmap
        lateinit var faceBitmap_2: Bitmap
        lateinit var blurImage_1: Bitmap
        lateinit var blurImage_2: Bitmap
}

