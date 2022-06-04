package com.example.cameraxapp

import BoolViewModel
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ImagePreview : AppCompatActivity() {

    private var countPhotoSaved = 0
    private var buttonPressed = true
    private lateinit var viewModel: BoolViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        preview()
        blurImage()
        viewModel = ViewModelProvider(this).get(BoolViewModel::class.java)
        viewModel.buttonPressed = buttonPressed
        if (viewModel.buttonPressed == false) {
            //viewModel.buttonPressed = false
            blurImageShow()
        } else {
            //viewModel.buttonPressed = true
            preview()
        }
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener { saveImage() }

        val blurButton: Button = findViewById(R.id.blur_button)
        blurButton.setOnClickListener {
            if (buttonPressed) {
                buttonPressed = false
                blurImageShow()
            } else {
                buttonPressed = true
                preview()
            }
        }

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean("BUTTON", buttonPressed)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        savedInstanceState.getBoolean("BUTTON", buttonPressed)
//        if (buttonPressed) {
//            preview()
//        } else {
//            blurImageShow()
//        }
//    }

    private fun preview() {
        val imageview1: ImageView = findViewById(R.id.imageView1)
        imageview1.setImageBitmap(DataRepositorySingleton.realImage_1)

        val imageview2: ImageView = findViewById(R.id.imageView2)
        imageview2.setImageBitmap(DataRepositorySingleton.realImage_2)

        val blurButton: Button = findViewById(R.id.blur_button)
        blurButton.text = "BLUR"

    }

    private fun blurImage() {
        DataRepositorySingleton.faceBitmap_1 = convertRectToBitmap(DataRepositorySingleton.faceCrop_1)
        DataRepositorySingleton.faceBitmap_2 = convertRectToBitmap(DataRepositorySingleton.faceCrop_2)
        DataRepositorySingleton.blurImage_1 = bitmapBlur(DataRepositorySingleton.faceBitmap_1, 1.0, 80, DataRepositorySingleton.realImage_1, DataRepositorySingleton.faceCrop_1)
        DataRepositorySingleton.blurImage_2 = bitmapBlur(DataRepositorySingleton.faceBitmap_2, 1.0, 80, DataRepositorySingleton.realImage_2, DataRepositorySingleton.faceCrop_2)
    }

    private fun saveImage() {
        if (buttonPressed) {
            saveMediaToStorage(DataRepositorySingleton.realImage_1)
            saveMediaToStorage(DataRepositorySingleton.realImage_2)
        } else {
            saveMediaToStorage(DataRepositorySingleton.blurImage_1)
            saveMediaToStorage(DataRepositorySingleton.blurImage_2)
        }

    }

    private fun convertRectToBitmap(faceRect: Rect?): Bitmap {
        val xOffset = (faceRect?.centerX() ?: 0) - (faceRect?.width() ?: 0) / 2
        val yOffset = (faceRect?.centerY() ?: 0) - (faceRect?.height() ?: 0) / 2
        val newWidth = 1200
        val newHeight = 1600

        return Bitmap.createBitmap(
            DataRepositorySingleton.realImage_1,
            xOffset,
            yOffset,
            newWidth,
            newHeight
        )
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
            if(countPhotoSaved == 1) {
                val msg = "Both photos saved to Gallery"
                Toast.makeText(this.baseContext, msg, Toast.LENGTH_SHORT).show()
            }
            countPhotoSaved++
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun blurImageShow() {

        val imageview1: ImageView = findViewById(R.id.imageView1)
        imageview1.setImageBitmap(DataRepositorySingleton.blurImage_1)

        val imageview2: ImageView = findViewById(R.id.imageView2)
        imageview2.setImageBitmap(DataRepositorySingleton.blurImage_2)

        val blurButton: Button = findViewById(R.id.blur_button)
        blurButton.text = "UNDO"
    }


    private fun bitmapBlur(sentBitmap: Bitmap, scale: Double, radius: Int, realImage: Bitmap, faceRect: Rect?): Bitmap{
        var sentBitmap = sentBitmap
        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width.toInt(), height.toInt(), false)
        val bitmap = sentBitmap.copy(sentBitmap.config, true)
//        if (radius < 1) {
//            return null
//        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
//        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1


        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }

        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
//        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)

        val mutableBitmap: Bitmap = realImage.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        faceRect?.let { canvas.drawBitmap(bitmap, null, it,null) }
        return mutableBitmap
    }
}