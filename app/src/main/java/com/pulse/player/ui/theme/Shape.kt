package com.pulse.player.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val HeroAlbumArtShape = RoundedCornerShape(28.dp)
val TrackThumbnailShape = RoundedCornerShape(12.dp)
val GlassPodShape = RoundedCornerShape(24.dp)
val TagBadgeShape = RoundedCornerShape(8.dp)
val CapsuleShape = RoundedCornerShape(12.dp)

val BPlayerShapes = PulseShapes
val ExpressiveHeroShape = RoundedCornerShape(32.dp)
val ExpressivePillShape = RoundedCornerShape(12.dp)
val HeroInnerShape = RoundedCornerShape(24.dp)
val StatPillShape = RoundedCornerShape(16.dp)
val ListContainerShape = RoundedCornerShape(28.dp)
val BadgePillShape = RoundedCornerShape(8.dp)
val NowPlayingCardShape = RoundedCornerShape(18.dp)
val TrackRowShape = RoundedCornerShape(16.dp)
val ArtworkShape = RoundedCornerShape(12.dp)

/**
 * Material 3 Expressive 10-step corner scale primitives.
 */
object M3ExpressiveShape {
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val LargeIncreased = RoundedCornerShape(20.dp)
    val ExtraLarge = RoundedCornerShape(28.dp)
    val ExtraLargeIncreased = RoundedCornerShape(32.dp)
    val ExtraExtraLarge = RoundedCornerShape(48.dp)
    val Full = RoundedCornerShape(percent = 50)

    // Asymmetric shapes for connected components (e.g. ConnectedButtonGroup)
    val GroupLeft = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 4.dp, bottomEnd = 4.dp)
    val GroupMiddle = RoundedCornerShape(4.dp)
    val GroupRight = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp)
    val GroupSingle = RoundedCornerShape(20.dp)
}
