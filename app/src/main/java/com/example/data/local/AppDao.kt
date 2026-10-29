package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Assessments
    @Query("SELECT * FROM assessments ORDER BY updatedTimestamp DESC")
    fun getAllAssessments(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE id = :id LIMIT 1")
    suspend fun getAssessmentById(id: String): AssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)

    @Update
    suspend fun updateAssessment(assessment: AssessmentEntity)

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteAssessment(id: String)

    // Answers
    @Query("SELECT * FROM answers WHERE assessmentId = :assessmentId ORDER BY sectionCode ASC, questionNumber ASC")
    fun getAnswersForAssessment(assessmentId: String): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers WHERE assessmentId = :assessmentId ORDER BY sectionCode ASC, questionNumber ASC")
    suspend fun getAnswersList(assessmentId: String): List<AnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AnswerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity)

    // Chemicals
    @Query("SELECT * FROM chemicals WHERE assessmentId = :assessmentId AND category = :category")
    fun getChemicalsForAssessment(assessmentId: String, category: String): Flow<List<ChemicalRecordEntity>>

    @Query("SELECT * FROM chemicals WHERE assessmentId = :assessmentId")
    suspend fun getAllChemicalsForAssessment(assessmentId: String): List<ChemicalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicals(chemicals: List<ChemicalRecordEntity>)

    @Query("DELETE FROM chemicals WHERE id = :id")
    suspend fun deleteChemical(id: String)

    // Environmental readings
    @Query("SELECT * FROM environmental_readings WHERE assessmentId = :assessmentId")
    fun getEnvironmentalReadings(assessmentId: String): Flow<List<EnvironmentalReadingEntity>>

    @Query("SELECT * FROM environmental_readings WHERE assessmentId = :assessmentId")
    suspend fun getEnvironmentalReadingsList(assessmentId: String): List<EnvironmentalReadingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvironmentalReadings(readings: List<EnvironmentalReadingEntity>)

    // Remedial Actions
    @Query("SELECT * FROM remedial_actions ORDER BY priority DESC, targetDate ASC")
    fun getAllRemedialActions(): Flow<List<RemedialActionEntity>>

    @Query("SELECT * FROM remedial_actions WHERE assessmentId = :assessmentId")
    fun getRemedialActionsForAssessment(assessmentId: String): Flow<List<RemedialActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedialAction(action: RemedialActionEntity)

    @Update
    suspend fun updateRemedialAction(action: RemedialActionEntity)

    @Query("DELETE FROM remedial_actions WHERE id = :id")
    suspend fun deleteRemedialAction(id: String)

    // Equipment
    @Query("SELECT * FROM equipment ORDER BY calibrationExpiryDate ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentList(equipment: List<EquipmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity)

    // Inspections (CRUD)
    @Query("SELECT * FROM inspections ORDER BY date DESC, id DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getInspectionById(id: Long): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE status = :status ORDER BY date DESC")
    fun getInspectionsByStatus(status: String): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspections(inspections: List<InspectionEntity>)

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    @Delete
    suspend fun deleteInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspectionById(id: Long)
}

