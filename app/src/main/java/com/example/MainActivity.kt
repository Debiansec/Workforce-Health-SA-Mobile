package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerFileData
import com.example.data.model.CustomerOperationalFile
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.WorkForceHealthTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WorkForceHealthTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val assessments by viewModel.assessments.collectAsStateWithLifecycle()
                val remedialActions by viewModel.remedialActions.collectAsStateWithLifecycle()
                val equipmentList by viewModel.equipmentList.collectAsStateWithLifecycle()
                val inspections by viewModel.inspections.collectAsStateWithLifecycle()
                val customers by viewModel.customers.collectAsStateWithLifecycle()
                val sites by viewModel.sites.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
                val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

                val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
                val selectedSite by viewModel.selectedSite.collectAsStateWithLifecycle()
                val selectedFacility by viewModel.selectedFacility.collectAsStateWithLifecycle()
                val selectedTemplateTitle by viewModel.selectedTemplateTitle.collectAsStateWithLifecycle()
                val wizardStep by viewModel.wizardStep.collectAsStateWithLifecycle()

                // Intercept back button for nested screens
                BackHandler(enabled = currentScreen !is Screen.Dashboard && currentScreen !is Screen.Splash) {
                    viewModel.navigateBack()
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val screen = currentScreen) {
                        is Screen.Splash -> {
                            SplashScreen(
                                onSplashFinished = {
                                    viewModel.navigateTo(Screen.Login)
                                }
                            )
                        }

                        is Screen.Login -> {
                            LoginScreen(
                                onLoginSuccess = { role ->
                                    viewModel.selectRole(role)
                                }
                            )
                        }

                        is Screen.Dashboard -> {
                            DashboardScreen(
                                user = currentUser,
                                assessments = assessments,
                                remedialActions = remedialActions,
                                equipmentList = equipmentList,
                                inspections = inspections,
                                isOnline = isOnline,
                                isSyncing = isSyncing,
                                onSyncClick = { viewModel.triggerSync() },
                                onToggleOnline = { viewModel.toggleOnline() },
                                onStartNewAssessment = {
                                    viewModel.navigateTo(Screen.CustomerSiteSelect)
                                },
                                onContinueAssessment = { id ->
                                    viewModel.continueAssessment(id)
                                },
                                onOpenReport = { id ->
                                    viewModel.openReportPreview(id)
                                },
                                onViewRemedialActions = {
                                    viewModel.navigateTo(Screen.RemedialActions)
                                },
                                onViewEquipment = {
                                    viewModel.navigateTo(Screen.EquipmentRegister)
                                },
                                onViewTrips = {
                                    viewModel.navigateTo(Screen.TripTracker)
                                },
                                onViewProfile = {
                                    viewModel.navigateTo(Screen.AssessorProfile)
                                },
                                onViewCustomerFiles = {
                                    viewModel.navigateTo(Screen.CustomerFileScreen())
                                },
                                onSwitchRoleClick = {
                                    viewModel.navigateTo(Screen.Login)
                                },
                                onCreateInspection = { site, score, date, inspector, status, notes ->
                                    viewModel.createInspection(site, score, date, inspector, status, notes)
                                },
                                onUpdateInspection = { viewModel.updateInspection(it) },
                                onDeleteInspection = { viewModel.deleteInspection(it) },
                                onNavigateToNewInspectionForm = {
                                    viewModel.navigateTo(Screen.NewInspectionForm)
                                },
                                onViewHistoricalInspections = {
                                    viewModel.openHistoricalInspections()
                                },
                                onViewSafetyTrends = {
                                    viewModel.openSafetyTrendAnalytics()
                                }
                            )
                        }

                        is Screen.CustomerSiteSelect -> {
                            CustomerSiteSelectionScreen(
                                customers = customers,
                                sites = sites,
                                facilities = viewModel.repository.facilities,
                                selectedCustomer = selectedCustomer,
                                selectedSite = selectedSite,
                                selectedFacility = selectedFacility,
                                selectedTemplateTitle = selectedTemplateTitle,
                                onSelectCustomer = { viewModel.setSelectedCustomer(it) },
                                onSelectSite = { viewModel.setSelectedSite(it) },
                                onSelectFacility = { viewModel.setSelectedFacility(it) },
                                onSelectTemplateTitle = { viewModel.setSelectedTemplateTitle(it) },
                                onStartInspection = {
                                    viewModel.startNewAssessment()
                                },
                                onBack = { viewModel.navigateBack() },
                                onAddCustomer = { viewModel.addCustomer(it) },
                                onAddSite = { viewModel.addSite(it) }
                            )
                        }

                        is Screen.ChecklistWizard -> {
                            ChecklistWizardScreen(
                                assessmentId = screen.assessmentId,
                                assessmentFlow = viewModel.assessments,
                                answersFlow = viewModel.repository.getAnswersForAssessment(screen.assessmentId),
                                deepChemicalsFlow = viewModel.repository.getChemicalsForAssessment(screen.assessmentId, "DEEP_CLEANING"),
                                routineChemicalsFlow = viewModel.repository.getChemicalsForAssessment(screen.assessmentId, "ROUTINE_CLEANING"),
                                environmentalReadingsFlow = viewModel.repository.getEnvironmentalReadings(screen.assessmentId),
                                currentStep = wizardStep,
                                onStepChange = { viewModel.setWizardStep(it) },
                                onNextStep = { viewModel.nextWizardStep() },
                                onPrevStep = { viewModel.prevWizardStep() },
                                onUpdateAnswer = { viewModel.updateAnswer(it) },
                                onAddCustomItem = { q, resp, rem ->
                                    viewModel.addCustomChecklistItem(screen.assessmentId, q, resp, rem)
                                },
                                onAddChemical = { cat, name, sds, mfg, rem ->
                                    viewModel.addChemical(screen.assessmentId, cat, name, sds, mfg, rem)
                                },
                                onDeleteChemical = { viewModel.deleteChemical(it) },
                                onUpdateEnvironmentalReading = { viewModel.updateEnvironmentalReading(it) },
                                onCreateRemedialAction = { finding, relQ, action, resp, target, prio ->
                                    viewModel.createRemedialAction(
                                        screen.assessmentId,
                                        finding,
                                        relQ,
                                        action,
                                        resp,
                                        target,
                                        prio
                                    )
                                },
                                onSubmitAssessment = { sig ->
                                    viewModel.submitAssessmentForReview(screen.assessmentId, sig)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.RemedialActions -> {
                            RemedialActionsScreen(
                                actions = remedialActions,
                                onUpdateStatus = { id, status ->
                                    viewModel.updateRemedialStatus(id, status)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.EquipmentRegister -> {
                            EquipmentRegisterScreen(
                                equipment = equipmentList,
                                onAddEquipment = { viewModel.addEquipment(it) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.TripTracker -> {
                            TripTrackerScreen(
                                currentTrip = currentTrip,
                                sites = sites,
                                onStartTrip = { site, coords -> viewModel.startTrip(site, coords) },
                                onEndTrip = { viewModel.endTrip() },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.ReportPreview -> {
                            ReportPreviewScreen(
                                assessmentId = screen.assessmentId,
                                assessmentFlow = viewModel.assessments,
                                answersFlow = viewModel.repository.getAnswersForAssessment(screen.assessmentId),
                                deepChemicalsFlow = viewModel.repository.getChemicalsForAssessment(screen.assessmentId, "DEEP_CLEANING"),
                                routineChemicalsFlow = viewModel.repository.getChemicalsForAssessment(screen.assessmentId, "ROUTINE_CLEANING"),
                                environmentalReadingsFlow = viewModel.repository.getEnvironmentalReadings(screen.assessmentId),
                                remedialActionsFlow = viewModel.repository.getRemedialActionsForAssessment(screen.assessmentId),
                                currentUser = currentUser,
                                onApproveReport = { sig, comments ->
                                    viewModel.approveAssessment(screen.assessmentId, sig, comments)
                                },
                                onReturnForCorrection = { comments ->
                                    viewModel.returnAssessmentForCorrection(screen.assessmentId, comments)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.AssessorProfile -> {
                            AssessorProfileScreen(
                                user = currentUser,
                                onSaveProfile = { viewModel.updateProfile(it) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.NewInspectionForm -> {
                            NewInspectionFormScreen(
                                currentUser = currentUser,
                                availableSites = sites,
                                onSaveInspection = { site, score, date, inspector, status, notes, photoUris ->
                                    viewModel.createInspection(site, score, date, inspector, status, notes, photoUris)
                                    viewModel.navigateBack()
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.CustomerFileScreen -> {
                            val operationalFiles by viewModel.customerOperationalFiles.collectAsStateWithLifecycle()
                            val currentFile = operationalFiles[screen.customerId] ?: CustomerFileData.AFRIMAT_FILE
                            CustomerFileScreen(
                                customerFile = currentFile,
                                onStartAssessmentForCustomer = { site ->
                                    viewModel.startAssessmentForSite(site)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.HistoricalInspections -> {
                            HistoricalInspectionsScreen(
                                inspections = inspections,
                                onViewInspectionPdf = { inspection ->
                                    viewModel.openInspectionPdf(inspection)
                                },
                                onNavigateToNewForm = {
                                    viewModel.navigateTo(Screen.NewInspectionForm)
                                },
                                onUpdateInspection = { viewModel.updateInspection(it) },
                                onDeleteInspection = { viewModel.deleteInspection(it) },
                                onNavigateToTrends = {
                                    viewModel.openSafetyTrendAnalytics()
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.InspectionPdfViewer -> {
                            InspectionPdfViewerScreen(
                                inspection = screen.inspection,
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        is Screen.SafetyTrendAnalytics -> {
                            SafetyTrendAnalyticsScreen(
                                inspections = inspections,
                                onViewInspectionPdf = { inspection ->
                                    viewModel.openInspectionPdf(inspection)
                                },
                                onNavigateToNewInspection = {
                                    viewModel.navigateTo(Screen.NewInspectionForm)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
