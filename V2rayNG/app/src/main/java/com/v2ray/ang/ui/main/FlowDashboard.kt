package com.v2ray.ang.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.extension.toSpeedString
import com.v2ray.ang.extension.toTrafficString

/** Only daemon-confirmed running state and persisted server data are displayed here. */
@Composable
internal fun FlowDashboard(
    state: MainUiState,
    onAction: (MainAction) -> Unit,
    onOpenServers: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val running = state.isRunning
    val accent by animateColorAsState(
        if (running) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer,
        label = "connection surface"
    )
    val status = when {
        running -> R.string.flow_connected
        state.status == MainStatus.Connecting -> R.string.flow_connecting
        state.status == MainStatus.Error -> R.string.flow_error
        else -> R.string.flow_disconnected
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            AnimatedContent(targetState = status, label = "connection status") { target ->
                Text(
                    text = stringResource(target),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(216.dp)
                    .background(accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAction(MainAction.ToggleService)
                    },
                    enabled = state.status != MainStatus.Connecting,
                    modifier = Modifier.size(164.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (running) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.status == MainStatus.Connecting) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        } else {
                            Icon(
                                painter = painterResource(if (running) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                when {
                                    state.status == MainStatus.Connecting -> R.string.flow_connecting
                                    running -> R.string.flow_disconnect
                                    else -> R.string.flow_connect
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenServers),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.flow_active_server),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.selectedServer?.profile?.remarks ?: stringResource(R.string.flow_choose_server),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    state.selectedServer?.let { server ->
                        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                server.profile.configType.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (server.testDelayMillis != 0L) {
                                Text(
                                    stringResource(R.string.server_test_delay_value, server.testDelayMillis),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
            val traffic = state.traffic.takeIf { running }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowMetric(
                    title = stringResource(R.string.flow_download),
                    value = traffic?.downloadBytesPerSecond?.toSpeedString() ?: stringResource(R.string.flow_unavailable),
                    modifier = Modifier.weight(1f)
                )
                FlowMetric(
                    title = stringResource(R.string.flow_upload),
                    value = traffic?.uploadBytesPerSecond?.toSpeedString() ?: stringResource(R.string.flow_unavailable),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowMetric(
                    title = stringResource(R.string.flow_transferred),
                    value = traffic?.let { (it.totalUploadBytes + it.totalDownloadBytes).toTrafficString() }
                        ?: stringResource(R.string.flow_unavailable),
                    modifier = Modifier.weight(1f)
                )
                val elapsedSeconds = (traffic?.measuredDurationMillis ?: 0L) / 1000
                FlowMetric(
                    title = stringResource(R.string.flow_duration),
                    value = if (traffic == null) stringResource(R.string.flow_unavailable) else stringResource(
                        R.string.flow_duration_value,
                        elapsedSeconds / 3600,
                        (elapsedSeconds / 60) % 60,
                        elapsedSeconds % 60
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (running && traffic == null) {
                Text(
                    stringResource(R.string.flow_waiting_stats),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (running) {
                Text(
                    text = stringResource(R.string.flow_test_connection),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(MainAction.TestCurrentServer) }
                        .padding(16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (state.status is MainStatus.ConnectionTest) {
                    Text(
                        text = stringResource(R.string.server_test_delay_value, state.status.result.delayMillis),
                        color = if (state.status.result.delayMillis < 0) MaterialTheme.colorScheme.error else Color.Unspecified,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowMetric(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
