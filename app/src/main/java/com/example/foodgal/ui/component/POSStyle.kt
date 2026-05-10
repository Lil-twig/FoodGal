package com.example.foodgal.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodgal.R
import com.example.foodgal.models.Product
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: Product,
    name: String = "Burger",
    price: Int = 25000,
    isSelected: Boolean = false,
    quantity: Int = 0,
    onClick: () -> Unit = {}
) {

    Box(modifier = modifier) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(if (isSelected) 12.dp else 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(6.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // In your product card / detail screen
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(product.imagePath))  // load from local file path
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.wooper),  // fallback image
                    placeholder = painterResource(R.drawable.wooper)
                )

                Text(
                    text = name,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
                )

                Text(
                    text = formatToRupiah(price),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
                )

                if (isSelected) {
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (quantity > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 6.dp, top = 6.dp)
                    .size(24.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quantity.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    } // end outer Box
}

fun formatToRupiah(price: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(price).replace(",00", "").replace("Rp", "Rp ")
}

@Composable
fun CartBottomBar(
    itemCount: Int,
    totalPrice: Int
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (itemCount > 0) Color(0xFFB84A4A) else Color.Gray
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (itemCount > 0) "🛒" else "🧺",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "$itemCount Items",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = formatToRupiah(totalPrice),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
