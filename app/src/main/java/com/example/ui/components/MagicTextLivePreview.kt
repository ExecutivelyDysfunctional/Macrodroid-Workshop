package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlockingWarningBg
import com.example.ui.theme.BlockingWarningBorder
import com.example.ui.theme.BlockingWarningText
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchBorderSubtle
import com.example.ui.theme.WorkbenchInset
import com.example.ui.theme.WorkbenchOnPrimary
import com.example.ui.theme.WorkbenchPrimary
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchSurfaceElevated
import com.example.ui.theme.WorkbenchSurfaceVariant
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary
import com.example.util.BracketStyle
import com.example.util.MagicTextCategory
import com.example.util.MagicTextEvaluator
import com.example.util.TokenEvaluationDetail
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MagicTextLivePreviewCard(
    initialText: String = "Battery is at [battery]% on [device_manufacturer] [device_model] at [hour_12_pad]:[minute_pad] [am_pm]",
    bracketStyle: BracketStyle = BracketStyle.CURLY,
    onSavePreset: ((name: String, content: String, tags: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var inputText by remember {
        mutableStateOf(bracketStyle.formatTemplate(initialText))
    }

    var selectedPaletteCategory by remember { mutableStateOf(MagicTextCategory.ALL) }
    var paletteSearchQuery by remember { mutableStateOf("") }
    var isPaletteExpanded by remember { mutableStateOf(false) }
    var isBreakdownExpanded by remember { mutableStateOf(true) }
    var selectedTokenForInfo by remember { mutableStateOf<TokenEvaluationDetail?>(null) }

    var recentlyCopiedType by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var presetTagsInput by remember { mutableStateOf("Live Preview, Formula") }

    // Evaluation updates dynamically in real time
    val evaluationResult = remember(inputText, context) {
        MagicTextEvaluator.evaluateMagicTextDetailed(inputText, context)
    }

    LaunchedEffect(recentlyCopiedType) {
        if (recentlyCopiedType != null) {
            delay(2000)
            recentlyCopiedType = null
        }
    }

    // Pulse animation for live data indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    fun copyToClipboard(text: String, label: String, copyKey: String) {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
        recentlyCopiedType = copyKey
        Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, WorkbenchBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("magic_text_live_preview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(WorkbenchPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = WorkbenchPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Live Magic Text Preview",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkbenchTextPrimary
                        )
                        Text(
                            text = "Real-time token resolution & evaluated rendering",
                            fontSize = 11.sp,
                            color = WorkbenchTextSecondary
                        )
                    }
                }

                // Live Status Badge
                Surface(
                    color = if (evaluationResult.unresolvedCount > 0) {
                        BlockingWarningBg
                    } else if (evaluationResult.isLiveEvaluated) {
                        StatusDeployed.copy(alpha = 0.12f)
                    } else {
                        WorkbenchInset
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (evaluationResult.unresolvedCount > 0) BlockingWarningBorder
                        else if (evaluationResult.isLiveEvaluated) StatusDeployed.copy(alpha = 0.4f)
                        else WorkbenchBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .alpha(if (evaluationResult.isLiveEvaluated) pulseAlpha else 1f)
                                .background(
                                    if (evaluationResult.unresolvedCount > 0) BlockingWarningText
                                    else if (evaluationResult.isLiveEvaluated) StatusDeployed
                                    else WorkbenchTextSecondary
                                )
                        )
                        Text(
                            text = when {
                                evaluationResult.unresolvedCount > 0 -> "${evaluationResult.unresolvedCount} Unresolved"
                                evaluationResult.isLiveEvaluated -> "Live System Data"
                                evaluationResult.detectedTokens.isNotEmpty() -> "Live Sample"
                                else -> "Literal Text"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                evaluationResult.unresolvedCount > 0 -> BlockingWarningText
                                evaluationResult.isLiveEvaluated -> StatusDeployed
                                else -> WorkbenchTextSecondary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Starter Sample Templates Row
            Text(
                text = "QUICK RECIPES & SAMPLES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = WorkbenchTextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MagicTextEvaluator.sampleLivePreviewTemplates.forEach { templateItem ->
                    val isSelected = inputText == bracketStyle.formatTemplate(templateItem.template)
                    Surface(
                        color = if (isSelected) WorkbenchPrimary.copy(alpha = 0.2f) else WorkbenchInset,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) WorkbenchPrimary else WorkbenchBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                inputText = bracketStyle.formatTemplate(templateItem.template)
                            }
                            .testTag("sample_template_${templateItem.title.replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = templateItem.iconEmoji, fontSize = 12.sp)
                            Text(
                                text = templateItem.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WorkbenchPrimary else WorkbenchTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Formula Input Box
            Text(
                text = "MAGIC TEXT FORMULA (INPUT)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = WorkbenchTextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp, max = 130.dp)
                    .testTag("magic_text_input_field"),
                placeholder = {
                    Text(
                        text = "Type or paste Magic Text e.g. Current battery: {battery}% on {device_model}",
                        fontSize = 12.sp,
                        color = WorkbenchTextMuted
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = WorkbenchPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WorkbenchInset,
                    unfocusedContainerColor = WorkbenchInset,
                    focusedBorderColor = WorkbenchPrimary,
                    unfocusedBorderColor = WorkbenchBorder
                ),
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { inputText = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear input",
                                tint = WorkbenchTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )

            // Input Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${inputText.length} chars • ${evaluationResult.detectedTokens.size} tokens",
                    fontSize = 11.sp,
                    color = WorkbenchTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            copyToClipboard(inputText, "Magic Text Formula", "formula")
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = if (recentlyCopiedType == "formula") Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (recentlyCopiedType == "formula") StatusDeployed else WorkbenchPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (recentlyCopiedType == "formula") "Copied Formula!" else "Copy Formula",
                            fontSize = 11.sp,
                            color = if (recentlyCopiedType == "formula") StatusDeployed else WorkbenchPrimary
                        )
                    }

                    TextButton(
                        onClick = { isPaletteExpanded = !isPaletteExpanded },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaletteExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                            contentDescription = null,
                            tint = WorkbenchTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaletteExpanded) "Hide Tokens" else "Insert Tokens",
                            fontSize = 11.sp,
                            color = WorkbenchTextSecondary
                        )
                    }
                }
            }

            // Collapsible Quick-Insert Token Palette
            AnimatedVisibility(
                visible = isPaletteExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .background(WorkbenchInset, RoundedCornerShape(8.dp))
                        .border(1.dp, WorkbenchBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    // Category Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MagicTextCategory.values().forEach { cat ->
                            val isSelected = selectedPaletteCategory == cat
                            Surface(
                                color = if (isSelected) WorkbenchPrimary else WorkbenchSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) WorkbenchPrimary else WorkbenchBorderSubtle
                                ),
                                modifier = Modifier.clickable { selectedPaletteCategory = cat }
                            ) {
                                Text(
                                    text = cat.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Palette Search
                    OutlinedTextField(
                        value = paletteSearchQuery,
                        onValueChange = { paletteSearchQuery = it },
                        placeholder = { Text("Filter token palette...", fontSize = 11.sp, color = WorkbenchTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = WorkbenchTextPrimary),
                        shape = RoundedCornerShape(6.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = WorkbenchTextMuted, modifier = Modifier.size(14.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchSurface,
                            unfocusedContainerColor = WorkbenchSurface,
                            focusedBorderColor = WorkbenchPrimary,
                            unfocusedBorderColor = WorkbenchBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Token Chips Grid
                    val availableTokens = remember(selectedPaletteCategory, paletteSearchQuery) {
                        MagicTextEvaluator.allMagicTextTokens.filter { item ->
                            val matchesCategory = selectedPaletteCategory == MagicTextCategory.ALL || item.category == selectedPaletteCategory
                            val matchesSearch = paletteSearchQuery.isBlank() ||
                                item.token.contains(paletteSearchQuery, ignoreCase = true) ||
                                item.label.contains(paletteSearchQuery, ignoreCase = true) ||
                                item.description.contains(paletteSearchQuery, ignoreCase = true)
                            matchesCategory && matchesSearch
                        }
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTokens.forEach { tokenItem ->
                            val formatted = bracketStyle.formatToken(tokenItem.token)
                            Surface(
                                color = WorkbenchSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier
                                    .clickable {
                                        inputText = if (inputText.isBlank()) formatted else "$inputText $formatted"
                                    }
                                    .testTag("insert_token_${tokenItem.token}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = WorkbenchPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = formatted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkbenchTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Real-Time Evaluated Output Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME EVALUATED OUTPUT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkbenchTextMuted,
                    letterSpacing = 0.8.sp
                )

                if (evaluationResult.evaluatedText.isNotBlank()) {
                    Text(
                        text = "${evaluationResult.evaluatedText.length} chars",
                        fontSize = 11.sp,
                        color = WorkbenchTextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = WorkbenchInset,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (evaluationResult.unresolvedCount > 0) BlockingWarningBorder
                    else WorkbenchBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (evaluationResult.evaluatedText.isNotBlank()) {
                            copyToClipboard(evaluationResult.evaluatedText, "Evaluated Output", "evaluated")
                        }
                    }
                    .testTag("evaluated_output_box")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (evaluationResult.evaluatedText.isBlank()) {
                        Text(
                            text = "(Evaluated output will appear here as you type or pick tokens)",
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = WorkbenchTextMuted
                        )
                    } else {
                        Text(
                            text = evaluationResult.evaluatedText,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            color = WorkbenchTextPrimary,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Output Bottom Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left feedback hint
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (evaluationResult.unresolvedCount > 0) Icons.Default.ErrorOutline else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (evaluationResult.unresolvedCount > 0) BlockingWarningText else StatusDeployed,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (evaluationResult.unresolvedCount > 0) "Has unresolved variables" else "Live evaluated & ready",
                                fontSize = 11.sp,
                                color = if (evaluationResult.unresolvedCount > 0) BlockingWarningText else WorkbenchTextSecondary
                            )
                        }

                        // Copy Evaluated Output Button
                        Button(
                            onClick = {
                                if (evaluationResult.evaluatedText.isNotBlank()) {
                                    copyToClipboard(evaluationResult.evaluatedText, "Evaluated Output", "evaluated")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (recentlyCopiedType == "evaluated") StatusDeployed else WorkbenchPrimary,
                                contentColor = WorkbenchOnPrimary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = if (recentlyCopiedType == "evaluated") Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (recentlyCopiedType == "evaluated") "Copied Output!" else "Copy Output",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 3. Detected Tokens Breakdown Inspector
            if (evaluationResult.detectedTokens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBreakdownExpanded = !isBreakdownExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DETECTED TOKENS (${evaluationResult.detectedTokens.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkbenchTextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Surface(
                            color = WorkbenchPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${evaluationResult.resolvedCount} resolved",
                                fontSize = 10.sp,
                                color = WorkbenchPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isBreakdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = WorkbenchTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = isBreakdownExpanded) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            evaluationResult.detectedTokens.forEach { tokenDetail ->
                                val isTokenSelected = selectedTokenForInfo == tokenDetail
                                Surface(
                                    color = if (!tokenDetail.isResolved) BlockingWarningBg
                                    else if (isTokenSelected) WorkbenchPrimary.copy(alpha = 0.2f)
                                    else WorkbenchInset,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (!tokenDetail.isResolved) BlockingWarningBorder
                                        else if (isTokenSelected) WorkbenchPrimary
                                        else WorkbenchBorder
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            selectedTokenForInfo = if (selectedTokenForInfo == tokenDetail) null else tokenDetail
                                        }
                                        .testTag("detected_token_chip_${tokenDetail.tokenKey}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = tokenDetail.tokenRaw,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (!tokenDetail.isResolved) BlockingWarningText else WorkbenchPrimary
                                        )
                                        Text(
                                            text = "➔",
                                            fontSize = 10.sp,
                                            color = WorkbenchTextMuted
                                        )
                                        Text(
                                            text = tokenDetail.evaluatedValue,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = if (!tokenDetail.isResolved) BlockingWarningText else WorkbenchTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (tokenDetail.isLive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(StatusDeployed)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Token Inspector Detail Box
                        selectedTokenForInfo?.let { detail ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = WorkbenchSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${detail.label} (${detail.category.displayName})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WorkbenchTextPrimary
                                        )
                                        Text(
                                            text = if (detail.isLive) "Evaluated with live device sensor/state" else "Evaluated using standard sample data",
                                            fontSize = 10.sp,
                                            color = WorkbenchTextSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = { selectedTokenForInfo = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = WorkbenchTextMuted, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Action: Save as Custom Preset
            if (onSavePreset != null) {
                OutlinedButton(
                    onClick = {
                        presetNameInput = MagicTextEvaluator.deriveLabel(inputText)
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("save_live_preview_preset_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, WorkbenchBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WorkbenchTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = WorkbenchPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save as Custom Preset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog && onSavePreset != null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Live Magic Text Preset",
                    style = MaterialTheme.typography.titleMedium,
                    color = WorkbenchTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = presetTagsInput,
                        onValueChange = { presetTagsInput = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        color = WorkbenchInset,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = inputText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = WorkbenchPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onSavePreset(presetNameInput.trim(), inputText.trim(), presetTagsInput.trim())
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkbenchPrimary)
                ) {
                    Text("Save Preset", color = WorkbenchOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurfaceElevated
        )
    }
}
