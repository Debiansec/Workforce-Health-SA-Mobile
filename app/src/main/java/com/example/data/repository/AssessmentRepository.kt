package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AssessmentRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.appDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory Customer & Site register (with reference customer from prompt)
    private val _customers = MutableStateFlow<List<Customer>>(
        listOf(
            Customer(
                id = "cust_letaba",
                name = "Letaba Crushers (Pty) Ltd",
                legalEntity = "Letaba Crushers Proprietary Limited",
                parentCompany = "Raubex Group Mining Division",
                vatNumber = "4810239482",
                cipcRegistration = "2008/014923/07",
                miningRightRef = "LP30/5/1/2/2/1004MR",
                waterLicenceRef = "04/B81B/ABEGJ/1982"
            ),
            Customer(
                id = "cust_afrimat",
                name = "Afrimat CM North (Pty) Ltd",
                legalEntity = "Afrimat CM North (Pty) Ltd",
                parentCompany = "Afrimat Ltd",
                vatNumber = "4910205428",
                cipcRegistration = "2002/025895/07",
                miningRightRef = "LP30/5/1/2/2/10023MR",
                waterLicenceRef = "01/A42J/AGI/4378"
            ),
            Customer(
                id = "cust_palabora",
                name = "Palabora Mining Company Ltd",
                legalEntity = "Palabora Copper (Pty) Ltd",
                parentCompany = "HBIS Group",
                vatNumber = "4020119384",
                cipcRegistration = "1956/002134/06",
                miningRightRef = "LP30/5/1/2/2/00091MR",
                waterLicenceRef = "B81J/04/1990"
            )
        )
    )
    val customers = _customers.asStateFlow()

    private val _sites = MutableStateFlow<List<CustomerSite>>(
        listOf(
            CustomerSite(
                id = "site_letaba_01",
                customerId = "cust_letaba",
                customerName = "Letaba Crushers (Pty) Ltd",
                siteName = "Tarentaalrant Quarry & Plant",
                propertyDescription = "Farm Tarentaalrant 24LT",
                location = "Tzaneen, Limpopo Province",
                province = "Limpopo",
                dmeMineCode = "14466",
                mineManager = "J. Van Der Merwe (+27 82 456 7890)",
                mineManagerContact = "j.vdmerwe@letabacrushers.co.za",
                siteOccupationalHygienist = "Dr. K. Naidoo (OH-SA-8821)",
                occupationalMedicalPractitioner = "Dr. S. Mokoena (OMP-4102)",
                gpsCoordinates = "-23.8315° S, 30.1628° E"
            ),
            CustomerSite(
                id = "site_letaba_02",
                customerId = "cust_letaba",
                customerName = "Letaba Crushers (Pty) Ltd",
                siteName = "Secondary Crushing & Screening Depot",
                propertyDescription = "Portion 4 of Farm Riverside 528LT",
                location = "Letsitele Valley, Limpopo Province",
                province = "Limpopo",
                dmeMineCode = "14467",
                mineManager = "P. Smit (+27 83 234 5678)",
                mineManagerContact = "p.smit@letabacrushers.co.za",
                siteOccupationalHygienist = "Dr. K. Naidoo (OH-SA-8821)",
                occupationalMedicalPractitioner = "Dr. S. Mokoena (OMP-4102)",
                gpsCoordinates = "-23.8642° S, 30.2911° E"
            ),
            CustomerSite(
                id = "site_afrimat_kuipersbult",
                customerId = "cust_afrimat",
                customerName = "Afrimat CM North (Pty) Ltd",
                siteName = "Kuipersbult Quarry & Ready-Mix Plant",
                propertyDescription = "Remainder of the farm Kuipersbult 511 LQ",
                location = "Lephalale, Limpopo Province",
                province = "Limpopo",
                dmeMineCode = "26001",
                mineManager = "Mr P K Lekgeu (+27 60 742 8208)",
                mineManagerContact = "peter.lekgeu@afrimat.co.za",
                siteOccupationalHygienist = "Mr N B Letlape (ChiefOH@workforcehealthsa.co.za)",
                occupationalMedicalPractitioner = "Dr K O Sebopa (+27 82 925 1158)",
                gpsCoordinates = "-23.70760° S, 27.57939° E"
            )
        )
    )
    val sites = _sites.asStateFlow()

    // Facilities for sites
    val facilities = listOf(
        "Main Change House — Male Ablution Block",
        "Secondary Plant Change House — Male",
        "Kuipersbult Quarry Workshop Ablution Facility",
        "Ready-Mix Batching Plant Restrooms",
        "Administration Building Restrooms",
        "Weighbridge Restroom & Shower"
    )

    // Customer Operational File (from WFH-AFR-KUI-OD-26-001)
    private val _customerOperationalFiles = MutableStateFlow<Map<String, CustomerOperationalFile>>(
        mapOf("cust_afrimat" to CustomerFileData.AFRIMAT_FILE)
    )
    val customerOperationalFiles = _customerOperationalFiles.asStateFlow()


    // Current user profile
    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser = _currentUser.asStateFlow()

    // Live Trip Tracking
    private val _currentTrip = MutableStateFlow<SiteTrip?>(null)
    val currentTrip = _currentTrip.asStateFlow()

    // Network & Sync State
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        scope.launch {
            seedInitialDatabaseIfNeeded()
        }
    }

    private suspend fun seedInitialDatabaseIfNeeded() {
        val count = dao.getAllAssessments().first().size
        if (count == 0) {
            // Seed Equipment
            val equip = listOf(
                EquipmentEntity(
                    id = "eq_01",
                    name = "WBGT Logger AZ 87786",
                    manufacturer = "AZ Instrument Corp",
                    model = "AZ 87786",
                    serialNumber = "AZ-2024-881",
                    assetIdentifier = "WFH-ENV-014",
                    calibrationDate = "2026-05-10",
                    calibrationExpiryDate = "2026-11-10",
                    status = "Calibrated",
                    isWarning = false
                ),
                EquipmentEntity(
                    id = "eq_02",
                    name = "Kestrel 4000 Weather Tracker",
                    manufacturer = "Nielsen-Kellerman",
                    model = "Kestrel 4000 Pocket Tracker",
                    serialNumber = "KEST-9932",
                    assetIdentifier = "WFH-ENV-022",
                    calibrationDate = "2026-06-14",
                    calibrationExpiryDate = "2026-12-14",
                    status = "Calibrated",
                    isWarning = false
                ),
                EquipmentEntity(
                    id = "eq_03",
                    name = "Digital Lux Meter LX-1330B",
                    manufacturer = "Dr. Meter",
                    model = "LX-1330B",
                    serialNumber = "LX-8041",
                    assetIdentifier = "WFH-OPT-008",
                    calibrationDate = "2026-04-20",
                    calibrationExpiryDate = "2027-01-10",
                    status = "Calibrated",
                    isWarning = false
                ),
                EquipmentEntity(
                    id = "eq_04",
                    name = "Quest Sound Level Meter 2900",
                    manufacturer = "3M Quest Technologies",
                    model = "SoundPro DL-2",
                    serialNumber = "QST-4412",
                    assetIdentifier = "WFH-ACU-003",
                    calibrationDate = "2026-02-15",
                    calibrationExpiryDate = "2026-11-01",
                    status = "Calibration Due Soon",
                    isWarning = true
                )
            )
            dao.insertEquipmentList(equip)

            // Seed Sample Initial Assessment for Letaba Crushers
            val sampleAssessmentId = "ASM-2026-LET-001"
            val initialAssessment = AssessmentEntity(
                id = sampleAssessmentId,
                templateTitle = "Monthly ablution facilities checklist *Male",
                customerId = "cust_letaba",
                customerName = "Letaba Crushers (Pty) Ltd",
                siteId = "site_letaba_01",
                siteName = "Tarentaalrant Quarry & Plant",
                facilityName = "Main Change House — Male Ablution Block",
                dmeMineCode = "14466",
                mineManager = "J. Van Der Merwe",
                mineManagerContact = "+27 82 456 7890",
                siteOccupationalHygienist = "Dr. K. Naidoo (OH-SA-8821)",
                occupationalMedicalPractitioner = "Dr. S. Mokoena (OMP-4102)",
                assessorName = "David Nkosi",
                assessmentType = "Monthly Compliance Inspection",
                documentReference = "WFH-LET-ABL-2026-10",
                fieldVisitDate = "2026-10-24",
                previousVisitDate = "2026-09-22",
                previousRemedialActions = "Fixed showerhead #3, replaced rusted locker latches in bay B.",
                hygienistSpecialInstructions = "Verify deep sanitisation certificate and check for mould behind urinals.",
                status = AssessmentStatus.IN_PROGRESS.name,
                progressPercent = 85,
                assessorSignature = "Signed by David Nkosi (2026-10-24 14:30)",
                reviewerSignature = null,
                reviewComments = null,
                createdTimestamp = System.currentTimeMillis() - 86400000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSynced = true
            )
            dao.insertAssessment(initialAssessment)

            // Seed answers with realistic occupational hygiene data
            val defaultQuestions = ChecklistTemplate.createDefaultAblutionChecklist()
            val answerEntities = defaultQuestions.map { q ->
                val (resp, rem) = when (q.id) {
                    "A_1" -> Pair(ComplianceResponse.YES.name, "All 4 ceramic basins washed and disinfected.")
                    "A_2" -> Pair(ComplianceResponse.YES.name, "Dispensers topped up with HandSan liquid soap.")
                    "A_3" -> Pair(ComplianceResponse.YES.name, "Adequate pressure, no leaks observed.")
                    "A_4" -> Pair(ComplianceResponse.YES.name, "Laminated SABS hand washing poster mounted above basins.")
                    "A_5" -> Pair(ComplianceResponse.NO.name, "Trace mildew starting around silicon sealant on Basin 2.")

                    "B_1" -> Pair(ComplianceResponse.YES.name, "Shower tiles scrubbed, drainage running free.")
                    "B_2" -> Pair(ComplianceResponse.YES.name, "Hot and cold mixer taps shutting tightly.")
                    "B_3" -> Pair(ComplianceResponse.YES.name, "Water-saving flow restrictor heads fitted.")
                    "B_4" -> Pair(ComplianceResponse.NO.name, "Shower curtain #2 torn at lower hem, needs replacement.")
                    "B_5" -> Pair(ComplianceResponse.YES.name, "No mould detected on floor or wall grout.")

                    "C_1" -> Pair(ComplianceResponse.YES.name, "Toilets spotless, floor dried with mop.")
                    "C_2" -> Pair(ComplianceResponse.YES.name, "Heavy duty polypropylene lids intact.")
                    "C_3" -> Pair(ComplianceResponse.YES.name, "All seats securely bolted.")
                    "C_4" -> Pair(ComplianceResponse.YES.name, "Dual-flush cisterns operational.")
                    "C_5" -> Pair(ComplianceResponse.YES.name, "3 reserve rolls in dispenser.")
                    "C_6" -> Pair(ComplianceResponse.YES.name, "Cubicle latches and indicator locks work.")
                    "C_7" -> Pair(ComplianceResponse.YES.name, "No moulds present.")

                    "D_1" -> Pair(ComplianceResponse.YES.name, "Stainless steel trough clean.")
                    "D_2" -> Pair(ComplianceResponse.YES.name, "Auto-flush sensor triggers every 8 mins.")
                    "D_3" -> Pair(ComplianceResponse.YES.name, "Tiled floor dry and non-slip.")
                    "D_4" -> Pair(ComplianceResponse.YES.name, "Ventilation fan prevents odours.")
                    "D_5" -> Pair(ComplianceResponse.YES.name, "Clean.")

                    "E_1" -> Pair(ComplianceResponse.YES.name, "48 lockers assigned to shift complement.")
                    "E_2" -> Pair(ComplianceResponse.YES.name, "Numbered tags 01-48 affixed.")
                    "E_3" -> Pair(ComplianceResponse.YES.name, "Doors aligned.")
                    "E_4" -> Pair(ComplianceResponse.YES.name, "Padlock hasps functional.")
                    "E_5" -> Pair(ComplianceResponse.YES.name, "Locker alley cleared of obstacles.")

                    "F_6" -> Pair(ComplianceResponse.YES.name, "Deep sanitisation frequency: Bi-monthly by CleanBio Services.")
                    "F_7" -> Pair(ComplianceResponse.YES.name, "Certificate #CB-8819 dated 2026-10-02 sighted.")
                    "F_8" -> Pair(ComplianceResponse.YES.name, "Daily cleaning schedule displayed on noticeboard.")
                    "F_9" -> Pair(ComplianceResponse.YES.name, "Signed twice daily by supervisor.")
                    "F_16" -> Pair(ComplianceResponse.YES.name, "Ventilation report #VR-2026-04 dated 2026-04-12.")
                    "F_17" -> Pair(ComplianceResponse.YES.name, "Illumination report #IR-2026-08, avg 180 Lux.")
                    "F_19" -> Pair(ComplianceResponse.YES.name, "Approx 42 workers on day shift.")
                    "F_20" -> Pair(ComplianceResponse.YES.name, "Procedure SOP-CL-04 obtained; biocide and quaternary ammonium used.")
                    "F_22" -> Pair(ComplianceResponse.YES.name, "Pine disinfectant, toilet cleaner, HandSan soap with valid SDS.")
                    else -> Pair(ComplianceResponse.YES.name, "Inspected and compliant.")
                }
                AnswerEntity(
                    assessmentId = sampleAssessmentId,
                    questionId = q.id,
                    sectionCode = q.sectionCode,
                    sectionTitle = q.sectionTitle,
                    questionNumber = q.questionNumber,
                    questionText = q.questionText,
                    response = resp,
                    remarks = rem,
                    photoUri = if (q.id == "B_4" || q.id == "A_5") "demo_evidence_photo" else null,
                    geoCoordinates = "-23.8315° S, 30.1628° E",
                    timestamp = System.currentTimeMillis()
                )
            }
            dao.insertAnswers(answerEntities)

            // Seed Chemical Records
            val chemicals = listOf(
                ChemicalRecordEntity(
                    id = "chem_01",
                    assessmentId = sampleAssessmentId,
                    category = "DEEP_CLEANING",
                    chemicalName = "SuperCid Sanitiser Concentrate",
                    sdsReturned = true,
                    manufacturer = "Diversey SA",
                    remarks = "Quarterly autoclave deep scrub, SDS on file"
                ),
                ChemicalRecordEntity(
                    id = "chem_02",
                    assessmentId = sampleAssessmentId,
                    category = "DEEP_CLEANING",
                    chemicalName = "Chlor-Clean High-Level Biocide",
                    sdsReturned = true,
                    manufacturer = "Guest Medical",
                    remarks = "Used for wall scrub and fungal decontamination"
                ),
                ChemicalRecordEntity(
                    id = "chem_03",
                    assessmentId = sampleAssessmentId,
                    category = "ROUTINE_CLEANING",
                    chemicalName = "Multi-Surface Pine Disinfectant",
                    sdsReturned = true,
                    manufacturer = "CleanPro Industrial",
                    remarks = "Daily floor wash, diluted 1:20"
                ),
                ChemicalRecordEntity(
                    id = "chem_04",
                    assessmentId = sampleAssessmentId,
                    category = "ROUTINE_CLEANING",
                    chemicalName = "ActiFoam Toilet Bowl Cleaner",
                    sdsReturned = true,
                    manufacturer = "SafeChem SA",
                    remarks = "Twice daily bowl clean"
                )
            )
            dao.insertChemicals(chemicals)

            // Seed Environmental readings
            val envReadings = ChecklistTemplate.defaultEnvironmentalReadings().map { r ->
                EnvironmentalReadingEntity(
                    assessmentId = sampleAssessmentId,
                    periodName = r.periodName,
                    measurementTime = r.measurementTime,
                    wetBulbTemp = r.wetBulbTemp,
                    dryBulbTemp = r.dryBulbTemp,
                    globeTemp = r.globeTemp,
                    wbgtIndex = r.wbgtIndex,
                    relativeHumidity = r.relativeHumidity,
                    windSpeed = r.windSpeed,
                    generalConditions = r.generalConditions,
                    equipmentUsed = r.equipmentUsed,
                    comments = r.comments
                )
            }
            dao.insertEnvironmentalReadings(envReadings)

            // Seed Remedial action from the findings
            val action = RemedialActionEntity(
                id = "RA-2026-001",
                referenceNumber = "RA-2026-001",
                assessmentId = sampleAssessmentId,
                findingDescription = "Shower curtain #2 torn at hem; compromised privacy",
                relatedQuestion = "SECTION B - Question 4",
                recommendedAction = "Procure and hang heavy-duty mildew-resistant vinyl shower curtain",
                responsibleParty = "Facilities Maintenance (K. Jacobs)",
                targetDate = "2026-10-30",
                priority = RemedialPriority.MEDIUM.name,
                status = RemedialStatus.OPEN.name,
                photoUri = "demo_shower_curtain_tear",
                closedDate = null
            )
            dao.insertRemedialAction(action)

            // Seed Inspections for status and safety score tracking
            val initialInspections = listOf(
                InspectionEntity(
                    id = 1,
                    siteLocation = "Letaba Crushers — Tarentaalrant Quarry & Plant",
                    safetyScore = 92.5,
                    date = "2026-10-25",
                    inspectorName = "David Nkosi",
                    status = "COMPLIANT",
                    notes = "Full compliance with occupational health regulations. Water supply and soap dispensers in order."
                ),
                InspectionEntity(
                    id = 2,
                    siteLocation = "Secondary Crushing & Screening Depot — Bay 3",
                    safetyScore = 64.0,
                    date = "2026-10-24",
                    inspectorName = "David Nkosi",
                    status = "NON_COMPLIANT",
                    notes = "Torn shower curtain in Bay 3 and trace mildew noted on basin sealant. Action RA-2026-001 raised."
                ),
                InspectionEntity(
                    id = 3,
                    siteLocation = "Palabora Mining Company — Main Smelter Change House",
                    safetyScore = 78.0,
                    date = "2026-10-23",
                    inspectorName = "Dr. K. Naidoo",
                    status = "PENDING",
                    notes = "Fieldwork captured, awaiting hygiene laboratory sample verification and final sign-off."
                ),
                InspectionEntity(
                    id = 4,
                    siteLocation = "Tarentaalrant Workshop & Mobile Fleet Ablution",
                    safetyScore = 88.0,
                    date = "2026-10-21",
                    inspectorName = "David Nkosi",
                    status = "COMPLIANT",
                    notes = "Clean facilities, working soap dispensers, valid deep-cleaning certificate sighted."
                ),
                InspectionEntity(
                    id = 5,
                    siteLocation = "Letsitele Valley Secondary Depot — North Restrooms",
                    safetyScore = 55.0,
                    date = "2026-10-19",
                    inspectorName = "J. Van Der Merwe",
                    status = "NON_COMPLIANT",
                    notes = "Urinal auto-flush sensor failure and poor locker ventilation. Corrective work order initiated."
                ),
                InspectionEntity(
                    id = 6,
                    siteLocation = "Administration Complex & Visitors Ablution Facility",
                    safetyScore = 95.0,
                    date = "2026-10-18",
                    inspectorName = "David Nkosi",
                    status = "COMPLIANT",
                    notes = "Exceptional sanitation standard. Posters and SDS documentation up to date."
                ),
                InspectionEntity(
                    id = 7,
                    siteLocation = "Weighbridge Restroom & Security Post Shower",
                    safetyScore = 74.0,
                    date = "2026-10-16",
                    inspectorName = "David Nkosi",
                    status = "PENDING",
                    notes = "Shift environmental readings submitted, pending hygienist assessment approval."
                )
            )
            dao.insertInspections(initialInspections)
        }
    }

    // Role switcher
    fun setCurrentUserRole(role: UserRole) {
        _currentUser.value = _currentUser.value.copy(role = role)
    }

    fun updateAssessorProfile(profile: UserProfile) {
        _currentUser.value = profile
    }

    // Customer & Site Methods
    fun addCustomer(customer: Customer) {
        _customers.value = _customers.value + customer
    }

    fun addSite(site: CustomerSite) {
        _sites.value = _sites.value + site
    }

    // Assessments
    fun getAllAssessments(): Flow<List<AssessmentEntity>> = dao.getAllAssessments()

    suspend fun getAssessmentById(id: String): AssessmentEntity? = dao.getAssessmentById(id)

    suspend fun createNewAssessment(
        templateTitle: String,
        site: CustomerSite,
        facilityName: String,
        assessorName: String
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val newId = "ASM-${System.currentTimeMillis().toString().takeLast(6)}"
        val docRef = "WFH-${site.dmeMineCode}-ABL-${todayStr.replace("-", "")}"

        val assessment = AssessmentEntity(
            id = newId,
            templateTitle = templateTitle,
            customerId = site.customerId,
            customerName = site.customerName,
            siteId = site.id,
            siteName = site.siteName,
            facilityName = facilityName,
            dmeMineCode = site.dmeMineCode,
            mineManager = site.mineManager,
            mineManagerContact = site.mineManagerContact,
            siteOccupationalHygienist = site.siteOccupationalHygienist,
            occupationalMedicalPractitioner = site.occupationalMedicalPractitioner,
            assessorName = assessorName,
            assessmentType = "Monthly Compliance Inspection",
            documentReference = docRef,
            fieldVisitDate = todayStr,
            previousVisitDate = "2026-09-22",
            previousRemedialActions = "None outstanding from previous cycle.",
            hygienistSpecialInstructions = "Verify deep cleaning SOP and inspect mould/fungal control.",
            status = AssessmentStatus.DRAFT.name,
            progressPercent = 0,
            assessorSignature = null,
            reviewerSignature = null,
            reviewComments = null,
            createdTimestamp = System.currentTimeMillis(),
            updatedTimestamp = System.currentTimeMillis(),
            isSynced = false
        )
        dao.insertAssessment(assessment)

        // Generate template questions
        val questions = ChecklistTemplate.createDefaultAblutionChecklist()
        val answers = questions.map { q ->
            AnswerEntity(
                assessmentId = newId,
                questionId = q.id,
                sectionCode = q.sectionCode,
                sectionTitle = q.sectionTitle,
                questionNumber = q.questionNumber,
                questionText = q.questionText,
                response = ComplianceResponse.NOT_ASSESSED.name,
                remarks = "",
                photoUri = null,
                geoCoordinates = site.gpsCoordinates,
                timestamp = System.currentTimeMillis()
            )
        }
        dao.insertAnswers(answers)

        // Initialize chemicals
        val deepChems = ChecklistTemplate.defaultDeepCleaningChemicals().map { c ->
            ChemicalRecordEntity(
                id = UUID.randomUUID().toString(),
                assessmentId = newId,
                category = "DEEP_CLEANING",
                chemicalName = c.chemicalName,
                sdsReturned = c.sdsReturned,
                manufacturer = c.manufacturer,
                remarks = c.remarks
            )
        }
        val routineChems = ChecklistTemplate.defaultRoutineCleaningChemicals().map { c ->
            ChemicalRecordEntity(
                id = UUID.randomUUID().toString(),
                assessmentId = newId,
                category = "ROUTINE_CLEANING",
                chemicalName = c.chemicalName,
                sdsReturned = c.sdsReturned,
                manufacturer = c.manufacturer,
                remarks = c.remarks
            )
        }
        dao.insertChemicals(deepChems + routineChems)

        // Initialize environmental readings
        val readings = ChecklistTemplate.defaultEnvironmentalReadings().map { r ->
            EnvironmentalReadingEntity(
                assessmentId = newId,
                periodName = r.periodName,
                measurementTime = "",
                wetBulbTemp = "",
                dryBulbTemp = "",
                globeTemp = "",
                wbgtIndex = "",
                relativeHumidity = "",
                windSpeed = "",
                generalConditions = "",
                equipmentUsed = "WBGT Logger AZ 87786 (SN: AZ-2024-881)",
                comments = ""
            )
        }
        dao.insertEnvironmentalReadings(readings)

        return newId
    }

    suspend fun updateAssessment(assessment: AssessmentEntity) {
        dao.updateAssessment(assessment)
    }

    // Answers
    fun getAnswersForAssessment(assessmentId: String): Flow<List<AnswerEntity>> =
        dao.getAnswersForAssessment(assessmentId)

    suspend fun updateAnswer(answer: AnswerEntity) {
        dao.insertAnswer(answer)
        updateAssessmentProgress(answer.assessmentId)
    }

    suspend fun addCustomChecklistItem(
        assessmentId: String,
        questionText: String,
        response: ComplianceResponse,
        remarks: String
    ) {
        val currentAnswers = dao.getAnswersList(assessmentId).filter { it.sectionCode == "G" }
        val nextNum = currentAnswers.size + 1
        val answer = AnswerEntity(
            assessmentId = assessmentId,
            questionId = "G_$nextNum",
            sectionCode = "G",
            sectionTitle = "SECTION G — OTHER",
            questionNumber = nextNum,
            questionText = questionText,
            response = response.name,
            remarks = remarks,
            photoUri = null,
            geoCoordinates = "-23.8315° S, 30.1628° E",
            timestamp = System.currentTimeMillis(),
            isCustom = true
        )
        dao.insertAnswer(answer)
    }

    private suspend fun updateAssessmentProgress(assessmentId: String) {
        val allAnswers = dao.getAnswersList(assessmentId)
        if (allAnswers.isNotEmpty()) {
            val answeredCount = allAnswers.count { it.response != ComplianceResponse.NOT_ASSESSED.name }
            val percent = (answeredCount * 100) / allAnswers.size
            val assessment = dao.getAssessmentById(assessmentId)
            if (assessment != null) {
                dao.updateAssessment(
                    assessment.copy(
                        progressPercent = percent,
                        status = if (percent == 100 && assessment.status == AssessmentStatus.DRAFT.name)
                            AssessmentStatus.IN_PROGRESS.name
                        else assessment.status,
                        updatedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Chemicals
    fun getChemicalsForAssessment(assessmentId: String, category: String): Flow<List<ChemicalRecordEntity>> =
        dao.getChemicalsForAssessment(assessmentId, category)

    suspend fun addChemical(
        assessmentId: String,
        category: String,
        name: String,
        sds: Boolean,
        manufacturer: String,
        remarks: String
    ) {
        val item = ChemicalRecordEntity(
            id = UUID.randomUUID().toString(),
            assessmentId = assessmentId,
            category = category,
            chemicalName = name,
            sdsReturned = sds,
            manufacturer = manufacturer,
            remarks = remarks
        )
        dao.insertChemicals(listOf(item))
    }

    suspend fun deleteChemical(id: String) {
        dao.deleteChemical(id)
    }

    // Environmental Readings
    fun getEnvironmentalReadings(assessmentId: String): Flow<List<EnvironmentalReadingEntity>> =
        dao.getEnvironmentalReadings(assessmentId)

    suspend fun updateEnvironmentalReading(reading: EnvironmentalReadingEntity) {
        dao.insertEnvironmentalReadings(listOf(reading))
    }

    // Remedial Actions
    fun getAllRemedialActions(): Flow<List<RemedialActionEntity>> = dao.getAllRemedialActions()

    fun getRemedialActionsForAssessment(assessmentId: String): Flow<List<RemedialActionEntity>> =
        dao.getRemedialActionsForAssessment(assessmentId)

    suspend fun createRemedialAction(
        assessmentId: String,
        finding: String,
        relatedQuestion: String,
        actionText: String,
        responsible: String,
        targetDate: String,
        priority: RemedialPriority
    ) {
        val ref = "RA-${System.currentTimeMillis().toString().takeLast(4)}"
        val action = RemedialActionEntity(
            id = UUID.randomUUID().toString(),
            referenceNumber = ref,
            assessmentId = assessmentId,
            findingDescription = finding,
            relatedQuestion = relatedQuestion,
            recommendedAction = actionText,
            responsibleParty = responsible,
            targetDate = targetDate,
            priority = priority.name,
            status = RemedialStatus.OPEN.name,
            photoUri = null,
            closedDate = null
        )
        dao.insertRemedialAction(action)
    }

    suspend fun updateRemedialActionStatus(id: String, newStatus: RemedialStatus) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val closedDate = if (newStatus == RemedialStatus.COMPLETED || newStatus == RemedialStatus.VERIFIED)
            dateFormat.format(Date()) else null
        val actions = dao.getAllRemedialActions().first()
        val item = actions.find { it.id == id }
        if (item != null) {
            dao.updateRemedialAction(
                item.copy(
                    status = newStatus.name,
                    closedDate = closedDate
                )
            )
        }
    }

    // Equipment
    fun getAllEquipment(): Flow<List<EquipmentEntity>> = dao.getAllEquipment()

    suspend fun addEquipment(item: EquipmentEntity) {
        dao.insertEquipment(item)
    }

    // Trip Tracking
    fun startTrip(siteName: String, startCoords: String) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        _currentTrip.value = SiteTrip(
            siteName = siteName,
            startTime = sdf.format(Date()),
            startCoordinates = startCoords,
            currentCoordinates = startCoords,
            isActive = true
        )
    }

    fun endTrip() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        _currentTrip.value?.let {
            _currentTrip.value = it.copy(
                endTime = sdf.format(Date()),
                isActive = false
            )
        }
    }

    // Sync Simulation
    suspend fun triggerSync() {
        _isSyncing.value = true
        kotlinx.coroutines.delay(1200) // Realistic cloud sync delay
        val assessments = dao.getAllAssessments().first()
        assessments.forEach {
            dao.updateAssessment(it.copy(isSynced = true))
        }
        _isSyncing.value = false
    }

    fun toggleOnlineStatus() {
        _isOnline.value = !_isOnline.value
    }

    // Inspections Repository Methods (CRUD)
    fun getAllInspections(): Flow<List<InspectionEntity>> = dao.getAllInspections()

    suspend fun getInspectionById(id: Long): InspectionEntity? = dao.getInspectionById(id)

    fun getInspectionsByStatus(status: String): Flow<List<InspectionEntity>> = dao.getInspectionsByStatus(status)

    suspend fun createInspection(
        siteLocation: String,
        safetyScore: Double,
        date: String,
        inspectorName: String,
        status: String,
        notes: String,
        photoUris: String = ""
    ): Long {
        val entity = InspectionEntity(
            siteLocation = siteLocation,
            safetyScore = safetyScore,
            date = date,
            inspectorName = inspectorName,
            status = status,
            notes = notes,
            photoUris = photoUris
        )
        return dao.insertInspection(entity)
    }

    suspend fun attachPhotoToInspection(id: Long, photoUri: String) {
        val inspection = dao.getInspectionById(id) ?: return
        val updated = inspection.withPhotoAdded(photoUri)
        dao.updateInspection(updated)
    }

    suspend fun removePhotoFromInspection(id: Long, photoUri: String) {
        val inspection = dao.getInspectionById(id) ?: return
        val updated = inspection.withPhotoRemoved(photoUri)
        dao.updateInspection(updated)
    }

    suspend fun updateInspection(inspection: InspectionEntity) {
        dao.updateInspection(inspection)
    }

    suspend fun deleteInspection(id: Long) {
        dao.deleteInspectionById(id)
    }
}

