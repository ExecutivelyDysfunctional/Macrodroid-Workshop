package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchInset
import com.example.ui.theme.WorkbenchOnPrimary
import com.example.ui.theme.WorkbenchPrimary
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary
import com.example.util.BracketStyle
import com.example.util.DateTimeBlockLibrary
import com.example.util.DateTimeBlockPiece
import com.example.util.MagicTextEvaluator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomDateTimeBuilder(
    bracketStyle: BracketStyle,
    onCopyText: (textToCopy: String, label: String) -> Unit,
    onSavePreset: (name: String, content: String, tags: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBlocks = remember { mutableStateListOf<DateTimeBlockPiece>() }
    var activeBlockCategory by remember { mutableStateOf(0) } // 0 = Date, 1 = Time, 2 = Separators
    var showSaveDialog by remember { mutableStateOf(false) }
    var savePresetName by remember { mutableStateOf("") }
    var savePresetTags by remember { mutableStateOf("datetime, custom") }

    val rawTemplate = remember(selectedBlocks.toList()) {
        selectedBlocks.joinToString("") { it.tokenValue }
    }

    val formattedMagicText = remember(rawTemplate, bracketStyle) {
        if (rawTemplate.isBlank()) "" else bracketStyle.formatTemplate(rawTemplate)
    }

    val (liveSample, _) = remember(rawTemplate) {
        if (rawTemplate.isBlank()) Pair("No blocks selected yet", false)
        else MagicTextEvaluator.evaluateLiveSample(rawTemplate, context)
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Custom DateTime Preset",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = WorkbenchTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Formula: $formattedMagicText",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = WorkbenchPrimary
                    )

                    OutlinedTextField(
                        value = savePresetName,
                        onValueChange = { savePresetName = it },
                        label = { Text("Preset Title / Name") },
                        placeholder = { Text("e.g., My Custom Stamp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = savePresetTags,
                        onValueChange = { savePresetTags = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (savePresetName.isNotBlank() && formattedMagicText.isNotBlank()) {
                            onSavePreset(savePresetName, formattedMagicText, savePresetTags)
                            showSaveDialog = false
                            savePresetName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkbenchPrimary)
                ) {
                    Text("Save Preset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurface
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, WorkbenchPrimary.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("custom_datetime_builder_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(WorkbenchPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = WorkbenchPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Build Your Own DateTime",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkbenchTextPrimary
                        )
                        Text(
                            text = "Tap block pieces below to assemble custom Magic Text",
                            fontSize = 11.sp,
                            color = WorkbenchTextSecondary
                        )
                    }
                }

                if (selectedBlocks.isNotEmpty()) {
                    TextButton(
                        onClick = { selectedBlocks.clear() },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = WorkbenchTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            color = WorkbenchTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Output Display Card
            Surface(
                color = WorkbenchInset,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MAGIC TEXT FORMULA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkbenchTextMuted,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = bracketStyle.label,
                            fontSize = 10.sp,
                            color = WorkbenchPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (formattedMagicText.isNotBlank()) formattedMagicText else "Tap pieces below to build formula...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (formattedMagicText.isNotBlank()) WorkbenchPrimary else WorkbenchTextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Evaluated Output Preview
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Live Result:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkbenchTextSecondary
                        )
                        Text(
                            text = liveSample,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (formattedMagicText.isNotBlank()) StatusDeployed else WorkbenchTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (formattedMagicText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCopyText(formattedMagicText, "Custom DateTime Formula") },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkbenchPrimary),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("copy_built_datetime_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Magic Text",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Magic Text", fontSize = 12.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    savePresetName = "Custom DateTime (${liveSample.take(15)})"
                                    showSaveDialog = true
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Preset",
                                    tint = WorkbenchTextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Preset", fontSize = 12.sp, color = WorkbenchTextPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Preset Formula Bar
            Text(
                text = "Quick Presets:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WorkbenchTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DateTimeBlockLibrary.presetTemplates) { (name, template) ->
                    Surface(
                        color = WorkbenchInset,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WorkbenchBorder),
                        modifier = Modifier.clickable {
                            selectedBlocks.clear()
                            val regex = Regex("(\\[[^\\]]+\\]|[^\\s\\[\\]]+|\\s+)")
                            val matches = regex.findAll(template).map { it.value }.toList()
                            for (part in matches) {
                                val allPieces = DateTimeBlockLibrary.datePieces +
                                        DateTimeBlockLibrary.timePieces +
                                        DateTimeBlockLibrary.separatorPieces
                                val foundPiece = allPieces.find { it.tokenValue == part }
                                if (foundPiece != null) {
                                    selectedBlocks.add(foundPiece)
                                } else if (part.isNotBlank()) {
                                    selectedBlocks.add(
                                        DateTimeBlockPiece(
                                            id = "custom_$part",
                                            label = "'$part'",
                                            tokenValue = part,
                                            isLiteral = true
                                        )
                                    )
                                } else {
                                    selectedBlocks.add(DateTimeBlockLibrary.separatorPieces[0])
                                }
                            }
                        }
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkbenchPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Assembled Blocks Area
            if (selectedBlocks.isNotEmpty()) {
                Text(
                    text = "Assembled Blocks (${selectedBlocks.size}):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkbenchTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedBlocks.forEachIndexed { index, piece ->
                        Surface(
                            color = if (piece.isLiteral) WorkbenchInset else WorkbenchPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (piece.isLiteral) WorkbenchBorder else WorkbenchPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = if (piece.tokenValue == " ") "[Space]" else piece.label,
                                    fontSize = 11.sp,
                                    fontFamily = if (piece.isLiteral) FontFamily.SansSerif else FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = if (piece.isLiteral) WorkbenchTextPrimary else WorkbenchPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove block",
                                    tint = WorkbenchTextMuted,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedBlocks.removeAt(index) }
                                )
                            }
                        }
                    }

                    // Quick Backspace button
                    Surface(
                        color = WorkbenchInset,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WorkbenchBorder),
                        modifier = Modifier.clickable {
                            if (selectedBlocks.isNotEmpty()) selectedBlocks.removeAt(selectedBlocks.size - 1)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Undo last block",
                                tint = WorkbenchTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Block Palette Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("📅 Date Pieces", "⏰ Time Pieces", "🔣 Separators")
                tabs.forEachIndexed { idx, title ->
                    val isSelected = activeBlockCategory == idx
                    Surface(
                        color = if (isSelected) WorkbenchPrimary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSelected) WorkbenchPrimary else WorkbenchBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeBlockCategory = idx }
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary,
                            modifier = Modifier.padding(vertical = 6.dp),
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Palette Block Pieces
            val activePieces = when (activeBlockCategory) {
                0 -> DateTimeBlockLibrary.datePieces
                1 -> DateTimeBlockLibrary.timePieces
                else -> DateTimeBlockLibrary.separatorPieces
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                activePieces.forEach { piece ->
                    Surface(
                        color = WorkbenchInset,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, WorkbenchBorder),
                        modifier = Modifier.clickable { selectedBlocks.add(piece) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = WorkbenchPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = piece.label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = WorkbenchTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
