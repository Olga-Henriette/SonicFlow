package com.sonicflow.app.core.common

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

fun buildHighlightedText(
    fullText: String,
    query: String,
    highlightColor: Color,
    isBold: Boolean = true
): AnnotatedString {
    return buildAnnotatedString {
        val lowerText = fullText.lowercase()
        val lowerQuery = query.lowercase()

        if (query.isEmpty() || !lowerText.contains(lowerQuery)) {
            append(fullText)
            return@buildAnnotatedString
        }

        var start = 0
        while (start < fullText.length) {
            val index = lowerText.indexOf(lowerQuery, start)
            if (index == -1) {
                append(fullText.substring(start))
                break
            }

            append(fullText.substring(start, index))

            withStyle(style = SpanStyle(
                color = highlightColor,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
            ) {
                append(fullText.substring(index, index + query.length))
            }

            start = index + query.length
        }
    }
}