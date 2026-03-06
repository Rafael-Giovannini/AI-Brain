package app.ghostfit.ui.subscription

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import app.ghostfit.domain.BillingProvider
import app.ghostfit.domain.PurchaseEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Upgrade screen showing plan options: monthly subscription and one-time pack.
 *
 * FR-017: Offer packs (avulsos) and monthly subscription for unlimited use.
 * Story 4 Scenario 2: Free user hits limit → upgrade screen with plan options.
 * Story 4 Scenario 5: Monthly subscription removes limits and ads.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    productDetailsFlow: StateFlow<Map<String, ProductDetails>>,
    purchaseEventFlow: StateFlow<PurchaseEvent?> = MutableStateFlow(null),
    onPurchase: (productId: String) -> Unit = {},
    onPurchaseEventConsumed: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val productDetails by productDetailsFlow.collectAsState()
    val purchaseEvent by purchaseEventFlow.collectAsState()
    var selectedPlan by remember { mutableStateOf(BillingProvider.SKU_MONTHLY) }
    var purchaseSuccess by remember { mutableStateOf(false) }

    val monthlyDetails = productDetails[BillingProvider.SKU_MONTHLY]
    val packDetails = productDetails[BillingProvider.SKU_PACK_10]

    // React to purchase events
    LaunchedEffect(purchaseEvent) {
        when (purchaseEvent) {
            is PurchaseEvent.Success -> {
                purchaseSuccess = true
                onPurchaseEventConsumed()
            }
            is PurchaseEvent.Cancelled -> {
                onPurchaseEventConsumed()
            }
            is PurchaseEvent.Error -> {
                onPurchaseEventConsumed()
            }
            null -> { /* no-op */ }
        }
    }

    // Auto-close after showing success briefly
    if (purchaseSuccess) {
        PurchaseSuccessScreen(onContinue = onClose)
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Desbloqueie o GhostFit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Suas 3 tentativas gratuitas acabaram hoje. Faça upgrade para continuar experimentando roupas!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly subscription card
            PlanCard(
                title = "Premium Mensal",
                description = "Tentativas ilimitadas + sem anúncios",
                price = monthlyDetails.formattedMonthlyPrice() ?: "R\$ 14,90/mês",
                isSelected = selectedPlan == BillingProvider.SKU_MONTHLY,
                isRecommended = true,
                benefits = listOf(
                    "Tentativas ilimitadas por dia",
                    "Sem anúncios",
                    "Cancele quando quiser"
                ),
                onClick = { selectedPlan = BillingProvider.SKU_MONTHLY }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // One-time pack card
            PlanCard(
                title = "Pacote Avulso",
                description = "10 tentativas extras",
                price = packDetails.formattedPrice() ?: "R\$ 4,90",
                isSelected = selectedPlan == BillingProvider.SKU_PACK_10,
                isRecommended = false,
                benefits = listOf(
                    "+10 tentativas extras",
                    "Sem validade",
                    "Com anúncios"
                ),
                onClick = { selectedPlan = BillingProvider.SKU_PACK_10 }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Purchase button
            Button(
                onClick = { onPurchase(selectedPlan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (selectedPlan == BillingProvider.SKU_MONTHLY) {
                        "Assinar agora"
                    } else {
                        "Comprar pacote"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onClose) {
                Text("Continuar com plano gratuito")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    description: String,
    price: String,
    isSelected: Boolean,
    isRecommended: Boolean,
    benefits: List<String>,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isRecommended) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recomendado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PurchaseSuccessScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Upgrade realizado!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Seu plano foi ativado. Aproveite o GhostFit sem limites!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Extract formatted monthly price from ProductDetails subscription offer.
 */
private fun ProductDetails?.formattedMonthlyPrice(): String? {
    return this?.subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
        ?.formattedPrice
}

/**
 * Extract formatted one-time price from ProductDetails.
 */
private fun ProductDetails?.formattedPrice(): String? {
    return this?.oneTimePurchaseOfferDetails?.formattedPrice
}
