package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Macro
import com.example.data.models.MacroStatus
import com.example.data.models.TriggerType
import com.example.ui.theme.BlockingWarningBg
import com.example.ui.theme.BlockingWarningBorder
import com.example.ui.theme.BlockingWarningText
import com.example.ui.theme.StatusBuilt
import com.example.ui.theme.StatusBuiltBg
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.StatusDeployedBg
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusDraftBg
import com.example.ui.theme.StatusIdea
import com.example.ui.theme.StatusIdeaBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchBorderSubtle
import com.example.ui.theme.WorkbenchInset
import com.example.ui.theme.WorkbenchPrimary
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary

@Composable
fun StatusBadge(
    status: MacroStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, bgColor) = when (status) {
        MacroStatus.IDEA -> StatusIdea to StatusIdeaBg
        MacroStatus.DRAFT -> StatusDraft to StatusDraftBg
        MacroStatus.BUILT -> StatusBuilt to StatusBuiltBg
        MacroStatus.DEPLOYED -> StatusDeployed to StatusDeployedBg
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.displayName,
                color = dotColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TriggerBadge(
    triggerType: TriggerType,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WorkbenchInset,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, WorkbenchBorder),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = triggerType.icon,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = WorkbenchPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = triggerType.displayName,
                color = WorkbenchTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun TagPill(
    tag: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WorkbenchInset,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, WorkbenchBorder),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Text(
            text = "#$tag",
            color = WorkbenchTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun BlockingIssueAlert(
    issue: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(BlockingWarningBg)
            .border(1.dp, BlockingWarningBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Blocking Issue",
            tint = BlockingWarningText,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = issue,
            color = BlockingWarningText,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MacroCard(
    macro: Macro,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, WorkbenchBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("macro_card_${macro.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Macro name on left (15sp, medium), Status on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = macro.name,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = WorkbenchTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadge(status = macro.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-row: Trigger tag + Category/Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TriggerBadge(triggerType = macro.triggerType)

                if (!macro.exportData.isNullOrBlank()) {
                    Text(
                        text = "XML/Snippet attached",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = WorkbenchPrimary
                    )
                }
            }

            // Description (if present) - 12sp, secondary text color
            if (macro.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = macro.description,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = WorkbenchTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Blocking Issue line (only when non-empty)
            if (!macro.blockingIssue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                BlockingIssueAlert(issue = macro.blockingIssue)
            }

            // Tags Row
            if (macro.tagList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    macro.tagList.take(5).forEach { tag ->
                        TagPill(tag = tag)
                    }
                    if (macro.tagList.size > 5) {
                        Text(
                            text = "+${macro.tagList.size - 5}",
                            color = WorkbenchTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

