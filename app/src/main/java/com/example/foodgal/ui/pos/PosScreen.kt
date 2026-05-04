package com.example.foodgal.ui.pos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodgal.R
import com.example.foodgal.ui.component.CartBottomBar
import com.example.foodgal.ui.component.ProductCard

@Composable
fun PosScreen(
    onMenuClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    viewModel: PosViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // CART STATES FROM VIEWMODEL
    val cartItems by viewModel.cartItems.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Topbar(onMenuClick = onMenuClick)
            },
            bottomBar = { 
                Box(modifier = Modifier.clickable { onCartClick() }) {
                    CartBottomBar(
                        itemCount = totalItems, 
                        totalPrice = totalPrice
                    ) 
                }
            }
        ) { innerPadding ->
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
                            name = "Food",
                            isSelected = selectedCategory == "Makanan",
                            onClick = { viewModel.selectCategory("Makanan") }
                        )

                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            image = R.drawable.softdrink,
                            name = "Drink",
                            isSelected = selectedCategory == "Minuman",
                            onClick = { viewModel.selectCategory("Minuman") }
                        )

                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            image = R.drawable.snack,
                            name = "Snack",
                            isSelected = selectedCategory == "Snack",
                            onClick = { viewModel.selectCategory("Snack") }
                        )
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            image = R.drawable.all_logo,
                            name = "All",
                            isSelected = selectedCategory == "All",
                            onClick = { viewModel.selectCategory("All") }
                        )

                    }
                }

                // SEARCH
                item(span = { GridItemSpan(2) }) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        label = { Text("Search product") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                }

                // PRODUCT LIST FROM FIREBASE
                items(products) { product ->

                    val quantity = cartItems[product.id] ?:0

                    ProductCard(
                        name = product.name,
                        price = product.price.toInt(),
                        isSelected = quantity > 0,
                        quantity = quantity,
                        onClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        // LOADING OVERLAY
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Mohon tunggu...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Topbar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "FoodGal") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    )
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    image: Int = R.drawable.hamburger,
    name: String = "Food",
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme
                    .primaryContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = name,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = name,
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
        )
    }


}
