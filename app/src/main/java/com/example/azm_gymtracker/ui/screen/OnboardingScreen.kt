package com.example.azm_gymtracker.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import com.example.azm_gymtracker.ui.animation.azmPhysicsPress
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmNeonGreen
import com.example.azm_gymtracker.ui.theme.AzmPrimaryGradient
import com.example.azm_gymtracker.ui.theme.LocalThemeIsDark

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
private val ProfileColors = listOf(
    "#8CFF68", // Brand Neon Green
    "#42E879", // Emerald
    "#00D2FF", // Cyan
    "#FFB74D", // Amber
    "#FF5252", // Coral Red
    "#E040FB", // Purple
)
data class OnboardingSlideData(
    val badge: String,
    val title: String,
    val description: String,
    val imageRes: Int,
)

private val OnboardingSlides = listOf(
    OnboardingSlideData(
        badge = "تمرين تشاركي",
        title = "هاتف واحد. عزم يجمعكم.",
        description = "تمرين الرفاق صار أوضح وأسرع. جهاز واحد تتناوبون عليه داخل الجيم لمتابعة التكرارات والأوزان بدون فوضى وبأعلى تركيز.",
        imageRes = R.drawable.img_onboarding_1,
    ),
    OnboardingSlideData(
        badge = "سرعة وإتقان",
        title = "سجّل جولتك بثوانٍ.",
        description = "واجهة مصممة لمنطقة الإبهام واليد الواحدة أثناء التمرين، مع مؤقت راحة ذكي وتبديل سلس ومباشر بين أدوار المتمرنين.",
        imageRes = R.drawable.img_onboarding_2,
    ),
    OnboardingSlideData(
        badge = "أرقام قياسية",
        title = "سجّل تقدمك، جولة بجولة.",
        description = "تابع تطور أوزانك وحجم تمرينك بدقة، واكتشف أرقامك القياسية في كل تمرين مع متابعة دقيقة لمسار قوتك المستمر.",
        imageRes = R.drawable.img_onboarding_3,
    ),
)

@Composable
fun OnboardingScreen(
    nameError: String?,
    onCreateMember: (name: String, colorHex: String) -> Unit,
    onClearNameError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    var currentStep by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(ProfileColors.first()) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Action Bar: "تخطي" (Skip) or Back navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentStep > 0) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { currentStep -= 1 },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "السابق",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }

                if (currentStep < 3) {
                    TextButton(
                        onClick = { currentStep = 3 },
                    ) {
                        Text(
                            text = "تخطي",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Animated Screen Content Area
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val springSpec = spring<IntOffset>(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    )
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = springSpec) { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = springSpec) { width -> width } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = springSpec) { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = springSpec) { width -> -width } + fadeOut()
                    }
                },
                label = "onboardingPager",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { step ->
                if (step < 3) {
                    val slide = OnboardingSlides[step]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Realistic Photographic Slide Artwork Card
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .shadow(
                                    24.dp,
                                    RoundedCornerShape(28.dp),
                                    spotColor = if (isDark) AzmNeonGreen.copy(alpha = 0.35f) else Color(0x18000000),
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(slide.imageRes),
                                contentDescription = slide.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Category Tag Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = slide.badge,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                } else {
                    // Step 3: Member Profile Creation Step
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Official AZM Folded Ayn Emblem
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(
                                    12.dp,
                                    RoundedCornerShape(26.dp),
                                    spotColor = if (isDark) AzmNeonGreen.copy(alpha = 0.25f) else Color(0x15000000),
                                )
                                .clip(RoundedCornerShape(26.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(26.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "شعار عزم",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "ابدأ رحلتك — أضف المتمرن الأول",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "أدخل اسمك واختر لونك المفضل لتمييز جولاتك في التمرين المشترك.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Name Input Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) onClearNameError()
                            },
                            label = { Text(stringResource(R.string.member_name_hint)) },
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Color Selector
                        Text(
                            text = stringResource(R.string.member_color_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            ProfileColors.forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = selectedColor == hex
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColor = hex }
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(3.dp, if (isDark) Color.White else MaterialTheme.colorScheme.primary, CircleShape)
                                            } else {
                                                Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            }
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Navigation Controls: Dots + Next CTA / Create CTA
            if (currentStep < 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Page Indicator Dots with Physics Elastic Width
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            val isActive = currentStep == index
                            val dotWidth by animateDpAsState(
                                targetValue = if (isActive) 28.dp else 8.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow,
                                ),
                                label = "dotWidth_$index",
                            )
                            val dotColor by animateColorAsState(
                                targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "dotColor_$index",
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(dotWidth)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                        }
                    }

                    // Next Step Button with Physics Press
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AzmPrimaryGradient)
                            .azmPhysicsPress(pressedScale = 0.94f) { currentStep += 1 }
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "التالي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF041C0E),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color(0xFF041C0E),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            } else {
                // Profile Creation Hero Button with Physics Press
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AzmPrimaryGradient)
                        .azmPhysicsPress(pressedScale = 0.95f) { onCreateMember(name, selectedColor) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_create_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF041C0E),
                    )
                }
            }
        }
    }
}
