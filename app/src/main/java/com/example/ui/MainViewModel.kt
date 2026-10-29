package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.AssessmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Dashboard : Screen()
    object CustomerSiteSelect : Screen()
    data class ChecklistWizard(val assessmentId: String, val initialStep: Int = 0) : Screen()
    object RemedialActions : Screen()
    object EquipmentRegister : Screen()
    object TripTracker : Screen()
    data class ReportPreview(val assessmentId: String) : Screen()
    object AssessorProfile : Screen()
    object NewInspectionForm : Screen()
    data class CustomerFileScreen(val customerId: String = "cust_afrimat") : Screen()
    object HistoricalInspections : Screen()
    data class InspectionPdfViewer(val inspection: InspectionEntity) : Screen()
    object SafetyTrendAnalytics : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AssessmentRepository(application)

    // Navigation stack
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    // Current active assessment for Wizard or Report
    private val _activeAssessmentId = MutableStateFlow<String?>(null)
    val activeAssessmentId = _activeAssessmentId.asStateFlow()

    // Wizard current step
    private val _wizardStep = MutableStateFlow(0)
    val wizardStep = _wizardStep.asStateFlow()

    // Data streams
    val assessments: StateFlow<List<AssessmentEntity>> = repository.getAllAssessments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val remedialActions: StateFlow<List<RemedialActionEntity>> = repository.getAllRemedialActions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equipmentList: StateFlow<List<EquipmentEntity>> = repository.getAllEquipment()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inspections: StateFlow<List<InspectionEntity>> = repository.getAllInspections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers = repository.customers
    val sites = repository.sites
    val currentUser = repository.currentUser
    val currentTrip = repository.currentTrip
    val isOnline = repository.isOnline
    val isSyncing = repository.isSyncing
    val customerOperationalFiles = repository.customerOperationalFiles

    // Selected customer & site during new assessment creation
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    private val _selectedSite = MutableStateFlow<CustomerSite?>(null)
    val selectedSite = _selectedSite.asStateFlow()

    private val _selectedFacility = MutableStateFlow("Main Change House — Male Ablution Block")
    val selectedFacility = _selectedFacility.asStateFlow()

    private val _selectedTemplateTitle = MutableStateFlow(ChecklistTemplate.TEMPLATE_TITLES.first())
    val selectedTemplateTitle = _selectedTemplateTitle.asStateFlow()

    init {
        viewModelScope.launch {
            customers.collect { list ->
                if (_selectedCustomer.value == null && list.isNotEmpty()) {
                    _selectedCustomer.value = list.first()
                }
            }
        }
        viewModelScope.launch {
            sites.collect { list ->
                if (_selectedSite.value == null && list.isNotEmpty()) {
                    _selectedSite.value = list.first()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        screenStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        } else if (_currentScreen.value !is Screen.Dashboard) {
            _currentScreen.value = Screen.Dashboard
            return true
        }
        return false
    }

    fun selectRole(role: UserRole) {
        repository.setCurrentUserRole(role)
        navigateTo(Screen.Dashboard)
    }

    fun setSelectedCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        val site = sites.value.firstOrNull { it.customerId == customer.id }
        _selectedSite.value = site
    }

    fun setSelectedSite(site: CustomerSite) {
        _selectedSite.value = site
    }

    fun setSelectedFacility(facility: String) {
        _selectedFacility.value = facility
    }

    fun setSelectedTemplateTitle(title: String) {
        _selectedTemplateTitle.value = title
    }

    fun startNewAssessment() {
        viewModelScope.launch {
            val site = _selectedSite.value ?: sites.value.firstOrNull() ?: return@launch
            val id = repository.createNewAssessment(
                templateTitle = _selectedTemplateTitle.value,
                site = site,
                facilityName = _selectedFacility.value,
                assessorName = currentUser.value.fullName
            )
            _activeAssessmentId.value = id
            _wizardStep.value = 0
            navigateTo(Screen.ChecklistWizard(id, 0))
        }
    }

    fun startAssessmentForSite(site: CustomerSite) {
        viewModelScope.launch {
            _selectedSite.value = site
            val cust = customers.value.find { it.id == site.customerId }
            if (cust != null) _selectedCustomer.value = cust
            val id = repository.createNewAssessment(
                templateTitle = _selectedTemplateTitle.value,
                site = site,
                facilityName = "Kuipersbult Main Quarry Ablution & Plant Facility",
                assessorName = currentUser.value.fullName
            )
            _activeAssessmentId.value = id
            _wizardStep.value = 0
            navigateTo(Screen.ChecklistWizard(id, 0))
        }
    }

    fun continueAssessment(assessmentId: String) {
        _activeAssessmentId.value = assessmentId
        _wizardStep.value = 0
        navigateTo(Screen.ChecklistWizard(assessmentId, 0))
    }

    fun openReportPreview(assessmentId: String) {
        _activeAssessmentId.value = assessmentId
        navigateTo(Screen.ReportPreview(assessmentId))
    }

    fun setWizardStep(step: Int) {
        _wizardStep.value = step.coerceIn(0, 9)
    }

    fun nextWizardStep() {
        if (_wizardStep.value < 9) {
            _wizardStep.value += 1
        }
    }

    fun prevWizardStep() {
        if (_wizardStep.value > 0) {
            _wizardStep.value -= 1
        }
    }

    fun updateAnswer(answer: AnswerEntity) {
        viewModelScope.launch {
            repository.updateAnswer(answer)
        }
    }

    fun addCustomChecklistItem(assessmentId: String, question: String, response: ComplianceResponse, remarks: String) {
        viewModelScope.launch {
            repository.addCustomChecklistItem(assessmentId, question, response, remarks)
        }
    }

    fun addChemical(assessmentId: String, category: String, name: String, sds: Boolean, manufacturer: String, remarks: String) {
        viewModelScope.launch {
            repository.addChemical(assessmentId, category, name, sds, manufacturer, remarks)
        }
    }

    fun deleteChemical(id: String) {
        viewModelScope.launch {
            repository.deleteChemical(id)
        }
    }

    fun updateEnvironmentalReading(reading: EnvironmentalReadingEntity) {
        viewModelScope.launch {
            repository.updateEnvironmentalReading(reading)
        }
    }

    fun createRemedialAction(
        assessmentId: String,
        finding: String,
        relatedQuestion: String,
        action: String,
        responsible: String,
        targetDate: String,
        priority: RemedialPriority
    ) {
        viewModelScope.launch {
            repository.createRemedialAction(
                assessmentId = assessmentId,
                finding = finding,
                relatedQuestion = relatedQuestion,
                actionText = action,
                responsible = responsible,
                targetDate = targetDate,
                priority = priority
            )
        }
    }

    fun updateRemedialStatus(id: String, status: RemedialStatus) {
        viewModelScope.launch {
            repository.updateRemedialActionStatus(id, status)
        }
    }

    fun submitAssessmentForReview(assessmentId: String, assessorSignatureText: String) {
        viewModelScope.launch {
            val a = repository.getAssessmentById(assessmentId) ?: return@launch
            repository.updateAssessment(
                a.copy(
                    status = AssessmentStatus.SUBMITTED_FOR_REVIEW.name,
                    assessorSignature = assessorSignatureText,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )
            navigateTo(Screen.ReportPreview(assessmentId))
        }
    }

    fun approveAssessment(assessmentId: String, reviewerSignature: String, comments: String) {
        viewModelScope.launch {
            val a = repository.getAssessmentById(assessmentId) ?: return@launch
            repository.updateAssessment(
                a.copy(
                    status = AssessmentStatus.APPROVED.name,
                    reviewerSignature = reviewerSignature,
                    reviewComments = comments,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun returnAssessmentForCorrection(assessmentId: String, comments: String) {
        viewModelScope.launch {
            val a = repository.getAssessmentById(assessmentId) ?: return@launch
            repository.updateAssessment(
                a.copy(
                    status = AssessmentStatus.RETURNED_FOR_CORRECTION.name,
                    reviewComments = comments,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.triggerSync()
        }
    }

    fun toggleOnline() {
        repository.toggleOnlineStatus()
    }

    fun startTrip(siteName: String, coords: String) {
        repository.startTrip(siteName, coords)
    }

    fun endTrip() {
        repository.endTrip()
    }

    fun addEquipment(item: EquipmentEntity) {
        viewModelScope.launch {
            repository.addEquipment(item)
        }
    }

    fun addSite(site: CustomerSite) {
        repository.addSite(site)
    }

    fun addCustomer(customer: Customer) {
        repository.addCustomer(customer)
    }

    fun updateProfile(profile: UserProfile) {
        repository.updateAssessorProfile(profile)
    }

    // Inspections CRUD
    fun createInspection(
        siteLocation: String,
        safetyScore: Double,
        date: String,
        inspectorName: String,
        status: String,
        notes: String = "",
        photoUris: String = ""
    ) {
        viewModelScope.launch {
            repository.createInspection(siteLocation, safetyScore, date, inspectorName, status, notes, photoUris)
        }
    }

    fun attachPhotoToInspection(id: Long, photoUri: String) {
        viewModelScope.launch {
            repository.attachPhotoToInspection(id, photoUri)
        }
    }

    fun removePhotoFromInspection(id: Long, photoUri: String) {
        viewModelScope.launch {
            repository.removePhotoFromInspection(id, photoUri)
        }
    }

    fun updateInspection(inspection: InspectionEntity) {
        viewModelScope.launch {
            repository.updateInspection(inspection)
        }
    }

    fun deleteInspection(id: Long) {
        viewModelScope.launch {
            repository.deleteInspection(id)
        }
    }

    fun openHistoricalInspections() {
        navigateTo(Screen.HistoricalInspections)
    }

    fun openInspectionPdf(inspection: InspectionEntity) {
        navigateTo(Screen.InspectionPdfViewer(inspection))
    }

    fun openSafetyTrendAnalytics() {
        navigateTo(Screen.SafetyTrendAnalytics)
    }
}

