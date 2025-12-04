package sdf.bitt.hydromate.utils

// File: utils/DensityExt.kt

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Конвертирует Dp → Px (в @Composable-контексте)
 */
@Composable
fun Dp.dpToPx(): Float = with(LocalDensity.current) { this@dpToPx.toPx() }

/**
 * Конвертирует Px → Dp (в @Composable-контексте
 */
@Composable
fun Float.pxToDp(): Dp = with(LocalDensity.current) { this@pxToDp.toDp() }

/**
 * Конвертирует Int (px) → Dp
 */
@Composable
fun Int.pxToDp(): Dp = with(LocalDensity.current) { this@pxToDp.toDp() }

/**
 * Конвертирует Dp → Px как Int (округление до целого)
 */
@Composable
fun Dp.toPxInt(): Int = with(LocalDensity.current) { this@toPxInt.toPx().toInt() }

/**
 * Конвертирует Dp → Px как Float
 */
@Composable
fun Dp.toPxFloat(): Float = with(LocalDensity.current) { this@toPxFloat.toPx() }