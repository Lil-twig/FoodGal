package com.example.foodgal.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun AppSidebar(
    selectedItemIndex: Int,
    onItemClick: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem(
            title = "POS",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Filled.Home,
        ),
        NavigationItem(
            title = "History Transaction",
            selectedIcon = Icons.Filled.DateRange,
            unselectedIcon = Icons.Filled.DateRange,
        ),
        NavigationItem(
            title = "Summary",
            selectedIcon = Icons.Filled.Email,
            unselectedIcon = Icons.Filled.Email,
        ),
        NavigationItem(
            title = "Daftar Product",
            selectedIcon = Icons.Filled.List,
            unselectedIcon = Icons.Filled.List,
        ),
        NavigationItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Filled.Person,
        ),
    )

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "FoodGal Menu",
            modifier = Modifier.padding(16.dp),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        items.forEachIndexed { index, item ->
            NavigationDrawerItem(
                label = { Text(text = item.title) },
                selected = index == selectedItemIndex,
                onClick = {
                    onItemClick(index)
                },
                icon = {
                    Icon(
                        imageVector = item.selectedIcon,
                        contentDescription = item.title
                    )
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
