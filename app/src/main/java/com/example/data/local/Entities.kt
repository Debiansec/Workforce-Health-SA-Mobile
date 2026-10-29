package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val templateTitle: String,
    val customerId: String,
    val customerName: String,
    val siteId: String,
    val siteName: String,
    val facilityName: String,
    val dmeMineCode: String,
    val mineManager: String,
    val mineManagerContact: String,
    val siteOccupationalHygienist: String,
    val occupationalMedicalPractitioner: String,
    val assessorName: String,
    val assessmentType: String,
    val documentReference: String,
    val fieldVisitDate: String,
    val previousVisitDate: String,
    val previousRemedialActions: String,
    val hygienistSpecialInstructions: String,
    val status: String,
    val progressPercent: Int,
    val assessorSignature: String?,
    val reviewerSignature: String?,
    val reviewComments: String?,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "answers", primaryKeys = ["assessmentId", "questionId"])
data class AnswerEntity(
    val assessmentId: String,
    val questionId: String,
    val sectionCode: String,
    val sectionTitle: String,
    val questionNumber: Int,
    val questionText: String,
    val response: String, // "NOT_ASSESSED", "YES", "NO", "NOT_APPLICABLE"
    val remarks: String,
    val photoUri: String?,
    val geoCoordinates: String,
    val timestamp: Long,
    val isCustom: Boolean = false
)

@Entity(tableName = "chemicals")
data class ChemicalRecordEntity(
    @PrimaryKey val id: String,
    val assessmentId: String,
    val category: String, // "DEEP_CLEANING" or "ROUTINE_CLEANING"
    val chemicalName: String,
    val sdsReturned: Boolean,
    val manufacturer: String,
    val remarks: String
)

@Entity(tableName = "environmental_readings", primaryKeys = ["assessmentId", "periodName"])
data class EnvironmentalReadingEntity(
    val assessmentId: String,
    val periodName: String,
    val measurementTime: String,
    val wetBulbTemp: String,
    val dryBulbTemp: String,
    val globeTemp: String,
    val wbgtIndex: String,
    val relativeHumidity: String,
    val windSpeed: String,
    val generalConditions: String,
    val equipmentUsed: String,
    val comments: String
)

@Entity(tableName = "remedial_actions")
data class RemedialActionEntity(
    @PrimaryKey val id: String,
    val referenceNumber: String,
    val assessmentId: String,
    val findingDescription: String,
    val relatedQuestion: String,
    val recommendedAction: String,
    val responsibleParty: String,
    val targetDate: String,
    val priority: String,
    val status: String,
    val photoUri: String?,
    val closedDate: String?
)

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val assetIdentifier: String,
    val calibrationDate: String,
    val calibrationExpiryDate: String,
    val status: String,
    val isWarning: Boolean
)

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteLocation: String,
    val safetyScore: Double,
    val date: String,
    val inspectorName: String,
    val status: String = "PENDING", // "PENDING", "COMPLIANT", "NON_COMPLIANT"
    val notes: String = "",
    val photoUris: String = "" // Pipe-separated list of photo paths/URIs
) {
    fun getPhotoList(): List<String> {
        if (photoUris.isBlank()) return emptyList()
        return photoUris.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun withPhotoAdded(pathOrUri: String): InspectionEntity {
        val current = getPhotoList().toMutableList()
        if (!current.contains(pathOrUri)) {
            current.add(pathOrUri)
        }
        return this.copy(photoUris = current.joinToString("|"))
    }

    fun withPhotoRemoved(pathOrUri: String): InspectionEntity {
        val current = getPhotoList().filter { it != pathOrUri }
        return this.copy(photoUris = current.joinToString("|"))
    }
}
