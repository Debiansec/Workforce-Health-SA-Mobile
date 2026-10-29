package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerSite
import com.example.data.model.SiteTrip
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@Composable
fun TripTrackerScreen(
    currentTrip: SiteTrip?,
    sites: List<CustomerSite>,
    onStartTrip: (String, String) -> Unit,
    onEndTrip: () -> Unit,
    onBack: () -> Unit
) {
    var selectedSiteName by remember {
        mutableStateOf(sites.firstOrNull()?.siteName ?: "Letaba Crushers Tarentaalrant Plant")
    }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Field Trip & Geo Tracking",
                subtitle = "GPS Telemetry & Site Location",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (currentTrip?.isActive == true) ComplianceGreenLight else WfhTextMuted)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentTrip?.isActive == true) "Active Trip in Progress" else "Trip Standby",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTrip?.isActive == true) ComplianceGreenLight else Color.White
                                )
                            }

                            Surface(
                                color = WfhSurfaceElevated,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "OpenStreetMap / WGS84",
                                    fontSize = 10.sp,
                                    color = WfhTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // GPS Simulation Box
                        Surface(
                            color = Color(0xFF0F1115),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Current Coordinates", fontSize = 10.sp, color = WfhTextMuted)
                                        Text(
                                            text = currentTrip?.currentCoordinates ?: "-23.8315° S, 30.1628° E",
                                            fontWeight = FontWeight.Bold,
                                            color = WfhGold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Altitude", fontSize = 10.sp, color = WfhTextMuted)
                                        Text("682 m ASL", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = WfhBorderSubtle)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Destination Site", fontSize = 10.sp, color = WfhTextMuted)
                                        Text(
                                            text = currentTrip?.siteName ?: selectedSiteName,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Start Time", fontSize = 10.sp, color = WfhTextMuted)
                                        Text(
                                            text = currentTrip?.startTime ?: "--:--",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentTrip?.isActive == true) {
                            Button(
                                onClick = onEndTrip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("end_trip_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "End Trip")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Trip & Save Log", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onStartTrip(selectedSiteName, "-23.8315° S, 30.1628° E")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_trip_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start Trip")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Site Trip Tracking", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Today's Scheduled Site Visits",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WfhTextSecondary
                )
            }

            items(sites) { site ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = site.siteName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Surface(
                                color = WfhSurfaceElevated,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "DME: ${site.dmeMineCode}",
                                    color = WfhGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${site.propertyDescription}, ${site.location}",
                            fontSize = 12.sp,
                            color = WfhTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GPS: ${site.gpsCoordinates}",
                            fontSize = 11.sp,
                            color = WfhTextMuted
                        )
                    }
                }
            }
        }
    }
}
