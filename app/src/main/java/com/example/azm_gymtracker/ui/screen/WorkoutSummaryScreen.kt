package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.ui.component.AzmPrimaryButton
import com.example.azm_gymtracker.ui.component.ParticipantAvatarBox
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceSelected
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceVariant
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.MemberColorGold
import java.util.Locale

data class ParticipantSummaryItem(
    val memberId: Long,
    val name: String,
    val colorHex: String,
    val volumeKg: Double,
    val totalSets: Int,
)

/**
 * Workout Summary Screen (Figma Node #87:550 "ملخص التمرين")
 * Celebrates workout completion with glowing total volume ring and participant breakdown.
 */
@Composable
fun WorkoutSummaryScreen(
    totalVolumeKg: Double,
    totalSets: Int,
    totalExercises: Int,
    durationSeconds: Long,
    participantSummaries: List<ParticipantSummaryItem>,
    newPersonalRecordText: String? = null,
    onReturnHome: () -> Unit,
    onViewDetails: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val durationFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val formattedTotalVolume = String.format(Locale.US, "%,.0f", totalVolumeKg)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AzmDarkBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Heading
            Text(
                text = stringResource(R.string.workout_summary_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AzmDarkOnSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total Volume Glowing Ring (Figma Node #87:572)
            Box(
                modifier = Modifier
                    .size(196.dp)
                    .shadow(elevation = 24.dp, shape = CircleShape, spotColor = AzmEmeraldGreen.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(AzmDarkSurfaceSelected)
                    .border(width = 3.dp, color = AzmEmeraldGreen, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.total_volume_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmDarkOnSurfaceMuted,
                    )
                    Text(
                        text = formattedTotalVolume,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = AzmDarkOnSurface,
                    )
                    Text(
                        text = stringResource(R.string.unit_kg),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AzmEmeraldGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Session Statistics Cards (3 cards in row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(
                    value = "$totalSets",
                    label = stringResource(R.string.stats_sets),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = "$totalExercises",
                    label = stringResource(R.string.stats_exercises),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = durationFormatted,
                    label = stringResource(R.string.stats_minutes),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Participant Results List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(participantSummaries, key = { it.memberId }) { item ->
                    ParticipantResultCard(item = item)
                }

                // Optional Personal Record Badge
                if (newPersonalRecordText != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(CircleShape)
                                .background(AzmDarkSurfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = newPersonalRecordText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MemberColorGold,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            AzmPrimaryButton(
                text = stringResource(R.string.action_return_home),
                onClick = onReturnHome,
            )

            TextButton(
                onClick = onViewDetails,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_view_details),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmEmeraldGreen,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AzmDarkSurface)
            .border(width = 1.dp, color = AzmBorderSubtle, shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AzmDarkOnSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AzmDarkOnSurfaceMuted,
        )
    }
}

@Composable
private fun ParticipantResultCard(
    item: ParticipantSummaryItem,
    modifier: Modifier = Modifier,
) {
    val memberColor = parseMemberColor(item.colorHex)
    val formattedVol = String.format(Locale.US, "%,.0f", item.volumeKg)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AzmDarkSurface)
            .border(width = 1.dp, color = AzmBorderSubtle, shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left side: Volume & sets count
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "$formattedVol كجم",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzmDarkOnSurface,
            )
            Text(
                text = "${item.totalSets} جولات",
                style = MaterialTheme.typography.bodySmall,
                color = AzmDarkOnSurfaceMuted,
            )
        }

        // Right side: Name & Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzmDarkOnSurface,
            )
            ParticipantAvatarBox(
                character = item.name,
                backgroundColor = memberColor,
                size = 40.dp,
                fontSize = 20,
            )
        }
    }
}
