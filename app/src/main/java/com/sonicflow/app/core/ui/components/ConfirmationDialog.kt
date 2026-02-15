package com.sonicflow.app.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.AnimatedVisibility
import com.sonicflow.app.core.ui.animation.TransitionAnimations
import kotlinx.coroutines.launch

/**
 * Dialog de confirmation réutilisable
 * Évite la duplication de code
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Warning,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = TransitionAnimations.dialogEnter(),
        exit = TransitionAnimations.dialogExit()
    ) {
        AlertDialog(
            onDismissRequest = {
                isVisible = false
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(200)
                    onDismiss()
                }
            },
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isVisible = false
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(200)
                            onConfirm()
                        }
                    },
                    colors = if (isDestructive) {
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ButtonDefaults.textButtonColors()
                    }
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isVisible = false
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(200)
                            onDismiss()
                        }
                    }
                ) {
                    Text(dismissText)
                }
            }
        )
    }
}