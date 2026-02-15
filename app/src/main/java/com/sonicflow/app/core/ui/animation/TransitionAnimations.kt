package com.sonicflow.app.core.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith

object TransitionAnimations {

    private const val DURATION_FAST = 200
    private const val DURATION_NORMAL = 300
    private const val DURATION_SLOW = 400

    private val fastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    private val linearOutSlowIn = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    fun slideVertical(): ContentTransform {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 450,
                easing = fastOutSlowIn
            )
        ) + fadeIn(
            animationSpec = tween(DURATION_NORMAL)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(450)
        ) togetherWith slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(
                durationMillis = 450,
                easing = fastOutSlowIn
            )
        ) + fadeOut(
            animationSpec = tween(250)
        ) + scaleOut(
            targetScale = 0.95f
        )
    }

    fun slideHorizontalWithScale(): ContentTransform {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(DURATION_SLOW, easing = fastOutSlowIn)
        ) + fadeIn(
            animationSpec = tween(DURATION_NORMAL)
        ) + scaleIn(
            initialScale = 0.9f
        ) togetherWith slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(DURATION_SLOW, easing = fastOutSlowIn)
        ) + fadeOut(
            animationSpec = tween(250)
        ) + scaleOut(
            targetScale = 0.92f
        )
    }

    fun slideBack(): ContentTransform {
        return slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(DURATION_SLOW, easing = fastOutSlowIn)
        ) + fadeIn(
            animationSpec = tween(DURATION_NORMAL)
        ) + scaleIn(
            initialScale = 0.92f
        ) togetherWith slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(DURATION_SLOW, easing = fastOutSlowIn)
        ) + fadeOut(
            animationSpec = tween(250)
        ) + scaleOut(
            targetScale = 0.9f
        )
    }

    fun fade(): ContentTransform =
        fadeIn(animationSpec = tween(DURATION_FAST)) togetherWith
                fadeOut(animationSpec = tween(DURATION_FAST))

    fun dialogEnter(): EnterTransition =
        scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(DURATION_FAST, easing = linearOutSlowIn)
        ) + fadeIn()

    fun dialogExit(): ExitTransition =
        scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(DURATION_FAST, easing = fastOutSlowIn)
        ) + fadeOut()

    /**
     * Shared element transition
     */
    fun sharedElement(): ContentTransform {
        return fadeIn(
            animationSpec = tween(DURATION_NORMAL)
        ) togetherWith fadeOut(
            animationSpec = tween(DURATION_NORMAL)
        )
    }
}