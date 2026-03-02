package com.onserver1.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

/**
 * Dialog types for different states
 */
enum class DialogType {
    ERROR,
    SUCCESS,
    WARNING,
    INFO
}

/**
 * Unified beautiful dialog for showing messages across the app.
 *
 * @param show Whether to show the dialog
 * @param type The type of dialog (ERROR, SUCCESS, WARNING, INFO)
 * @param title Optional title - if null, a default title based on type is used
 * @param message The message to display
 * @param buttonText Optional button text - if null, a default based on type is used
 * @param onDismiss Called when the dialog is dismissed
 * @param onButtonClick Called when the button is clicked (defaults to onDismiss)
 * @param secondaryButtonText Optional secondary button text
 * @param onSecondaryClick Called when secondary button is clicked
 */
@Composable
fun AppDialog(
    show: Boolean,
    type: DialogType = DialogType.ERROR,
    title: String? = null,
    message: String,
    buttonText: String? = null,
    onDismiss: () -> Unit,
    onButtonClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    if (!show) return

    val (icon, iconBgColor, iconTint, defaultTitle, defaultButton) = when (type) {
        DialogType.ERROR -> DialogStyle(
            icon = Icons.Default.ErrorOutline,
            iconBgColor = ErrorRed.copy(alpha = 0.15f),
            iconTint = ErrorRed,
            defaultTitle = stringResource(R.string.dialog_error_title),
            defaultButton = stringResource(R.string.dialog_ok)
        )
        DialogType.SUCCESS -> DialogStyle(
            icon = Icons.Default.CheckCircle,
            iconBgColor = SuccessGreen.copy(alpha = 0.15f),
            iconTint = SuccessGreen,
            defaultTitle = stringResource(R.string.dialog_success_title),
            defaultButton = stringResource(R.string.dialog_ok)
        )
        DialogType.WARNING -> DialogStyle(
            icon = Icons.Default.Warning,
            iconBgColor = WarningOrange.copy(alpha = 0.15f),
            iconTint = WarningOrange,
            defaultTitle = stringResource(R.string.dialog_warning_title),
            defaultButton = stringResource(R.string.dialog_ok)
        )
        DialogType.INFO -> DialogStyle(
            icon = Icons.Default.Info,
            iconBgColor = InfoBlue.copy(alpha = 0.15f),
            iconTint = InfoBlue,
            defaultTitle = stringResource(R.string.dialog_info_title),
            defaultButton = stringResource(R.string.dialog_ok)
        )
    }

    val displayTitle = title ?: defaultTitle
    val displayButton = buttonText ?: defaultButton
    val d = LocalDimens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(d.corner24),
            colors = CardDefaults.cardColors(
                containerColor = CardDark
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = d.elevation8),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.space8)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(d.space24),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(d.dialogIconCircle)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(d.dialogIconSize),
                        tint = iconTint
                    )
                }

                Spacer(modifier = Modifier.height(d.space20))

                // Title
                Text(
                    text = displayTitle,
                    fontSize = d.font22,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(d.space12))

                // Message
                Text(
                    text = message,
                    fontSize = d.font16,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = d.lineHeight22
                )

                Spacer(modifier = Modifier.height(d.space24))

                // Primary Button
                Button(
                    onClick = { (onButtonClick ?: onDismiss)() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(d.dialogButtonHeight),
                    shape = RoundedCornerShape(d.corner12),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (type) {
                            DialogType.ERROR -> ErrorRed
                            DialogType.SUCCESS -> SuccessGreen
                            DialogType.WARNING -> WarningOrange
                            DialogType.INFO -> AccentYellow
                        },
                        contentColor = when (type) {
                            DialogType.INFO -> Color.Black
                            else -> Color.White
                        }
                    )
                ) {
                    Text(
                        text = displayButton,
                        fontSize = d.font16,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Secondary Button (optional)
                if (secondaryButtonText != null) {
                    Spacer(modifier = Modifier.height(d.space8))
                    TextButton(
                        onClick = { onSecondaryClick?.invoke() ?: onDismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(d.buttonSmallHeight),
                        shape = RoundedCornerShape(d.corner12)
                    ) {
                        Text(
                            text = secondaryButtonText,
                            fontSize = d.font14,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Internal data class for dialog styling
 */
private data class DialogStyle(
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color,
    val defaultTitle: String,
    val defaultButton: String
)
