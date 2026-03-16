package com.example.foodgal.ui.pos

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.foodgal.R
import com.example.foodgal.ui.component.CartBottomBar
import com.example.foodgal.ui.component.ProductCard

@Composable
fun PosScreen() {
    Scaffold(

        topBar = { Topbar() },
        bottomBar = { CartBottomBar(itemCount = 2, totalPrice = 50000) }

    ) { innerPadding ->
        // Screen content
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // CATEGORY
            item(span = { GridItemSpan(2) }) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
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

            // SEARCH
            item(span = { GridItemSpan(2) }) {

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    label = { Text("Search") }
                )
            }

            // PRODUCT LIST
            items(10) {

                ProductCard(
                    image = R.drawable.wooper,
                    name = "Burger",
                    price = 25000
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
fun CategoryCard (
    modifier: Modifier = Modifier,
    image: Int = R.drawable.hamburger,
    name: String = "Food"
) {

    Column(
        modifier = modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            onClick = { },
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






