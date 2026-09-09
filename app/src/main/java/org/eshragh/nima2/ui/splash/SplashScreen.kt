package org.eshragh.nima2.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.eshragh.nima2.R
import org.eshragh.nima2.ui.theme.BrandCyan
import org.eshragh.nima2.ui.theme.PrimaryBlue
import org.eshragh.nima2.ui.theme.PrimaryDarkBlue

@Composable
fun SplashScreen() {
    val brandGradient = Brush.verticalGradient(
        colors = listOf(PrimaryDarkBlue, PrimaryBlue, BrandCyan)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = brandGradient),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_nima2),
            contentDescription = "لوگو نیما ۲",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "نیما ۲",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "ثبت و آپلود آفلاین کارت‌ها",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(36.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 3.dp
        )
    }
}
