package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    onConsentGiven: (Boolean) -> Unit = {}
) {
    var step by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = step == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OnboardingStepOne(onNext = { step = 2 })
        }

        AnimatedVisibility(
            visible = step == 2,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PermissionsStepTwo(onAllowAll = { step = 3 }, onCustomize = { step = 3 })
        }

        AnimatedVisibility(
            visible = step == 3,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OnboardingStepThree(onContinue = { step = 4 })
        }

        AnimatedVisibility(
            visible = step == 4,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ConsentStepFour(
                onAccept = {
                    onConsentGiven(true)
                    onComplete()
                }
            )
        }
    }
}

// STEP 4: DPDP Act consent — explicit, plain-language, unbundled from the rest
// of onboarding. Per the PRD's compliance requirements (India DPDP Act
// 2023/Rules 2025), this consent must be clear, not pre-checked, and not a
// condition of using unrelated parts of the app — so only the AI/screenshot
// processing itself is gated on it (see AiPrivacyScreen for withdrawal).
@Composable
fun ConsentStepFour(onAccept: () -> Unit) {
    var consentChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PrivacyTip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your data, your control",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Before we process any screenshot, here's exactly what happens to it:",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConsentPoint(
                    icon = Icons.Default.PhoneAndroid,
                    text = "Text is extracted from your screenshot on-device — the image itself never leaves your phone."
                )
                ConsentPoint(
                    icon = Icons.Default.CloudQueue,
                    text = "Only the extracted text (never the image) may be sent to our AI provider for summarizing and categorizing — you can turn this off anytime in AI & Privacy settings."
                )
                ConsentPoint(
                    icon = Icons.Default.Gavel,
                    text = "You can access, correct, export, or permanently erase your data at any time, and withdraw this consent, from Settings → AI & Privacy."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { consentChecked = !consentChecked }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = consentChecked, onCheckedChange = { consentChecked = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I understand and consent to my screenshot text being processed as described above, in line with the DPDP Act, 2023.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Button(
                onClick = onAccept,
                enabled = consentChecked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .widthIn(max = 320.dp)
                    .testTag("consent_accept_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Accept & Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConsentPoint(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        )
    }
}

// STEP 1: Screenshot Intelligence
@Composable
fun OnboardingStepOne(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAXGVe5xi0wLTJruYTM7GYyc96wo28nlOm9JFvvgZFM1H-W4aWktEm5J2-XH9aV23_yFBd-qtbuc6ZgqUAckAOYIpwsPbFJuyxEhYAYzfNSeoLqOZCoB1XvsgqddBmEBv2YI0Rm3GNhw7fmz1Veu3whgoLT_-ziqK5_MDAHdxl7SECMxuBzxx38A1AuVPCJ3DFTEKZlgDr_EvymIK5YzAE65q75SMXHP3Zcd4I6i9V_ZKiZ8Pmxq-UIHUQRV8l3UjUCh9n8zdhpn6Wr",
                contentDescription = "Onboarding Illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Text(
            text = "Screenshot Intelligence",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.02).sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Transform your raw captures into structured, actionable intelligence automatically.",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4 Feature Cards (Grid-like layout in column)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DocumentScanner,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    title = "OCR Engine",
                    desc = "Extract text seamlessly from any screenshot."
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Summarize,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                    title = "AI Summary",
                    desc = "Get the gist of long articles instantly."
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FolderSpecial,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    title = "Smart Sort",
                    desc = "Auto-sort incoming captures into folders."
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Search,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBg = MaterialTheme.colorScheme.surfaceVariant,
                    title = "Search Text",
                    desc = "Search for any text directly inside images."
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .widthIn(max = 300.dp)
                .testTag("onboarding_step1_next_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Arrow Forward",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// STEP 2: Permissions Screen
@Composable
fun PermissionsStepTwo(onAllowAll: () -> Unit, onCustomize: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Permissions",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "To provide the best AI-powered experience, we need a few permissions.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Bento Permissions Stack
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionRowCard(
                    icon = Icons.Default.Image,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Photo Picker",
                    desc = "Pick individual screenshots to import — the system picker never shares your full gallery with the app."
                )

                PermissionRowCard(
                    icon = Icons.Default.Notifications,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Notifications",
                    desc = "For timely reminders and intelligent AI task updates."
                )

                PermissionRowCard(
                    icon = Icons.Default.AutoAwesome,
                    iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "AI Processing",
                    desc = "To securely extract actionable notes and tasks from your images."
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            Button(
                onClick = onAllowAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .widthIn(max = 320.dp)
                    .testTag("permissions_allow_all_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Allow All",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onCustomize,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .widthIn(max = 320.dp)
            ) {
                Text(
                    text = "Customize",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// STEP 3: Planner Intro / Unified Screen
@Composable
fun OnboardingStepThree(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Background Deco & Interactive mockup calendar box
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today, 24 Oct",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated schedule grid slots
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Box(modifier = Modifier.size(width = 80.dp, height = 6.dp).background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(3.dp)))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.size(width = 40.dp, height = 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                            }
                        }

                        // Colored primary container task
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Box(modifier = Modifier.size(width = 120.dp, height = 6.dp).background(MaterialTheme.colorScheme.onPrimaryContainer, RoundedCornerShape(3.dp)))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.size(width = 60.dp, height = 4.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                }
                            }
                        }

                        // AI note chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Box(modifier = Modifier.size(width = 100.dp, height = 6.dp).background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(3.dp)))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.size(width = 50.dp, height = 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Your entire day,\nunified.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.02).sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Connect your calendar and let CaptureFlow fill the gaps with tasks and notes extracted from your screenshots.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )
        }

        // Continue Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .widthIn(max = 320.dp)
                    .testTag("onboarding_continue_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow Forward",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Subcomponents
@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    desc: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PermissionRowCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    desc: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}
