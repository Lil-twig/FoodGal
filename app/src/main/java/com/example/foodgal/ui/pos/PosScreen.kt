package com.example.foodgal.ui.pos

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.foodgal.R

@Composable
fun PosScreen() {
    Scaffold(

        topBar = { Topbar() },
    ) { innerPadding ->
        // Screen content
        Text(text = "This is the POS screen")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically

            ) {
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    image = R.drawable.hamburger,
                    name = "Food"
                )

                CategoryCard(
                    modifier = Modifier.weight(1f),
                    image = R.drawable.softdrink,
                    name = "Drink"
                )

                CategoryCard(
                    modifier = Modifier.weight(1f),
                    image = R.drawable.snack,
                    name = "Snack"

                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Topbar() {
    TopAppBar(
        title = { Text(text = "FoodGal") },

        )
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    image: Int = R.drawable.hamburger,
    name: String = "Food"
) {

    Column(
        modifier = modifier
            .padding(8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {

        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = name,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = name,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

}




