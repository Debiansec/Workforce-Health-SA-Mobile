package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.InspectionEntity
import com.example.data.model.ChecklistTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WorkForce Health SA", appName)
    }

    @Test
    fun `verify ablution checklist template questions count and sections`() {
        val questions = ChecklistTemplate.createDefaultAblutionChecklist()

        // Section A (Basins): 5 questions
        val sectionA = questions.filter { it.sectionCode == "A" }
        assertEquals(5, sectionA.size)

        // Section B (Showers): 5 questions
        val sectionB = questions.filter { it.sectionCode == "B" }
        assertEquals(5, sectionB.size)

        // Section C (Toilets): 7 questions
        val sectionC = questions.filter { it.sectionCode == "C" }
        assertEquals(7, sectionC.size)

        // Section D (Urinals): 5 questions
        val sectionD = questions.filter { it.sectionCode == "D" }
        assertEquals(5, sectionD.size)

        // Section E (Lockers): 5 questions
        val sectionE = questions.filter { it.sectionCode == "E" }
        assertEquals(5, sectionE.size)

        // Section F (General): 23 questions
        val sectionF = questions.filter { it.sectionCode == "F" }
        assertEquals(23, sectionF.size)

        // Total standard questions: 50
        assertEquals(50, questions.size)

        // Verify deep cleaning Q20 exists
        val q20 = sectionF.find { it.questionNumber == 20 }
        assertTrue(q20 != null)
        assertTrue(q20!!.questionText.contains("deep-cleaning", ignoreCase = true))

        // Verify routine cleaning Q22 exists
        val q22 = sectionF.find { it.questionNumber == 22 }
        assertTrue(q22 != null)
        assertTrue(q22!!.questionText.contains("daily / weekly / monthly cleaning", ignoreCase = true))
    }

    @Test
    fun `verify Inspection Room entity CRUD operations in DAO`() = runBlocking {
        val dao = db.appDao()

        // 1. CREATE
        val inspection = InspectionEntity(
            siteLocation = "Letaba Crushers Tarentaalrant Plant",
            safetyScore = 91.5,
            date = "2026-10-26",
            inspectorName = "David Nkosi",
            status = "COMPLIANT",
            notes = "Full compliance observed."
        )
        val generatedId = dao.insertInspection(inspection)
        assertTrue(generatedId > 0)

        // 2. READ
        val fetched = dao.getInspectionById(generatedId)
        assertNotNull(fetched)
        assertEquals("Letaba Crushers Tarentaalrant Plant", fetched!!.siteLocation)
        assertEquals(91.5, fetched.safetyScore, 0.001)
        assertEquals("2026-10-26", fetched.date)
        assertEquals("David Nkosi", fetched.inspectorName)
        assertEquals("COMPLIANT", fetched.status)

        // 3. READ ALL Flow
        val allInspections = dao.getAllInspections().first()
        assertEquals(1, allInspections.size)

        // 4. UPDATE
        val updatedInspection = fetched.copy(
            safetyScore = 62.0,
            status = "NON_COMPLIANT",
            notes = "Defective wash tap observed."
        )
        dao.updateInspection(updatedInspection)

        val refetched = dao.getInspectionById(generatedId)
        assertNotNull(refetched)
        assertEquals(62.0, refetched!!.safetyScore, 0.001)
        assertEquals("NON_COMPLIANT", refetched.status)
        assertEquals("Defective wash tap observed.", refetched.notes)

        // 5. QUERY BY STATUS
        val nonCompliantList = dao.getInspectionsByStatus("NON_COMPLIANT").first()
        assertEquals(1, nonCompliantList.size)
        val compliantList = dao.getInspectionsByStatus("COMPLIANT").first()
        assertEquals(0, compliantList.size)

        // 6. DELETE
        dao.deleteInspectionById(generatedId)
        val deleted = dao.getInspectionById(generatedId)
        assertNull(deleted)
    }

    @Test
    fun `verify CSV export generation and formatting`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inspections = listOf(
            InspectionEntity(
                id = 101,
                siteLocation = "Afrimat Kuipersbult Quarry",
                safetyScore = 88.5,
                date = "2026-10-27",
                inspectorName = "David Nkosi",
                status = "COMPLIANT",
                notes = "All systems operational and disinfected."
            ),
            InspectionEntity(
                id = 102,
                siteLocation = "Palabora Smelter Main Depot",
                safetyScore = 58.0,
                date = "2026-10-28",
                inspectorName = "Dr. K. Naidoo",
                status = "NON_COMPLIANT",
                notes = "Requires ventilation duct cleaning, fan repair."
            )
        )

        val csvFile = com.example.data.util.InspectionExportHelper.exportInspectionsToCsv(context, inspections)
        assertTrue(csvFile.exists())
        assertTrue(csvFile.length() > 0)

        val content = csvFile.readText()
        assertTrue(content.contains("Inspection ID,Site Location,Safety Score (%),Date,Inspector Name,Status,Observations & Notes"))
        assertTrue(content.contains("101,\"Afrimat Kuipersbult Quarry\",88.5,\"2026-10-27\",\"David Nkosi\",\"COMPLIANT\""))
        assertTrue(content.contains("102,\"Palabora Smelter Main Depot\",58.0,\"2026-10-28\",\"Dr. K. Naidoo\",\"NON_COMPLIANT\""))
    }

    @Test
    fun `verify PDF document generation creates valid non-empty file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inspection = InspectionEntity(
            id = 501,
            siteLocation = "Tarentaalrant Quarry & Plant",
            safetyScore = 95.0,
            date = "2026-10-29",
            inspectorName = "David Nkosi",
            status = "COMPLIANT",
            notes = "Full statutory compliance verified with DMRE standard."
        )

        val pdfFile = com.example.data.util.InspectionExportHelper.generateInspectionPdf(context, inspection)
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 500)
        assertTrue(pdfFile.name.endsWith(".pdf"))
    }

    @Test
    fun `verify safety score trend metrics, chronological ordering, and 75 percent threshold calculations`() {
        val testInspections = listOf(
            InspectionEntity(1, "Afrimat Kuipersbult Quarry", 65.0, "2026-10-20", "David Nkosi", "PENDING"),
            InspectionEntity(2, "Palabora Smelter Main Depot", 90.0, "2026-10-25", "Dr. K. Naidoo", "COMPLIANT"),
            InspectionEntity(3, "Afrimat Kuipersbult Quarry", 80.0, "2026-10-22", "David Nkosi", "COMPLIANT"),
            InspectionEntity(4, "Letaba Crushers", 55.0, "2026-10-18", "T. Baloyi", "NON_COMPLIANT"),
            InspectionEntity(5, "Letaba Crushers", 85.0, "2026-10-28", "T. Baloyi", "COMPLIANT")
        )

        // 1. Chronological Ordering
        val chronological = testInspections.sortedBy { it.date }
        assertEquals("2026-10-18", chronological.first().date)
        assertEquals("2026-10-28", chronological.last().date)

        // 2. Average Safety Score
        val avgScore = chronological.map { it.safetyScore }.average()
        assertEquals(75.0, avgScore, 0.01)

        // 3. Statutory 75% Pass Rate (Scores >= 75.0: 90, 80, 85 = 3 out of 5 -> 60%)
        val compliantCount = chronological.count { it.safetyScore >= 75.0 }
        val passRate = (compliantCount.toDouble() / chronological.size) * 100.0
        assertEquals(3, compliantCount)
        assertEquals(60.0, passRate, 0.01)

        // 4. Site-specific filtering for Afrimat Kuipersbult Quarry
        val afrimatOnly = chronological.filter { it.siteLocation.contains("Afrimat Kuipersbult") }
        assertEquals(2, afrimatOnly.size)
        val afrimatAvg = afrimatOnly.map { it.safetyScore }.average()
        assertEquals(72.5, afrimatAvg, 0.01)
    }

    @Test
    fun `verify RECORD_AUDIO permission declared in AndroidManifest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS
        )
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        assertTrue("RECORD_AUDIO permission must be declared in AndroidManifest.xml", permissions.contains("android.permission.RECORD_AUDIO"))
    }

    @Test
    fun `verify CAMERA permission declared in AndroidManifest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS
        )
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        assertTrue("CAMERA permission must be declared in AndroidManifest.xml", permissions.contains("android.permission.CAMERA"))
    }

    @Test
    fun `verify inspection site photos attached directly to Room database entity`() = runBlocking {
        val dao = db.appDao()

        // Create inspection with 2 attached photos
        val testPhotos = "/data/user/0/com.example/files/inspection_photos/photo1.jpg|/data/user/0/com.example/files/inspection_photos/photo2.jpg"
        val inspection = InspectionEntity(
            siteLocation = "Kuipersbult Plant Ablution Block",
            safetyScore = 88.0,
            date = "2026-10-29",
            inspectorName = "David Nkosi",
            status = "COMPLIANT",
            notes = "Photos attached of washing basins and disinfected urinals.",
            photoUris = testPhotos
        )

        val id = dao.insertInspection(inspection)
        val fetched = dao.getInspectionById(id)
        assertNotNull(fetched)
        assertEquals(2, fetched!!.getPhotoList().size)
        assertEquals("/data/user/0/com.example/files/inspection_photos/photo1.jpg", fetched.getPhotoList()[0])
        assertEquals("/data/user/0/com.example/files/inspection_photos/photo2.jpg", fetched.getPhotoList()[1])

        // Add 3rd photo using helper
        val updatedWith3rd = fetched.withPhotoAdded("/data/user/0/com.example/files/inspection_photos/photo3.jpg")
        dao.updateInspection(updatedWith3rd)

        val refetched = dao.getInspectionById(id)
        assertNotNull(refetched)
        assertEquals(3, refetched!!.getPhotoList().size)

        // Remove 1st photo
        val updatedRemoved = refetched.withPhotoRemoved("/data/user/0/com.example/files/inspection_photos/photo1.jpg")
        dao.updateInspection(updatedRemoved)

        val finalCheck = dao.getInspectionById(id)
        assertNotNull(finalCheck)
        assertEquals(2, finalCheck!!.getPhotoList().size)
        assertTrue(finalCheck.getPhotoList().contains("/data/user/0/com.example/files/inspection_photos/photo3.jpg"))
        assertTrue(!finalCheck.getPhotoList().contains("/data/user/0/com.example/files/inspection_photos/photo1.jpg"))
    }

    @Test
    fun `verify CameraCaptureHelper creates photo files and FileProvider Uris`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (file, uri) = com.example.data.util.CameraCaptureHelper.createCaptureFile(context)

        assertNotNull(file)
        assertNotNull(uri)
        assertTrue(file.name.startsWith("SITE_AUDIT_"))
        assertTrue(file.name.endsWith(".jpg"))
        assertTrue(uri.toString().contains("fileprovider"))

        // Verify sample photo generation
        val samplePath = com.example.data.util.CameraCaptureHelper.createSampleSitePhoto(
            context = context,
            siteLocation = "Letaba Crushers",
            category = "Basin Inspection"
        )
        val sampleFile = java.io.File(samplePath)
        assertTrue(sampleFile.exists())
        assertTrue(sampleFile.length() > 1000) // Non-empty JPEG

        // Clean up
        sampleFile.delete()
        file.delete()
    }
}

