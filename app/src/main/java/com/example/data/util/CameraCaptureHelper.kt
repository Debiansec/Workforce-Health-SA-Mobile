package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraCaptureHelper {

    private const val PHOTOS_FOLDER = "inspection_photos"

    /**
     * Prepares a new file for the camera capture and returns the FileProvider Uri.
     */
    fun createCaptureFile(context: Context): Pair<File, Uri> {
        val storageDir = File(context.filesDir, PHOTOS_FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(storageDir, "SITE_AUDIT_${timeStamp}.jpg")

        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )

        return Pair(photoFile, photoUri)
    }

    /**
     * Generates a sample high-resolution site photo asset directly onto internal storage.
     * Useful for emulator testing or rapid field audit simulation.
     */
    fun createSampleSitePhoto(
        context: Context,
        siteLocation: String,
        category: String = "Ablution Facility Compliance Check"
    ): String {
        val storageDir = File(context.filesDir, PHOTOS_FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(storageDir, "SITE_IMG_${timeStamp}.jpg")

        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background dark industrial slate
        val bgPaint = Paint().apply {
            color = Color.parseColor("#14181F")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Accent top bar
        val barPaint = Paint().apply {
            color = Color.parseColor("#C62828") // WFH Red
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 18f, barPaint)

        // Inner frame card
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1F242D")
            style = Paint.Style.FILL
        }
        val cardRect = RectF(40f, 60f, width - 40f, height - 60f)
        canvas.drawRoundRect(cardRect, 16f, 16f, cardPaint)

        // Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#37474F")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 16f, 16f, borderPaint)

        // Camera badge icon box
        val badgePaint = Paint().apply {
            color = Color.parseColor("#C62828")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(70f, 90f, 220f, 130f), 8f, 8f, badgePaint)

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("SITE PHOTO", 85f, 118f, badgeTextPaint)

        // Timestamp watermark
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val timePaint = Paint().apply {
            color = Color.parseColor("#FFA000") // Gold
            textSize = 18f
            isAntiAlias = true
        }
        canvas.drawText(dateStr, 240f, 118f, timePaint)

        // Title and Site text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val cleanSite = if (siteLocation.length > 36) siteLocation.take(34) + "..." else siteLocation
        canvas.drawText(cleanSite, 70f, 200f, titlePaint)

        val catPaint = Paint().apply {
            color = Color.parseColor("#90A4AE")
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText(category, 70f, 240f, catPaint)

        // Grid lines to simulate architectural/technical photo grid
        val gridPaint = Paint().apply {
            color = Color.parseColor("#263238")
            strokeWidth = 2f
        }
        canvas.drawLine(70f, 300f, width - 70f, 300f, gridPaint)
        canvas.drawLine(70f, 380f, width - 70f, 380f, gridPaint)
        canvas.drawLine(width / 2f, 270f, width / 2f, 420f, gridPaint)

        // Legal compliance verification stamp
        val stampPaint = Paint().apply {
            color = Color.parseColor("#4CAF50")
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("AUDIT EVIDENCE VERIFIED • MHSA ACT 29 / OHS ACT 85", 70f, 480f, stampPaint)

        // GPS coordinates watermark
        val gpsPaint = Paint().apply {
            color = Color.parseColor("#78909C")
            textSize = 16f
            isAntiAlias = true
        }
        canvas.drawText("GEO: -23.8315° S, 30.1628° E (WGS84) • SABS/DMRE SPEC", 70f, 515f, gpsPaint)

        // Save to file
        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()

        return photoFile.absolutePath
    }

    /**
     * Safely deletes a photo file by path.
     */
    fun deletePhotoFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }
}
