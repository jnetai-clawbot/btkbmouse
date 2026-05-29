package com.jnetaol.btkbmouse.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jnetaol.btkbmouse.ui.AppViewModel
import com.jnetaol.btkbmouse.ui.components.*
import com.jnetaol.btkbmouse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchpadScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val btManager = viewModel.btManager
    val connectionState by btManager.connectionState.collectAsState()
    var sensitivity by remember { mutableFloatStateOf(1.0f) }
    var isLeftClicking by remember { mutableStateOf(false) }
    var isRightClicking by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Touchpad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionStatusBadge(
                isConnected = connectionState.isConnected,
                deviceName = connectionState.deviceName,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            ErrorDisplay(connectionState.error)
            Spacer(Modifier.height(8.dp))

            NeonSlider(
                value = sensitivity,
                onValueChange = {
                    sensitivity = it
                    viewModel.saveSetting("mouse_sensitivity", String.format("%.2f", it))
                },
                valueRange = 0.1f..3.0f,
                label = "Sensitivity",
                displayValue = String.format("%.1fx", sensitivity),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            viewModel.sendMouseDelta(
                                dragAmount.x * sensitivity,
                                dragAmount.y * sensitivity
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { viewModel.sendMouseDelta(0f, 0f) }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (connectionState.isConnected) {
                    Text(
                        "Swipe to move cursor\nTap to click",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        "Not connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarningOrange,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.sendMouseDelta(0f, 0f) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLeftClicking) NeonBlue else DarkCard,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.TouchApp, "Left Click", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Left Click", fontSize = MaterialTheme.typography.labelMedium.fontSize)
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { viewModel.sendMouseDelta(0f, 0f) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRightClicking) NeonBlue else DarkCard,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.TouchApp, "Right Click", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Right Click", fontSize = MaterialTheme.typography.labelMedium.fontSize)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
