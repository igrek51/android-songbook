package igrek.songbook.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween

val debugAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 2000, delayMillis = 500)