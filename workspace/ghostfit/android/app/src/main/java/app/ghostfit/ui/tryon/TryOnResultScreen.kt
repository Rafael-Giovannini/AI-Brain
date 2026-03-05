package app.ghostfit.ui.tryon

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import app.ghostfit.domain.TryOnSession
import app.ghostfit.domain.TryOnStatus
import java.io.File
import java.io.FileOutputStream

@Composable
fun TryOnResultScreen(
    session: TryOnSession,
    onRegenerate: () -> Unit = {},
    onChangePhoto: () -> Unit = {},
    onThumbsUp: () -> Unit = {},
    onThumbsDown: () -> Unit = {},
    onShare: () -> Unit = {},
    onClose: () -> Unit = {},
    onUpgrade: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when (session.status) {
            TryOnStatus.IDLE -> { /* Nothing to show */ }
            TryOnStatus.CAPTURING -> LoadingState("Capturando tela...")
            TryOnStatus.DETECTING -> LoadingState("Detectando roupa...")
            TryOnStatus.GENERATING -> LoadingState("Gerando imagem...")
            TryOnStatus.NO_GARMENT -> NoGarmentDetected(onClose = onClose)
            TryOnStatus.ERROR -> ErrorState(
                message = session.errorMessage ?: "Erro desconhecido",
                isLimitError = session.errorMessage?.contains("Limite") == true,
                onRetry = onRegenerate,
                onUpgrade = onUpgrade,
                onClose = onClose
            )
            TryOnStatus.DONE -> ResultView(
                session = session,
                onRegenerate = onRegenerate,
                onChangePhoto = onChangePhoto,
                onThumbsUp = onThumbsUp,
                onThumbsDown = onThumbsDown,
                onShare = onShare,
                onClose = onClose
            )
        }
    }
}

@Composable
private fun LoadingState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun NoGarmentDetected(onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhuma roupa detectada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tente em uma página de produto com uma imagem clara da roupa.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhuma tentativa foi consumida.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onClose) {
                Text("Entendi")
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    isLimitError: Boolean,
    onRetry: () -> Unit,
    onUpgrade: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLimitError) "Limite atingido" else "Ops! Algo deu errado",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isLimitError) {
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fazer upgrade")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar")
                }
            } else {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tentar novamente")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}

@Composable
private fun ResultView(
    session: TryOnSession,
    onRegenerate: () -> Unit,
    onChangePhoto: () -> Unit,
    onThumbsUp: () -> Unit,
    onThumbsDown: () -> Unit,
    onShare: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onClose) {
                Text("Fechar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Generated image
        session.generatedImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Resultado do provador virtual",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duration info
        Text(
            text = "Gerado em ${session.durationMs / 1000}s via ${session.modelUsed ?: "AI"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback row
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbs down
            IconButton(
                onClick = onThumbsDown,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    )
            ) {
                Text("👎", fontSize = 20.sp)
            }

            // Thumbs up
            IconButton(
                onClick = onThumbsUp,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
            ) {
                Text("👍", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRegenerate,
                modifier = Modifier.weight(1f)
            ) {
                Text("Tentar novamente")
            }
            OutlinedButton(
                onClick = onChangePhoto,
                modifier = Modifier.weight(1f)
            ) {
                Text("Trocar foto")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Share button
        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Compartilhar")
        }
    }
}

/**
 * Share the generated try-on image via Android share sheet.
 */
fun shareTryOnImage(context: android.content.Context, bitmap: Bitmap) {
    try {
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "ghostfit_tryon_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Experimentei com GhostFit! \uD83D\uDC7B")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar via"))
    } catch (_: Exception) {
        // Sharing failed silently
    }
}
