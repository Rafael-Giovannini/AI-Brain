package app.ghostfit.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme

@Composable
fun OverlayComposable(
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = onDragEnd
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👻",
            fontSize = 28.sp
        )
    }
}
