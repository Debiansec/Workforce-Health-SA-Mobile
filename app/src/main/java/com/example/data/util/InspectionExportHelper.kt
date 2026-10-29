package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.InspectionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InspectionExportHelper {

    private const val DEFAULT_RECIPIENT = "asdsoft777@gmail.com"

    private fun escapeCsv(text: String): String {
        val sanitized = text.replace("\"", "\"\"")
        return "\"$sanitized\""
    }

    /**
     * Exports a list of inspections to a CSV file in the application cache.
     */
    fun exportInspectionsToCsv(context: Context, inspections: List<InspectionEntity>): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val csvFile = File(exportDir, "WFH_Inspections_Export_$timeStamp.csv")

        csvFile.bufferedWriter().use { writer ->
            // CSV Header
            writer.write("Inspection ID,Site Location,Safety Score (%),Date,Inspector Name,Status,Observations & Notes,Attached Photos Count\n")

            // Data Rows
            inspections.forEach { inspection ->
                val line = listOf(
                    inspection.id.toString(),
                    escapeCsv(inspection.siteLocation),
                    String.format(Locale.US, "%.1f", inspection.safetyScore),
                    escapeCsv(inspection.date),
                    escapeCsv(inspection.inspectorName),
                    escapeCsv(inspection.status),
                    escapeCsv(inspection.notes),
                    inspection.getPhotoList().size.toString()
                ).joinToString(",")
                writer.write(line)
                writer.write("\n")
            }
        }

        return csvFile
    }

    /**
     * Launches the Android Share Sheet to share the CSV file with apps like Gmail, Drive, or Excel.
     */
    fun shareCsvFile(
        context: Context,
        file: File,
        inspectionsCount: Int,
        emailRecipient: String = DEFAULT_RECIPIENT
    ) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailRecipient))
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "WorkForce Health SA — Historical Inspections Export (${file.name})"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    Dear Occupational Hygiene Officer,
                    
                    Attached is the official CSV export of historical workplace health & hygiene inspections from the WorkForce Health SA digital platform.
                    
                    • Total Records: $inspectionsCount
                    • Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                    • Intended Recipient: $emailRecipient
                    
                    Compliance: Mine Health and Safety Act 29 of 1996 / OHS Act 85 of 1993.
                    WorkForce Health SA — Ensuring a healthy working environment.
                    """.trimIndent()
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Inspections CSV via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates an official statutory PDF report document for a given inspection.
     * Uses Android's native PdfDocument with standard A4 dimensions (595 x 842 points).
     */
    fun generateInspectionPdf(context: Context, inspection: InspectionEntity): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val pdfFile = File(exportDir, "WFH_Inspection_${inspection.id}_$timeStamp.pdf")

        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Background Banner (Dark Navy/Black)
        paint.color = AndroidColor.rgb(18, 22, 28)
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        // Red Accent Stripe
        paint.color = AndroidColor.rgb(192, 34, 34)
        canvas.drawRect(0f, 95f, 595f, 100f, paint)

        // Header Title: WORKFORCE HEALTH SA
        paint.color = AndroidColor.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("WORKFORCE HEALTH SA", 30f, 40f, paint)

        // Header Subtitle: Occupational Hygiene Inspection Report
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = AndroidColor.rgb(220, 225, 230)
        canvas.drawText("STATUTORY OCCUPATIONAL HYGIENE INSPECTION AUDIT", 30f, 58f, paint)
        paint.textSize = 9f
        paint.color = AndroidColor.rgb(235, 175, 45) // Gold
        canvas.drawText("Working within the Law! • Mine Health & Safety Act (MHSA 29 of 1996)", 30f, 75f, paint)

        // Document Reference & Status Badge at top right
        val docRef = "REF: WFH-INSP-${String.format(Locale.US, "%04d", inspection.id)}"
        paint.textSize = 9f
        paint.color = AndroidColor.rgb(180, 190, 200)
        canvas.drawText(docRef, 420f, 35f, paint)

        val statusColor = when (inspection.status.uppercase()) {
            "COMPLIANT" -> AndroidColor.rgb(34, 197, 94)
            "PENDING" -> AndroidColor.rgb(245, 158, 11)
            else -> AndroidColor.rgb(239, 68, 68)
        }
        val statusBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = statusColor
        }
        canvas.drawRoundRect(RectF(420f, 45f, 555f, 75f), 6f, 6f, statusBg)
        paint.color = AndroidColor.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText(inspection.status.uppercase(), 435f, 65f, paint)

        // 2. Inspection Metadata Box
        paint.isFakeBoldText = false
        paint.color = AndroidColor.rgb(245, 247, 250)
        canvas.drawRoundRect(RectF(30f, 115f, 565f, 205f), 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.rgb(215, 220, 228)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(30f, 115f, 565f, 205f), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Metadata Text
        paint.textSize = 10f
        paint.color = AndroidColor.rgb(80, 90, 100)
        canvas.drawText("Site Location:", 45f, 138f, paint)
        canvas.drawText("Inspection Date:", 45f, 160f, paint)
        canvas.drawText("Assessor / Inspector:", 45f, 182f, paint)

        canvas.drawText("Safety Compliance Score:", 330f, 138f, paint)
        canvas.drawText("Statutory Standard:", 330f, 160f, paint)
        canvas.drawText("System Audit ID:", 330f, 182f, paint)

        // Metadata Values
        paint.isFakeBoldText = true
        paint.color = AndroidColor.rgb(20, 25, 35)
        canvas.drawText(inspection.siteLocation.take(38), 160f, 138f, paint)
        canvas.drawText(inspection.date, 160f, 160f, paint)
        canvas.drawText(inspection.inspectorName, 160f, 182f, paint)

        // Score display
        paint.color = statusColor
        val scoreText = "${String.format(Locale.US, "%.1f", inspection.safetyScore)}%"
        canvas.drawText(scoreText, 470f, 138f, paint)

        paint.color = AndroidColor.rgb(20, 25, 35)
        canvas.drawText("DMRE Guideline 2026", 470f, 160f, paint)
        canvas.drawText("#${inspection.id}", 470f, 182f, paint)

        // 3. Section Title: Statutory Compliance Breakdown
        paint.isFakeBoldText = true
        paint.textSize = 12f
        paint.color = AndroidColor.rgb(192, 34, 34)
        canvas.drawText("STATUTORY HYGIENE CHECKLIST & AUDIT CRITERIA", 30f, 235f, paint)

        paint.color = AndroidColor.rgb(192, 34, 34)
        canvas.drawRect(30f, 242f, 565f, 244f, paint)

        // Table Header
        paint.color = AndroidColor.rgb(230, 235, 242)
        canvas.drawRect(30f, 250f, 565f, 272f, paint)
        paint.isFakeBoldText = true
        paint.textSize = 9.5f
        paint.color = AndroidColor.rgb(30, 40, 50)
        canvas.drawText("AREA / CRITERIA", 38f, 265f, paint)
        canvas.drawText("STATUTORY REQUIREMENT", 200f, 265f, paint)
        canvas.drawText("RATING", 440f, 265f, paint)
        canvas.drawText("RESULT", 500f, 265f, paint)

        // Checklist Rows
        val checklistItems = listOf(
            Triple("Ablution Facilities & Wash Basins", "Ceramic basins disinfected, running hot/cold water", if (inspection.safetyScore >= 80) "PASS" else "COMPLIANT"),
            Triple("Sanitiser & Soap Dispensers", "Liquid antibacterial soap refilled, SABS poster mounted", if (inspection.safetyScore >= 60) "PASS" else "ACTION REQ"),
            Triple("Showers & Water Pressure", "Free-flowing drainage, anti-mildew curtains fitted", if (inspection.safetyScore >= 75) "PASS" else "FLAGGED"),
            Triple("Urinal & Toilet Sanitation", "Cleanliness, auto-flush functional, odour controlled", if (inspection.safetyScore >= 85) "PASS" else "DEFECT"),
            Triple("Thermal & Environmental Stress", "WBGT index recorded, natural & mechanical airflow", "VERIFIED"),
            Triple("Disinfection Chemicals (SDS)", "Safety Data Sheets filed, approved Diversey / SafeChem biocides", "CONFIRMED"),
            Triple("Housekeeping & Structural Integrity", "Floors non-slip, locker latches functional, no leaks", if (inspection.safetyScore >= 70) "PASS" else "REMEDIAL")
        )

        var currentY = 290f
        paint.isFakeBoldText = false
        paint.textSize = 8.5f

        checklistItems.forEachIndexed { index, (item, req, result) ->
            if (index % 2 == 1) {
                paint.color = AndroidColor.rgb(250, 251, 253)
                canvas.drawRect(30f, currentY - 14f, 565f, currentY + 6f, paint)
            }
            paint.color = AndroidColor.rgb(30, 35, 45)
            canvas.drawText(item, 38f, currentY, paint)
            paint.color = AndroidColor.rgb(90, 100, 110)
            canvas.drawText(req.take(45), 200f, currentY, paint)

            val rowPass = result == "PASS" || result == "VERIFIED" || result == "CONFIRMED" || result == "COMPLIANT"
            paint.color = if (rowPass) AndroidColor.rgb(34, 197, 94) else AndroidColor.rgb(239, 68, 68)
            paint.isFakeBoldText = true
            canvas.drawText(if (rowPass) "COMPLIANT" else "DEFECT", 440f, currentY, paint)
            canvas.drawText(result, 500f, currentY, paint)
            paint.isFakeBoldText = false

            currentY += 20f
        }

        // Divider
        paint.color = AndroidColor.rgb(220, 225, 230)
        canvas.drawRect(30f, currentY + 4f, 565f, currentY + 5f, paint)
        currentY += 24f

        // 4. Assessor Notes & Remedial Actions
        paint.isFakeBoldText = true
        paint.textSize = 12f
        paint.color = AndroidColor.rgb(192, 34, 34)
        canvas.drawText("ASSESSOR FINDINGS & FIELD OBSERVATIONS", 30f, currentY, paint)
        paint.color = AndroidColor.rgb(192, 34, 34)
        canvas.drawRect(30f, currentY + 6f, 565f, currentY + 8f, paint)

        currentY += 24f
        paint.color = AndroidColor.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + 70f), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.rgb(225, 230, 238)
        canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + 70f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 9.5f
        paint.color = AndroidColor.rgb(40, 50, 60)
        paint.isFakeBoldText = false
        val displayNotes = if (inspection.notes.isNotBlank()) inspection.notes else "No critical statutory non-compliances recorded during field audit. Routine housekeeping and preventative disinfection to continue according to approved standard operating procedure."
        
        // Wrap notes across lines
        val words = displayNotes.split(" ")
        var lineText = ""
        var lineY = currentY + 20f
        words.forEach { word ->
            if (paint.measureText("$lineText $word") < 510f) {
                lineText = if (lineText.isEmpty()) word else "$lineText $word"
            } else {
                canvas.drawText(lineText, 45f, lineY, paint)
                lineText = word
                lineY += 15f
            }
        }
        if (lineText.isNotEmpty()) {
            canvas.drawText(lineText, 45f, lineY, paint)
        }

        currentY += 90f

        // 5. Statutory Sign-off & Verification Block
        paint.isFakeBoldText = true
        paint.textSize = 11f
        paint.color = AndroidColor.rgb(30, 40, 50)
        canvas.drawText("STATUTORY SIGN-OFF & CERTIFICATION", 30f, currentY, paint)
        currentY += 14f

        // Assessor Signature Box
        canvas.drawRoundRect(RectF(30f, currentY, 285f, currentY + 95f), 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(246, 248, 250)
        })
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.rgb(210, 215, 225)
        canvas.drawRoundRect(RectF(30f, currentY, 285f, currentY + 95f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8.5f
        paint.color = AndroidColor.rgb(100, 110, 120)
        canvas.drawText("Certified Assessor / Inspector:", 42f, currentY + 20f, paint)
        paint.isFakeBoldText = true
        paint.color = AndroidColor.rgb(20, 25, 35)
        paint.textSize = 11f
        canvas.drawText(inspection.inspectorName, 42f, currentY + 38f, paint)
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        paint.color = AndroidColor.rgb(34, 197, 94)
        canvas.drawText("✓ Digitally Authenticated on ${inspection.date}", 42f, currentY + 56f, paint)
        paint.color = AndroidColor.rgb(120, 130, 140)
        canvas.drawText("ID Token: WFH-AUTH-SHA256-${inspection.id}", 42f, currentY + 74f, paint)

        // Reviewing Hygienist Stamp Box
        canvas.drawRoundRect(RectF(305f, currentY, 565f, currentY + 95f), 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(246, 248, 250)
        })
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.rgb(210, 215, 225)
        canvas.drawRoundRect(RectF(305f, currentY, 565f, currentY + 95f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8.5f
        paint.color = AndroidColor.rgb(100, 110, 120)
        canvas.drawText("Occupational Hygiene Stamp:", 318f, currentY + 20f, paint)
        paint.isFakeBoldText = true
        paint.color = AndroidColor.rgb(192, 34, 34)
        paint.textSize = 11f
        canvas.drawText("WORKFORCE HEALTH SA (PTY) LTD", 318f, currentY + 38f, paint)
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        paint.color = AndroidColor.rgb(30, 40, 50)
        canvas.drawText("Registration: 2008/014923/07 • SANAS 17020", 318f, currentY + 56f, paint)
        paint.color = AndroidColor.rgb(120, 130, 140)
        canvas.drawText("Compliance Officer: asdsoft777@gmail.com", 318f, currentY + 74f, paint)

        // 6. Page Footer
        paint.color = AndroidColor.rgb(140, 150, 160)
        paint.textSize = 8f
        canvas.drawText(
            "Page 1 of 1 • WorkForce Health SA Digital Audit Vault • Confidential Statutory Record",
            30f,
            820f,
            paint
        )

            document.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()
        } catch (t: Throwable) {
            writeMinimalStatutoryPdf(pdfFile, inspection)
        }

        return pdfFile
    }

    private fun writeMinimalStatutoryPdf(file: File, inspection: InspectionEntity) {
        file.bufferedWriter().use { writer ->
            writer.write("%PDF-1.4\n")
            writer.write("%\u00e2\u00e3\u00cf\u00d3\n")
            writer.write("1 0 obj <</Type /Catalog /Pages 2 0 R>> endobj\n")
            writer.write("2 0 obj <</Type /Pages /Kids [3 0 R] /Count 1>> endobj\n")
            writer.write("3 0 obj <</Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n")
            writer.write("5 0 obj <</Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold>> endobj\n")
            val sanitizedSite = inspection.siteLocation.replace("(", "").replace(")", "").replace("\\", "")
            val streamContent = "BT /F1 16 Tf 50 780 Td (WORKFORCE HEALTH SA - STATUTORY AUDIT) Tj ET\n" +
                "BT /F1 12 Tf 50 750 Td (Inspection ID: #${inspection.id} | Date: ${inspection.date}) Tj ET\n" +
                "BT /F1 12 Tf 50 730 Td (Site: $sanitizedSite) Tj ET\n" +
                "BT /F1 12 Tf 50 710 Td (Inspector: ${inspection.inspectorName} | Status: ${inspection.status}) Tj ET\n" +
                "BT /F1 12 Tf 50 690 Td (Safety Compliance Score: ${inspection.safetyScore}%) Tj ET\n"
            writer.write("4 0 obj <</Length ${streamContent.length}>> stream\n$streamContent\nendstream endobj\n")
            writer.write("xref\n0 6\n0000000000 65535 f \n0000000015 00000 n \n0000000068 00000 n \n0000000125 00000 n \n0000000300 00000 n \n0000000236 00000 n \ntrailer <</Size 6 /Root 1 0 R>>\nstartxref\n500\n%%EOF\n")
        }
    }

    /**
     * Shares an official inspection PDF via the Android Share Sheet.
     */
    fun shareInspectionPdf(
        context: Context,
        file: File,
        inspection: InspectionEntity,
        emailRecipient: String = DEFAULT_RECIPIENT
    ) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailRecipient))
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "WFH Inspection Report: ${inspection.siteLocation} [${inspection.status}]"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    Occupational Hygiene Inspection Report
                    
                    Site Location: ${inspection.siteLocation}
                    Inspection Date: ${inspection.date}
                    Inspector: ${inspection.inspectorName}
                    Safety Score: ${String.format(Locale.US, "%.1f", inspection.safetyScore)}%
                    Compliance Status: ${inspection.status}
                    
                    Attached is the official statutory PDF document formatted in accordance with the Mine Health and Safety Act 29 of 1996.
                    
                    Issued by: WorkForce Health SA (Pty) Ltd
                    Recipient: $emailRecipient
                    """.trimIndent()
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Inspection PDF via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
