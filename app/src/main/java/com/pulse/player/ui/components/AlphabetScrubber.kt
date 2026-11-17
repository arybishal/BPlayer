package com.pulse.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.player.ui.theme.LocalExtendedColors

@Composable
fun AlphabetScrubber(
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = remember { ('A'..'Z').toList() + '#' }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    val extendedColors = LocalExtendedColors.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .padding(end = 4.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val itemHeight = size.height / alphabet.size
                            val index = (offset.y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
                            activeLetter = alphabet[index]
                            onLetterSelected(alphabet[index])
                        },
                        onDragEnd = { activeLetter = null },
                        onDragCancel = { activeLetter = null },
                        onVerticalDrag = { change, _ ->
                            val itemHeight = size.height / alphabet.size
                            val index = (change.position.y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
                            val letter = alphabet[index]
                            if (activeLetter != letter) {
                                activeLetter = letter
                                onLetterSelected(letter)
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { letter ->
                Text(
                    text = letter.toString(),
                    fontSize = 10.sp,
                    color = if (activeLetter == letter) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceSubtle,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Active Letter Floating Bubble
        activeLetter?.let { letter ->
            Box(
                modifier = Modifier
                    .padding(end = 36.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
