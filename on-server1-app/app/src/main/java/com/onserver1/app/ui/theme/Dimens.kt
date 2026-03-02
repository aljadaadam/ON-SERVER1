package com.onserver1.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive dimensions system.
 * Scales all UI sizes based on actual screen dimensions.
 * Reference design: 360dp width × 780dp height.
 */
data class Dimens(
    private val ws: Float = 1f,  // width scale
    private val hs: Float = 1f,  // height scale
) {
    // ================== Spacing ==================
    val space2: Dp = (2 * ws).dp
    val space4: Dp = (4 * ws).dp
    val space6: Dp = (6 * ws).dp
    val space8: Dp = (8 * ws).dp
    val space10: Dp = (10 * ws).dp
    val space12: Dp = (12 * ws).dp
    val space16: Dp = (16 * ws).dp
    val space20: Dp = (20 * ws).dp
    val space24: Dp = (24 * ws).dp
    val space28: Dp = (28 * ws).dp
    val space32: Dp = (32 * ws).dp
    val space40: Dp = (40 * ws).dp
    val space48: Dp = (48 * ws).dp

    // ================== Screen Padding ==================
    val screenPadding: Dp = (16 * ws).dp
    val cardPadding: Dp = (12 * ws).dp
    val contentPadding: Dp = (20 * ws).dp

    // ================== Font Sizes ==================
    val font10: TextUnit = (10 * ws).sp
    val font11: TextUnit = (11 * ws).sp
    val font12: TextUnit = (12 * ws).sp
    val font13: TextUnit = (13 * ws).sp
    val font14: TextUnit = (14 * ws).sp
    val font15: TextUnit = (15 * ws).sp
    val font16: TextUnit = (16 * ws).sp
    val font18: TextUnit = (18 * ws).sp
    val font20: TextUnit = (20 * ws).sp
    val font22: TextUnit = (22 * ws).sp
    val font24: TextUnit = (24 * ws).sp
    val font28: TextUnit = (28 * ws).sp
    val font32: TextUnit = (32 * ws).sp
    val font36: TextUnit = (36 * ws).sp

    // ================== Icon Sizes ==================
    val icon16: Dp = (16 * ws).dp
    val icon18: Dp = (18 * ws).dp
    val icon20: Dp = (20 * ws).dp
    val icon24: Dp = (24 * ws).dp
    val icon28: Dp = (28 * ws).dp
    val icon32: Dp = (32 * ws).dp
    val icon40: Dp = (40 * ws).dp
    val icon48: Dp = (48 * ws).dp

    // ================== Component Heights ==================
    val balanceStripHeight: Dp = (50 * hs).dp
    val bottomNavHeight: Dp = (64 * hs).dp
    val productCardWidth: Dp = (145 * ws).dp
    val productCardHeight: Dp = (195 * hs).dp
    val productImageHeight: Dp = (105 * hs).dp
    val bannerHeight: Dp = (145 * hs).dp
    val quickActionSize: Dp = (58 * ws).dp
    val avatarSize: Dp = (80 * ws).dp

    // ================== Buttons ==================
    val buttonHeight: Dp = (50 * hs).dp
    val buttonSmallHeight: Dp = (40 * hs).dp

    // ================== Corner Radius ==================
    val corner8: Dp = (8 * ws).dp
    val corner12: Dp = (12 * ws).dp
    val corner16: Dp = (16 * ws).dp
    val corner20: Dp = (20 * ws).dp
    val corner24: Dp = (24 * ws).dp

    // ================== Elevation ==================
    val elevation4: Dp = (4 * ws).dp
    val elevation8: Dp = (8 * ws).dp

    // ================== Dialog ==================
    val dialogIconCircle: Dp = (80 * ws).dp
    val dialogIconSize: Dp = (44 * ws).dp
    val dialogButtonHeight: Dp = (52 * hs).dp

    // ================== OTP ==================
    val otpFontSize: TextUnit = (28 * ws).sp
    val otpLetterSpacing: TextUnit = (8 * ws).sp

    // ================== Line Height ==================
    val lineHeight22: TextUnit = (22 * ws).sp
}

val LocalDimens = staticCompositionLocalOf { Dimens() }

@Composable
fun rememberDimens(): Dimens {
    val config = LocalConfiguration.current
    val widthScale = (config.screenWidthDp / 360f).coerceIn(0.9f, 1.6f)
    val heightScale = (config.screenHeightDp / 780f).coerceIn(0.9f, 1.6f)

    return remember(config.screenWidthDp, config.screenHeightDp) {
        Dimens(ws = widthScale, hs = heightScale)
    }
}
