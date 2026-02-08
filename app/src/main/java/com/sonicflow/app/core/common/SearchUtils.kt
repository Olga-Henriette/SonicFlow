package com.sonicflow.app.core.common

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun <T> rememberDebouncedValue(
    value: T,
    delayMillis: Long = 300L
): T {
    var debouncedValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        delay(delayMillis)
        debouncedValue = value
    }

    return debouncedValue
}