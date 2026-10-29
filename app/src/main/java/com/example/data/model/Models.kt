package com.example.data.model

import java.util.UUID

enum class UserRole(val displayName: String, val description: String) {
    ASSESSOR("Field Assessor", "Conduct inspections, capture evidence, save offline drafts"),
    HYGIENIST("Occupational Hygienist", "Review submitted assessments, evaluate findings, approve reports"),
    MINE_MANAGEMENT("Mine Management / Client", "View authorised site reports, acknowledge findings"),
    ADMIN("System Administrator", "Manage users, sites, checklist templates and audit logs")
}

data class UserProfile(
    val id: String = "user_001",
    val fullName: String = "David Nkosi",
    val role: UserRole = UserRole.ASSESSOR,
    val email: String = "d.nkosi@workforcehealth.co.za",
    val mobileNumber: String = "+27 82 123 4567",
    val workMobile: String = "+27 11 987 6543",
    val whatsAppNumber: String = "+27 82 123 4567",
    val emergencyContactName: String = "Sarah Nkosi",
    val emergencyContactPhone: String = "+27 83 555 8899",
    val jobDescription: String = "Senior Occupational Hygiene Field Assessor (SAIOH Registered)",
    val certificationNumber: String = "OH-TECH-2024-912"
)

data class Customer(
    val id: String,
    val name: String,
    val legalEntity: String,
    val parentCompany: String = "",
    val vatNumber: String = "4810239482",
    val cipcRegistration: String = "2008/014923/07",
    val miningRightRef: String = "LP30/5/1/2/2/1004MR",
    val waterLicenceRef: String = "04/B81B/ABEGJ/1982"
)

data class CustomerSite(
    val id: String,
    val customerId: String,
    val customerName: String,
    val siteName: String,
    val propertyDescription: String,
    val location: String,
    val province: String,
    val dmeMineCode: String,
    val mineManager: String,
    val mineManagerContact: String,
    val siteOccupationalHygienist: String,
    val occupationalMedicalPractitioner: String,
    val gpsCoordinates: String = "-23.8315° S, 30.1628° E"
)

enum class ComplianceResponse {
    NOT_ASSESSED,
    YES,
    NO,
    NOT_APPLICABLE
}

data class ChemicalEntry(
    val id: String = UUID.randomUUID().toString(),
    val chemicalName: String,
    val sdsReturned: Boolean = true,
    val manufacturer: String = "",
    val remarks: String = ""
)

data class EnvironmentalReading(
    val periodName: String, // "Start of shift", "Middle of shift", "End of shift"
    val measurementTime: String = "",
    val wetBulbTemp: String = "", // °C
    val dryBulbTemp: String = "", // °C
    val globeTemp: String = "",    // °C
    val wbgtIndex: String = "",    // °C
    val relativeHumidity: String = "", // %
    val windSpeed: String = "",    // m/s
    val generalConditions: String = "",
    val equipmentUsed: String = "WBGT Logger AZ 87786 (SN: AZ-2024-881)",
    val comments: String = ""
)

data class ChecklistItem(
    val id: String,
    val sectionCode: String, // "A", "B", "C", "D", "E", "F", "G"
    val sectionTitle: String,
    val questionNumber: Int,
    val questionText: String,
    val response: ComplianceResponse = ComplianceResponse.NOT_ASSESSED,
    val remarks: String = "",
    val photoUri: String? = null,
    val geoCoordinates: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false
)

enum class RemedialPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class RemedialStatus {
    OPEN, IN_PROGRESS, COMPLETED, VERIFIED
}

data class RemedialAction(
    val id: String = UUID.randomUUID().toString(),
    val referenceNumber: String,
    val assessmentId: String,
    val findingDescription: String,
    val relatedQuestion: String,
    val recommendedAction: String,
    val responsibleParty: String,
    val targetDate: String,
    val priority: RemedialPriority = RemedialPriority.MEDIUM,
    val status: RemedialStatus = RemedialStatus.OPEN,
    val photoUri: String? = null,
    val closedDate: String? = null
)

enum class AssessmentStatus {
    DRAFT,
    IN_PROGRESS,
    SUBMITTED_FOR_REVIEW,
    RETURNED_FOR_CORRECTION,
    APPROVED,
    ARCHIVED
}

data class EquipmentItem(
    val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val assetIdentifier: String,
    val calibrationDate: String,
    val calibrationExpiryDate: String,
    val status: String = "Calibrated",
    val isWarning: Boolean = false
)

data class SiteTrip(
    val id: String = UUID.randomUUID().toString(),
    val siteName: String,
    val startTime: String,
    val endTime: String? = null,
    val startCoordinates: String,
    val currentCoordinates: String,
    val isActive: Boolean = true,
    val distanceKm: Double = 0.0
)
