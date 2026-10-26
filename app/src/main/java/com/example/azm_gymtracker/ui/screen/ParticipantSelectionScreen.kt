package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.ui.component.AzmPrimaryButton
import com.example.azm_gymtracker.ui.component.AzmSecondaryButton
import com.example.azm_gymtracker.ui.component.ParticipantAvatarBox
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceSelected
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceVariant
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmError
import com.example.azm_gymtracker.ui.theme.MemberColorCoral
import com.example.azm_gymtracker.ui.theme.MemberColorGold
import com.example.azm_gymtracker.ui.theme.MemberColorGreen
import com.example.azm_gymtracker.ui.theme.MemberColorIndigo

/**
 * Participant Selection Screen (Figma Node #87:346 "من يتمرن اليوم؟")
 * Select partners for the workout session, pass the phone around.
 */
@Composable
fun ParticipantSelectionScreen(
    members: List<Member>,
    initialSelectedMemberIds: Set<Long> = emptySet(),
    onStartWorkout: (selectedMemberIds: List<Long>) -> Unit,
    onCreateMember: (name: String, colorHex: String) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = remember {
        mutableStateListOf<Long>().apply {
            if (initialSelectedMemberIds.isNotEmpty()) {
                addAll(initialSelectedMemberIds)
            } else if (members.isNotEmpty()) {
                // By default select all active members for a group session
                addAll(members.map { it.id })
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AzmDarkBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // Header Row: Title + Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Back button (48x48dp in #10271C)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AzmDarkSurfaceVariant)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.action_back),
                        tint = AzmDarkOnSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Text(
                    text = stringResource(R.string.participants_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.participants_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AzmDarkOnSurfaceMuted,
                modifier = Modifier.align(Alignment.End),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selection Heading Row: "المشاركون" + Badge "3 محددون"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AzmDarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.participants_count_badge, selectedIds.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmEmeraldGreen,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = stringResource(R.string.participants_heading),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Participants List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(members, key = { it.id }) { member ->
                    val isSelected = selectedIds.contains(member.id)
                    val memberColor = parseMemberColor(member.colorHex)

                    ParticipantCard(
                        member = member,
                        color = memberColor,
                        isSelected = isSelected,
                        onToggle = {
                            if (isSelected) selectedIds.remove(member.id)
                            else selectedIds.add(member.id)
                        },
                        onEdit = { editingMember = member },
                        onDelete = { memberToDelete = member },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Secondary Button: "＋ إضافة مشارك"
                    AzmSecondaryButton(
                        text = stringResource(R.string.participants_add_button),
                        onClick = { showAddDialog = true },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Area: Reassurance footnote + Start button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.participants_reassurance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmDarkOnSurfaceMuted,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = AzmDarkOnSurfaceMuted,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Button: "ابدأ التمرين"
            AzmPrimaryButton(
                text = stringResource(R.string.hero_start_button),
                enabled = selectedIds.isNotEmpty(),
                onClick = {
                    if (selectedIds.isNotEmpty()) {
                        onStartWorkout(selectedIds.toList())
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Add Participant Dialog
    if (showAddDialog) {
        AddParticipantDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colorHex ->
                onCreateMember(name, colorHex)
                showAddDialog = false
            },
        )
    }

    // Delete Member Confirmation
    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            containerColor = AzmDarkSurface,
            title = {
                Text(
                    text = stringResource(R.string.action_delete),
                    style = MaterialTheme.typography.titleLarge,
                    color = AzmDarkOnSurface,
                )
            },
            text = {
                Text(
                    text = "${member.name} • سيتم حذف المتدرب من هذا الجهاز.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmDarkOnSurfaceMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMember(member)
                        selectedIds.remove(member.id)
                        memberToDelete = null
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = AzmError,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurface)
                }
            },
        )
    }
}

@Composable
private fun ParticipantCard(
    member: Member,
    color: Color,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardBackground = if (isSelected) AzmDarkSurfaceSelected else AzmDarkSurface
    val cardBorder = if (isSelected) AzmEmeraldGreen else AzmBorderSubtle

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBackground)
            .border(width = 1.dp, color = cardBorder, shape = RoundedCornerShape(24.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left Actions: Delete & Edit circular buttons (48dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AzmDarkSurfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = AzmDarkOnSurfaceMuted,
                    modifier = Modifier.size(20.dp),
                )
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AzmDarkSurfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل",
                    tint = AzmDarkOnSurfaceMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Right Content: Name + Status Subtitle + Avatar Initial
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
                if (isSelected) {
                    Text(
                        text = stringResource(R.string.participants_selected_badge),
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmEmeraldGreen,
                    )
                }
            }

            ParticipantAvatarBox(
                character = member.name,
                backgroundColor = color,
            )
        }
    }
}

/**
 * Add Participant Dialog matching Figma Node #87:1779
 */
@Composable
fun AddParticipantDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val colors = listOf(
        "#3FE779" to MemberColorGreen,
        "#FFD177" to MemberColorGold,
        "#8E9CFF" to MemberColorIndigo,
        "#E9A87C" to MemberColorCoral,
    )
    var selectedColorHex by remember { mutableStateOf(colors.first().first) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AzmDarkSurface,
        shape = RoundedCornerShape(32.dp),
        title = {
            Text(
                text = "إضافة مشارك",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzmDarkOnSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzmEmeraldGreen,
                        unfocusedBorderColor = AzmBorderSubtle,
                        focusedTextColor = AzmDarkOnSurface,
                        unfocusedTextColor = AzmDarkOnSurface,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Text(
                    text = "اللون والحرف الأول",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmDarkOnSurfaceMuted,
                )

                // Color picker row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    colors.forEach { (hex, composeColor) ->
                        val isChosen = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(composeColor)
                                .then(
                                    if (isChosen) Modifier.border(
                                        2.dp,
                                        Color.White,
                                        RoundedCornerShape(16.dp),
                                    ) else Modifier
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = name.take(1).ifEmpty { "أ" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AzmDarkBackground,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            AzmPrimaryButton(
                text = "إضافة",
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), selectedColorHex) },
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurfaceMuted)
            }
        },
    )
}

fun parseMemberColor(colorHex: String?): Color {
    return when (colorHex) {
        "#FFD177" -> MemberColorGold
        "#8E9CFF" -> MemberColorIndigo
        "#E9A87C" -> MemberColorCoral
        else -> MemberColorGreen
    }
}
